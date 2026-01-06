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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepository(
    private val context: Context,
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
            NetworkResult.Error(e.message ?: "Error en el registro")
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
            NetworkResult.Error(e.message ?: "Error en el login")
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

    suspend fun logout() {
        prefs.edit().clear().apply()
    }

    fun getAuthToken(): Flow<String?> {
        // SharedPreferences doesn't emit updates natively as Flow, but for login check just returning current state in flow is okay
        // For a more robust solution we could use callbackFlow, but this suffices for "check persistence on init"
        return flow {
             emit(prefs.getString(Constants.KEY_AUTH_TOKEN, null))
        }
    }

    fun isLoggedIn(): Flow<Boolean> {
        return flow {
            emit(prefs.getString(Constants.KEY_AUTH_TOKEN, null) != null)
        }
    }

    suspend fun saveAuthToken(token: String) {
        prefs.edit().putString(Constants.KEY_AUTH_TOKEN, token).apply()
    }

    suspend fun saveUserId(userId: String) {
        prefs.edit().putString(Constants.KEY_USER_ID, userId).apply()
    }

    suspend fun getUserId(): String {
        return prefs.getString(Constants.KEY_USER_ID, "demo_user") ?: "demo_user"
    }
}

