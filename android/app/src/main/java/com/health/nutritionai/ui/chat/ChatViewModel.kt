package com.health.nutritionai.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.nutritionai.data.remote.api.NutritionApiService
import com.health.nutritionai.data.remote.dto.ChatMessage
import com.health.nutritionai.data.remote.dto.ChatRequest
import com.health.nutritionai.data.repository.MealRepository
import com.health.nutritionai.data.repository.UserRepository
import com.health.nutritionai.util.ErrorMapper
import com.health.nutritionai.util.ErrorContext
import com.health.nutritionai.util.UserFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed class ChatUiState {
    data object Loading : ChatUiState()
    data class Success(val messages: List<ChatMessage>, val isProcessing: Boolean = false) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatViewModel(
    private val apiService: NutritionApiService,
    private val mealRepository: MealRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Success(emptyList()))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _feedback = MutableStateFlow<UserFeedback>(UserFeedback.None)
    val feedback: StateFlow<UserFeedback> = _feedback.asStateFlow()

    private val conversationHistory = mutableListOf<ChatMessage>()

    private val supportedLanguages = setOf("es", "en", "fr", "de")

    private fun getUserLanguage(): String {
        val deviceLanguage = Locale.getDefault().language
        return if (supportedLanguages.contains(deviceLanguage)) deviceLanguage else "es"
    }

    init {
        // Mensaje inicial solo de asesoramiento
        conversationHistory.add(
            ChatMessage(
                role = "assistant",
                content = "¡Hola! Soy tu asistente nutricional. Estoy aquí para responder tus dudas y darte consejos sobre alimentación saludable."
            )
        )
        _uiState.value = ChatUiState.Success(conversationHistory.toList())
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        viewModelScope.launch {
            // Agregar mensaje del usuario
            val userMsg = ChatMessage(role = "user", content = userMessage)
            conversationHistory.add(userMsg)

            // Mostrar los mensajes con indicador de procesamiento
            _uiState.value = ChatUiState.Success(conversationHistory.toList(), isProcessing = true)

            try {
                val request = ChatRequest(
                    message = userMessage,
                    conversationHistory = conversationHistory.takeLast(10),
                    language = getUserLanguage()
                )

                val response = apiService.chat(request)

                // Agregar respuesta del asistente
                val assistantMsg = ChatMessage(role = "assistant", content = response.message)
                conversationHistory.add(assistantMsg)

                // Ya no se registra ninguna comida desde el chat

                _uiState.value = ChatUiState.Success(conversationHistory.toList(), isProcessing = false)
            } catch (e: Exception) {
                val errorMessage = ErrorMapper.mapErrorToMessage(e, ErrorContext.MEAL_ANALYSIS)
                conversationHistory.add(
                    ChatMessage(
                        role = "assistant",
                        content = "Lo siento, tuve un problema procesando tu mensaje. Por favor, intenta de nuevo."
                    )
                )
                _uiState.value = ChatUiState.Success(conversationHistory.toList(), isProcessing = false)
                _feedback.value = UserFeedback.Error(errorMessage)
            }
        }
    }

    // Eliminada la función de registrar comidas desde el chat

    fun clearFeedback() {
        _feedback.value = UserFeedback.None
    }

    fun clearConversation() {
        conversationHistory.clear()
        conversationHistory.add(
            ChatMessage(
                role = "assistant",
                content = "Conversación reiniciada. ¿En qué puedo ayudarte sobre nutrición?"
            )
        )
        _uiState.value = ChatUiState.Success(conversationHistory.toList())
    }
}
