package com.health.nutritionai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AnalyzeTextRequest(
    @SerializedName("description")
    val description: String,
    @SerializedName("mealType")
    val mealType: String?,
    @SerializedName("timestamp")
    val timestamp: String?
)

data class AnalyzeMealResponse(
    @SerializedName("mealId")
    val mealId: String,
    @SerializedName("detectedFoods")
    val detectedFoods: List<DetectedFoodDto>,
    @SerializedName("totalNutrition")
    val totalNutrition: NutritionDto,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("mealContext")
    val mealContext: MealContextDto?
)

data class DetectedFoodDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("confidence")
    val confidence: Double,
    @SerializedName("portion")
    val portion: PortionDto,
    @SerializedName("nutrition")
    val nutrition: NutritionDto,
    @SerializedName("category")
    val category: String,
    @SerializedName("imageUrl")
    val imageUrl: String? = null
)

data class PortionDto(
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("unit")
    val unit: String
)

data class NutritionDto(
    @SerializedName("calories")
    val calories: Int,
    @SerializedName("protein")
    val protein: Double,
    @SerializedName("carbs")
    val carbs: Double,
    @SerializedName("fat")
    val fat: Double,
    @SerializedName("fiber")
    val fiber: Double?
)

data class MealContextDto(
    @SerializedName("estimatedMealType")
    val estimatedMealType: String?,
    @SerializedName("portionSize")
    val portionSize: String?,
    @SerializedName("healthScore")
    val healthScore: Double?
)

