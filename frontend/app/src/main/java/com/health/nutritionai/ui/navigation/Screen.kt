package com.health.nutritionai.ui.navigation

sealed class Screen(val route: String) {
    data object Camera : Screen("camera")
    data object Dashboard : Screen("dashboard")
    data object History : Screen("history")
    data object Profile : Screen("profile")
}

