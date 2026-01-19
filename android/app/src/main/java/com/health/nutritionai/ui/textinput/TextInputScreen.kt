package com.health.nutritionai.ui.textinput

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.health.nutritionai.util.UserFeedback
import org.koin.androidx.compose.koinViewModel
import com.health.nutritionai.R

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun TextInputScreen(
    viewModel: TextInputViewModel = koinViewModel(),
    onMealAnalyzed: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var foodDescription by remember { mutableStateOf("") }
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    // Speech recognition launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { text ->
            foodDescription += if (foodDescription.isEmpty()) text else " $text"
            viewModel.resetToIdle()
        }
    }

    // Show feedback messages
    LaunchedEffect(feedback) {
        when (feedback) {
            is UserFeedback.Success -> {
                snackbarHostState.showSnackbar(
                    message = (feedback as UserFeedback.Success).message,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearFeedback()
                onMealAnalyzed()
            }
            is UserFeedback.Error -> {
                snackbarHostState.showSnackbar(
                    message = (feedback as UserFeedback.Error).message,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearFeedback()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.add_by_description),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.describe_your_meal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.description_instructions),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = foodDescription,
                    onValueChange = { foodDescription = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    label = { Text(stringResource(R.string.food_description_label)) },
                    placeholder = { Text(stringResource(R.string.food_description_placeholder)) },
                    supportingText = { Text("${foodDescription.length}/500 ${stringResource(R.string.characters_limit)}") },
                    maxLines = 8,
                    enabled = uiState !is TextInputUiState.Analyzing
                )

                Spacer(modifier = Modifier.height(16.dp))

                val context = LocalContext.current // 1. Capturado fuera (Correcto)

                FloatingActionButton(
                    onClick = {
                        if (audioPermission.status.isGranted) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.describe_food_prompt))
                            }
                            viewModel.startVoiceRecognition()
                            speechRecognizerLauncher.launch(intent)
                        } else {
                            audioPermission.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "🎤",
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                Text(
                    stringResource(R.string.tap_to_record),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                when (uiState) {
                    is TextInputUiState.Analyzing -> {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    stringResource(R.string.analyzing_description),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                    is TextInputUiState.Recording -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    stringResource(R.string.listening),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    else -> {
                        Button(
                            onClick = {
                                if (foodDescription.isNotBlank()) {
                                    viewModel.analyzeTextDescription(foodDescription)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = foodDescription.isNotBlank()
                        ) {
                            Text(stringResource(R.string.analyze_and_save))
                        }
                    }
                }
            }
        }
    }
}
