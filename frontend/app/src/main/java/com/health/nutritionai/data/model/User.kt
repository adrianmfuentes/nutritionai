package com.health.nutritionai.data.model

data class UserProfile(
    val userId: String,
    val email: String,
    val name: String,
    val goals: NutritionGoals? = null
)

data class AuthResponse(
    val token: String,
    val user: UserProfile? = null,
    val userId: String? = null
)

