package com.rizeabovedataworks.thegoodiebox

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Activity for adding new custom recipes or editing existing custom recipes.
 *
 * Features:
 * - Dynamic category dropdown populated from database SSOT categories
 * - Photo picker via ActivityResultContracts
 * - Optional storage handling and modification inputs
 * - Generous 280dp bottom padding and soft keyboard insets for clear visibility while typing
 */
class AddRecipeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RECIPE_ID = "extra_recipe_id"
    }

    private var selectedImageUri: Uri? = null
    private var existingImageUriStr: String = ""
    private var editingRecipeId: String? = null
    private var existingIsFavorite: Boolean = false
    private var existingIsCustom: Boolean = true

    private lateinit var ivPreview: ImageView
    private lateinit var spinnerCategory: Spinner
    private var categories: Array<String> = arrayOf("Nuts and Snacks", "Cookies", "Bars", "Candies", "Fudge", "Bread", "Frostings", "Main Dishes")

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            ivPreview.visibility = View.VISIBLE
            ivPreview.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        editingRecipeId = intent.getStringExtra(EXTRA_RECIPE_ID)

        // Setup top action bar toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarAddRecipe)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Adjust bottom padding when soft keyboard appears
        val scrollView = findViewById<android.widget.ScrollView>(R.id.scrollViewAddRecipe)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val imeInsets = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime() or androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, imeInsets.bottom + 280)
            insets
        }

        if (editingRecipeId != null) {
            supportActionBar?.title = "Edit Recipe"
            findViewById<Button>(R.id.btnSaveRecipe).text = "Update Recipe"
        } else {
            supportActionBar?.title = "Add New Recipe"
        }

        ivPreview = findViewById(R.id.ivRecipePreview)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        // Populate category dropdown dynamically from database SSOT
        lifecycleScope.launch {
            val repo = RecipeRepository(this@AddRecipeActivity)
            val dbCategories = repo.getCategoriesList()
            if (dbCategories.isNotEmpty()) {
                categories = dbCategories.toTypedArray()
                spinnerCategory.adapter = ArrayAdapter(this@AddRecipeActivity, android.R.layout.simple_spinner_dropdown_item, categories)
            }
            if (editingRecipeId != null) {
                loadRecipeForEditing(editingRecipeId!!)
            }
        }

        findViewById<Button>(R.id.btnPickImage).setOnClickListener {
            pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        findViewById<Button>(R.id.btnSaveRecipe).setOnClickListener {
            saveRecipe()
        }
    }

    /**
     * Pre-fills inputs when editing an existing custom recipe.
     */
    private fun loadRecipeForEditing(recipeId: String) {
        lifecycleScope.launch {
            val repo = RecipeRepository(this@AddRecipeActivity)
            val recipe = repo.getRecipeById(recipeId)
            if (recipe != null) {
                findViewById<TextInputEditText>(R.id.etTitle).setText(recipe.title)
                findViewById<TextInputEditText>(R.id.etDesc).setText(recipe.description)
                findViewById<TextInputEditText>(R.id.etPrepTime).setText(recipe.prepTime)
                findViewById<TextInputEditText>(R.id.etCookTime).setText(recipe.cookTime)
                findViewById<TextInputEditText>(R.id.etIngredients).setText(recipe.ingredients.joinToString("\n"))
                findViewById<TextInputEditText>(R.id.etInstructions).setText(recipe.instructions.joinToString("\n"))
                findViewById<TextInputEditText>(R.id.etHandling).setText(recipe.handlingInstructions)
                findViewById<TextInputEditText>(R.id.etModifications).setText(recipe.modifications)
                findViewById<TextInputEditText>(R.id.etMemories).setText(recipe.memories)

                existingImageUriStr = recipe.imageUri
                existingIsFavorite = recipe.isFavorite
                existingIsCustom = recipe.isCustom

                val catIndex = categories.indexOfFirst { it.equals(recipe.category, ignoreCase = true) }
                if (catIndex != -1) {
                    spinnerCategory.setSelection(catIndex)
                }

                if (recipe.imageUri.isNotBlank()) {
                    ivPreview.visibility = View.VISIBLE
                    if (recipe.imageUri.startsWith("content://") || recipe.imageUri.startsWith("file://")) {
                        try {
                            ivPreview.setImageURI(Uri.parse(recipe.imageUri))
                        } catch (e: Exception) {
                            ivPreview.setImageResource(R.drawable.ic_tbg_splash_main)
                        }
                    } else {
                        val resId = resources.getIdentifier(recipe.imageUri, "drawable", packageName)
                        if (resId != 0) {
                            ivPreview.setImageResource(resId)
                        }
                    }
                }
            }
        }
    }

    /**
     * Saves or updates custom recipe in database.
     */
    private fun saveRecipe() {
        val title = findViewById<TextInputEditText>(R.id.etTitle).text.toString().trim()
        val desc = findViewById<TextInputEditText>(R.id.etDesc).text.toString().trim()
        val prep = findViewById<TextInputEditText>(R.id.etPrepTime).text.toString().trim()
        val cook = findViewById<TextInputEditText>(R.id.etCookTime).text.toString().trim()
        val ingredientsText = findViewById<TextInputEditText>(R.id.etIngredients).text.toString().trim()
        val instructionsText = findViewById<TextInputEditText>(R.id.etInstructions).text.toString().trim()
        val handlingText = findViewById<TextInputEditText>(R.id.etHandling).text.toString().trim()
        val modificationsText = findViewById<TextInputEditText>(R.id.etModifications).text.toString().trim()
        val memoriesText = findViewById<TextInputEditText>(R.id.etMemories).text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a recipe title", Toast.LENGTH_SHORT).show()
            return
        }

        val ingredients = ingredientsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val instructions = instructionsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        val imageUriToSave = selectedImageUri?.toString() ?: existingImageUriStr
        val recipeIdToSave = editingRecipeId ?: "custom_${System.currentTimeMillis()}"

        val recipeToSave = Recipe(
            id = recipeIdToSave,
            title = title,
            description = desc,
            category = category,
            prepTime = if (prep.isEmpty()) "15 mins" else prep,
            cookTime = if (cook.isEmpty()) "30 mins" else cook,
            ingredients = if (ingredients.isEmpty()) listOf("Standard ingredients") else ingredients,
            instructions = if (instructions.isEmpty()) listOf("Follow standard steps") else instructions,
            handlingInstructions = handlingText,
            modifications = modificationsText,
            imageUri = imageUriToSave,
            memories = memoriesText,
            isFavorite = existingIsFavorite,
            isCustom = existingIsCustom
        )

        lifecycleScope.launch {
            val repo = RecipeRepository(this@AddRecipeActivity)
            repo.updateRecipe(recipeToSave)
            val msg = if (editingRecipeId != null) "Recipe updated successfully!" else "Recipe saved to cookbook!"
            Toast.makeText(this@AddRecipeActivity, msg, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
