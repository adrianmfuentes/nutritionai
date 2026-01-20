package com.health.nutritionai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.health.nutritionai.ui.auth.EmailVerificationScreen
import com.health.nutritionai.ui.auth.LoginScreen
import com.health.nutritionai.ui.auth.RegisterScreen
import com.health.nutritionai.ui.camera.CameraScreen
import com.health.nutritionai.ui.chat.ChatScreen
import com.health.nutritionai.ui.dashboard.DashboardScreen
import com.health.nutritionai.ui.history.HistoryScreen
import com.health.nutritionai.ui.settings.SettingsScreen
import com.health.nutritionai.ui.textinput.TextInputScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToEmailVerification = { email ->
                    navController.navigate(Screen.EmailVerification.createRoute(email))
                }
            )
        }

        composable(Screen.EmailVerification.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EmailVerificationScreen(
                email = email,
                onVerificationSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Main App Screens
        composable(Screen.Dashboard.route) { backStackEntry ->
            DashboardScreen(
                // Pass a key that changes when returning from other screens
                refreshKey = backStackEntry.savedStateHandle.get<Long>("refresh_key") ?: 0L,
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                },
                onNavigateToTextInput = {
                    navController.navigate(Screen.TextInput.route)
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route)
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onMealAnalyzed = {
                    // Set refresh key before navigating back
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_key", System.currentTimeMillis())
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.TextInput.route) {
            TextInputScreen(
                onMealAnalyzed = {
                    // Set refresh key before navigating back
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_key", System.currentTimeMillis())
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                onMealRegistered = {
                    // Set refresh key when a meal is registered via chat
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_key", System.currentTimeMillis())
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
