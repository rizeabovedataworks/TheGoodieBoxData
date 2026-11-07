package com.rizeabovedataworks.thegoodiebox

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainRecipeBoxFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var tvSectionTitle: TextView
    private lateinit var tvEmptyState: TextView
    private lateinit var etSearchRecipes: EditText
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var recyclerViewRecipes: RecyclerView

    private var currentCategoriesList: List<String> = listOf(
        "Favorites", "Nuts and Snacks", "Cookies", "Candies", "Fudge", "Bars", "Bread", "Frostings", "Main Dishes"
    )

    private data class TabTheme(
        val categoryKey: String,
        val label: String,
        val mutedAccentColor: Int
    )

    private fun getTabThemeForCategory(categoryKey: String): TabTheme {
        return when (categoryKey.lowercase().trim()) {
            "favorites" -> TabTheme("Favorites", "★ Favorites", 0xFFC23B22.toInt())
            "nuts and snacks", "nuts & snacks" -> TabTheme(categoryKey, "Nuts & Snacks", 0xFFC07D2B.toInt())
            "cookies" -> TabTheme("Cookies", "Cookies", 0xFF8B6B92.toInt())
            "candies" -> TabTheme("Candies", "Candies", 0xFFC2593F.toInt())
            "fudge" -> TabTheme("Fudge", "Fudge", 0xFF705246.toInt())
            "bars" -> TabTheme("Bars", "Bars", 0xFF4E7C59.toInt())
            "bread", "breads" -> TabTheme(categoryKey, "Bread", 0xFF4A6B82.toInt())
            "frostings" -> TabTheme("Frostings", "Frostings", 0xFF48807A.toInt())
            "main dishes", "main" -> TabTheme(categoryKey, "Main Dishes", 0xFFA8423F.toInt())
            "side dishes", "sides" -> TabTheme(categoryKey, "Side Dishes", 0xFF2A8C82.toInt())
            else -> {
                val colors = intArrayOf(
                    0xFFC07D2B.toInt(), 0xFF8B6B92.toInt(), 0xFFC2593F.toInt(), 0xFF705246.toInt(),
                    0xFF4E7C59.toInt(), 0xFF4A6B82.toInt(), 0xFF48807A.toInt(), 0xFFA8423F.toInt()
                )
                val colorIndex = Math.abs(categoryKey.hashCode()) % colors.size
                TabTheme(categoryKey, categoryKey, colors[colorIndex])
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe_box, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        tvSectionTitle = view.findViewById(R.id.tvSectionTitle)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        etSearchRecipes = view.findViewById(R.id.etSearchRecipes)
        nestedScrollView = view.findViewById(R.id.nestedScrollView)
        recyclerViewRecipes = view.findViewById(R.id.recyclerViewRecipes)

        val fabScrollDown = view.findViewById<ImageView>(R.id.fabScrollDown)
        fabScrollDown.setOnClickListener {
            nestedScrollView.smoothScrollBy(0, 500)
        }

        setupSearchView()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupSearchView() {
        etSearchRecipes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        recyclerViewRecipes.layoutManager = LinearLayoutManager(requireContext())

        recipeAdapter = RecipeAdapter(
            emptyList(),
            onClick = { recipe ->
                val intent = Intent(requireContext(), RecipeDetailActivity::class.java).apply {
                    putExtra(RecipeDetailActivity.EXTRA_ID, recipe.id)
                    putExtra(RecipeDetailActivity.EXTRA_TITLE, recipe.title)
                    putExtra(RecipeDetailActivity.EXTRA_DESC, recipe.description)
                    putExtra(RecipeDetailActivity.EXTRA_PREP, recipe.prepTime)
                    putExtra(RecipeDetailActivity.EXTRA_COOK, recipe.cookTime)
                    putStringArrayListExtra(RecipeDetailActivity.EXTRA_INGREDIENTS, ArrayList(recipe.ingredients))
                    putStringArrayListExtra(RecipeDetailActivity.EXTRA_INSTRUCTIONS, ArrayList(recipe.instructions))
                    putExtra(RecipeDetailActivity.EXTRA_HANDLING, recipe.handlingInstructions)
                    putExtra(RecipeDetailActivity.EXTRA_MODS, recipe.modifications)
                    putExtra(RecipeDetailActivity.EXTRA_CATEGORY, recipe.category)
                    putExtra(RecipeDetailActivity.EXTRA_IMAGE_URI, recipe.imageUri)
                    putExtra(RecipeDetailActivity.EXTRA_MEMORIES, recipe.memories)
                }
                startActivity(intent)
            },
            onFavoriteClick = { recipe ->
                viewModel.toggleFavorite(recipe.id, !recipe.isFavorite)
            }
        )
        recyclerViewRecipes.adapter = recipeAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categoriesState.collect { dbCategories ->
                        currentCategoriesList = dbCategories
                        setupCategoryChips()
                    }
                }
                launch {
                    viewModel.selectedCategory.collect { category ->
                        tvSectionTitle.text = category
                        setupCategoryChips()
                    }
                }
                launch {
                    viewModel.recipesState.collect { recipes ->
                        recipeAdapter.updateRecipes(recipes)
                        if (recipes.isEmpty()) {
                            recyclerViewRecipes.visibility = View.GONE
                            tvEmptyState.visibility = View.VISIBLE
                            val currentCat = viewModel.selectedCategory.value
                            if (currentCat.equals("Favorites", ignoreCase = true)) {
                                tvEmptyState.text = "No favorite recipes yet!\nTap ⭐ on any recipe to save it here."
                            } else {
                                tvEmptyState.text = "No recipes found in \"$currentCat\"."
                            }
                        } else {
                            recyclerViewRecipes.visibility = View.VISIBLE
                            tvEmptyState.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun setupCategoryChips() {
        val chipContainer = view?.findViewById<LinearLayout>(R.id.categoryChipContainer) ?: return
        chipContainer.removeAllViews()

        val currentCat = viewModel.selectedCategory.value
        val fixedTabWidthPx = dpToPx(110)
        val tabHeightPx = dpToPx(34)
        val casualTypeface = Typeface.create("casual", Typeface.BOLD)

        for (categoryKey in currentCategoriesList) {
            val theme = getTabThemeForCategory(categoryKey)
            val isSelected = categoryKey.equals(currentCat, ignoreCase = true)

            val tabLayout = android.widget.FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    fixedTabWidthPx,
                    tabHeightPx
                ).apply {
                    setMargins(0, 0, dpToPx(4), 0)
                }

                if (isSelected) {
                    translationY = -dpToPx(2).toFloat()
                } else {
                    translationY = 0f
                }
            }

            val tabImageView = ImageView(requireContext()).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
                setImageResource(R.drawable.index_tab_image)
                scaleType = ImageView.ScaleType.FIT_XY
                alpha = if (isSelected) 1.0f else 0.85f
            }

            val tabTextView = TextView(requireContext()).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )

                val rawLabel = theme.label
                if (rawLabel.contains("★")) {
                    val spannable = SpannableString(rawLabel)
                    val starIndex = rawLabel.indexOf("★")
                    if (starIndex != -1) {
                        spannable.setSpan(
                            ForegroundColorSpan(0xFFC23B22.toInt()),
                            starIndex,
                            starIndex + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    text = spannable
                } else {
                    text = rawLabel
                }

                textSize = 13.5f
                setPadding(dpToPx(4), 0, dpToPx(4), 0)
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                setTextColor(if (isSelected) 0xFF110B06.toInt() else 0xFF3B281A.toInt())
                typeface = casualTypeface
            }

            tabLayout.addView(tabImageView)
            tabLayout.addView(tabTextView)

            tabLayout.setOnClickListener {
                viewModel.setCategory(categoryKey)
            }

            chipContainer.addView(tabLayout)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
