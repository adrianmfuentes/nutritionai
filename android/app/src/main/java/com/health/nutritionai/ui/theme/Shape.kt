package com.health.nutritionai.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Custom shapes for the NutritionAI app
 * Using modern, soft rounded corners for an elegant look
 */
val Shapes = Shapes(
    // Extra small - Chips, small buttons
    extraSmall = RoundedCornerShape(10.dp),

    // Small - Buttons, text fields
    small = RoundedCornerShape(14.dp),

    // Medium - Cards, dialogs
    medium = RoundedCornerShape(20.dp),

    // Large - Bottom sheets, large cards
    large = RoundedCornerShape(28.dp),

    // Extra large - Modal sheets
    extraLarge = RoundedCornerShape(36.dp)
)
