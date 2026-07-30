package com.health.nutritionai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.health.nutritionai.data.local.dao.FoodDao
import com.health.nutritionai.data.local.dao.MealDao
import com.health.nutritionai.data.local.dao.PendingMealDao
import com.health.nutritionai.data.local.entity.FoodEntity
import com.health.nutritionai.data.local.entity.MealEntity
import com.health.nutritionai.data.local.entity.PendingMealEntity

@Database(
    entities = [MealEntity::class, FoodEntity::class, PendingMealEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun foodDao(): FoodDao
    abstract fun pendingMealDao(): PendingMealDao
}

