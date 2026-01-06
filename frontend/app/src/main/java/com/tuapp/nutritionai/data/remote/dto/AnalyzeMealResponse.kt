package com.tuapp.nutritionai.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.tuapp.nutritionai.data.model.DetectedFood
import com.tuapp.nutritionai.data.model.Meal
import com.tuapp.nutritionai.data.model.NutritionSummary
import com.tuapp.nutritionai.data.model.NutritionValues
import com.tuapp.nutritionai.data.model.Portion

data class AnalyzeMealResponse(
    @SerializedName("mealId") val mealId: String,
    @SerializedName("detectedFoods") val detectedFoods: List<DetectedFoodDto>,
    @SerializedName("totalNutrition") val totalNutrition: NutritionSummaryDto,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("timestamp") val timestamp: String
)

data class DetectedFoodDto(
    @SerializedName("name") val name: String,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("portion") val portion: PortionDto,
    @SerializedName("nutrition") val nutrition: NutritionValuesDto
)

data class PortionDto(
    @SerializedName("amount") val amount: Double,
    @SerializedName("unit") val unit: String
)

data class NutritionValuesDto(
    @SerializedName("calories") val calories: Int,
    @SerializedName("protein") val protein: Double,
    @SerializedName("carbs") val carbs: Double,
    @SerializedName("fat") val fat: Double,
    @SerializedName("fiber") val fiber: Double?
)

data class NutritionSummaryDto(
    @SerializedName("calories") val calories: Int,
    @SerializedName("protein") val protein: Double,
    @SerializedName("carbs") val carbs: Double,
    @SerializedName("fat") val fat: Double
)

// Mappers directly in DTO file for simplicity
fun AnalyzeMealResponse.toDomain(): Meal {
    return Meal(
        mealId = mealId,
        detectedFoods = detectedFoods.map { it.toDomain() },
        totalNutrition = totalNutrition.toDomain(),
        imageUrl = imageUrl,
        timestamp = timestamp
    )
}

fun DetectedFoodDto.toDomain(): DetectedFood {
    return DetectedFood(
        name = name,
        confidence = confidence,
        portion = Portion(portion.amount, portion.unit),
        nutrition = NutritionValues(nutrition.calories, nutrition.protein, nutrition.carbs, nutrition.fat, nutrition.fiber ?: 0.0)
    )
}

fun NutritionSummaryDto.toDomain(): NutritionSummary {
    return NutritionSummary(
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat
    )
}
