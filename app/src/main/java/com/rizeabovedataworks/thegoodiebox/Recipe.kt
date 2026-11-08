package com.rizeabovedataworks.thegoodiebox

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val prepTime: String = "15 mins",
    val cookTime: String = "30 mins",
    val ingredients: List<String>,
    val instructions: List<String>,
    val handlingInstructions: String = "",
    val modifications: String = "",
    val imageUri: String = "",
    val memories: String = "",
    val userNotes: String = "",
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
)

data class RecipeResponse(
    val version: Int = 1,
    val recipes: List<Recipe>
)
