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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.health.nutritionai.R
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
                        .height(140.dp)
                ) {
                    if (!meal.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = meal.imageUrl,
                            contentDescription = stringResource(R.string.food_image),
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
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Meal type badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Primary
                    ) {
                        Text(
                            text = "${getMealEmoji(meal.mealType)} ${meal.mealType?.replaceFirstChar { it.uppercase() } ?: stringResource(R.string.meal)}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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

                    // Consejo personalizado (advice)
                    if (!meal.advice.isNullOrBlank()) {
                        item {
                            AdviceCard(advice = meal.advice)
                        }
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
                                text = stringResource(R.string.detected_foods, meal.detectedFoods.size),
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
                text = stringResource(R.string.nutritional_information),
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
                    text = stringResource(R.string.kcal),
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
                    label = stringResource(R.string.protein),
                    value = "${String.format(Locale.US, "%.1f", meal.totalNutrition.protein)}g",
                    color = ProteinColor
                )
                MacroItem(
                    emoji = "🍞",
                    label = stringResource(R.string.carbs),
                    value = "${String.format(Locale.US, "%.1f", meal.totalNutrition.carbs)}g",
                    color = CarbsColor
                )
                MacroItem(
                    emoji = "🥑",
                    label = stringResource(R.string.fat),
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
                        text = "🌾 ${stringResource(R.string.fiber)}: ${String.format(Locale.US, "%.1f", fiber)}g",
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
        score >= 8.0 -> Success
        score >= 6.0 -> Warning
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
                    text = stringResource(R.string.health_score),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (score != null) getHealthScoreDescription(score) else stringResource(R.string.score_not_calculated),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (score != null) {
                    if (score % 1 == 0.0) "${score.toInt()}/10" else "${score}/10"
                } else "—",
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
                    text = stringResource(R.string.notes),
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
                        text = "${stringResource(R.string.protein_abbrev)}: ${String.format(Locale.US, "%.1f", food.nutrition.protein)}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProteinColor
                    )
                    Text(
                        text = "${stringResource(R.string.carbs_abbrev)}: ${String.format(Locale.US, "%.1f", food.nutrition.carbs)}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = CarbsColor
                    )
                    Text(
                        text = "${stringResource(R.string.fat_abbrev)}: ${String.format(Locale.US, "%.1f", food.nutrition.fat)}g",
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

@Composable
fun AdviceCard(advice: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.personalized_advice),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = advice,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
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
        "fruta", "frutas", "fruit", "fruits", "obst" -> "🍎"
        "verdura", "verduras", "vegetable", "vegetables", "légume", "gemüse" -> "🥬"
        "carne", "carnes", "meat", "meats", "viande", "fleisch" -> "🥩"
        "pescado", "pescados", "fish", "seafood", "poisson", "fisch" -> "🐟"
        "lácteo", "lácteos", "dairy", "produit laitier", "milchprodukt" -> "🥛"
        "cereal", "cereales", "grains", "céréale", "getreide" -> "🌾"
        "bebida", "bebidas", "drink", "drinks", "boisson", "getränk" -> "🥤"
        "postre", "postres", "dessert", "desserts", "nachspeise" -> "🍰"
        "snack", "snacks", "goûter" -> "🍿"
        "pan", "bread", "pain", "brot" -> "🍞"
        "huevo", "huevos", "egg", "eggs", "œuf", "ei" -> "🥚"
        else -> "🍽️"
    }
}

private fun getMealEmoji(mealType: String?): String {
    return when (mealType?.lowercase()) {
        "breakfast", "desayuno", "petit-déjeuner", "frühstück" -> "🥐"
        "lunch", "almuerzo", "comida", "déjeuner", "mittagessen" -> "🍱"
        "dinner", "cena", "dîner", "abendessen" -> "🍽️"
        "snack", "merienda", "snacks", "goûter" -> "🍎"
        else -> "🍴"
    }
}

@Composable
private fun getHealthScoreDescription(score: Double): String {
    return when {
        score >= 9.0 -> stringResource(R.string.excellent_choice)
        score >= 8.0 -> stringResource(R.string.very_good_option)
        score >= 7.0 -> stringResource(R.string.good_meal)
        score >= 6.0 -> stringResource(R.string.moderate_option)
        score >= 5.0 -> stringResource(R.string.could_improve)
        else -> stringResource(R.string.consider_healthier_options)
    }
}

private fun formatDetailedTimestamp(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val locale = Locale.getDefault()
        val pattern = when (locale.language) {
            "es" -> "EEEE, d 'de' MMMM 'de' yyyy 'a las' HH:mm"
            else -> "EEEE, MMMM d, yyyy 'at' HH:mm"
        }
        val outputFormat = SimpleDateFormat(pattern, locale)
        val date = inputFormat.parse(timestamp)
        date?.let { outputFormat.format(it).replaceFirstChar { c -> c.uppercase() } } ?: timestamp
    } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
        timestamp
    }
}
