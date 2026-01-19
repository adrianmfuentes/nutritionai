package com.health.nutritionai.ui.dashboard.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.health.nutritionai.data.model.MealSummary
import com.health.nutritionai.ui.theme.*

@Composable
fun MealCard(
    meal: MealSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mealEmoji = getMealEmoji(meal.mealType)
    val mealGradient = getMealGradient(meal.mealType)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Primary.copy(alpha = 0.15f)
            )
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image section with gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (!meal.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = meal.imageUrl,
                        contentDescription = meal.mealType,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay for better text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )
                } else {
                    // Beautiful gradient placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(colors = mealGradient),
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mealEmoji,
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }

                // Floating calorie badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${meal.totalCalories}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CaloriesColor
                        )
                        Text(
                            text = " kcal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Meal type with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = mealEmoji,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = formatMealType(meal.mealType),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Time
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🕐",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatTimestamp(meal.timestamp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Arrow indicator
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Ver detalles",
                            tint = Primary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getMealEmoji(mealType: String): String {
    return when (mealType.lowercase()) {
        "breakfast", "desayuno" -> "🥐"
        "lunch", "almuerzo", "comida" -> "🍱"
        "dinner", "cena" -> "🍽️"
        "snack", "merienda", "snacks" -> "🍎"
        else -> "🍴"
    }
}

private fun getMealGradient(mealType: String): List<Color> {
    return when (mealType.lowercase()) {
        "breakfast", "desayuno" -> listOf(Color(0xFFFCD34D), Color(0xFFF59E0B))
        "lunch", "almuerzo", "comida" -> listOf(Color(0xFF34D399), Color(0xFF10B981))
        "dinner", "cena" -> listOf(Color(0xFF818CF8), Color(0xFF6366F1))
        "snack", "merienda", "snacks" -> listOf(Color(0xFFF9A8D4), Color(0xFFEC4899))
        else -> listOf(Color(0xFF93C5FD), Color(0xFF3B82F6))
    }
}

private fun formatMealType(mealType: String): String {
    return mealType.replaceFirstChar { it.uppercase() }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val parts = timestamp.split("T")
        if (parts.size > 1) {
            val time = parts[1].substring(0, 5)
            val hour = time.substring(0, 2).toInt()
            val minutes = time.substring(3, 5)
            val amPm = if (hour < 12) "AM" else "PM"
            val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            "$hour12:$minutes $amPm"
        } else {
            timestamp
        }
    } catch (_: Exception) {
        timestamp
    }
}

