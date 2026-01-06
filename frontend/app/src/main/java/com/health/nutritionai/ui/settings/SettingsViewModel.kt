package com.health.nutritionai.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.nutritionai.data.model.NutritionGoals
import com.health.nutritionai.data.model.UserProfile
import com.health.nutritionai.data.repository.UserRepository
import com.health.nutritionai.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SettingsUiState {
    data object Loading : SettingsUiState()
    data class Success(val userProfile: UserProfile) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

class SettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _showGoalsDialog = MutableStateFlow(false)
    val showGoalsDialog: StateFlow<Boolean> = _showGoalsDialog.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            when (val result = userRepository.getProfile()) {
                is NetworkResult.Success -> {
                    result.data?.let { profile ->
                        _uiState.value = SettingsUiState.Success(profile)
                    } ?: run {
                        _uiState.value = SettingsUiState.Error("Error al cargar el perfil")
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = SettingsUiState.Error(result.message ?: "Error desconocido")
                }
                else -> {
                    _uiState.value = SettingsUiState.Error("Error al cargar el perfil")
                }
            }
        }
    }

    fun updateGoals(goals: NutritionGoals) {
        viewModelScope.launch {
            when (val result = userRepository.updateGoals(goals)) {
                is NetworkResult.Success -> {
                    loadUserProfile()
                    _showGoalsDialog.value = false
                }
                is NetworkResult.Error -> {
                    _uiState.value = SettingsUiState.Error(result.message ?: "Error al actualizar objetivos")
                }
                else -> {
                    _uiState.value = SettingsUiState.Error("Error al actualizar objetivos")
                }
            }
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun showGoalsDialog() {
        _showGoalsDialog.value = true
    }

    fun hideGoalsDialog() {
        _showGoalsDialog.value = false
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}

