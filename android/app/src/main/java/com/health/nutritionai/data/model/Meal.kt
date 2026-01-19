package com.health.nutritionai.data.model

data class Meal(
    val mealId: String,
    val detectedFoods: List<Food>,
    val totalNutrition: Nutrition,
    val imageUrl: String?,
    val timestamp: String,
    val mealType: String? = null,
    val notes: String? = null,
    val healthScore: Double? = null,
    val advice: String? = null
)

data class MealSummary(
    val mealId: String,
    val mealType: String,
    val imageUrl: String?,
    val totalCalories: Int,
    val timestamp: String
)

