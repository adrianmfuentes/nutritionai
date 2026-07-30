package com.health.nutritionai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.health.nutritionai.data.local.entity.PendingMealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingMealDao {

    @Insert
    suspend fun insert(pendingMeal: PendingMealEntity): Long

    @Query("SELECT * FROM pending_meals ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingMealEntity>

    @Query("SELECT * FROM pending_meals ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<PendingMealEntity>>

    @Query("SELECT COUNT(*) FROM pending_meals")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM pending_meals WHERE id = :id")
    suspend fun deleteById(id: Long)
}
