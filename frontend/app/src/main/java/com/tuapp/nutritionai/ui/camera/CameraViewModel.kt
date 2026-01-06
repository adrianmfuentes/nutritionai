package com.tuapp.nutritionai.ui.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.nutritionai.data.model.Meal
import com.tuapp.nutritionai.data.repository.MealRepository
import com.tuapp.nutritionai.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

sealed class CameraUiState {
    data object Idle : CameraUiState()
    data object Capturing : CameraUiState()
    data object Analyzing : CameraUiState()
    data class Success(val meal: Meal) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
}

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri.asStateFlow()

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
                    // Safety check if data is null, though Success usually implies non-null data
                    // Depending on NetworkResult implementation details
                    if (result.data != null) {
                         _uiState.value = CameraUiState.Success(result.data)
                    } else {
                         _uiState.value = CameraUiState.Error("Analisis completado sin datos")
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
