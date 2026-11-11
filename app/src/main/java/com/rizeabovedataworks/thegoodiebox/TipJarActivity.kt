package com.rizeabovedataworks.thegoodiebox

import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Developer Tip Jar Activity.
 *
 * Provides voluntary developer appreciation tipping options ($0.99, $2.99, $4.99).
 * Features a warm story card from creator Greg explaining that tips help cover server costs
 * and continuous free recipe updates for the community.
 */
class TipJarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip_jar)

        val toolbar = findViewById<Toolbar>(R.id.toolbarTipJar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Developer Tip Jar"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val scrollView = findViewById<ScrollView>(R.id.scrollViewTipJar)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, 0, v.paddingRight, systemBars.bottom + 48)
            insets
        }

        // Tip Tier Buttons
        val btnTipSmall = findViewById<Button>(R.id.btnTipSmall)
        val btnTipMedium = findViewById<Button>(R.id.btnTipMedium)
        val btnTipLarge = findViewById<Button>(R.id.btnTipLarge)

        btnTipSmall?.setOnClickListener {
            handleTipClick("$0.99 Cup of Coffee")
        }

        btnTipMedium?.setOnClickListener {
            handleTipClick("$2.99 Slice of Cake")
        }

        btnTipLarge?.setOnClickListener {
            handleTipClick("$4.99 Goodie Box Supporter")
        }
    }

    private fun handleTipClick(tierName: String) {
        Toast.makeText(
            this,
            "Thank you so much! $tierName tip selected. Connecting to Google Play Billing...",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
