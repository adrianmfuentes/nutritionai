package com.health.nutritionai.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context, language: String): Context {
        val locale = when (language) {
            "English" -> Locale.forLanguageTag("en")
            "Español" -> Locale.forLanguageTag("es")
            "Français" -> Locale.forLanguageTag("fr")
            "Deutsch" -> Locale.forLanguageTag("de")
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun getLanguageCode(language: String): String {
        return when (language) {
            "English" -> "en"
            "Español" -> "es"
            "Français" -> "fr"
            "Deutsch" -> "de"
            else -> "es"
        }
    }
}
