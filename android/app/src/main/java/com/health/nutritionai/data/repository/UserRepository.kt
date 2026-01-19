package com.health.nutritionai.data.repository

import android.content.Context
import com.health.nutritionai.data.model.AuthResponse
import com.health.nutritionai.data.model.NutritionGoals
import com.health.nutritionai.data.model.UserProfile
import com.health.nutritionai.data.remote.api.NutritionApiService
import com.health.nutritionai.data.remote.dto.LoginRequest
import com.health.nutritionai.data.remote.dto.RegisterRequest
import com.health.nutritionai.util.Constants
import com.health.nutritionai.util.NetworkResult
import androidx.core.content.edit
import com.health.nutritionai.util.ErrorContext
import com.health.nutritionai.util.ErrorMapper
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UserRepository(
    context: Context,
    private val apiService: NutritionApiService
) {
    private val prefs = context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)

    suspend fun register(email: String, password: String, name: String): NetworkResult<AuthResponse> {
        return try {
            val request = RegisterRequest(email, password, name)
            val responseDto = apiService.register(request)
            
            val userProfile = responseDto.user?.let { dto ->
                UserProfile(
                    userId = dto.id,
                    email = dto.email,
                    name = dto.name,
                    photoUrl = dto.photoUrl,
                    goals = dto.goals?.let {
                        NutritionGoals(it.calories, it.protein, it.carbs, it.fat)
                    }
                )
            }

            val authResponse = AuthResponse(
                token = responseDto.token,
                user = userProfile,
                userId = userProfile?.userId
            )

            saveAuthToken(authResponse.token)
            authResponse.userId?.let { saveUserId(it) }

            NetworkResult.Success(authResponse)
        } catch (e: Exception) {
            val userFriendlyMessage = ErrorMapper.mapErrorToMessage(e, ErrorContext.AUTH_REGISTER)
            NetworkResult.Error(userFriendlyMessage)
        }
    }

    suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        return try {
            val request = LoginRequest(email, password)
            val responseDto = apiService.login(request)
            
            val userProfile = responseDto.user?.let { dto ->
                UserProfile(
                    userId = dto.id,
                    email = dto.email,
                    name = dto.name,
                    photoUrl = dto.photoUrl,
                    goals = dto.goals?.let {
                        NutritionGoals(it.calories, it.protein, it.carbs, it.fat)
                    }
                )
            }

            val authResponse = AuthResponse(
                token = responseDto.token,
                user = userProfile,
                userId = userProfile?.userId
            )

            saveAuthToken(authResponse.token)
            authResponse.userId?.let { saveUserId(it) }

            NetworkResult.Success(authResponse)
        } catch (e: Exception) {
            val userFriendlyMessage = ErrorMapper.mapErrorToMessage(e, ErrorContext.AUTH_LOGIN)
            NetworkResult.Error(userFriendlyMessage)
        }
    }

    suspend fun getProfile(): NetworkResult<UserProfile> {
        return try {
            val response = apiService.getProfile()
            val userDto = response.user
            val goalsDto = response.goals

            val userProfile = UserProfile(
                userId = userDto.id,
                email = userDto.email,
                name = userDto.name,
                photoUrl = userDto.photoUrl,
                goals = goalsDto?.let {
                    NutritionGoals(it.calories, it.protein, it.carbs, it.fat)
                } ?: NutritionGoals(2000, 150.0, 200.0, 65.0) // Default goals if null
            )
            
            NetworkResult.Success(userProfile)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error al obtener el perfil")
        }
    }

    suspend fun updateGoals(goals: NutritionGoals): NetworkResult<NutritionGoals> {
        return try {
            val request = com.health.nutritionai.data.remote.dto.UpdateGoalsRequest(
                dailyCalories = goals.calories,
                proteinGrams = goals.protein,
                carbsGrams = goals.carbs,
                fatGrams = goals.fat
            )
            
            val response = apiService.updateGoals(request)
            
            // Map response back to NutritionGoals
            val updatedGoals = NutritionGoals(
                calories = response.goals.calories,
                protein = response.goals.protein,
                carbs = response.goals.carbs,
                fat = response.goals.fat
            )
            
            NetworkResult.Success(updatedGoals)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error al actualizar los objetivos")
        }
    }

    suspend fun updateProfile(name: String? = null, photoUrl: String? = null): NetworkResult<UserProfile> {
        return try {
            val request = com.health.nutritionai.data.remote.dto.UpdateProfileRequest(
                name = name,
                photoUrl = photoUrl
            )
            val response = apiService.updateProfile(request)

            val userDto = response.user
            val goalsDto = response.goals

            val userProfile = UserProfile(
                userId = userDto.id,
                email = userDto.email,
                name = userDto.name,
                photoUrl = userDto.photoUrl,
                goals = goalsDto?.let {
                    NutritionGoals(it.calories, it.protein, it.carbs, it.fat)
                }
            )

            NetworkResult.Success(userProfile)
        } catch (e: Exception) {
            val userFriendlyMessage = ErrorMapper.mapErrorToMessage(e, ErrorContext.USER_PROFILE)
            NetworkResult.Error(userFriendlyMessage)
        }
    }

    suspend fun updateProfileWithImage(name: String? = null, imageFile: File?): NetworkResult<UserProfile> {
        return try {
            val namePart = name?.toRequestBody("text/plain".toMediaTypeOrNull())
            val imagePart = imageFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", it.name, requestFile)
            }
            val response = apiService.updateProfileWithImage(namePart, imagePart)

            val userDto = response.user
            val goalsDto = response.goals

            val userProfile = UserProfile(
                userId = userDto.id,
                email = userDto.email,
                name = userDto.name,
                photoUrl = userDto.photoUrl,
                goals = goalsDto?.let {
                    NutritionGoals(it.calories, it.protein, it.carbs, it.fat)
                }
            )

            NetworkResult.Success(userProfile)
        } catch (e: Exception) {
            val userFriendlyMessage = ErrorMapper.mapErrorToMessage(e, ErrorContext.USER_PROFILE)
            NetworkResult.Error(userFriendlyMessage)
        }
    }

    fun logout() {
        prefs.edit { clear() }
    }

    fun saveAuthToken(token: String) {
        prefs.edit { putString(Constants.KEY_AUTH_TOKEN, token) }
    }

    fun saveUserId(userId: String) {
        prefs.edit { putString(Constants.KEY_USER_ID, userId) }
    }

    fun getUserId(): String {
        return prefs.getString(Constants.KEY_USER_ID, null) ?: ""
    }

    fun getAuthToken(): String? {
        return prefs.getString(Constants.KEY_AUTH_TOKEN, null)
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    // Preferences management
    fun getNotificationsEnabled(): Boolean {
        return prefs.getBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun saveNotificationsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, enabled) }
    }

    fun getLanguage(): String {
        return prefs.getString(Constants.KEY_LANGUAGE, "Español") ?: "Español"
    }

    fun saveLanguage(language: String) {
        prefs.edit { putString(Constants.KEY_LANGUAGE, language) }
    }

    fun getUnits(): String {
        return prefs.getString(Constants.KEY_UNITS, "Métrico (g, kg)") ?: "Métrico (g, kg)"
    }

    fun saveUnits(units: String) {
        prefs.edit { putString(Constants.KEY_UNITS, units) }
    }

    // Change password
    suspend fun changePassword(currentPassword: String, newPassword: String): NetworkResult<Unit> {
        return try {
            val request = com.health.nutritionai.data.remote.dto.ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
            apiService.changePassword(request)
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            val userFriendlyMessage = ErrorMapper.mapErrorToMessage(e, ErrorContext.PASSWORD_CHANGE)
            NetworkResult.Error(userFriendlyMessage)
        }
    }
}
