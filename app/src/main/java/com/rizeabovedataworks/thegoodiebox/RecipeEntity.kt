package com.rizeabovedataworks.thegoodiebox

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val prepTime: String,
    val cookTime: String,
    val ingredientsJson: String,
    val instructionsJson: String,
    val handlingInstructions: String,
    val modifications: String,
    val imageUri: String = "",
    val memories: String = "",
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
) {
    fun toRecipe(): Recipe {
        val ingredientsList = try {
            val jsonArray = org.json.JSONArray(ingredientsJson)
            List(jsonArray.length()) { jsonArray.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }

        val instructionsList = try {
            val jsonArray = org.json.JSONArray(instructionsJson)
            List(jsonArray.length()) { jsonArray.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }

        return Recipe(
            id = id,
            title = title,
            description = description,
            category = category,
            prepTime = prepTime,
            cookTime = cookTime,
            ingredients = ingredientsList,
            instructions = instructionsList,
            handlingInstructions = handlingInstructions,
            modifications = modifications,
            imageUri = imageUri,
            memories = memories,
            isFavorite = isFavorite,
            isCustom = isCustom
        )
    }

    companion object {
        fun fromRecipe(recipe: Recipe): RecipeEntity {
            return RecipeEntity(
                id = recipe.id,
                title = recipe.title,
                description = recipe.description,
                category = recipe.category,
                prepTime = recipe.prepTime,
                cookTime = recipe.cookTime,
                ingredientsJson = org.json.JSONArray(recipe.ingredients).toString(),
                instructionsJson = org.json.JSONArray(recipe.instructions).toString(),
                handlingInstructions = recipe.handlingInstructions,
                modifications = recipe.modifications,
                imageUri = recipe.imageUri,
                memories = recipe.memories,
                isFavorite = recipe.isFavorite,
                isCustom = recipe.isCustom
            )
        }
    }
}
