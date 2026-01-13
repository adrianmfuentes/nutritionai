package com.health.nutritionai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.health.nutritionai.data.local.dao.FoodDao
import com.health.nutritionai.data.local.dao.MealDao
import com.health.nutritionai.data.local.entity.FoodEntity
import com.health.nutritionai.data.local.entity.MealEntity

@Database(
    entities = [MealEntity::class, FoodEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun foodDao(): FoodDao
}

