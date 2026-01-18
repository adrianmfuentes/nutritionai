package com.health.nutritionai.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.nutritionai.data.model.NutritionSummary
import com.health.nutritionai.data.repository.MealRepository
import com.health.nutritionai.data.repository.NutritionRepository
import com.health.nutritionai.data.repository.UserRepository
import com.health.nutritionai.util.Constants
import com.health.nutritionai.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(val nutritionSummary: NutritionSummary) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    private val nutritionRepository: NutritionRepository,
    private val mealRepository: MealRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    init {
        loadDailyNutrition()
    }

    fun loadDailyNutrition(date: String = getCurrentDate()) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            _selectedDate.value = date

            // Sync meals with server first
            try {
                val userId = userRepository.getUserId()
                mealRepository.refreshMeals(userId)
            } catch (e: Exception) {
                // Continue even if sync fails
            }

            when (val result = nutritionRepository.getDailyNutrition(date)) {
                is NetworkResult.Success -> {
                    result.data?.let { nutritionSummary ->
                        _uiState.value = DashboardUiState.Success(nutritionSummary)
                    } ?: run {
                        _uiState.value = DashboardUiState.Error("No se pudieron cargar los datos")
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = DashboardUiState.Error(
                        result.message ?: "Error al cargar la nutrición diaria"
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled
                }
            }
        }
    }

    fun refresh() {
        loadDailyNutrition(_selectedDate.value)
    }

    fun selectPreviousDay() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
        calendar.time = dateFormat.parse(_selectedDate.value) ?: Date()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        loadDailyNutrition(dateFormat.format(calendar.time))
    }

    fun selectNextDay() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
        calendar.time = dateFormat.parse(_selectedDate.value) ?: Date()
        calendar.add(Calendar.DAY_OF_MONTH, 1)

        // Don't allow future dates
        if (calendar.time <= Date()) {
            loadDailyNutrition(dateFormat.format(calendar.time))
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(Date())
    }
}
