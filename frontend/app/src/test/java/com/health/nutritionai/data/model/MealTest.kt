package com.health.nutritionai.data.model

import org.junit.Assert.*
import org.junit.Test

class MealTest {

    private fun createSampleNutrition() = Nutrition(
        calories = 500,
        protein = 25.0,
        carbs = 60.0,
        fat = 15.0,
        fiber = 5.0
    )

    private fun createSampleFood() = Food(
        name = "Pollo",
        confidence = 0.9,
        portion = Portion(150.0, "g"),
        nutrition = createSampleNutrition(),
        category = "Proteínas"
    )

    @Test
    fun `Meal creation with all fields`() {
        val foods = listOf(createSampleFood())
        val meal = Meal(
            mealId = "meal-123",
            detectedFoods = foods,
            totalNutrition = createSampleNutrition(),
            imageUrl = "https://example.com/meal.jpg",
            timestamp = "2024-01-15T12:30:00",
            mealType = "lunch",
            notes = "Almuerzo saludable",
            healthScore = 8.5
        )

        assertEquals("meal-123", meal.mealId)
        assertEquals(1, meal.detectedFoods.size)
        assertEquals("Pollo", meal.detectedFoods[0].name)
        assertEquals(500, meal.totalNutrition.calories)
        assertEquals("https://example.com/meal.jpg", meal.imageUrl)
        assertEquals("2024-01-15T12:30:00", meal.timestamp)
        assertEquals("lunch", meal.mealType)
        assertEquals("Almuerzo saludable", meal.notes)
        assertEquals(8.5, meal.healthScore)
    }

    @Test
    fun `Meal creation with default values`() {
        val meal = Meal(
            mealId = "meal-456",
            detectedFoods = emptyList(),
            totalNutrition = createSampleNutrition(),
            imageUrl = null,
            timestamp = "2024-01-15T08:00:00"
        )

        assertNull(meal.mealType)
        assertNull(meal.notes)
        assertNull(meal.healthScore)
        assertNull(meal.imageUrl)
        assertTrue(meal.detectedFoods.isEmpty())
    }

    @Test
    fun `Meal with multiple detected foods`() {
        val food1 = createSampleFood()
        val food2 = Food(
            name = "Arroz",
            confidence = 0.85,
            portion = Portion(200.0, "g"),
            nutrition = Nutrition(260, 5.0, 56.0, 0.5, 0.6),
            category = "Carbohidratos"
        )
        val foods = listOf(food1, food2)

        val totalNutrition = Nutrition(
            calories = 760,
            protein = 30.0,
            carbs = 116.0,
            fat = 15.5,
            fiber = 5.6
        )

        val meal = Meal(
            mealId = "meal-789",
            detectedFoods = foods,
            totalNutrition = totalNutrition,
            imageUrl = "https://example.com/plate.jpg",
            timestamp = "2024-01-15T19:00:00",
            mealType = "dinner"
        )

        assertEquals(2, meal.detectedFoods.size)
        assertEquals(760, meal.totalNutrition.calories)
    }

    @Test
    fun `MealSummary creation`() {
        val summary = MealSummary(
            mealId = "meal-100",
            mealType = "breakfast",
            imageUrl = "https://example.com/breakfast.jpg",
            totalCalories = 350,
            timestamp = "2024-01-15T08:30:00"
        )

        assertEquals("meal-100", summary.mealId)
        assertEquals("breakfast", summary.mealType)
        assertEquals(350, summary.totalCalories)
        assertEquals("2024-01-15T08:30:00", summary.timestamp)
    }

    @Test
    fun `Meal copy with modifications`() {
        val meal = Meal(
            mealId = "meal-original",
            detectedFoods = listOf(createSampleFood()),
            totalNutrition = createSampleNutrition(),
            imageUrl = null,
            timestamp = "2024-01-15T12:00:00",
            mealType = "lunch"
        )

        val modifiedMeal = meal.copy(
            notes = "Nueva nota",
            healthScore = 9.0
        )

        assertEquals("Nueva nota", modifiedMeal.notes)
        assertEquals(9.0, modifiedMeal.healthScore)
        assertEquals("meal-original", modifiedMeal.mealId)
        assertEquals("lunch", modifiedMeal.mealType)
    }

    @Test
    fun `Meal equality`() {
        val nutrition = createSampleNutrition()
        val foods = listOf(createSampleFood())

        val meal1 = Meal(
            mealId = "meal-1",
            detectedFoods = foods,
            totalNutrition = nutrition,
            imageUrl = null,
            timestamp = "2024-01-15T12:00:00"
        )

        val meal2 = Meal(
            mealId = "meal-1",
            detectedFoods = foods,
            totalNutrition = nutrition,
            imageUrl = null,
            timestamp = "2024-01-15T12:00:00"
        )

        assertEquals(meal1, meal2)
    }
}

