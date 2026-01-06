package com.tuapp.nutritionai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tuapp.nutritionai.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Query("DELETE FROM meals")
    suspend fun deleteAllMeals()
    
    // Simple query for daily totals would be nice here
    @Query("SELECT SUM(totalCalories) FROM meals WHERE timestamp LIKE :datePattern || '%'")
    fun getDailyCalories(datePattern: String): Flow<Int?>
}
