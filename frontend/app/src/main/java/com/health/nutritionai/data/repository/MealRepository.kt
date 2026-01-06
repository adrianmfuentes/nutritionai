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
                        category = food.category
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
            NetworkResult.Error(e.message ?: "Error al analizar la comida")
        }
    }

    suspend fun getMealById(mealId: String): NetworkResult<Meal> {
        return try {
            // Try network first for fresh data
            val response = apiService.getMealById(mealId)
            val analyzeResponse = response.meal
            
            val meal = Meal(
                mealId = analyzeResponse.mealId,
                detectedFoods = analyzeResponse.detectedFoods.map { food ->
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
                        category = food.category
                    )
                },
                totalNutrition = Nutrition(
                    calories = analyzeResponse.totalNutrition.calories,
                    protein = analyzeResponse.totalNutrition.protein,
                    carbs = analyzeResponse.totalNutrition.carbs,
                    fat = analyzeResponse.totalNutrition.fat,
                    fiber = analyzeResponse.totalNutrition.fiber ?: 0.0
                ),
                imageUrl = analyzeResponse.imageUrl,
                timestamp = analyzeResponse.timestamp,
                mealType = analyzeResponse.mealContext?.estimatedMealType ?: "unknown",
                healthScore = analyzeResponse.mealContext?.healthScore ?: 0.0,
                notes = null
            )
            
            // Update local cache with actual user ID
            val userId = userRepository.getUserId()
            saveMealLocally(meal, userId)

            NetworkResult.Success(meal)
        } catch (e: Exception) {
            // Fallback to local
            try {
               val localMeal = mealDao.getMealById(mealId)
               if (localMeal != null) {
                   return NetworkResult.Success(localMeal.toMeal())
               }
            } catch (localEx: Exception) {
               // Ignore
            }
            NetworkResult.Error(e.message ?: "Error al obtener la comida")
        }
    }

    suspend fun deleteMeal(mealId: String): NetworkResult<Boolean> {
        return try {
            apiService.deleteMeal(mealId)
            mealDao.deleteMealById(mealId)
            NetworkResult.Success(true)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error al eliminar la comida")
        }
    }
    
    suspend fun refreshMeals(userId: String) {
        try {
            val response = apiService.getMeals()
            val meals = response.meals.map { summary ->
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

