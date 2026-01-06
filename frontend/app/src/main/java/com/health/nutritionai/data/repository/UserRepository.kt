package com.health.nutritionai.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.health.nutritionai.data.model.AuthResponse
import com.health.nutritionai.data.model.NutritionGoals
import com.health.nutritionai.data.model.UserProfile
import com.health.nutritionai.data.remote.api.NutritionApiService
import com.health.nutritionai.data.remote.dto.LoginRequest
import com.health.nutritionai.data.remote.dto.RegisterRequest
import com.health.nutritionai.util.Constants
import com.health.nutritionai.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREFERENCES_NAME)

class UserRepository(
    private val context: Context,
    private val apiService: NutritionApiService
) {

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
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    fun getAuthToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_AUTH_TOKEN)]
        }
    }

    fun isLoggedIn(): Flow<Boolean> {
        return getAuthToken().map { it != null }
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_AUTH_TOKEN)] = token
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_USER_ID)] = userId
        }
    }
}

