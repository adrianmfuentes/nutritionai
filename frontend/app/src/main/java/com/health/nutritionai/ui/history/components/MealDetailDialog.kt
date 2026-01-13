package com.health.nutritionai.ui.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.health.nutritionai.data.model.Food
import com.health.nutritionai.data.model.Meal
import com.health.nutritionai.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MealDetailDialog(
    meal: Meal,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header with image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    if (!meal.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = meal.imageUrl,
                            contentDescription = "Imagen de la comida",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getMealEmoji(meal.mealType),
                                style = MaterialTheme.typography.displayLarge
                            )
                        }
                    }

                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Meal type badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Primary
                    ) {
                        Text(
                            text = "${getMealEmoji(meal.mealType)} ${meal.mealType?.replaceFirstChar { it.uppercase() } ?: "Comida"}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimary
                        )
                    }
                }

                // Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date and time
                    item {
                        Text(
                            text = formatDetailedTimestamp(meal.timestamp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Total nutrition summary
                    item {
                        NutritionSummaryCard(meal = meal)
                    }

                    // Health score - always show
                    item {
                        HealthScoreCard(score = meal.healthScore)
                    }

                    // Notes if available
                    if (!meal.notes.isNullOrEmpty()) {
                        item {
                            NotesCard(notes = meal.notes)
                        }
                    }

                    // Detected foods header
                    if (meal.detectedFoods.isNotEmpty()) {
                        item {
                            Text(
                                text = "Alimentos detectados (${meal.detectedFoods.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Food items
                        items(meal.detectedFoods) { food ->
                            FoodDetailCard(food = food)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionSummaryCard(meal: Meal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Información Nutricional",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Calories - larger and centered
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🔥",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${meal.totalNutrition.calories}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = CaloriesColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "kcal",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Macros row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroItem(
                    emoji = "🥩",
                    label = "Proteína",
                    value = "${String.format(Locale.US, "%.1f", meal.totalNutrition.protein)}g",
                    color = ProteinColor
                )
                MacroItem(
                    emoji = "🍞",
                    label = "Carbos",
                    value = "${String.format(Locale.US, "%.1f", meal.totalNutrition.carbs)}g",
                    color = CarbsColor
                )
                MacroItem(
                    emoji = "🥑",
                    label = "Grasas",
                    value = "${String.format(Locale.US, "%.1f", meal.totalNutrition.fat)}g",
                    color = FatColor
                )
            }

            // Fiber if available
            meal.totalNutrition.fiber?.let { fiber ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🌾 Fibra: ${String.format(Locale.US, "%.1f", fiber)}g",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroItem(
    emoji: String,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HealthScoreCard(score: Double?) {
    val scoreColor = when {
        score == null -> MaterialTheme.colorScheme.onSurfaceVariant
        score >= 80 -> Success
        score >= 60 -> Warning
        else -> Error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = scoreColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💪",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Puntuación de Salud",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (score != null) getHealthScoreDescription(score) else "Puntuación no calculada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (score != null) "${score.toInt()}" else "—",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Secondary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📝",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FoodDetailCard(food: Food) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food image or category icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (!food.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = food.imageUrl,
                        contentDescription = food.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = getCategoryEmoji(food.category),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Food info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${food.portion.amount.toInt()} ${food.portion.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Compact nutrition info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔥 ${food.nutrition.calories}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CaloriesColor
                    )
                    Text(
                        text = "P: ${String.format(Locale.US, "%.1f", food.nutrition.protein)}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProteinColor
                    )
                    Text(
                        text = "C: ${String.format(Locale.US, "%.1f", food.nutrition.carbs)}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = CarbsColor
                    )
                    Text(
                        text = "G: ${String.format(Locale.US, "%.1f", food.nutrition.fat)}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = FatColor
                    )
                }
            }

            // Confidence badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = getConfidenceColor(food.confidence).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${(food.confidence * 100).toInt()}%",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = getConfidenceColor(food.confidence)
                )
            }
        }
    }
}

private fun getConfidenceColor(confidence: Double): androidx.compose.ui.graphics.Color {
    return when {
        confidence >= 0.8 -> Success
        confidence >= 0.6 -> Warning
        else -> Error
    }
}

private fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "fruta", "frutas", "fruit", "fruits" -> "🍎"
        "verdura", "verduras", "vegetable", "vegetables" -> "🥬"
        "carne", "carnes", "meat", "meats" -> "🥩"
        "pescado", "pescados", "fish", "seafood" -> "🐟"
        "lácteo", "lácteos", "dairy" -> "🥛"
        "cereal", "cereales", "grains" -> "🌾"
        "bebida", "bebidas", "drink", "drinks" -> "🥤"
        "postre", "postres", "dessert", "desserts" -> "🍰"
        "snack", "snacks" -> "🍿"
        "pan", "bread" -> "🍞"
        "huevo", "huevos", "egg", "eggs" -> "🥚"
        else -> "🍽️"
    }
}

private fun getMealEmoji(mealType: String?): String {
    return when (mealType?.lowercase()) {
        "breakfast", "desayuno" -> "🥐"
        "lunch", "almuerzo", "comida" -> "🍱"
        "dinner", "cena" -> "🍽️"
        "snack", "merienda", "snacks" -> "🍎"
        else -> "🍴"
    }
}

private fun getHealthScoreDescription(score: Double): String {
    return when {
        score >= 90 -> "¡Excelente elección!"
        score >= 80 -> "Muy buena opción"
        score >= 70 -> "Buena comida"
        score >= 60 -> "Opción moderada"
        score >= 50 -> "Podría mejorar"
        else -> "Considera opciones más saludables"
    }
}

private fun formatDetailedTimestamp(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy 'a las' HH:mm", Locale.forLanguageTag("es-ES"))
        val date = inputFormat.parse(timestamp)
        date?.let { outputFormat.format(it).replaceFirstChar { c -> c.uppercase() } } ?: timestamp
    } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
        timestamp
    }
}

