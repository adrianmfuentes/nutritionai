package com.health.nutritionai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class RegisterRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("name")
    val name: String
)

data class AuthResponseDto(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: UserProfileDto?
)

data class UserProfileDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("profile_photo")
    val photoUrl: String? = null,
    @SerializedName("goals")
    val goals: NutritionGoalsDto? = null
)
data class UpdateGoalsRequest(
    @SerializedName("dailyCalories")
    val dailyCalories: Int,
    @SerializedName("proteinGrams")
    val proteinGrams: Double,
    @SerializedName("carbsGrams")
    val carbsGrams: Double,
    @SerializedName("fatGrams")
    val fatGrams: Double
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword")
    val currentPassword: String,
    @SerializedName("newPassword")
    val newPassword: String
)

data class UpdateProfileRequest(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("profile_photo")
    val photoUrl: String? = null
)

data class SendVerificationRequest(
    @SerializedName("email")
    val email: String
)

data class VerifyEmailRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("code")
    val code: String
)

data class DeleteAccountRequest(
    @SerializedName("password")
    val password: String
)

data class BaseResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null
)

data class RegisterResponseDto(
    @SerializedName("message")
    val message: String,
    @SerializedName("user")
    val user: UserProfileDto?
)
