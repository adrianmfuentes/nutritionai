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

import com.health.nutritionai.data.remote.api.NutritionApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class MealRepository(
    private val mealDao: MealDao,
    private val foodDao: FoodDao,
    private val apiService: NutritionApiService,
    private val userRepository: UserRepository
) {

    // Mock goals for offline mode (removed mock data)
    
    fun getAllMeals(userId: String): Flow<List<Meal>> {
        return mealDao.getAllMealsWithFoods(userId).map { mealsWithFoods ->
            mealsWithFoods.map { it.toMeal() }
        }
    }

    suspend fun analyzeMeal(imageFile: File, mealType: String? = null): NetworkResult<Meal> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            val mealTypePart = mealType?.toRequestBody("text/plain".toMediaTypeOrNull())
            val timestampPart = System.currentTimeMillis().toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.analyzeMeal(imagePart, mealTypePart, timestampPart)

            val meal = Meal(
                mealId = response.mealId,
                detectedFoods = response.detectedFoods.map { food ->
                    Food(
                        name = food.name,
                        confidence = food.confidence,
                        portion = Portion(food.portion.amount, food.portion.unit),
                        nutrition = Nutrition(
                            calories = food.nutrition.calories,
                            protein = food.nutrition.protein,
                            carbs = food.nutrition.carbs,
                            fat = food.nutrition.fat,
                            fiber = food.nutrition.fiber ?: 0.0
                        ),
                        category = food.category,
                        imageUrl = food.imageUrl
                    )
                },
                totalNutrition = Nutrition(
                    calories = response.totalNutrition.calories,
                    protein = response.totalNutrition.protein,
                    carbs = response.totalNutrition.carbs,
                    fat = response.totalNutrition.fat,
                    fiber = response.totalNutrition.fiber ?: 0.0
                ),
                imageUrl = response.imageUrl,
                timestamp = response.timestamp,
                mealType = response.mealContext?.estimatedMealType ?: mealType ?: "unknown", // Prioritize detected, then requested, then default
                healthScore = response.mealContext?.healthScore ?: 0.0,
                notes = null // Inicialmente sin notas
            )

            // Save locally with actual user ID
            val userId = userRepository.getUserId()
            saveMealLocally(meal, userId)

            NetworkResult.Success(meal)
        } catch (e: Exception) {
            val userFriendlyMessage = com.health.nutritionai.util.ErrorMapper.mapErrorToMessage(
                e,
                com.health.nutritionai.util.ErrorContext.MEAL_ANALYSIS
            )
            NetworkResult.Error(userFriendlyMessage)
        }
    }

    suspend fun analyzeTextDescription(description: String, mealType: String? = null): NetworkResult<Meal> {
        return try {
            val response = apiService.analyzeTextDescription(
                request = com.health.nutritionai.data.remote.dto.AnalyzeTextRequest(
                    description = description,
                    mealType = mealType,
                    timestamp = System.currentTimeMillis().toString()
                )
            )

            val meal = Meal(
                mealId = response.mealId,
                detectedFoods = response.detectedFoods.map { food ->
                    Food(
                        name = food.name,
                        confidence = food.confidence,
                        portion = Portion(food.portion.amount, food.portion.unit),
                        nutrition = Nutrition(
                            calories = food.nutrition.calories,
                            protein = food.nutrition.protein,
                            carbs = food.nutrition.carbs,
                            fat = food.nutrition.fat,
                            fiber = food.nutrition.fiber ?: 0.0
                        ),
                        category = food.category,
                        imageUrl = food.imageUrl
                    )
                },
                totalNutrition = Nutrition(
                    calories = response.totalNutrition.calories,
                    protein = response.totalNutrition.protein,
                    carbs = response.totalNutrition.carbs,
                    fat = response.totalNutrition.fat,
                    fiber = response.totalNutrition.fiber ?: 0.0
                ),
                imageUrl = response.imageUrl,
                timestamp = response.timestamp,
                mealType = response.mealContext?.estimatedMealType ?: mealType ?: "unknown",
                healthScore = response.mealContext?.healthScore ?: 0.0,
                notes = description // Save the text description as notes
            )

            // Save locally with actual user ID
            val userId = userRepository.getUserId()
            saveMealLocally(meal, userId)

            NetworkResult.Success(meal)
        } catch (e: Exception) {
            val userFriendlyMessage = com.health.nutritionai.util.ErrorMapper.mapErrorToMessage(
                e,
                com.health.nutritionai.util.ErrorContext.MEAL_ANALYSIS
            )
            NetworkResult.Error(userFriendlyMessage)
        }
    }


    suspend fun deleteMeal(mealId: String): NetworkResult<Boolean> {
        return try {
            apiService.deleteMeal(mealId)
            mealDao.deleteMealById(mealId)
            NetworkResult.Success(true)
        } catch (e: Exception) {
            val userFriendlyMessage = com.health.nutritionai.util.ErrorMapper.mapErrorToMessage(
                e,
                com.health.nutritionai.util.ErrorContext.MEAL_DELETE
            )
            NetworkResult.Error(userFriendlyMessage)
        }
    }

    suspend fun updateMeal(meal: Meal): NetworkResult<Boolean> {
        return try {
            val userId = userRepository.getUserId()
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
            mealDao.updateMeal(mealEntity)
            NetworkResult.Success(true)
        } catch (e: Exception) {
            NetworkResult.Error("Error al actualizar la comida")
        }
    }

    suspend fun refreshMeals(userId: String) {
        try {
            val response = apiService.getMeals()
            val meals = response.meals.mapNotNull { summary ->
                // Filter out meals with null required fields
                if (summary.mealId != null && summary.mealType != null &&
                    summary.imageUrl != null && summary.totalCalories != null &&
                    summary.timestamp != null) {
                    MealEntity(
                        mealId = summary.mealId,
                        userId = userId,
                        mealType = summary.mealType,
                        imageUrl = summary.imageUrl,
                        notes = null,
                        totalCalories = summary.totalCalories,
                        totalProtein = 0.0,
                        totalCarbs = 0.0,
                        totalFat = 0.0,
                        totalFiber = 0.0,
                        healthScore = 0.0,
                        timestamp = summary.timestamp
                    )
                } else {
                    null // Skip meals with missing data
                }
            }
            mealDao.insertMeals(meals)
        } catch (e: Exception) {
            e.printStackTrace()
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
                category = food.category,
                imageUrl = food.imageUrl
            )
        }

        foodDao.insertFoods(foodEntities)
    }

    suspend fun saveMealWithFoods(mealEntity: MealEntity, foodEntities: List<FoodEntity>) {
        mealDao.insertMeal(mealEntity)
        foodDao.insertFoods(foodEntities)
    }

    // Extension functions for mapping

    private fun com.health.nutritionai.data.local.entity.MealWithFoods.toMeal() = Meal(
        mealId = meal.mealId,
        detectedFoods = foods.map { it.toFood() },
        totalNutrition = Nutrition(
            calories = meal.totalCalories,
            protein = meal.totalProtein,
            carbs = meal.totalCarbs,
            fat = meal.totalFat,
            fiber = meal.totalFiber
        ),
        imageUrl = meal.imageUrl,
        timestamp = meal.timestamp,
        mealType = meal.mealType,
        notes = meal.notes,
        healthScore = meal.healthScore
    )

    private fun FoodEntity.toFood() = Food(
        name = name,
        confidence = confidence,
        portion = Portion(portionAmount, portionUnit),
        nutrition = Nutrition(
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = fiber
        ),
        category = category,
        imageUrl = imageUrl
    )
}

