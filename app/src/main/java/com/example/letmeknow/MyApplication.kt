package com.example.letmeknow

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.letmeknow.utils.SharedPreferenceManager

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        val sharedPreferenceManager = SharedPreferenceManager(this)
        AppCompatDelegate.setDefaultNightMode(sharedPreferenceManager.themeFlag[sharedPreferenceManager.theme])
    }

}