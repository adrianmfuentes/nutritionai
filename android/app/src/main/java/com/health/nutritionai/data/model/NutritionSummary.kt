package com.health.nutritionai.data.model

data class NutritionSummary(
    val date: String,
    val totals: Nutrition,
    val goals: NutritionGoals,
    val progress: NutritionProgress,
    val meals: List<MealSummary>
)

data class NutritionGoals(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

data class NutritionProgress(
    val caloriesPercent: Double,
    val proteinPercent: Double,
    val carbsPercent: Double,
    val fatPercent: Double
)

data class WeeklyNutrition(
    val days: List<DailyNutrition>,
    val averages: Nutrition,
    val trend: String
)

data class DailyNutrition(
    val date: String,
    val totals: Nutrition,
    val mealCount: Int
)

