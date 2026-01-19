package com.health.nutritionai.data.model

data class Food(
    val name: String,
    val confidence: Double,
    val portion: Portion,
    val nutrition: Nutrition,
    val category: String,
    val imageUrl: String? = null // Image/logo URL for the food
)

data class Portion(
    val amount: Double,
    val unit: String
)

data class Nutrition(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double?
)

