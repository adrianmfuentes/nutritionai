package com.health.nutritionai.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class MealWithFoods(
    @Embedded val meal: MealEntity,
    @Relation(
        parentColumn = "mealId",
        entityColumn = "mealId"
    )
    val foods: List<FoodEntity>
)

