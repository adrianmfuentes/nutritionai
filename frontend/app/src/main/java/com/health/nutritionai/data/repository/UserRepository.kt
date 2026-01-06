package com.health.nutritionai.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.health.nutritionai.data.model.AuthResponse
import com.health.nutritionai.data.model.NutritionGoals
import com.health.nutritionai.data.model.UserProfile
import com.health.nutritionai.util.Constants
import com.health.nutritionai.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREFERENCES_NAME)

class UserRepository(
    private val context: Context
) {

    suspend fun register(email: String, password: String, name: String): NetworkResult<AuthResponse> {
        return try {
            // TODO: Call backend API when available
            // For now, return mock data
            val mockResponse = AuthResponse(
                token = "mock_token_${System.currentTimeMillis()}",
                userId = "mock_user_id",
                user = UserProfile(
                    userId = "mock_user_id",
                    email = email,
                    name = name
                )
            )
            saveAuthToken(mockResponse.token)
            mockResponse.userId?.let { saveUserId(it) }

            NetworkResult.Success(mockResponse)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error en el registro")
        }
    }

    suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        return try {
            // TODO: Call backend API when available
            // For now, return mock data
            val mockResponse = AuthResponse(
                token = "mock_token_${System.currentTimeMillis()}",
                userId = "mock_user_id",
                user = UserProfile(
                    userId = "mock_user_id",
                    email = email,
                    name = "Mock User"
                )
            )
            saveAuthToken(mockResponse.token)
            mockResponse.userId?.let { saveUserId(it) }

            NetworkResult.Success(mockResponse)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error en el login")
        }
    }

    suspend fun getProfile(): NetworkResult<UserProfile> {
        return try {
            // TODO: Call backend API when available
            val mockProfile = UserProfile(
                userId = "mock_user_id",
                email = "mock@example.com",
                name = "Mock User",
                goals = NutritionGoals(2000, 150.0, 200.0, 65.0)
            )
            NetworkResult.Success(mockProfile)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Error al obtener el perfil")
        }
    }

    suspend fun updateGoals(goals: NutritionGoals): NetworkResult<NutritionGoals> {
        return try {
            // TODO: Call backend API when available
            // For now, just return the goals back
            NetworkResult.Success(goals)
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

    private suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_AUTH_TOKEN)] = token
        }
    }

    private suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(Constants.KEY_USER_ID)] = userId
        }
    }
}

