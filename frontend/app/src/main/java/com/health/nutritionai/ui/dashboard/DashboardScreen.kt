package com.health.nutritionai.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.health.nutritionai.ui.dashboard.components.CaloriesCard
import com.health.nutritionai.ui.dashboard.components.MacroCard
import com.health.nutritionai.ui.dashboard.components.MealCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onNavigateToCamera: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nutrición AI") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCamera,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar comida")
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
                    CircularProgressIndicator()
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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (uiState as DashboardUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date Selector
                    item {
                        Card {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.selectPreviousDay() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Día anterior")
                                }
                                Text(
                                    text = selectedDate,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(onClick = { viewModel.selectNextDay() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Día siguiente")
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

                    // Macros Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MacroCard(
                                title = "Proteína",
                                current = nutritionSummary.totals.protein,
                                goal = nutritionSummary.goals.protein,
                                unit = "g",
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f)
                            )
                            MacroCard(
                                title = "Carbos",
                                current = nutritionSummary.totals.carbs,
                                goal = nutritionSummary.goals.carbs,
                                unit = "g",
                                color = Color(0xFF2196F3),
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
                            color = Color(0xFFFF9800),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Meals Section
                    item {
                        Text(
                            text = "Comidas de hoy",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    if (nutritionSummary.meals.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No hay comidas registradas aún.\nToca el botón + para agregar una.",
                                    modifier = Modifier.padding(24.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(nutritionSummary.meals) { meal ->
                            MealCard(
                                meal = meal,
                                onClick = { /* Navigate to meal detail */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

