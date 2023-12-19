package com.example.letmeknow.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.letmeknow.R

class ThemeAlertDialog(
    context: Context,
    private val checkedTheme: Int,
    private val onThemeSelected: (Int) -> Unit
): Dialog(context) {
    private lateinit var themeRadioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_theme_alert)

        themeRadioGroup = findViewById(R.id.radioGroup)  // Replace with the actual ID of your RadioGroup

        val btnLight = findViewById<RadioButton>(R.id.btnLight)
        val btnDark = findViewById<RadioButton>(R.id.btnDark)
        val btnSystemDefault = findViewById<RadioButton>(R.id.btnSystemDefault)

        btnLight.isChecked = (checkedTheme == LIGHT_MODE)
        btnDark.isChecked = (checkedTheme == DARK_MODE)
        btnSystemDefault.isChecked = (checkedTheme == DEFAULT_MODE)

        setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dismiss()
                true
            } else {
                false
            }
        }

        findViewById<Button>(R.id.btnOK).setOnClickListener {
            val selectedTheme = when {
                btnLight.isChecked -> LIGHT_MODE
                btnDark.isChecked -> DARK_MODE
                btnSystemDefault.isChecked -> DEFAULT_MODE
                else -> DEFAULT_MODE
            }

            onThemeSelected.invoke(selectedTheme)
            dismiss()
        }

        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val LIGHT_MODE = 0
        const val DARK_MODE = 1
        const val DEFAULT_MODE = 2
    }
}