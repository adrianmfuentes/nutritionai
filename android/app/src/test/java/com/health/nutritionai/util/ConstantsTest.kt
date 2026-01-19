package com.health.nutritionai.util

import org.junit.Assert.*
import org.junit.Test

class ConstantsTest {

    @Test
    fun `DATABASE_NAME is correct`() {
        assertEquals("nutrition_ai_db", Constants.DATABASE_NAME)
    }

    @Test
    fun `PREFERENCES_NAME is correct`() {
        assertEquals("nutrition_ai_prefs", Constants.PREFERENCES_NAME)
    }

    @Test
    fun `API timeout values have reasonable defaults`() {
        assertEquals(30L, Constants.CONNECT_TIMEOUT)
        assertEquals(30L, Constants.READ_TIMEOUT)
        assertEquals(30L, Constants.WRITE_TIMEOUT)
    }

    @Test
    fun `Preference keys are not empty`() {
        assertTrue(Constants.KEY_AUTH_TOKEN.isNotEmpty())
        assertTrue(Constants.KEY_USER_ID.isNotEmpty())
        assertTrue(Constants.KEY_USER_EMAIL.isNotEmpty())
        assertTrue(Constants.KEY_NOTIFICATIONS_ENABLED.isNotEmpty())
        assertTrue(Constants.KEY_LANGUAGE.isNotEmpty())
        assertTrue(Constants.KEY_UNITS.isNotEmpty())
    }

    @Test
    fun `Preference keys are unique`() {
        val keys = listOf(
            Constants.KEY_AUTH_TOKEN,
            Constants.KEY_USER_ID,
            Constants.KEY_USER_EMAIL,
            Constants.KEY_NOTIFICATIONS_ENABLED,
            Constants.KEY_LANGUAGE,
            Constants.KEY_UNITS
        )
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `DATE_FORMAT is valid format`() {
        assertEquals("yyyy-MM-dd", Constants.DATE_FORMAT)
    }

    @Test
    fun `DATETIME_FORMAT is valid format`() {
        assertEquals("yyyy-MM-dd'T'HH:mm:ss", Constants.DATETIME_FORMAT)
    }

    @Test
    fun `Meal type constants are correct`() {
        assertEquals("breakfast", Constants.MEAL_TYPE_BREAKFAST)
        assertEquals("lunch", Constants.MEAL_TYPE_LUNCH)
        assertEquals("dinner", Constants.MEAL_TYPE_DINNER)
        assertEquals("snack", Constants.MEAL_TYPE_SNACK)
    }

    @Test
    fun `Meal type constants are unique`() {
        val mealTypes = listOf(
            Constants.MEAL_TYPE_BREAKFAST,
            Constants.MEAL_TYPE_LUNCH,
            Constants.MEAL_TYPE_DINNER,
            Constants.MEAL_TYPE_SNACK
        )
        assertEquals(mealTypes.size, mealTypes.toSet().size)
    }

    @Test
    fun `Meal type constants are lowercase`() {
        assertEquals(Constants.MEAL_TYPE_BREAKFAST, Constants.MEAL_TYPE_BREAKFAST.lowercase())
        assertEquals(Constants.MEAL_TYPE_LUNCH, Constants.MEAL_TYPE_LUNCH.lowercase())
        assertEquals(Constants.MEAL_TYPE_DINNER, Constants.MEAL_TYPE_DINNER.lowercase())
        assertEquals(Constants.MEAL_TYPE_SNACK, Constants.MEAL_TYPE_SNACK.lowercase())
    }
}

