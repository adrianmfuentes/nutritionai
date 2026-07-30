package com.health.nutritionai.data.remote.api

import com.health.nutritionai.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface NutritionApiService {

    // Authentication
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponseDto

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponseDto

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): BaseResponse

    // Meal Analysis
    @Multipart
    @POST("meals/analyze")
    suspend fun analyzeMeal(
        @Part image: MultipartBody.Part,
        @Part("mealType") mealType: RequestBody?,
        @Part("timestamp") timestamp: RequestBody?
    ): AnalyzeMealResponse

    @POST("meals/analyze-text")
    suspend fun analyzeTextDescription(
        @Body request: AnalyzeTextRequest
    ): AnalyzeMealResponse

    @POST("meals/barcode")
    suspend fun scanBarcode(
        @Body request: BarcodeRequest
    ): AnalyzeMealResponse

    @POST("chat")
    suspend fun chat(
        @Body request: ChatRequest
    ): ChatResponse

    // Meal Management
    @GET("meals")
    suspend fun getMeals(
        @Query("date") date: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): MealsListResponse


    @DELETE("meals/{mealId}")
    suspend fun deleteMeal(@Path("mealId") mealId: String): DeleteResponse

    @PATCH("meals/{mealId}")
    suspend fun updateMeal(
        @Path("mealId") mealId: String,
        @Body request: UpdateMealRequest
    ): UpdateMealResponse

    // Nutrition Tracking
    @GET("nutrition/daily")
    suspend fun getDailyNutrition(@Query("date") date: String): DailyNutritionResponse

    @GET("nutrition/weekly")
    suspend fun getWeeklyNutrition(@Query("startDate") startDate: String): WeeklyNutritionResponse

    // User Profile & Goals
    @GET("profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ProfileResponse

    @Multipart
    @PUT("profile")
    suspend fun updateProfileWithImage(
        @Part("name") name: RequestBody?,
        @Part image: MultipartBody.Part?
    ): ProfileResponse

    @PUT("nutrition/goals")
    suspend fun updateGoals(@Body request: UpdateGoalsRequest): GoalsResponse

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest)

    // Email Verification
    @POST("auth/send-verification")
    suspend fun sendVerificationEmail(@Body request: SendVerificationRequest): BaseResponse

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): AuthResponseDto

    // Account Deletion
    @POST("auth/delete-account")
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
        @Body request: DeleteAccountRequest
    ): BaseResponse

    // Push notifications
    @POST("notifications/register-device")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): BaseResponse

    @DELETE("notifications/device")
    suspend fun unregisterDevice(@Body request: UnregisterDeviceRequest): BaseResponse

    @PATCH("notifications/preferences")
    suspend fun updateNotificationPreferences(@Body request: NotificationPreferencesRequest): BaseResponse
}

data class MealsListResponse(
    val meals: List<MealSummaryDto>,
    val pagination: PaginationDto
)

data class PaginationDto(
    val total: Int,
    val page: Int
)


data class DeleteResponse(
    val success: Boolean
)

data class UpdateMealResponse(
    val success: Boolean,
    val meal: MealSummaryDto
)

data class ProfileResponse(
    val user: UserProfileDto,
    val goals: NutritionGoalsDto?
)

data class GoalsResponse(
    val goals: NutritionGoalsDto
)

