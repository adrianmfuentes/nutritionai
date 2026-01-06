package com.tuapp.nutritionai.data.model

data class Meal(
    val mealId: String,
    val detectedFoods: List<DetectedFood>,
    val totalNutrition: NutritionSummary,
    val imageUrl: String?,
    val timestamp: String
)

data class DetectedFood(
    val name: String,
    val confidence: Double,
    val portion: Portion,
    val nutrition: NutritionValues
)

data class Portion(
    val amount: Double,
    val unit: String
)

data class NutritionValues(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double? = null
)

data class NutritionSummary(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)
