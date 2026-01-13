package com.health.nutritionai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateMealRequest(
    @SerializedName("mealType")
    val mealType: String?,
    @SerializedName("notes")
    val notes: String?
)
