package com.health.nutritionai.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.nutritionai.data.local.entity.FoodEntity
import com.health.nutritionai.data.local.entity.MealEntity
import com.health.nutritionai.data.remote.api.NutritionApiService
import com.health.nutritionai.data.remote.dto.ChatMessage
import com.health.nutritionai.data.remote.dto.ChatRequest
import com.health.nutritionai.data.repository.MealRepository
import com.health.nutritionai.data.repository.UserRepository
import com.health.nutritionai.util.ErrorMapper
import com.health.nutritionai.util.ErrorContext
import com.health.nutritionai.util.SuccessAction
import com.health.nutritionai.util.UserFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class ChatUiState {
    data object Loading : ChatUiState()
    data class Success(val messages: List<ChatMessage>) : ChatUiState()
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

    init {
        // Agregar mensaje inicial del asistente
        conversationHistory.add(
            ChatMessage(
                role = "assistant",
                content = "¡Hola! Soy tu asistente nutricional. Cuéntame qué has comido y te ayudaré a registrarlo. Puedes decirme el nombre de los alimentos y las cantidades (por ejemplo: '200g de pollo con arroz' o 'una manzana mediana')."
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
            _uiState.value = ChatUiState.Success(conversationHistory.toList())

            _uiState.value = ChatUiState.Loading

            try {
                val request = ChatRequest(
                    message = userMessage,
                    conversationHistory = conversationHistory.takeLast(10) // Últimos 10 mensajes para contexto
                )

                val response = apiService.chat(request)

                // Agregar respuesta del asistente
                val assistantMsg = ChatMessage(role = "assistant", content = response.message)
                conversationHistory.add(assistantMsg)

                // Si el LLM identificó una comida para registrar
                if (response.shouldRegisterMeal && response.mealData != null) {
                    registerMealFromChat(response.mealData)
                }

                _uiState.value = ChatUiState.Success(conversationHistory.toList())
            } catch (e: Exception) {
                val errorMessage = ErrorMapper.mapErrorToMessage(e, ErrorContext.MEAL_ANALYSIS)
                conversationHistory.add(
                    ChatMessage(
                        role = "assistant",
                        content = "Lo siento, tuve un problema procesando tu mensaje. Por favor, intenta de nuevo."
                    )
                )
                _uiState.value = ChatUiState.Success(conversationHistory.toList())
                _feedback.value = UserFeedback.Error(errorMessage)
            }
        }
    }

    private suspend fun registerMealFromChat(mealData: com.health.nutritionai.data.remote.dto.MealDataDto) {
        try {
            val userId = userRepository.getUserId()
            val mealId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis().toString()

            // Crear entidad de comida
            val mealEntity = MealEntity(
                mealId = mealId,
                userId = userId,
                mealType = mealData.mealType ?: "snack",
                imageUrl = "", // No hay imagen para chat
                notes = conversationHistory.lastOrNull { it.role == "user" }?.content,
                totalCalories = mealData.totalNutrition.calories,
                totalProtein = mealData.totalNutrition.protein,
                totalCarbs = mealData.totalNutrition.carbs,
                totalFat = mealData.totalNutrition.fat,
                totalFiber = mealData.totalNutrition.fiber ?: 0.0,
                healthScore = null,
                timestamp = timestamp
            )

            // Crear entidades de alimentos
            val foodEntities = mealData.foods.map { food ->
                FoodEntity(
                    mealId = mealId,
                    name = food.name,
                    confidence = 1.0, // Alta confianza porque fue especificado por el usuario
                    portionAmount = food.amount,
                    portionUnit = food.unit,
                    calories = food.nutrition.calories,
                    protein = food.nutrition.protein,
                    carbs = food.nutrition.carbs,
                    fat = food.nutrition.fat,
                    fiber = food.nutrition.fiber ?: 0.0,
                    category = food.category,
                    imageUrl = null
                )
            }

            // Guardar en la base de datos local
            mealRepository.saveMealWithFoods(mealEntity, foodEntities)

            val successMessage = ErrorMapper.getSuccessMessage(SuccessAction.MEAL_ANALYZED)
            _feedback.value = UserFeedback.Success("✅ $successMessage")
        } catch (e: Exception) {
            val errorMessage = ErrorMapper.mapErrorToMessage(e, ErrorContext.MEAL_ANALYSIS)
            _feedback.value = UserFeedback.Error(errorMessage)
        }
    }

    fun clearFeedback() {
        _feedback.value = UserFeedback.None
    }

    fun clearConversation() {
        conversationHistory.clear()
        conversationHistory.add(
            ChatMessage(
                role = "assistant",
                content = "Conversación reiniciada. ¿Qué has comido?"
            )
        )
        _uiState.value = ChatUiState.Success(conversationHistory.toList())
    }
}

