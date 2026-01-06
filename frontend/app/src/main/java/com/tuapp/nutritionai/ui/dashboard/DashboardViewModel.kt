package com.tuapp.nutritionai.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.nutritionai.data.model.Meal
import com.tuapp.nutritionai.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    mealRepository: MealRepository
) : ViewModel() {

    // Simple state: just list of recent meals. 
    // In a real app we'd aggregate daily totals here.
    val recentMeals: StateFlow<List<Meal>> = mealRepository.getAllMeals()
        .map { meals -> meals.take(5) } // Show last 5
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
