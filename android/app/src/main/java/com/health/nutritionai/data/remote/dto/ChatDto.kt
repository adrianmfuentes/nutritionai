package com.health.nutritionai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    @SerializedName("role")
    val role: String, // "user" or "assistant"
    @SerializedName("content")
    val content: String,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatRequest(
    @SerializedName("message")
    val message: String,
    @SerializedName("conversationHistory")
    val conversationHistory: List<ChatMessage>? = null,
    @SerializedName("language")
    val language: String
)

data class ChatResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("shouldRegisterMeal")
    val shouldRegisterMeal: Boolean = false,
    @SerializedName("mealData")
    val mealData: MealDataDto? = null
)

data class MealDataDto(
    @SerializedName("foods")
    val foods: List<FoodItemDto>,
    @SerializedName("totalNutrition")
    val totalNutrition: NutritionDto,
    @SerializedName("mealType")
    val mealType: String? = null
)

data class FoodItemDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("unit")
    val unit: String,
    @SerializedName("nutrition")
    val nutrition: NutritionDto,
    @SerializedName("category")
    val category: String = "other"
)

