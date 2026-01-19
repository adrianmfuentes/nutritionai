package com.health.nutritionai.util

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, languageName: String): Context {
        Log.d("LocaleHelper", "Setting locale for name: $languageName")
        val code = getLanguageCode(languageName)
        Log.d("LocaleHelper", "Applied ISO code: $code")
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    fun getLanguageCode(language: String): String {
        return when (language) {
            "Español" -> "es"
            "English" -> "en"
            "Français" -> "fr"
            "Deutsch" -> "de"
            "es", "en", "fr", "de" -> language
            else -> "es"
        }
    }
}
