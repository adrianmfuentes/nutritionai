package com.health.nutritionai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey
    val mealId: String,
    val userId: String,
    val mealType: String?,
    val imageUrl: String?,
    val notes: String?,
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalFiber: Double?,
    val healthScore: Double?,
    val timestamp: String,
    val createdAt: Long = System.currentTimeMillis()
)

