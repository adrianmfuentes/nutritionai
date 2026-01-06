package com.health.nutritionai.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.nutritionai.data.model.Meal
import com.health.nutritionai.data.repository.MealRepository
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
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadMeals()
    }

    private fun loadMeals() {
        viewModelScope.launch {
            try {
                // Sync with server
                mealRepository.refreshMeals("current_user")
            } catch (e: Exception) {
                // Continue to load local data even if sync fails
            }
            
            mealRepository.getAllMeals("current_user").collect { meals ->
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

