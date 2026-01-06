package com.tuapp.nutritionai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tuapp.nutritionai.data.model.Meal
import com.tuapp.nutritionai.data.model.NutritionSummary
import com.tuapp.nutritionai.data.model.DetectedFood
import java.util.UUID

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey
    val mealId: String,
    val imageUrl: String?,
    val timestamp: String,
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    // Store detected foods as JSON string or relation? 
    // For simplicity, we can ignore storing detailed food list in this table for now
    // or use TypeConverters. Let's use TypeConverters if we want to store it here, 
    // but for now, let's keep it simple and just map the summary.
    // Ideally we would have a FoodEntity with a foreign key to MealEntity.
)

// We need TypeConverters for List<DetectedFood> if we want to store it in one table
// Or we create a relation. Let's create a simple converter for now.

fun Meal.toEntity(): MealEntity {
    return MealEntity(
        mealId = mealId, // Assuming mealId is UUID string
        imageUrl = imageUrl,
        timestamp = timestamp,
        totalCalories = totalNutrition.calories,
        totalProtein = totalNutrition.protein,
        totalCarbs = totalNutrition.carbs,
        totalFat = totalNutrition.fat
    )
}

fun MealEntity.toDomain(): Meal {
    // This is lossy as we aren't storing the foods list nicely in this simple entity example
    // In a real app we'd fetch foods from a related table.
    return Meal(
        mealId = mealId,
        detectedFoods = emptyList(), 
        totalNutrition = NutritionSummary(totalCalories, totalProtein, totalCarbs, totalFat),
        imageUrl = imageUrl,
        timestamp = timestamp
    )
}
