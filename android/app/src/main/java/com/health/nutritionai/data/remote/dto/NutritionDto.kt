package com.health.nutritionai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DailyNutritionResponse(
    @SerializedName("date")
    val date: String,
    @SerializedName("totals")
    val totals: NutritionDto,
    @SerializedName("goals")
    val goals: NutritionGoalsDto,
    @SerializedName("progress")
    val progress: NutritionProgressDto,
    @SerializedName("meals")
    val meals: List<MealSummaryDto>
)

data class NutritionGoalsDto(
    @SerializedName("calories")
    val calories: Int,
    @SerializedName("protein")
    val protein: Double,
    @SerializedName("carbs")
    val carbs: Double,
    @SerializedName("fat")
    val fat: Double
)

data class NutritionProgressDto(
    @SerializedName("caloriesPercent")
    val caloriesPercent: Double,
    @SerializedName("proteinPercent")
    val proteinPercent: Double,
    @SerializedName("carbsPercent")
    val carbsPercent: Double,
    @SerializedName("fatPercent")
    val fatPercent: Double
)

data class MealSummaryDto(
    @SerializedName("mealId")
    val mealId: String?,
    @SerializedName("mealType")
    val mealType: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("totalCalories")
    val totalCalories: Int?,
    @SerializedName("totalProtein")
    val totalProtein: Double? = null,
    @SerializedName("totalCarbs")
    val totalCarbs: Double? = null,
    @SerializedName("totalFat")
    val totalFat: Double? = null,
    @SerializedName("totalFiber")
    val totalFiber: Double? = null,
    @SerializedName("healthScore")
    val healthScore: Double? = null,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("timestamp")
    val timestamp: String?
)

data class WeeklyNutritionResponse(
    @SerializedName("days")
    val days: List<DailyNutritionDto>,
    @SerializedName("averages")
    val averages: NutritionDto,
    @SerializedName("trend")
    val trend: String
)

data class DailyNutritionDto(
    @SerializedName("date")
    val date: String,
    @SerializedName("totals")
    val totals: NutritionDto,
    @SerializedName("mealCount")
    val mealCount: Int
)

