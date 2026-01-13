package com.health.nutritionai.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.health.nutritionai.data.model.Meal
import com.health.nutritionai.ui.history.components.DetailedMealCard
import com.health.nutritionai.ui.history.components.MealDetailDialog
import com.health.nutritionai.util.UserFeedback
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedMeal by remember { mutableStateOf<Meal?>(null) }

    // Show meal detail dialog
    selectedMeal?.let { meal ->
        MealDetailDialog(
            meal = meal,
            onDismiss = { selectedMeal = null }
        )
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (uiState) {
            is HistoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is HistoryUiState.Error -> {
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
                            text = (uiState as HistoryUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            is HistoryUiState.Success -> {
                val meals = (uiState as HistoryUiState.Success).meals

                if (meals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay comidas registradas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(meals, key = { it.mealId }) { meal ->
                            DetailedMealCard(
                                meal = meal,
                                onClick = { selectedMeal = meal },
                                onDelete = { viewModel.deleteMeal(meal.mealId) },
                                onEdit = { updatedMeal -> viewModel.updateMeal(updatedMeal) }
                            )
                        }
                    }
                }
            }
        }
    }
}

