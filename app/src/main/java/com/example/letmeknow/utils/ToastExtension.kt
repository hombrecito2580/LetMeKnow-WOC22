package com.example.letmeknow.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.example.letmeknow.R

fun Context.showCustomToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val customToastView = inflater.inflate(R.layout.toast_custom, null)

    val customToastText = customToastView.findViewById<TextView>(R.id.customToastText)
    customToastText.text = message

    val toast = Toast(this)
    toast.view = customToastView
    toast.duration = duration
    toast.setGravity(Gravity.TOP, 0, 200)
    toast.show()
}
