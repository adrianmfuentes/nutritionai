package com.health.nutritionai.ui.camera

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

sealed class CameraUiState {
    data object Idle : CameraUiState()
    data object Capturing : CameraUiState()
    data object Analyzing : CameraUiState()
    data class Success(val meal: Meal, val successMessage: String) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
}

class CameraViewModel(
    private val mealRepository: MealRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _capturedImageUri = MutableStateFlow<Uri?>(null)

    fun capturePhoto(
        context: Context,
        imageCapture: ImageCapture,
        onImageCaptured: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        _uiState.value = CameraUiState.Capturing

        val photoFile = createImageFile(context)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    _capturedImageUri.value = savedUri
                    onImageCaptured(savedUri)
                    analyzeMeal(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    _uiState.value = CameraUiState.Error(
                        exception.message ?: "Error al capturar la imagen"
                    )
                    onError(exception)
                }
            }
        )
    }

    private fun analyzeMeal(imageFile: File) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Analyzing

            when (val result = mealRepository.analyzeMeal(imageFile)) {
                is NetworkResult.Success -> {
                    result.data?.let { meal ->
                        val successMessage = ErrorMapper.getSuccessMessage(application, SuccessAction.MEAL_ANALYZED)
                        _uiState.value = CameraUiState.Success(meal, successMessage)
                    } ?: run {
                        _uiState.value = CameraUiState.Error("No se pudo procesar la comida")
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = CameraUiState.Error(
                        result.message ?: "Error al analizar la comida"
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = CameraUiState.Idle
        _capturedImageUri.value = null
    }

    private fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val storageDir = context.getExternalFilesDir(null)
        return File.createTempFile(
            "MEAL_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
}
