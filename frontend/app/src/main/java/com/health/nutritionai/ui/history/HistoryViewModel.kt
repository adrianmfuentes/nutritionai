package com.health.nutritionai.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.nutritionai.data.model.Meal
import com.health.nutritionai.data.repository.MealRepository
import com.health.nutritionai.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HistoryUiState {
    data object Loading : HistoryUiState()
    data class Success(val meals: List<Meal>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class HistoryViewModel(
    private val mealRepository: MealRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadMeals()
    }

    private fun loadMeals() {
        viewModelScope.launch {
            val userId = userRepository.getUserId()

            try {
                // Sync with server
                mealRepository.refreshMeals(userId)
            } catch (e: Exception) {
                // Continue to load local data even if sync fails
            }
            
            mealRepository.getAllMeals(userId).collect { meals ->
                _uiState.value = HistoryUiState.Success(meals)
            }
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            mealRepository.deleteMeal(mealId)
            // The Flow will automatically update
        }
    }

    fun refresh() {
        loadMeals()
    }
}

