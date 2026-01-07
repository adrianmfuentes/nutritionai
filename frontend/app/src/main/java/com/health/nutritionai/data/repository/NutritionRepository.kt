package com.health.nutritionai.data.repository

import com.health.nutritionai.data.model.*
import com.health.nutritionai.data.remote.api.NutritionApiService
import com.health.nutritionai.data.remote.dto.NutritionDto
import com.health.nutritionai.data.remote.dto.NutritionGoalsDto
import com.health.nutritionai.data.remote.dto.NutritionProgressDto
import com.health.nutritionai.util.NetworkResult

class NutritionRepository(
    private val apiService: NutritionApiService
) {

    suspend fun getDailyNutrition(date: String): NetworkResult<NutritionSummary> {
        return try {
            val response = apiService.getDailyNutrition(date)
            val summary = NutritionSummary(
                date = response.date,
                totals = response.totals.toNutrition(),
                goals = response.goals.toGoals(),
                progress = response.progress.toProgress(),
                meals = response.meals.mapNotNull { meal ->
                    // Filter out meals with null required fields
                    if (meal.mealId != null && meal.mealType != null &&
                        meal.imageUrl != null && meal.totalCalories != null &&
                        meal.timestamp != null) {
                        MealSummary(
                            mealId = meal.mealId,
                            mealType = meal.mealType,
                            imageUrl = meal.imageUrl,
                            totalCalories = meal.totalCalories,
                            timestamp = meal.timestamp
                        )
                    } else {
                        null // Skip meals with missing data
                    }
                }
            )
            NetworkResult.Success(summary)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error al obtener la nutrición diaria")
        }
    }

    suspend fun getWeeklyNutrition(startDate: String): NetworkResult<WeeklyNutrition> {
        return try {
            val response = apiService.getWeeklyNutrition(startDate)
            val weekly = WeeklyNutrition(
                days = response.days.map { day ->
                    DailyNutrition(
                        date = day.date,
                        totals = day.totals.toNutrition(),
                        mealCount = day.mealCount
                    )
                },
                averages = response.averages.toNutrition(),
                trend = response.trend
            )
            NetworkResult.Success(weekly)
        } catch (e: Exception) {
            val userFriendlyMessage = com.health.nutritionai.util.ErrorMapper.mapErrorToMessage(
                e,
                com.health.nutritionai.util.ErrorContext.GENERAL
            )
            NetworkResult.Error(userFriendlyMessage)
        }
    }

    private fun NutritionDto.toNutrition() = Nutrition(
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = fiber
    )

    private fun NutritionGoalsDto.toGoals() = NutritionGoals(
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat
    )

    private fun NutritionProgressDto.toProgress() = NutritionProgress(
        caloriesPercent = caloriesPercent,
        proteinPercent = proteinPercent,
        carbsPercent = carbsPercent,
        fatPercent = fatPercent
    )
}

