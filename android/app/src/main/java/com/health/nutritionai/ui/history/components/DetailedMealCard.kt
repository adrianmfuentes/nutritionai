package com.health.nutritionai.ui.history.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.health.nutritionai.data.model.Food
import com.health.nutritionai.data.model.Meal
import com.health.nutritionai.data.model.Nutrition
import com.health.nutritionai.ui.theme.*
import java.util.Locale

@Composable
fun DetailedMealCard(
    meal: Meal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
    onEdit: ((Meal) -> Unit)? = null
) {
    val mealEmoji = getMealEmoji(meal.mealType)
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Eliminar comida") },
            text = { Text("¿Estás seguro de que deseas eliminar esta comida? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Edit dialog
    if (showEditDialog) {
        EditMealDialog(
            meal = meal,
            onDismiss = { showEditDialog = false },
            onSave = { updatedMeal, shouldDelete ->
                if (shouldDelete) {
                    onDelete?.invoke()
                } else {
                    onEdit?.invoke(updatedMeal)
                }
                showEditDialog = false
            }
        )
    }

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .height(180.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Primary.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Meal Type, Calories and Menu (Compacto)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = mealEmoji,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = meal.mealType?.replaceFirstChar { it.uppercase() } ?: "Comida",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatTimestamp(meal.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Options menu
                if (onDelete != null || onEdit != null) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (onEdit != null) {
                                DropdownMenuItem(
                                    text = { Text("Editar") },
                                    onClick = {
                                        showMenu = false
                                        showEditDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                            if (onDelete != null) {
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirmation = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Calories badge (más compacto)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = CaloriesColor.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${meal.totalNutrition.calories}",
                        style = MaterialTheme.typography.labelMedium,
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

            // Health score badge
            meal.healthScore?.let { score ->
                Spacer(modifier = Modifier.height(4.dp))
                val scoreColor = when {
                    score >= 8.0 -> Success
                    score >= 6.0 -> Warning
                    else -> Error
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = scoreColor.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💪",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (score % 1 == 0.0) "${score.toInt()}/10" else "${score}/10",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                    }
                }
            }

            // Meal image if available (smaller for compact view)
            if (!meal.imageUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = meal.imageUrl,
                        contentDescription = "Imagen de la comida",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Food count
            if (meal.detectedFoods.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${meal.detectedFoods.size} alimento${if (meal.detectedFoods.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditMealDialog(
    meal: Meal,
    onDismiss: () -> Unit,
    onSave: (Meal, Boolean) -> Unit // Boolean: true si se debe eliminar la comida
) {
    val initialMealType = remember(meal.mealType) {
        when (meal.mealType?.lowercase()) {
            "breakfast", "desayuno" -> "breakfast"
            "lunch", "almuerzo", "comida" -> "lunch"
            "dinner", "cena" -> "dinner"
            "snack", "merienda", "snacks" -> "snack"
            else -> "snack"
        }
    }
    var selectedMealType by remember { mutableStateOf(initialMealType) }
    var notes by remember { mutableStateOf(meal.notes ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val mealTypes = listOf("breakfast", "lunch", "dinner", "snack")
    fun mealTypeLabel(type: String): String {
        return when (type) {
            "breakfast" -> "Desayuno"
            "lunch" -> "Comida"
            "dinner" -> "Cena"
            "snack" -> "Snack"
            else -> type.replaceFirstChar { it.uppercase() }
        }
    }
    // Edición de alimentos
    var foods by remember { mutableStateOf(meal.detectedFoods.toMutableList()) }
    var showAddFoodDialog by remember { mutableStateOf(false) }

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
                    .padding(24.dp)
            ) {
                Text(
                    text = "Editar comida",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Meal Type Dropdown
                Text(
                    text = "Tipo de comida",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = mealTypeLabel(selectedMealType),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        mealTypes.forEach { mealType ->
                            DropdownMenuItem(
                                text = { Text(mealTypeLabel(mealType)) },
                                onClick = {
                                    selectedMealType = mealType
                                    expanded = false
                                },
                                leadingIcon = {
                                    Text(getMealEmoji(mealType))
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes field
                Text(
                    text = "Notas",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Añade notas sobre esta comida...") },
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lista editable de alimentos
                Text("Alimentos", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                foods.forEachIndexed { idx, food ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(food.name, fontWeight = FontWeight.Bold)
                                Text("Cantidad: ${food.portion.amount} ${food.portion.unit}")
                                Text("Calorías: ${food.nutrition.calories}")
                            }
                            IconButton(onClick = {
                                foods = foods.toMutableList().also { it.removeAt(idx) }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar alimento")
                            }
                        }
                    }
                }
                OutlinedButton(onClick = { showAddFoodDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Añadir alimento")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (foods.isEmpty()) {
                                // Si no hay alimentos, eliminar la comida
                                onSave(meal, true)
                            } else {
                                // Recalcular nutrición total
                                val total = foods.fold(Nutrition(0,0.0,0.0,0.0,0.0)) { acc, f ->
                                    Nutrition(
                                        calories = acc.calories + f.nutrition.calories,
                                        protein = acc.protein + f.nutrition.protein,
                                        carbs = acc.carbs + f.nutrition.carbs,
                                        fat = acc.fat + f.nutrition.fat,
                                        fiber = (acc.fiber ?: 0.0) + (f.nutrition.fiber ?: 0.0)
                                    )
                                }
                                val updatedMeal = meal.copy(
                                    mealType = selectedMealType,
                                    notes = notes.ifBlank { null },
                                    detectedFoods = foods.toList(),
                                    totalNutrition = total
                                )
                                onSave(updatedMeal, false)
                            }
                        }
                    ) {
                        Text(if (foods.isEmpty()) "Eliminar comida" else "Guardar")
                    }
                }
            }
        }
    }

    // Diálogo para añadir alimento
    if (showAddFoodDialog) {
        AddFoodDialog(
            onAdd = { newFood ->
                foods = foods.toMutableList().apply { add(newFood) }
            },
            onDismiss = { showAddFoodDialog = false }
        )
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

@Suppress("unused")
@Composable
private fun FoodItem(
    food: Food,
    modifier: Modifier = Modifier
) {
    val showDialog = remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .width(130.dp)
            .clickable { showDialog.value = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Food image or icon
            if (!food.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = food.imageUrl,
                    contentDescription = food.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = Primary.copy(alpha = 0.15f)
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
    color: Color
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
