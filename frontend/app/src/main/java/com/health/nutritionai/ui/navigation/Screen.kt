package com.health.nutritionai.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Camera : Screen("camera")
    data object Dashboard : Screen("dashboard")
    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
}

