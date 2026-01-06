package com.tuapp.nutritionai.ui.camera

import android.Manifest
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onMealAnalyzed: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.hasPermission) {
            cameraPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is CameraUiState.Success) {
            onMealAnalyzed()
            viewModel.resetState() // Optionally reset or handle navigation properly
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermission.hasPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        
                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build()
                        
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            // Handle error
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Camera Controls & Overlays
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState !is CameraUiState.Analyzing) {
                    Button(
                        onClick = {
                            val capture = imageCapture ?: return@Button
                            viewModel.capturePhoto(
                                context = context,
                                imageCapture = capture,
                                onImageCaptured = { 
                                    // Can implement a thumbnail preview here
                                },
                                onError = { 
                                    // Can show toast
                                }
                            )
                        },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = uiState !is CameraUiState.Capturing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera, 
                            contentDescription = "Capturar",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        } else {
            // Permission denied UI
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Se requiere permiso de cámara para usar esta función")
                Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                    Text("Dar Permiso")
                }
            }
        }
        
        // Progress Overlay
        if (uiState is CameraUiState.Analyzing || uiState is CameraUiState.Capturing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState is CameraUiState.Capturing) "Capturando..." else "Analizando con IA...",
                        color = Color.White
                    )
                }
            }
        }
        
        // Error Overlay
        if (uiState is CameraUiState.Error) {
             val errorMsg = (uiState as CameraUiState.Error).message
             AlertDialog(
                 onDismissRequest = { viewModel.resetState() },
                 confirmButton = {
                     TextButton(onClick = { viewModel.resetState() }) {
                         Text("OK")
                     }
                 },
                 title = { Text("Error") },
                 text = { Text(errorMsg) }
             )
        }
    }
}
