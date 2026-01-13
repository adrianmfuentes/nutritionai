package com.health.nutritionai.data.model

import org.junit.Assert.*
import org.junit.Test

class NutritionSummaryTest {

    @Test
    fun `NutritionSummary creation with all fields`() {
        val totals = Nutrition(
            calories = 1800,
            protein = 90.0,
            carbs = 200.0,
            fat = 60.0,
            fiber = 25.0
        )

        val goals = NutritionGoals(
            calories = 2000,
            protein = 100.0,
            carbs = 250.0,
            fat = 70.0
        )

        val progress = NutritionProgress(
            caloriesPercent = 90.0,
            proteinPercent = 90.0,
            carbsPercent = 80.0,
            fatPercent = 85.7
        )

        val meals = listOf(
            MealSummary("meal-1", "breakfast", "url1", 400, "2024-01-15T08:00:00"),
            MealSummary("meal-2", "lunch", "url2", 700, "2024-01-15T13:00:00"),
            MealSummary("meal-3", "dinner", "url3", 700, "2024-01-15T20:00:00")
        )

        val summary = NutritionSummary(
            date = "2024-01-15",
            totals = totals,
            goals = goals,
            progress = progress,
            meals = meals
        )

        assertEquals("2024-01-15", summary.date)
        assertEquals(1800, summary.totals.calories)
        assertEquals(2000, summary.goals.calories)
        assertEquals(90.0, summary.progress.caloriesPercent, 0.001)
        assertEquals(3, summary.meals.size)
    }

    @Test
    fun `NutritionGoals creation`() {
        val goals = NutritionGoals(
            calories = 2500,
            protein = 150.0,
            carbs = 300.0,
            fat = 80.0
        )

        assertEquals(2500, goals.calories)
        assertEquals(150.0, goals.protein, 0.001)
        assertEquals(300.0, goals.carbs, 0.001)
        assertEquals(80.0, goals.fat, 0.001)
    }

    @Test
    fun `NutritionProgress with 100 percent values`() {
        val progress = NutritionProgress(
            caloriesPercent = 100.0,
            proteinPercent = 100.0,
            carbsPercent = 100.0,
            fatPercent = 100.0
        )

        assertEquals(100.0, progress.caloriesPercent, 0.001)
        assertEquals(100.0, progress.proteinPercent, 0.001)
        assertEquals(100.0, progress.carbsPercent, 0.001)
        assertEquals(100.0, progress.fatPercent, 0.001)
    }

    @Test
    fun `NutritionProgress with values over 100 percent`() {
        val progress = NutritionProgress(
            caloriesPercent = 120.0,
            proteinPercent = 150.0,
            carbsPercent = 90.0,
            fatPercent = 110.0
        )

        assertTrue(progress.caloriesPercent > 100.0)
        assertTrue(progress.proteinPercent > 100.0)
        assertFalse(progress.carbsPercent > 100.0)
        assertTrue(progress.fatPercent > 100.0)
    }

    @Test
    fun `WeeklyNutrition creation`() {
        val dailyNutritions = listOf(
            DailyNutrition(
                date = "2024-01-08",
                totals = Nutrition(1800, 90.0, 200.0, 60.0, 25.0),
                mealCount = 3
            ),
            DailyNutrition(
                date = "2024-01-09",
                totals = Nutrition(2000, 100.0, 220.0, 65.0, 28.0),
                mealCount = 4
            ),
            DailyNutrition(
                date = "2024-01-10",
                totals = Nutrition(1900, 95.0, 210.0, 62.0, 26.0),
                mealCount = 3
            )
        )

        val averages = Nutrition(1900, 95.0, 210.0, 62.3, 26.3)

        val weekly = WeeklyNutrition(
            days = dailyNutritions,
            averages = averages,
            trend = "stable"
        )

        assertEquals(3, weekly.days.size)
        assertEquals(1900, weekly.averages.calories)
        assertEquals("stable", weekly.trend)
    }

    @Test
    fun `DailyNutrition creation`() {
        val daily = DailyNutrition(
            date = "2024-01-15",
            totals = Nutrition(2100, 105.0, 230.0, 70.0, 30.0),
            mealCount = 5
        )

        assertEquals("2024-01-15", daily.date)
        assertEquals(2100, daily.totals.calories)
        assertEquals(5, daily.mealCount)
    }

    @Test
    fun `NutritionSummary with empty meals list`() {
        val summary = NutritionSummary(
            date = "2024-01-15",
            totals = Nutrition(0, 0.0, 0.0, 0.0, 0.0),
            goals = NutritionGoals(2000, 100.0, 250.0, 70.0),
            progress = NutritionProgress(0.0, 0.0, 0.0, 0.0),
            meals = emptyList()
        )

        assertTrue(summary.meals.isEmpty())
        assertEquals(0, summary.totals.calories)
        assertEquals(0.0, summary.progress.caloriesPercent, 0.001)
    }

    @Test
    fun `WeeklyNutrition trends`() {
        val weeklyUp = WeeklyNutrition(
            days = emptyList(),
            averages = Nutrition(2000, 100.0, 220.0, 65.0, 28.0),
            trend = "up"
        )

        val weeklyDown = WeeklyNutrition(
            days = emptyList(),
            averages = Nutrition(1500, 75.0, 180.0, 50.0, 20.0),
            trend = "down"
        )

        assertEquals("up", weeklyUp.trend)
        assertEquals("down", weeklyDown.trend)
    }
}

