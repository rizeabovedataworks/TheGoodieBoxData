package com.rizeabovedataworks.thegoodiebox

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class RecipeRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.recipeDao()

    companion object {
        const val OTA_URL = "https://raw.githubusercontent.com/rizeabovedataworks/TheGoodieBoxData/main/recipes.json"
    }

    suspend fun ensureDatabaseSeeded() = withContext(Dispatchers.IO) {
        val storedVersion = dao.getStoredVersion() ?: 0
        val (assetVersion, localRecipes) = loadRecipesFromAssetsWithVersion()
        if (dao.getRecipeCount() == 0 || assetVersion > storedVersion) {
            val existingList = dao.getAllRecipes()
            for (recipe in localRecipes) {
                val existing = existingList.find { it.id == recipe.id }
                val isFav = existing?.isFavorite ?: false
                val entity = RecipeEntity.fromRecipe(recipe).copy(isFavorite = isFav)
                dao.insertRecipe(entity)
            }
            dao.saveVersion(MetadataEntity(datasetVersion = assetVersion))
        }
    }

    fun getRecipesFlow(category: String, query: String = ""): Flow<List<Recipe>> {
        val entityFlow = when {
            category.equals("Favorites", ignoreCase = true) -> dao.observeFavoriteRecipes()
            category.equals("All", ignoreCase = true) -> dao.observeAllRecipes()
            else -> dao.observeRecipesByCategory(category)
        }
        return entityFlow.map { entities ->
            val recipes = entities.map { it.toRecipe() }
            if (query.isBlank()) {
                recipes
            } else {
                val q = query.trim().lowercase()
                recipes.filter { recipe ->
                    recipe.title.lowercase().contains(q) ||
                    recipe.description.lowercase().contains(q) ||
                    recipe.category.lowercase().contains(q) ||
                    recipe.ingredients.any { it.lowercase().contains(q) } ||
                    recipe.instructions.any { it.lowercase().contains(q) }
                }
            }
        }
    }

    fun observeCategories(): Flow<List<String>> {
        return dao.observeDistinctCategories().map { dbCategories ->
            val result = mutableListOf("Favorites")
            for (cat in dbCategories) {
                if (!cat.equals("Favorites", ignoreCase = true) && !cat.equals("All", ignoreCase = true)) {
                    result.add(cat)
                }
            }
            result
        }
    }

    suspend fun getCategoriesList(): List<String> = withContext(Dispatchers.IO) {
        val dbCategories = dao.getDistinctCategories()
        val result = mutableListOf<String>()
        for (cat in dbCategories) {
            if (!cat.equals("Favorites", ignoreCase = true) && !cat.equals("All", ignoreCase = true)) {
                result.add(cat)
            }
        }
        if (result.isEmpty()) {
            listOf("Nuts and Snacks", "Cookies", "Bars", "Candies", "Fudge", "Bread", "Frostings", "Main Dishes")
        } else {
            result
        }
    }

    suspend fun getRecipeById(recipeId: String): Recipe? = withContext(Dispatchers.IO) {
        dao.getRecipeById(recipeId)?.toRecipe()
    }

    private fun loadRecipesFromAssetsWithVersion(): Pair<Int, List<Recipe>> {
        val recipes = mutableListOf<Recipe>()
        var version = 1
        try {
            val inputStream = context.assets.open("recipes.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()
            reader.close()

            val jsonObject = JSONObject(jsonString)
            version = jsonObject.optInt("version", 1)
            val jsonArray = jsonObject.getJSONArray("recipes")
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                recipes.add(parseRecipe(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(version, recipes)
    }

    suspend fun syncOta(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(OTA_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "TheGoodieBox-Android")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonString = reader.readText()
                reader.close()

                val jsonObject = JSONObject(jsonString)
                val remoteVersion = jsonObject.optInt("version", 1)
                val localVersion = dao.getStoredVersion() ?: 1

                if (remoteVersion > localVersion) {
                    val jsonArray = jsonObject.getJSONArray("recipes")
                    val remoteRecipes = mutableListOf<Recipe>()
                    for (i in 0 until jsonArray.length()) {
                        remoteRecipes.add(parseRecipe(jsonArray.getJSONObject(i)))
                    }

                    val existingList = dao.getAllRecipes()
                    for (remote in remoteRecipes) {
                        val existing = existingList.find { it.id == remote.id }
                        val isFav = existing?.isFavorite ?: false
                        val entity = RecipeEntity.fromRecipe(remote).copy(isFavorite = isFav)
                        dao.insertRecipe(entity)
                    }
                    dao.saveVersion(MetadataEntity(datasetVersion = remoteVersion))
                    return@withContext Pair(true, "Successfully updated recipes to version $remoteVersion!")
                } else {
                    return@withContext Pair(false, "Recipes are already up to date (v$localVersion).")
                }
            }
            connection.disconnect()
        } catch (e: Exception) {
            return@withContext Pair(false, "OTA Sync failed: ${e.localizedMessage}")
        }
        Pair(false, "OTA Sync failed: Network error.")
    }

    suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        dao.updateFavoriteStatus(recipeId, isFavorite)
    }

    suspend fun addCustomRecipe(recipe: Recipe) = withContext(Dispatchers.IO) {
        val entity = RecipeEntity.fromRecipe(recipe.copy(isCustom = true))
        dao.insertRecipe(entity)
    }

    suspend fun updateRecipe(recipe: Recipe) = withContext(Dispatchers.IO) {
        val entity = RecipeEntity.fromRecipe(recipe)
        dao.insertRecipe(entity)
    }

    suspend fun deleteRecipe(recipeId: String) = withContext(Dispatchers.IO) {
        dao.deleteRecipeById(recipeId)
    }

    private fun parseRecipe(obj: JSONObject): Recipe {
        val id = obj.optString("id", "")
        val title = obj.optString("title", "")
        val description = obj.optString("description", "")
        val category = obj.optString("category", "Nuts and Snacks")
        val prepTime = obj.optString("prepTime", "15 mins")
        val cookTime = obj.optString("cookTime", "30 mins")
        val handlingInstructions = obj.optString("handlingInstructions", "Store in an airtight container.")
        val modifications = obj.optString("modifications", "None.")
        val imageUri = obj.optString("imageUri", "")
        val memories = obj.optString("memories", "")

        val ingredientsArray = obj.optJSONArray("ingredients") ?: org.json.JSONArray()
        val ingredients = mutableListOf<String>()
        for (j in 0 until ingredientsArray.length()) {
            ingredients.add(ingredientsArray.getString(j))
        }

        val instructionsArray = obj.optJSONArray("instructions") ?: org.json.JSONArray()
        val instructions = mutableListOf<String>()
        for (j in 0 until instructionsArray.length()) {
            instructions.add(instructionsArray.getString(j))
        }

        return Recipe(id, title, description, category, prepTime, cookTime, ingredients, instructions, handlingInstructions, modifications, imageUri, memories)
    }
}
