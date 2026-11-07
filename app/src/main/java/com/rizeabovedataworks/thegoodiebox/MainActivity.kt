package com.rizeabovedataworks.thegoodiebox

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var viewPagerMain: ViewPager2
    private lateinit var tabLayoutPages: TabLayout

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = findViewById(R.id.drawerLayout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        viewPagerMain = findViewById(R.id.viewPagerMain)
        tabLayoutPages = findViewById(R.id.tabLayoutPages)

        viewPagerMain.adapter = MainPagerAdapter(this)

        TabLayoutMediator(tabLayoutPages, viewPagerMain) { tab, position ->
            tab.text = if (position == 0) "📜 Recipe Box" else "💡 Tools & Guides"
        }.attach()

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categoriesState.collect { dbCategories ->
                        currentCategoriesList = dbCategories
                        setupSideDrawerTabs()
                    }
                }
                launch {
                    viewModel.selectedCategory.collect {
                        setupSideDrawerTabs()
                    }
                }
            }
        }
    }

    private fun setupSideDrawerTabs() {
        val drawerCategoryTabContainer = findViewById<LinearLayout>(R.id.drawerCategoryTabContainer) ?: return
        drawerCategoryTabContainer.removeAllViews()

        val currentCat = viewModel.selectedCategory.value
        val casualTypeface = Typeface.create("casual", Typeface.BOLD)

        for (categoryKey in currentCategoriesList) {
            val theme = getTabThemeForCategory(categoryKey)
            val isSelected = categoryKey.equals(currentCat, ignoreCase = true)

            val actionView = layoutInflater.inflate(R.layout.item_drawer_category_tab, drawerCategoryTabContainer, false).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(165),
                    dpToPx(36)
                ).apply {
                    setMargins(0, 0, 0, dpToPx(3))
                }
            }
            val ivBg = actionView.findViewById<ImageView>(R.id.ivDrawerTabBg)
            val tvLabel = actionView.findViewById<TextView>(R.id.tvDrawerTabLabel)

            tvLabel.typeface = casualTypeface

            val labelText = theme.label
            if (labelText.contains("★")) {
                val spannable = SpannableString(labelText)
                val starIndex = labelText.indexOf("★")
                if (starIndex != -1) {
                    spannable.setSpan(
                        ForegroundColorSpan(0xFFC23B22.toInt()),
                        starIndex,
                        starIndex + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                tvLabel.text = spannable
            } else {
                tvLabel.text = labelText
            }

            if (isSelected) {
                ivBg.alpha = 1.0f
                tvLabel.setTextColor(0xFF110B06.toInt())
            } else {
                ivBg.alpha = 0.85f
                tvLabel.setTextColor(0xFF3B281A.toInt())
            }

            actionView.setOnClickListener {
                viewModel.setCategory(categoryKey)
                viewPagerMain.currentItem = 0
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            drawerCategoryTabContainer.addView(actionView)
        }

        // Setup Drawer Tools & Guides Click Listeners
        findViewById<View>(R.id.btnDrawerAddRecipe)?.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<View>(R.id.btnDrawerTurkeyGuide)?.setOnClickListener {
            startActivity(Intent(this, TurkeyGuideActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<View>(R.id.btnDrawerHowToUse)?.setOnClickListener {
            startActivity(Intent(this, HowToUseActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<View>(R.id.btnDrawerTips)?.setOnClickListener {
            startActivity(Intent(this, TipsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<View>(R.id.btnDrawerConversions)?.setOnClickListener {
            startActivity(Intent(this, ConversionsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<View>(R.id.btnDrawerSync)?.setOnClickListener {
            syncOtaRecipes()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<View>(R.id.btnDrawerAbout)?.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun syncOtaRecipes() {
        Toast.makeText(this, "Checking GitHub OTA for updates...", Toast.LENGTH_SHORT).show()
        viewModel.syncOta { _, message ->
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    private class MainPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                MainRecipeBoxFragment()
            } else {
                ToolsHubFragment()
            }
        }
    }
}
