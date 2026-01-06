package com.tuapp.nutritionai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tuapp.nutritionai.data.local.dao.MealDao
import com.tuapp.nutritionai.data.local.entity.MealEntity

@Database(entities = [MealEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
}
