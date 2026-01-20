package com.health.nutritionai

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.health.nutritionai.ui.navigation.NavGraph
import com.health.nutritionai.ui.navigation.Screen
import com.health.nutritionai.ui.theme.NutritionaiTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritionaiTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Check if user is logged in
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences(com.health.nutritionai.util.Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
    val isLoggedIn = prefs.getString(com.health.nutritionai.util.Constants.KEY_AUTH_TOKEN, null) != null

    // Determine start destination
    val startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route

    // Show bottom bar only on main screens (not on auth screens)
    val showBottomBar = currentDestination?.route in listOf(
        Screen.Dashboard.route,
        Screen.Camera.route,
        Screen.History.route,
        Screen.Settings.route
    )

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard.route, Icons.Default.Home, stringResource(R.string.nav_home)),
        BottomNavItem(Screen.Camera.route, Icons.Default.Add, stringResource(R.string.nav_camera)),
        BottomNavItem(Screen.History.route, Icons.AutoMirrored.Filled.List, stringResource(R.string.nav_history)),
        BottomNavItem(Screen.Settings.route, Icons.Default.Settings, stringResource(R.string.nav_settings))
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            startDestination = startDestination
        )
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)
