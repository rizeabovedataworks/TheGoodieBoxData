package com.rizeabovedataworks.thegoodiebox

import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val toolbar = findViewById<Toolbar>(R.id.toolbarAbout)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "About & Legal"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val scrollView = findViewById<ScrollView>(R.id.scrollViewAbout)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, 0, v.paddingRight, systemBars.bottom + 48)
            insets
        }

        val tvAboutVersion = findViewById<TextView>(R.id.tvAboutVersion)
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AboutActivity)
            val version = db.recipeDao().getStoredVersion() ?: 27
            val count = db.recipeDao().getRecipeCount()
            tvAboutVersion.text = "App Version 1.0 • Dataset v$version ($count Recipes)"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
