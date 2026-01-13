package com.health.nutritionai.data.local.dao

import androidx.room.*
import com.health.nutritionai.data.local.entity.MealEntity
import com.health.nutritionai.data.local.entity.MealWithFoods
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    @Transaction
    @Query("SELECT * FROM meals WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllMealsWithFoods(userId: String): Flow<List<MealWithFoods>>

    @Query("SELECT * FROM meals WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllMeals(userId: String): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE userId = :userId AND DATE(timestamp) = :date ORDER BY timestamp DESC")
    fun getMealsByDate(userId: String, date: String): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE mealId = :mealId")
    suspend fun getMealById(mealId: String): MealEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealEntity>)

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("DELETE FROM meals WHERE mealId = :mealId")
    suspend fun deleteMealById(mealId: String)

    @Query("DELETE FROM meals")
    suspend fun deleteAllMeals()
}

