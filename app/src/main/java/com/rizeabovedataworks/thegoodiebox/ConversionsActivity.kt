package com.rizeabovedataworks.thegoodiebox

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.DecimalFormat

class ConversionsActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var tvResult: TextView

    private val units = arrayOf(
        "Pounds (lbs)",
        "Ounces (oz)",
        "Grams (g)",
        "Kilograms (kg)",
        "Teaspoons (tsp)",
        "Tablespoons (tbsp)",
        "Fluid Ounces (fl oz)",
        "Cups",
        "Pints",
        "Quarts",
        "Gallons",
        "Milliliters (ml)",
        "Liters (L)",
        "Fahrenheit (°F)",
        "Celsius (°C)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversions)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarConversions)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Measurement Conversions"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val scrollView = findViewById<ScrollView>(R.id.scrollViewConversions)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, 0, v.paddingRight, systemBars.bottom + 32)
            insets
        }

        etAmount = findViewById(R.id.etConvertAmount)
        spinnerFrom = findViewById(R.id.spinnerFromUnit)
        spinnerTo = findViewById(R.id.spinnerToUnit)
        tvResult = findViewById(R.id.tvConvertResult)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, units)
        spinnerFrom.adapter = adapter
        spinnerTo.adapter = adapter

        // Defaults: From Pounds (lbs) -> To Kilograms (kg)
        spinnerFrom.setSelection(0)
        spinnerTo.setSelection(3)

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                calculateConversion()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        spinnerFrom.onItemSelectedListener = listener
        spinnerTo.onItemSelectedListener = listener

        etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateConversion()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        calculateConversion()
    }

    private fun calculateConversion() {
        val amountStr = etAmount.text.toString().trim()
        if (amountStr.isEmpty()) {
            tvResult.text = "Enter an amount above"
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null) {
            tvResult.text = "Invalid amount"
            return
        }

        val fromUnit = spinnerFrom.selectedItem.toString()
        val toUnit = spinnerTo.selectedItem.toString()

        val resultStr = convert(amount, fromUnit, toUnit)
        tvResult.text = resultStr
    }

    private fun convert(amount: Double, from: String, to: String): String {
        val df = DecimalFormat("#,##0.####")

        val catFrom = getCategory(from)
        val catTo = getCategory(to)

        if (catFrom != catTo) {
            return "Cannot convert between $catFrom & $catTo"
        }

        if (catFrom == "Temperature") {
            val res = if (from.startsWith("Fahrenheit") && to.startsWith("Celsius")) {
                (amount - 32.0) * 5.0 / 9.0
            } else if (from.startsWith("Celsius") && to.startsWith("Fahrenheit")) {
                (amount * 9.0 / 5.0) + 32.0
            } else {
                amount
            }
            return "${df.format(amount)} ${shortUnit(from)} = ${df.format(res)} ${shortUnit(to)}"
        }

        if (catFrom == "Weight") {
            val grams = toGrams(amount, from)
            val finalVal = fromGrams(grams, to)
            return "${df.format(amount)} ${shortUnit(from)} = ${df.format(finalVal)} ${shortUnit(to)}"
        }

        if (catFrom == "Volume") {
            val ml = toMl(amount, from)
            val finalVal = fromMl(ml, to)
            return "${df.format(amount)} ${shortUnit(from)} = ${df.format(finalVal)} ${shortUnit(to)}"
        }

        return "${df.format(amount)} ${shortUnit(from)} = ${df.format(amount)} ${shortUnit(to)}"
    }

    private fun isTemp(u: String) = u.contains("°F") || u.contains("°C")
    private fun isWeight(u: String) = u.contains("lbs") || u.contains("oz") || u.contains("g)") || u.contains("kg")
    private fun isVolume(u: String) = u.contains("tsp") || u.contains("tbsp") || u.contains("fl oz") || u.contains("Cup") || u.contains("Pint") || u.contains("Quart") || u.contains("Gallon") || u.contains("ml") || u.contains("L)")

    private fun getCategory(u: String): String {
        return when {
            isTemp(u) -> "Temperature"
            isWeight(u) -> "Weight"
            isVolume(u) -> "Volume"
            else -> "Other"
        }
    }

    private fun toGrams(amount: Double, unit: String): Double {
        return when {
            unit.contains("lbs") -> amount * 453.59237
            unit.contains("oz") -> amount * 28.34952
            unit.contains("kg") -> amount * 1000.0
            else -> amount
        }
    }

    private fun fromGrams(grams: Double, unit: String): Double {
        return when {
            unit.contains("lbs") -> grams / 453.59237
            unit.contains("oz") -> grams / 28.34952
            unit.contains("kg") -> grams / 1000.0
            else -> grams
        }
    }

    private fun toMl(amount: Double, unit: String): Double {
        return when {
            unit.contains("tsp") -> amount * 4.92892
            unit.contains("tbsp") -> amount * 14.7868
            unit.contains("fl oz") -> amount * 29.5735
            unit.contains("Cup") -> amount * 236.588
            unit.contains("Pint") -> amount * 473.176
            unit.contains("Quart") -> amount * 946.353
            unit.contains("Gallon") -> amount * 3785.41
            unit.contains("L)") -> amount * 1000.0
            else -> amount
        }
    }

    private fun fromMl(ml: Double, unit: String): Double {
        return when {
            unit.contains("tsp") -> ml / 4.92892
            unit.contains("tbsp") -> ml / 14.7868
            unit.contains("fl oz") -> ml / 29.5735
            unit.contains("Cup") -> ml / 236.588
            unit.contains("Pint") -> ml / 473.176
            unit.contains("Quart") -> ml / 946.353
            unit.contains("Gallon") -> ml / 3785.41
            unit.contains("L)") -> ml / 1000.0
            else -> ml
        }
    }

    private fun shortUnit(u: String): String {
        return when {
            u.contains("lbs") -> "lb"
            u.contains("oz") -> "oz"
            u.contains("kg") -> "kg"
            u.contains("g)") -> "g"
            u.contains("tsp") -> "tsp"
            u.contains("tbsp") -> "tbsp"
            u.contains("fl oz") -> "fl oz"
            u.contains("Cup") -> "cup"
            u.contains("Pint") -> "pt"
            u.contains("Quart") -> "qt"
            u.contains("Gallon") -> "gal"
            u.contains("ml") -> "ml"
            u.contains("L)") -> "L"
            u.contains("°F") -> "°F"
            u.contains("°C") -> "°C"
            else -> u
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
