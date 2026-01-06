package com.health.nutritionai.data.repository

import com.health.nutritionai.data.local.dao.FoodDao
import com.health.nutritionai.data.local.dao.MealDao
import com.health.nutritionai.data.local.entity.FoodEntity
import com.health.nutritionai.data.local.entity.MealEntity
import com.health.nutritionai.data.model.*
import com.health.nutritionai.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class MealRepository(
    private val mealDao: MealDao,
    private val foodDao: FoodDao
) {

    // Mock goals for offline mode
    private val mockGoals = NutritionGoals(
        calories = 2000,
        protein = 150.0,
        carbs = 200.0,
        fat = 65.0
    )

    fun getAllMeals(userId: String): Flow<List<Meal>> {
        return mealDao.getAllMeals(userId).map { entities ->
            entities.map { it.toMeal() }
        }
    }

    fun getMealsByDate(userId: String, date: String): Flow<List<Meal>> {
        return mealDao.getMealsByDate(userId, date).map { entities ->
            entities.map { it.toMeal() }
        }
    }

    suspend fun analyzeMeal(imageFile: File, mealType: String? = null): NetworkResult<Meal> {
        return try {
            // TODO: Call backend API when available
            // For now, return mock data
            val mockMeal = Meal(
                mealId = System.currentTimeMillis().toString(),
                detectedFoods = listOf(
                    Food(
                        name = "Pollo a la plancha",
                        confidence = 0.95,
                        portion = Portion(150.0, "g"),
                        nutrition = Nutrition(165, 31.0, 0.0, 3.6, 0.0),
                        category = "protein"
                    ),
                    Food(
                        name = "Arroz blanco",
                        confidence = 0.90,
                        portion = Portion(100.0, "g"),
                        nutrition = Nutrition(130, 2.7, 28.0, 0.3, 0.5),
                        category = "carb"
                    )
                ),
                totalNutrition = Nutrition(
                    calories = 295,
                    protein = 33.7,
                    carbs = 28.0,
                    fat = 3.9,
                    fiber = 0.5
                ),
                imageUrl = imageFile.absolutePath,
                timestamp = System.currentTimeMillis().toString(),
                mealType = mealType ?: "lunch",
                healthScore = 8.5
            )

            // Save locally
            saveMealLocally(mockMeal, "mock_user")

            NetworkResult.Success(mockMeal)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error al analizar la comida")
        }
    }

    suspend fun getMealById(mealId: String): NetworkResult<Meal> {
        return try {
            val localMeal = mealDao.getMealById(mealId)
            if (localMeal != null) {
                return NetworkResult.Success(localMeal.toMeal())
            }
            NetworkResult.Error("Comida no encontrada")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error al obtener la comida")
        }
    }

    suspend fun deleteMeal(mealId: String): NetworkResult<Boolean> {
        return try {
            mealDao.deleteMealById(mealId)
            NetworkResult.Success(true)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error al eliminar la comida")
        }
    }

    suspend fun getDailyNutrition(date: String): NetworkResult<NutritionSummary> {
        return try {
            // TODO: Replace with real API call when backend is ready
            // For now, return mock data
            val mockTotals = Nutrition(
                calories = 1200,
                protein = 80.0,
                carbs = 120.0,
                fat = 40.0,
                fiber = 15.0
            )

            val mockProgress = NutritionProgress(
                caloriesPercent = 60.0,
                proteinPercent = 53.3,
                carbsPercent = 60.0,
                fatPercent = 61.5
            )

            val mockSummary = NutritionSummary(
                date = date,
                totals = mockTotals,
                goals = mockGoals,
                progress = mockProgress,
                meals = emptyList() // TODO: Get from local DB
            )

            NetworkResult.Success(mockSummary)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error al obtener los datos nutricionales")
        }
    }

    private suspend fun saveMealLocally(meal: Meal, userId: String) {
        val mealEntity = MealEntity(
            mealId = meal.mealId,
            userId = userId,
            mealType = meal.mealType,
            imageUrl = meal.imageUrl,
            notes = meal.notes,
            totalCalories = meal.totalNutrition.calories,
            totalProtein = meal.totalNutrition.protein,
            totalCarbs = meal.totalNutrition.carbs,
            totalFat = meal.totalNutrition.fat,
            totalFiber = meal.totalNutrition.fiber,
            healthScore = meal.healthScore,
            timestamp = meal.timestamp
        )

        mealDao.insertMeal(mealEntity)

        val foodEntities = meal.detectedFoods.map { food ->
            FoodEntity(
                mealId = meal.mealId,
                name = food.name,
                confidence = food.confidence,
                portionAmount = food.portion.amount,
                portionUnit = food.portion.unit,
                calories = food.nutrition.calories,
                protein = food.nutrition.protein,
                carbs = food.nutrition.carbs,
                fat = food.nutrition.fat,
                fiber = food.nutrition.fiber,
                category = food.category
            )
        }

        foodDao.insertFoods(foodEntities)
    }

    // Extension functions for mapping
    private fun MealEntity.toMeal() = Meal(
        mealId = mealId,
        detectedFoods = emptyList(), // Would need to fetch foods separately
        totalNutrition = Nutrition(
            calories = totalCalories,
            protein = totalProtein,
            carbs = totalCarbs,
            fat = totalFat,
            fiber = totalFiber
        ),
        imageUrl = imageUrl,
        timestamp = timestamp,
        mealType = mealType,
        notes = notes,
        healthScore = healthScore
    )
}

