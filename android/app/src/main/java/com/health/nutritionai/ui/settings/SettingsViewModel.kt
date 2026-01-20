package com.health.nutritionai.ui.settings

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.nutritionai.data.model.NutritionGoals
import com.health.nutritionai.data.model.UserProfile
import com.health.nutritionai.data.repository.UserRepository
import com.health.nutritionai.util.ErrorMapper
import com.health.nutritionai.util.NetworkResult
import com.health.nutritionai.util.SuccessAction
import com.health.nutritionai.util.UserFeedback
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

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

    private val _feedback = MutableSharedFlow<UserFeedback>()
    val feedback: SharedFlow<UserFeedback> = _feedback.asSharedFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _showGoalsDialog = MutableStateFlow(false)
    val showGoalsDialog: StateFlow<Boolean> = _showGoalsDialog.asStateFlow()

    private val _showNotificationsDialog = MutableStateFlow(false)
    val showNotificationsDialog: StateFlow<Boolean> = _showNotificationsDialog.asStateFlow()

    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()

    private val _showUnitsDialog = MutableStateFlow(false)
    val showUnitsDialog: StateFlow<Boolean> = _showUnitsDialog.asStateFlow()

    private val _showChangePasswordDialog = MutableStateFlow(false)
    val showChangePasswordDialog: StateFlow<Boolean> = _showChangePasswordDialog.asStateFlow()

    private val _showEditProfileDialog = MutableStateFlow(false)
    val showEditProfileDialog: StateFlow<Boolean> = _showEditProfileDialog.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Español")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _selectedUnits = MutableStateFlow("metric")
    val selectedUnits: StateFlow<String> = _selectedUnits.asStateFlow()

    private val _languageChanged = MutableSharedFlow<String>()
    val languageChanged: SharedFlow<String> = _languageChanged.asSharedFlow()

    init {
        loadUserProfile()
        loadPreferences()
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

    private fun loadPreferences() {
        viewModelScope.launch {
            // Load preferences from SharedPreferences or repository
            _notificationsEnabled.value = userRepository.getNotificationsEnabled()
            _selectedLanguage.value = userRepository.getLanguage()
            _selectedUnits.value = userRepository.getUnits()
        }
    }

    fun showNotificationsDialog() {
        _showNotificationsDialog.value = true
    }

    fun hideNotificationsDialog() {
        _showNotificationsDialog.value = false
    }

    fun showLanguageDialog() {
        _showLanguageDialog.value = true
    }

    fun hideLanguageDialog() {
        _showLanguageDialog.value = false
    }

    fun showUnitsDialog() {
        _showUnitsDialog.value = true
    }

    fun hideUnitsDialog() {
        _showUnitsDialog.value = false
    }

    fun showChangePasswordDialog() {
        _showChangePasswordDialog.value = true
    }

    fun hideChangePasswordDialog() {
        _showChangePasswordDialog.value = false
    }

    fun showEditProfileDialog() {
        _showEditProfileDialog.value = true
    }

    fun hideEditProfileDialog() {
        _showEditProfileDialog.value = false
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _notificationsEnabled.value = enabled
            userRepository.saveNotificationsEnabled(enabled)
        }
    }

    fun updateLanguage(languageName: String) {
        val code = when (languageName) {
            "Español" -> "es"
            "English" -> "en"
            "Français" -> "fr"
            "Deutsch" -> "de"
            else -> "es"
        }

        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(appLocale)

        _selectedLanguage.value = languageName
        _showLanguageDialog.value = false
    }

    fun updateUnits(units: String) {
        viewModelScope.launch {
            _selectedUnits.value = units
            userRepository.saveUnits(units)
            _showUnitsDialog.value = false
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            when (val result = userRepository.changePassword(currentPassword, newPassword)) {
                is NetworkResult.Success -> {
                    _showChangePasswordDialog.value = false
                    _feedback.emit(UserFeedback.Success(
                        ErrorMapper.getSuccessMessage(SuccessAction.PASSWORD_CHANGED)
                    ))
                }
                is NetworkResult.Error -> {
                    _feedback.emit(UserFeedback.Error(result.message ?: "Error al cambiar contraseña"))
                }
                else -> {
                    _feedback.emit(UserFeedback.Error("Error al cambiar contraseña"))
                }
            }
        }
    }

    fun updateUserProfile(name: String, photoUrl: String? = null) {
        viewModelScope.launch {
            when (val result = userRepository.updateProfile(name = name, photoUrl = photoUrl)) {
                is NetworkResult.Success -> {
                    loadUserProfile()
                    _showEditProfileDialog.value = false
                    _feedback.emit(UserFeedback.Success(
                        ErrorMapper.getSuccessMessage(SuccessAction.PROFILE_UPDATED)
                    ))
                }
                is NetworkResult.Error -> {
                    _feedback.emit(UserFeedback.Error(result.message ?: "Error al actualizar perfil"))
                }
                else -> {
                    _feedback.emit(UserFeedback.Error("Error al actualizar perfil"))
                }
            }
        }
    }

    fun updateUserProfileWithImage(name: String, imageFile: File?) {
        viewModelScope.launch {
            when (val result = userRepository.updateProfileWithImage(name = name, imageFile = imageFile)) {
                is NetworkResult.Success -> {
                    loadUserProfile()
                    _showEditProfileDialog.value = false
                    _feedback.emit(UserFeedback.Success(
                        ErrorMapper.getSuccessMessage(SuccessAction.PROFILE_UPDATED)
                    ))
                }
                is NetworkResult.Error -> {
                    _feedback.emit(UserFeedback.Error(result.message ?: "Error al actualizar perfil"))
                }
                else -> {
                    _feedback.emit(UserFeedback.Error("Error al actualizar perfil"))
                }
            }
        }
    }

    fun getAuthToken(): String? {
        return userRepository.getAuthToken()
    }

    fun clearFeedback() {
        viewModelScope.launch {
            _feedback.emit(UserFeedback.None)
        }
    }
}
