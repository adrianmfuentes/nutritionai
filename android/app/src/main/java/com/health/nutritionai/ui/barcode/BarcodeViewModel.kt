package com.health.nutritionai.ui.barcode

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.nutritionai.data.model.Meal
import com.health.nutritionai.data.repository.MealRepository
import com.health.nutritionai.util.ErrorMapper
import com.health.nutritionai.util.NetworkResult
import com.health.nutritionai.util.SuccessAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BarcodeUiState {
    data object Scanning : BarcodeUiState()
    data class Confirming(val barcode: String) : BarcodeUiState()
    data object Saving : BarcodeUiState()
    data class Success(val meal: Meal, val successMessage: String) : BarcodeUiState()
    data class Error(val message: String) : BarcodeUiState()
}

class BarcodeViewModel(
    private val mealRepository: MealRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<BarcodeUiState>(BarcodeUiState.Scanning)
    val uiState: StateFlow<BarcodeUiState> = _uiState.asStateFlow()

    /** Called once per successful decode; further scans are ignored until [resetToScanning]. */
    fun onBarcodeDetected(barcode: String) {
        if (_uiState.value !is BarcodeUiState.Scanning) return
        _uiState.value = BarcodeUiState.Confirming(barcode)
    }

    fun confirmAndLog(barcode: String, grams: Double, mealType: String?) {
        viewModelScope.launch {
            _uiState.value = BarcodeUiState.Saving

            when (val result = mealRepository.analyzeBarcode(barcode, grams, mealType)) {
                is NetworkResult.Success -> {
                    result.data?.let { meal ->
                        val successMessage = ErrorMapper.getSuccessMessage(application, SuccessAction.MEAL_ANALYZED)
                        _uiState.value = BarcodeUiState.Success(meal, successMessage)
                    } ?: run {
                        _uiState.value = BarcodeUiState.Error("No se pudo procesar el producto")
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = BarcodeUiState.Error(result.message ?: "Error al registrar el producto")
                }
                is NetworkResult.Queued -> {
                    _uiState.value = BarcodeUiState.Error(
                        result.message ?: application.getString(com.health.nutritionai.R.string.offline_barcode_queued_message)
                    )
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun cancelConfirmation() {
        _uiState.value = BarcodeUiState.Scanning
    }

    fun resetToScanning() {
        _uiState.value = BarcodeUiState.Scanning
    }
}
