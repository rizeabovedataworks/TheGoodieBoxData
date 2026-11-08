package com.rizeabovedataworks.thegoodiebox

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RecipeDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESC = "extra_desc"
        const val EXTRA_PREP = "extra_prep"
        const val EXTRA_COOK = "extra_cook"
        const val EXTRA_INGREDIENTS = "extra_ingredients"
        const val EXTRA_INSTRUCTIONS = "extra_instructions"
        const val EXTRA_HANDLING = "extra_handling"
        const val EXTRA_MODS = "extra_mods"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_MEMORIES = "extra_memories"
    }

    private var recipeId: String = ""
    private var recipeTitle: String = ""
    private var isCustomRecipe: Boolean = false
    private var currentNotes: String = ""

    private lateinit var tvUserNotes: TextView
    private var optionMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        recipeId = intent.getStringExtra(EXTRA_ID) ?: ""
        recipeTitle = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val desc = intent.getStringExtra(EXTRA_DESC) ?: ""
        val prep = intent.getStringExtra(EXTRA_PREP) ?: "15 mins"
        val cook = intent.getStringExtra(EXTRA_COOK) ?: "30 mins"
        val ingredients = intent.getStringArrayListExtra(EXTRA_INGREDIENTS) ?: emptyList()
        val instructions = intent.getStringArrayListExtra(EXTRA_INSTRUCTIONS) ?: emptyList()
        val handling = intent.getStringExtra(EXTRA_HANDLING) ?: ""
        val mods = intent.getStringExtra(EXTRA_MODS) ?: ""
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: ""
        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI) ?: ""
        val memories = intent.getStringExtra(EXTRA_MEMORIES) ?: ""

        val toolbar = findViewById<Toolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = recipeTitle
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val scrollView = findViewById<ScrollView>(R.id.scrollViewDetail)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom + 48)
            insets
        }

        val ivRecipeImage = findViewById<ImageView>(R.id.ivRecipeImage)
        setupRecipeImage(ivRecipeImage, imageUri, category)

        findViewById<TextView>(R.id.tvDetailTitle).text = recipeTitle
        findViewById<TextView>(R.id.tvDetailDesc).text = desc
        findViewById<TextView>(R.id.tvPrepTime).text = "Prep: $prep"
        findViewById<TextView>(R.id.tvCookTime).text = "Cook: $cook"

        val ingredientsText = ingredients.joinToString(separator = "\n") { "• $it" }
        findViewById<TextView>(R.id.tvIngredients).text = ingredientsText

        val instructionsText = instructions.mapIndexed { index, step -> "${index + 1}. $step" }.joinToString(separator = "\n\n")
        findViewById<TextView>(R.id.tvInstructions).text = instructionsText

        val tvHandlingTitle = findViewById<TextView>(R.id.tvHandlingTitle)
        val tvHandling = findViewById<TextView>(R.id.tvHandling)
        if (handling.isNotBlank()) {
            tvHandlingTitle.visibility = View.VISIBLE
            tvHandling.visibility = View.VISIBLE
            tvHandling.text = handling
        } else {
            tvHandlingTitle.visibility = View.GONE
            tvHandling.visibility = View.GONE
        }

        val tvModificationsTitle = findViewById<TextView>(R.id.tvModificationsTitle)
        val tvModifications = findViewById<TextView>(R.id.tvModifications)
        if (mods.isNotBlank()) {
            tvModificationsTitle.visibility = View.VISIBLE
            tvModifications.visibility = View.VISIBLE
            tvModifications.text = mods
        } else {
            tvModificationsTitle.visibility = View.GONE
            tvModifications.visibility = View.GONE
        }

        val tvMemoriesTitle = findViewById<TextView>(R.id.tvMemoriesTitle)
        val tvMemories = findViewById<TextView>(R.id.tvMemories)
        if (memories.isNotBlank()) {
            tvMemoriesTitle.visibility = View.VISIBLE
            tvMemories.visibility = View.VISIBLE
            tvMemories.text = memories
        } else {
            tvMemoriesTitle.visibility = View.GONE
            tvMemories.visibility = View.GONE
        }

        tvUserNotes = findViewById(R.id.tvUserNotes)
        findViewById<Button>(R.id.btnEditUserNote).setOnClickListener {
            showEditNoteDialog()
        }

        loadRecipeDetailsFromDb()
    }

    private fun loadRecipeDetailsFromDb() {
        if (recipeId.isNotBlank()) {
            lifecycleScope.launch {
                val repo = RecipeRepository(this@RecipeDetailActivity)
                val recipe = repo.getRecipeById(recipeId)
                if (recipe != null) {
                    isCustomRecipe = recipe.isCustom
                    currentNotes = recipe.userNotes
                    updateNotesUi()
                    updateMenuVisibility()
                }
            }
        }
    }

    private fun updateNotesUi() {
        if (currentNotes.isNotBlank()) {
            tvUserNotes.text = currentNotes
            tvUserNotes.setTypeface(null, android.graphics.Typeface.NORMAL)
            tvUserNotes.setTextColor(0xFF3E2723.toInt())
        } else {
            tvUserNotes.text = "No personal notes added yet. Tap 'Edit Note' to add allergy notes, oven tweaks, or family variations!"
            tvUserNotes.setTypeface(null, android.graphics.Typeface.ITALIC)
            tvUserNotes.setTextColor(0xFF5A4638.toInt())
        }
    }

    private fun updateMenuVisibility() {
        val menu = optionMenu ?: return
        val editItem = menu.findItem(R.id.action_edit)
        val deleteItem = menu.findItem(R.id.action_delete)

        if (!isCustomRecipe) {
            // Original Heirloom Recipe: Lock & Hide Edit/Delete to protect Mom's recipe!
            editItem?.isVisible = false
            deleteItem?.isVisible = false
        } else {
            // Custom User Recipe: Show Edit and Delete
            editItem?.isVisible = true
            deleteItem?.isVisible = true
        }
    }

    private fun showEditNoteDialog() {
        val input = EditText(this).apply {
            hint = "e.g. Leave out cinnamon as Lizzy is allergic!"
            setText(currentNotes)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setLines(4)
            setPadding(32, 32, 32, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("📝 Personal Kitchen Notes")
            .setMessage("Add your own personal notes, allergy warnings, or cooking tweaks to this recipe. Mom's original recipe stays 100% preserved!")
            .setView(input)
            .setPositiveButton("Save Note") { _, _ ->
                val newNote = input.text.toString().trim()
                currentNotes = newNote
                updateNotesUi()

                lifecycleScope.launch {
                    val repo = RecipeRepository(this@RecipeDetailActivity)
                    repo.updateUserNotes(recipeId, newNote)
                    Toast.makeText(this@RecipeDetailActivity, "Personal note saved!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_recipe_detail, menu)
        optionMenu = menu
        updateMenuVisibility()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> {
                if (recipeId.isNotBlank() && isCustomRecipe) {
                    val intent = Intent(this, AddRecipeActivity::class.java).apply {
                        putExtra(AddRecipeActivity.EXTRA_RECIPE_ID, recipeId)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Original heirloom recipes are permanently preserved.", Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.action_delete -> {
                if (recipeId.isNotBlank() && isCustomRecipe) {
                    AlertDialog.Builder(this)
                        .setTitle("Delete Recipe")
                        .setMessage("Are you sure you want to delete '$recipeTitle' from your Goodie Box?")
                        .setPositiveButton("Delete") { _, _ ->
                            lifecycleScope.launch {
                                val repo = RecipeRepository(this@RecipeDetailActivity)
                                repo.deleteRecipe(recipeId)
                                Toast.makeText(this@RecipeDetailActivity, "Recipe deleted", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    Toast.makeText(this, "Original heirloom recipes cannot be deleted.", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecipeImage(imageView: ImageView, imageUri: String, category: String) {
        if (imageUri.isNotBlank()) {
            if (imageUri.startsWith("content://") || imageUri.startsWith("file://")) {
                try {
                    imageView.setImageURI(Uri.parse(imageUri))
                    return
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val resId = resources.getIdentifier(imageUri, "drawable", packageName)
                if (resId != 0) {
                    imageView.setImageResource(resId)
                    return
                }
            }
        }
        imageView.setImageResource(getCategoryFallbackImage(category))
    }

    private fun getCategoryFallbackImage(category: String): Int {
        return when (category.lowercase().trim()) {
            "nuts and snacks", "nuts & snacks", "nuts", "snacks" -> R.drawable.nutsandsnacks_fb_img
            "cookies" -> R.drawable.chocolatechipcookie_img
            "candies" -> R.drawable.img_category_candies
            "fudge" -> R.drawable.img_category_fudge
            "bars" -> R.drawable.img_category_bars
            "bread", "breads" -> R.drawable.breads_fb_img
            "frostings" -> R.drawable.img_category_frostings
            "main dishes", "main" -> R.drawable.meatloaf_img
            else -> R.drawable.ic_tbg_splash_main
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
