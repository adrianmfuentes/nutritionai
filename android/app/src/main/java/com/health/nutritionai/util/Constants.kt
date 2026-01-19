package com.health.nutritionai.util

object Constants {
    const val DATABASE_NAME = "nutrition_ai_db"
    const val PREFERENCES_NAME = "nutrition_ai_prefs"

    // API Constants
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // Preferences Keys
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val KEY_LANGUAGE = "language"
    const val KEY_UNITS = "units"

    // Date Format
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

    // Meal Types
    const val MEAL_TYPE_BREAKFAST = "breakfast"
    const val MEAL_TYPE_LUNCH = "lunch"
    const val MEAL_TYPE_DINNER = "dinner"
    const val MEAL_TYPE_SNACK = "snack"
}

