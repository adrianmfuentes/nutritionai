package com.health.nutritionai.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.nutritionai.data.repository.UserRepository
import com.health.nutritionai.util.ErrorMapper
import com.health.nutritionai.util.SuccessAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val userId: String, val successMessage: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val cleanEmail = email.trim()
            val cleanPassword = password.trim()

            // Validar entrada
            if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
                _uiState.value = AuthUiState.Error("Por favor, completa todos los campos")
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                _uiState.value = AuthUiState.Error("Correo electrónico inválido")
                return@launch
            }

            // Llamar al backend
            when (val result = userRepository.login(cleanEmail, cleanPassword)) {
                is com.health.nutritionai.util.NetworkResult.Success -> {
                    val userId = result.data?.userId ?: result.data?.user?.userId ?: "unknown"
                    val successMessage = ErrorMapper.getSuccessMessage(SuccessAction.LOGIN)
                    _uiState.value = AuthUiState.Success(userId, successMessage)
                }
                is com.health.nutritionai.util.NetworkResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message ?: "Error al iniciar sesión")
                }
                is com.health.nutritionai.util.NetworkResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            val cleanName = name.trim()
            val cleanEmail = email.trim()
            val cleanPassword = password.trim()

            // Validar entrada
            if (cleanName.isBlank() || cleanEmail.isBlank() || cleanPassword.isBlank()) {
                _uiState.value = AuthUiState.Error("Por favor, completa todos los campos")
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                _uiState.value = AuthUiState.Error("Correo electrónico inválido")
                return@launch
            }

            if (cleanName.length < 2) {
                _uiState.value = AuthUiState.Error("El nombre debe tener al menos 2 caracteres")
                return@launch
            }

            if (cleanPassword.length < 8) {
                _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 8 caracteres")
                return@launch
            }

            // Llamar al backend
            when (val result = userRepository.register(cleanEmail, cleanPassword, cleanName)) {
                is com.health.nutritionai.util.NetworkResult.Success -> {
                    val userId = result.data?.userId ?: result.data?.user?.userId ?: "unknown"
                    val successMessage = ErrorMapper.getSuccessMessage(SuccessAction.REGISTER)
                    _uiState.value = AuthUiState.Success(userId, successMessage)
                }
                is com.health.nutritionai.util.NetworkResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message ?: "Error al registrarse")
                }
                is com.health.nutritionai.util.NetworkResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

}

