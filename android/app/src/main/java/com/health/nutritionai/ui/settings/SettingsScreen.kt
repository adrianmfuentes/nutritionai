package com.health.nutritionai.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.nutritionai.data.model.NutritionGoals
import com.health.nutritionai.data.model.UserProfile
import com.health.nutritionai.util.UserFeedback
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val showGoalsDialog by viewModel.showGoalsDialog.collectAsState()
    val showNotificationsDialog by viewModel.showNotificationsDialog.collectAsState()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsState()
    val showUnitsDialog by viewModel.showUnitsDialog.collectAsState()
    val showChangePasswordDialog by viewModel.showChangePasswordDialog.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedUnits by viewModel.selectedUnits.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show feedback messages
    LaunchedEffect(feedback) {
        when (feedback) {
            is UserFeedback.Success -> {
                snackbarHostState.showSnackbar(
                    message = (feedback as UserFeedback.Success).message,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearFeedback()
            }
            is UserFeedback.Error -> {
                snackbarHostState.showSnackbar(
                    message = (feedback as UserFeedback.Error).message,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearFeedback()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (uiState) {
            is SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is SettingsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = (uiState as SettingsUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = { viewModel.loadUserProfile() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            is SettingsUiState.Success -> {
                val userProfile = (uiState as SettingsUiState.Success).userProfile

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Card
                    item {
                        ProfileCard(userProfile = userProfile)
                    }

                    // Nutrition Goals Section
                    item {
                        Text(
                            text = "Objetivos Nutricionales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        NutritionGoalsCard(
                            goals = userProfile.goals,
                            onEditClick = { viewModel.showGoalsDialog() }
                        )
                    }

                    // Preferences Section
                    item {
                        Text(
                            text = "Preferencias",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        PreferencesSection(
                            notificationsEnabled = notificationsEnabled,
                            selectedLanguage = selectedLanguage,
                            selectedUnits = selectedUnits,
                            onNotificationsClick = { viewModel.showNotificationsDialog() },
                            onLanguageClick = { viewModel.showLanguageDialog() },
                            onUnitsClick = { viewModel.showUnitsDialog() }
                        )
                    }

                    // Account Section
                    item {
                        Text(
                            text = "Cuenta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        AccountSection(
                            onChangePassword = { viewModel.showChangePasswordDialog() },
                            onLogout = {
                                viewModel.logout()
                                onLogout()
                            }
                        )
                    }
                }

                if (showGoalsDialog) {
                    EditGoalsDialog(
                        currentGoals = userProfile.goals,
                        onDismiss = { viewModel.hideGoalsDialog() },
                        onSave = { goals ->
                            viewModel.updateGoals(goals)
                        }
                    )
                }

                if (showNotificationsDialog) {
                    NotificationsDialog(
                        enabled = notificationsEnabled,
                        onDismiss = { viewModel.hideNotificationsDialog() },
                        onToggle = { viewModel.updateNotifications(it) }
                    )
                }

                if (showLanguageDialog) {
                    LanguageDialog(
                        selectedLanguage = selectedLanguage,
                        onDismiss = { viewModel.hideLanguageDialog() },
                        onSelect = { viewModel.updateLanguage(it) }
                    )
                }

                if (showUnitsDialog) {
                    UnitsDialog(
                        selectedUnits = selectedUnits,
                        onDismiss = { viewModel.hideUnitsDialog() },
                        onSelect = { viewModel.updateUnits(it) }
                    )
                }

                if (showChangePasswordDialog) {
                    ChangePasswordDialog(
                        onDismiss = { viewModel.hideChangePasswordDialog() },
                        onSave = { current, new ->
                            viewModel.changePassword(current, new)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(userProfile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar - Simple circle with icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = userProfile.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userProfile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun NutritionGoalsCard(
    goals: NutritionGoals?,
    onEditClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
                Text(
                    text = "Objetivos Diarios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar objetivos")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (goals != null) {
                GoalItem(label = "Calorías", value = "${goals.calories} kcal", icon = Icons.Default.Star)
                GoalItem(label = "Proteína", value = "${goals.protein.toInt()} g", icon = Icons.Default.FavoriteBorder)
                GoalItem(label = "Carbohidratos", value = "${goals.carbs.toInt()} g", icon = Icons.Default.CheckCircle)
                GoalItem(label = "Grasas", value = "${goals.fat.toInt()} g", icon = Icons.Default.Info)
            } else {
                Text(
                    text = "No has configurado tus objetivos aún",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GoalItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PreferencesSection(
    notificationsEnabled: Boolean,
    selectedLanguage: String,
    selectedUnits: String,
    onNotificationsClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onUnitsClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notificaciones",
                subtitle = if (notificationsEnabled) "Activadas" else "Desactivadas",
                onClick = onNotificationsClick
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsItem(
                icon = Icons.Default.Settings,
                title = "Idioma",
                subtitle = selectedLanguage,
                onClick = onLanguageClick
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsItem(
                icon = Icons.Default.Build,
                title = "Unidades",
                subtitle = selectedUnits,
                onClick = onUnitsClick
            )
        }
    }
}

@Composable
private fun AccountSection(
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Cambiar contraseña",
                subtitle = "Actualiza tu contraseña",
                onClick = onChangePassword
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Cerrar sesión",
                subtitle = "Salir de tu cuenta",
                onClick = onLogout,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditGoalsDialog(
    currentGoals: NutritionGoals?,
    onDismiss: () -> Unit,
    onSave: (NutritionGoals) -> Unit
) {
    var calories by remember { mutableStateOf(currentGoals?.calories?.toString() ?: "2000") }
    var protein by remember { mutableStateOf(currentGoals?.protein?.toInt()?.toString() ?: "150") }
    var carbs by remember { mutableStateOf(currentGoals?.carbs?.toInt()?.toString() ?: "200") }
    var fat by remember { mutableStateOf(currentGoals?.fat?.toInt()?.toString() ?: "65") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Objetivos Nutricionales") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calorías (kcal)") },
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Proteína (g)") },
                    leadingIcon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbohidratos (g)") },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Grasas (g)") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val goals = NutritionGoals(
                        calories = calories.toIntOrNull() ?: 2000,
                        protein = protein.toDoubleOrNull() ?: 150.0,
                        carbs = carbs.toDoubleOrNull() ?: 200.0,
                        fat = fat.toDoubleOrNull() ?: 65.0
                    )
                    onSave(goals)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun NotificationsDialog(
    enabled: Boolean,
    onDismiss: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configuración de Notificaciones") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Recibe recordatorios para registrar tus comidas")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notificaciones")
                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggle
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun LanguageDialog(
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val languages = listOf("Español", "English", "Français", "Deutsch")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Idioma") },
        text = {
            Column {
                languages.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(language) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(language)
                        if (language == selectedLanguage) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun UnitsDialog(
    selectedUnits: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val unitOptions = listOf("Métrico (g, kg)", "Imperial (oz, lb)")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Unidades") },
        text = {
            Column {
                unitOptions.forEach { unit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(unit) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(unit)
                        if (unit == selectedUnits) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Contraseña") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        showError = false
                    },
                    label = { Text("Contraseña actual") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        showError = false
                    },
                    label = { Text("Nueva contraseña") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        showError = false
                    },
                    label = { Text("Confirmar nueva contraseña") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError
                )
                if (showError) {
                    Text(
                        text = "Las contraseñas no coinciden",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPassword == confirmPassword && newPassword.isNotEmpty()) {
                        onSave(currentPassword, newPassword)
                    } else {
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

