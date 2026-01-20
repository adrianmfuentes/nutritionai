package com.health.nutritionai.ui.textinput

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.nutritionai.data.repository.MealRepository
import com.health.nutritionai.util.ErrorMapper
import com.health.nutritionai.util.NetworkResult
import com.health.nutritionai.util.SuccessAction
import com.health.nutritionai.util.UserFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TextInputUiState {
    data object Idle : TextInputUiState()
    data object Recording : TextInputUiState()
    data object Analyzing : TextInputUiState()
}

class TextInputViewModel(
    private val mealRepository: MealRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<TextInputUiState>(TextInputUiState.Idle)
    val uiState: StateFlow<TextInputUiState> = _uiState.asStateFlow()

    private val _feedback = MutableStateFlow<UserFeedback>(UserFeedback.None)
    val feedback: StateFlow<UserFeedback> = _feedback.asStateFlow()

    fun analyzeTextDescription(description: String) {
        viewModelScope.launch {
            _uiState.value = TextInputUiState.Analyzing

            when (val result = mealRepository.analyzeTextDescription(description)) {
                is NetworkResult.Success -> {
                    val successMessage = ErrorMapper.getSuccessMessage(application, SuccessAction.MEAL_ANALYZED)
                    _feedback.value = UserFeedback.Success(successMessage)
                    _uiState.value = TextInputUiState.Idle
                }
                is NetworkResult.Error -> {
                    _feedback.value = UserFeedback.Error(
                        result.message ?: "Error al analizar la descripción"
                    )
                    _uiState.value = TextInputUiState.Idle
                }
                is NetworkResult.Loading -> {
                    // Already handled
                }
            }
        }
    }

    fun startVoiceRecognition() {
        _uiState.value = TextInputUiState.Recording
        // This will be handled by the composable with Android's Speech Recognizer
    }


    fun resetToIdle() {
        _uiState.value = TextInputUiState.Idle
    }

    fun clearFeedback() {
        _feedback.value = UserFeedback.None
    }
}
