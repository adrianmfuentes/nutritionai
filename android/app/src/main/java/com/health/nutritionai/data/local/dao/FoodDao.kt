package com.health.nutritionai.data.local.dao

import androidx.room.*
import com.health.nutritionai.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Query("SELECT * FROM detected_foods WHERE mealId = :mealId")
    fun getFoodsByMealId(mealId: String): Flow<List<FoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Delete
    suspend fun deleteFood(food: FoodEntity)

    @Query("DELETE FROM detected_foods WHERE mealId = :mealId")
    suspend fun deleteFoodsByMealId(mealId: String)
}

