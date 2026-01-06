package com.tuapp.nutritionai.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Camera : Screen("camera")
    data object History : Screen("history")
}
