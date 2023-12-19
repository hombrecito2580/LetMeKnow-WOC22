package com.example.letmeknow.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import com.example.letmeknow.R

class LogoutDialog(context: Context, private val onOKPressed: () -> Unit): Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_logout)

        findViewById<Button>(R.id.btnNO).setOnClickListener {
            dismiss()
        }

        findViewById<Button>(R.id.btnYES).setOnClickListener {
            onOKPressed.invoke()
            dismiss()
        }
    }

}