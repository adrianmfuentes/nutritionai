package com.health.nutritionai.ui.camera

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = koinViewModel(),
    onMealAnalyzed: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Capturar Comida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                modifier = Modifier.height(48.dp)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (cameraPermission.status.isGranted) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
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
                            } catch (_: Exception) {
                                // Handle error
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Camera Controls
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (uiState) {
                        is CameraUiState.Analyzing -> {
                            Card(
                                modifier = Modifier.padding(bottom = 32.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Analizando comida...",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                        is CameraUiState.Success -> {
                            LaunchedEffect(Unit) {
                                val successState = uiState as CameraUiState.Success
                                snackbarHostState.showSnackbar(
                                    message = successState.successMessage,
                                    duration = SnackbarDuration.Short
                                )
                                onMealAnalyzed()
                            }
                        }
                        is CameraUiState.Error -> {
                            Card(
                                modifier = Modifier.padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    (uiState as CameraUiState.Error).message,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        else -> Unit
                    }

                    // Capture Button
                    if (uiState is CameraUiState.Idle || uiState is CameraUiState.Error) {
                        FloatingActionButton(
                            onClick = {
                                imageCapture?.let { capture ->
                                    viewModel.capturePhoto(
                                        context = context,
                                        imageCapture = capture,
                                        onImageCaptured = { /* handled in VM */ },
                                        onError = { /* handled in VM */ }
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .padding(bottom = 32.dp),
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Capturar",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Permiso de cámara requerido")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                        Text("Otorgar permiso")
                    }
                }
            }
        }
    }
}

