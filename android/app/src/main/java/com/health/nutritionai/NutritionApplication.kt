package com.health.nutritionai

import android.app.Application
import android.content.Context
import com.health.nutritionai.util.LocaleHelper

class NutritionApplication : Application() {

    override fun attachBaseContext(base: Context) {
        // Load saved language and apply locale
        val prefs = base.getSharedPreferences(com.health.nutritionai.util.Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val language = prefs.getString(com.health.nutritionai.util.Constants.KEY_LANGUAGE, "Español") ?: "Español"
        val context = LocaleHelper.setLocale(base, language)
        super.attachBaseContext(context)
    }
}
