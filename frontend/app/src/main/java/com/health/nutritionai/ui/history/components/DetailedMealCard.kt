package com.health.nutritionai.ui.history.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.health.nutritionai.data.model.Food
import com.health.nutritionai.data.model.Meal
import java.util.Locale

@Composable
fun DetailedMealCard(
    meal: Meal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Meal Type and Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meal.mealType?.replaceFirstChar { it.uppercase() } ?: "Comida",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${meal.totalNutrition.calories} kcal",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Timestamp
            Text(
                text = formatTimestamp(meal.timestamp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (meal.detectedFoods.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                // Detected Foods List
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(meal.detectedFoods) { food ->
                        FoodItem(food = food)
                    }
                }
            }

            // Meal image if available
            if (!meal.imageUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                AsyncImage(
                    model = meal.imageUrl,
                    contentDescription = "Imagen de la comida",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun FoodItem(
    food: Food,
    modifier: Modifier = Modifier
) {
    val showDialog = remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .width(140.dp)
            .clickable { showDialog.value = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Food image or icon
            if (!food.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = food.imageUrl,
                    contentDescription = food.name,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🍽️",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Food name
            Text(
                text = food.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Quantity in grams (convert to grams if needed)
            val quantityText = when (food.portion.unit.lowercase()) {
                "g", "gr", "grams", "gramos" -> "${food.portion.amount.toInt()}g"
                "kg", "kilos", "kilogramos" -> "${(food.portion.amount * 1000).toInt()}g"
                "ml", "milliliters", "mililitros" -> "${food.portion.amount.toInt()}ml"
                "l", "liters", "litros" -> "${(food.portion.amount * 1000).toInt()}ml"
                else -> "${food.portion.amount.toInt()} ${food.portion.unit}"
            }

            Text(
                text = quantityText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            // Calories
            Text(
                text = "${food.nutrition.calories} kcal",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Food details dialog
    if (showDialog.value) {
        FoodDetailsDialog(
            food = food,
            onDismiss = { showDialog.value = false }
        )
    }
}

@Composable
private fun FoodDetailsDialog(
    food: Food,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Food image or icon
                if (!food.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = food.imageUrl,
                        contentDescription = food.name,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🍽️",
                                style = MaterialTheme.typography.displayMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Food name
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Portion
                val quantityText = when (food.portion.unit.lowercase()) {
                    "g", "gr", "grams", "gramos" -> "${food.portion.amount.toInt()}g"
                    "kg", "kilos", "kilogramos" -> "${(food.portion.amount * 1000).toInt()}g"
                    "ml", "milliliters", "mililitros" -> "${food.portion.amount.toInt()}ml"
                    "l", "liters", "litros" -> "${(food.portion.amount * 1000).toInt()}ml"
                    else -> "${food.portion.amount.toInt()} ${food.portion.unit}"
                }

                Text(
                    text = "Porción: $quantityText",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Macros
                Text(
                    text = "Información Nutricional",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Calories
                MacroRow(
                    label = "Calorías",
                    value = "${food.nutrition.calories}",
                    unit = "kcal",
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Protein
                MacroRow(
                    label = "Proteína",
                    value = String.format(Locale.US, "%.1f", food.nutrition.protein),
                    unit = "g",
                    color = MaterialTheme.colorScheme.tertiary
                )

                // Carbs
                MacroRow(
                    label = "Carbohidratos",
                    value = String.format(Locale.US, "%.1f", food.nutrition.carbs),
                    unit = "g",
                    color = MaterialTheme.colorScheme.secondary
                )

                // Fat
                MacroRow(
                    label = "Grasas",
                    value = String.format(Locale.US, "%.1f", food.nutrition.fat),
                    unit = "g",
                    color = MaterialTheme.colorScheme.error
                )

                // Fiber if available
                food.nutrition.fiber?.let { fiber ->
                    MacroRow(
                        label = "Fibra",
                        value = String.format(Locale.US, "%.1f", fiber),
                        unit = "g",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Close button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
private fun MacroRow(
    label: String,
    value: String,
    unit: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val parts = timestamp.split("T")
        if (parts.size > 1) {
            val datePart = parts[0]
            val timePart = parts[1].substring(0, 5)
            "$datePart a las $timePart"
        } else {
            timestamp
        }
    } catch (_: Exception) {
        timestamp
    }
}

