package com.tuapp.nutritionai.data.repository

import com.tuapp.nutritionai.data.local.dao.MealDao
import com.tuapp.nutritionai.data.local.entity.toDomain
import com.tuapp.nutritionai.data.local.entity.toEntity
import com.tuapp.nutritionai.data.model.Meal
import com.tuapp.nutritionai.data.remote.api.NutritionApiService
import com.tuapp.nutritionai.data.remote.dto.toDomain
import com.tuapp.nutritionai.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(
    private val apiService: NutritionApiService,
    private val mealDao: MealDao
) {
    suspend fun analyzeMeal(imageFile: File): NetworkResult<Meal> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            
            val response = apiService.analyzeMeal(body)
            
            if (response.isSuccessful && response.body() != null) {
                val meal = response.body()!!.toDomain()
                // Cache locally
                mealDao.insertMeal(meal.toEntity())
                NetworkResult.Success(meal)
            } else {
                NetworkResult.Error("API Error: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Exception: ${e.localizedMessage}")
        }
    }

    fun getAllMeals(): Flow<List<Meal>> {
        return mealDao.getAllMeals().map { entities -> 
            entities.map { it.toDomain() }
        }
    }
}
