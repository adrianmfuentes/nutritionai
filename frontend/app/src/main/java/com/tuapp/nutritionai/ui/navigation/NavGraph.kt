package com.tuapp.nutritionai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tuapp.nutritionai.ui.camera.CameraScreen
import com.tuapp.nutritionai.ui.dashboard.DashboardScreen
import com.tuapp.nutritionai.ui.history.HistoryScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }
        composable(Screen.Camera.route) {
            CameraScreen(
                onMealAnalyzed = {
                    val popped = navController.popBackStack()
                     if (!popped) {
                         navController.navigate(Screen.Dashboard.route)
                     }
                }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
               onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
