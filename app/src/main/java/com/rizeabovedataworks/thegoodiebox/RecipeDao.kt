package com.rizeabovedataworks.thegoodiebox

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun observeAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY title ASC")
    fun observeFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE LOWER(category) = LOWER(:category) ORDER BY title ASC")
    fun observeRecipesByCategory(category: String): Flow<List<RecipeEntity>>

    @Query("SELECT DISTINCT category FROM recipes WHERE category IS NOT NULL AND TRIM(category) != '' ORDER BY category ASC")
    fun observeDistinctCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT category FROM recipes WHERE category IS NOT NULL AND TRIM(category) != '' ORDER BY category ASC")
    suspend fun getDistinctCategories(): List<String>

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    suspend fun getRecipeById(recipeId: String): RecipeEntity?

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int

    @Query("SELECT * FROM recipes WHERE isFavorite = 1")
    suspend fun getFavoriteRecipes(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE LOWER(category) = LOWER(:category)")
    suspend fun getRecipesByCategory(category: String): List<RecipeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: String)

    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :recipeId")
    suspend fun updateFavoriteStatus(recipeId: String, isFavorite: Boolean)

    @Query("UPDATE recipes SET userNotes = :userNotes WHERE id = :recipeId")
    suspend fun updateUserNotes(recipeId: String, userNotes: String)

    @Query("SELECT MAX(datasetVersion) FROM app_metadata")
    suspend fun getStoredVersion(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVersion(metadata: MetadataEntity)
}
