package com.health.nutritionai.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.nutritionai.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val userId: String) : AuthUiState()
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

            // Validar entrada
            if (email.isBlank() || password.isBlank()) {
                _uiState.value = AuthUiState.Error("Por favor, completa todos los campos")
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _uiState.value = AuthUiState.Error("Correo electrónico inválido")
                return@launch
            }

            // Llamar al backend
            when (val result = userRepository.login(email, password)) {
                is com.health.nutritionai.util.NetworkResult.Success -> {
                    val userId = result.data?.userId ?: result.data?.user?.userId ?: "unknown"
                    _uiState.value = AuthUiState.Success(userId)
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

            // Validar entrada
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                _uiState.value = AuthUiState.Error("Por favor, completa todos los campos")
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _uiState.value = AuthUiState.Error("Correo electrónico inválido")
                return@launch
            }

            if (password.length < 6) {
                _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 6 caracteres")
                return@launch
            }

            // Llamar al backend
            val result = userRepository.register(email, password, name)

            when (result) {
                is com.health.nutritionai.util.NetworkResult.Success -> {
                    val userId = result.data?.userId ?: result.data?.user?.userId ?: "unknown"
                    _uiState.value = AuthUiState.Success(userId)
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

    fun skipLogin() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            // Modo demo sin autenticación
            userRepository.saveAuthToken("demo_token")
            userRepository.saveUserId("demo_user")

            _uiState.value = AuthUiState.Success("demo_user")
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

