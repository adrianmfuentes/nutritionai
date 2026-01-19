package com.health.nutritionai.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.health.nutritionai.ui.dashboard.components.CaloriesCard
import com.health.nutritionai.ui.dashboard.components.MacroCard
import com.health.nutritionai.ui.theme.Background
import com.health.nutritionai.ui.theme.CarbsColor
import com.health.nutritionai.ui.theme.FatColor
import com.health.nutritionai.ui.theme.OnPrimary
import com.health.nutritionai.ui.theme.OnPrimaryContainer
import com.health.nutritionai.ui.theme.OnSecondary
import com.health.nutritionai.ui.theme.OnSecondaryContainer
import com.health.nutritionai.ui.theme.OnTertiaryContainer
import com.health.nutritionai.ui.theme.Primary
import com.health.nutritionai.ui.theme.PrimaryContainer
import com.health.nutritionai.ui.theme.ProteinColor
import com.health.nutritionai.ui.theme.Secondary
import com.health.nutritionai.ui.theme.SecondaryContainer
import com.health.nutritionai.ui.theme.Tertiary
import com.health.nutritionai.ui.theme.TertiaryContainer
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    refreshKey: Long = 0L,
    viewModel: DashboardViewModel = koinViewModel(),
    onNavigateToCamera: () -> Unit,
    onNavigateToTextInput: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var showAddMealDialog by remember { mutableStateOf(false) }

    // Refresh when refreshKey changes (i.e., when returning from adding a meal)
    LaunchedEffect(refreshKey) {
        if (refreshKey > 0L) {
            viewModel.refresh()
        }
    }

    // Observe lifecycle to refresh when screen resumes
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Helper function to dismiss dialog and navigate
    fun dismissAndNavigateToCamera() {
        showAddMealDialog = false
        onNavigateToCamera()
    }

    fun dismissAndNavigateToTextInput() {
        showAddMealDialog = false
        onNavigateToTextInput()
    }

    fun dismissAndNavigateToChat() {
        showAddMealDialog = false
        onNavigateToChat()
    }

    fun dismissDialog() {
        showAddMealDialog = false
    }

    Scaffold(
        containerColor = Background,
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { showAddMealDialog = true },
                shape = CircleShape,
                containerColor = Primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier.shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Primary.copy(alpha = 0.4f)
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar comida",
                    tint = OnPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { padding ->
        when (uiState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is DashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = (uiState as DashboardUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            is DashboardUiState.Success -> {
                val successState = uiState as DashboardUiState.Success
                val nutritionSummary = successState.nutritionSummary

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Date Selector
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    spotColor = Primary.copy(alpha = 0.15f)
                                ),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.selectPreviousDay() },
                                    modifier = Modifier
                                        .background(
                                            color = Primary.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Día anterior",
                                        tint = Primary
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "📅",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = selectedDate,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.selectNextDay() },
                                    modifier = Modifier
                                        .background(
                                            color = Primary.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Día siguiente",
                                        tint = Primary
                                    )
                                }
                            }
                        }
                    }

                    // Calories Card
                    item {
                        CaloriesCard(
                            current = nutritionSummary.totals.calories,
                            goal = nutritionSummary.goals.calories
                        )
                    }

                    // Macros Section Title
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "💪",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Macronutrientes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Macros Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MacroCard(
                                title = "Proteína",
                                current = nutritionSummary.totals.protein,
                                goal = nutritionSummary.goals.protein,
                                unit = "g",
                                color = ProteinColor,
                                modifier = Modifier.weight(1f)
                            )
                            MacroCard(
                                title = "Carbos",
                                current = nutritionSummary.totals.carbs,
                                goal = nutritionSummary.goals.carbs,
                                unit = "g",
                                color = CarbsColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        MacroCard(
                            title = "Grasas",
                            current = nutritionSummary.totals.fat,
                            goal = nutritionSummary.goals.fat,
                            unit = "g",
                            color = FatColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Add bottom spacing for FAB
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }

    // Add Meal Dialog
    if (showAddMealDialog) {
        AlertDialog(
            onDismissRequest = ::dismissDialog,
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "🍽️",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Añadir Comida",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "¿Cómo quieres añadir tu comida?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Camera Option
                    Card(
                        onClick = ::dismissAndNavigateToCamera,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PrimaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = Primary
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "📷",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                            Column {
                                Text(
                                    "Tomar Foto",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OnPrimaryContainer
                                )
                                Text(
                                    "Captura una imagen de tu comida",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Text Input Option
                    Card(
                        onClick = ::dismissAndNavigateToTextInput,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SecondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = Secondary
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Create,
                                        contentDescription = null,
                                        tint = OnSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    "Descripción",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSecondaryContainer
                                )
                                Text(
                                    "Escribe o graba la descripción",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Chat Option
                    Card(
                        onClick = ::dismissAndNavigateToChat,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = TertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = Tertiary
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "💬",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                            Column {
                                Text(
                                    "Chat con IA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OnTertiaryContainer
                                )
                                Text(
                                    "Habla con el asistente nutricional",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnTertiaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = ::dismissDialog) {
                    Text("Cancelar")
                }
            }
        )
    }
}
