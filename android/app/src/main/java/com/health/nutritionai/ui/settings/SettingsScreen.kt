package com.health.nutritionai.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.health.nutritionai.data.model.NutritionGoals
import com.health.nutritionai.data.model.UserProfile
import com.health.nutritionai.util.ImageUtils
import java.io.File
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.health.nutritionai.R
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val showGoalsDialog by viewModel.showGoalsDialog.collectAsState()
    val showNotificationsDialog by viewModel.showNotificationsDialog.collectAsState()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsState()
    val showUnitsDialog by viewModel.showUnitsDialog.collectAsState()
    val showChangePasswordDialog by viewModel.showChangePasswordDialog.collectAsState()
    val showEditProfileDialog by viewModel.showEditProfileDialog.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedUnits by viewModel.selectedUnits.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                            Text(stringResource(R.string.retry))
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
                        ProfileCard(userProfile = userProfile, onEditClick = { viewModel.showEditProfileDialog() })
                    }

                    // Nutrition Goals Section
                    item {
                        Text(
                            text = stringResource(R.string.nutrition_goals),
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
                            text = stringResource(R.string.preferences),
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
                            text = stringResource(R.string.account),
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

                if (showEditProfileDialog) {
                    EditProfileDialog(
                        userProfile = userProfile,
                        onDismiss = { viewModel.hideEditProfileDialog() },
                        onSave = { name, photoFile ->
                            viewModel.updateUserProfileWithImage(name, photoFile)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    userProfile: UserProfile,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
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
            // Avatar - Show photo if available, otherwise default icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (userProfile.photoUrl != null) {
                    AsyncImage(
                        model = userProfile.photoUrl,
                        contentDescription = "User avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
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
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
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

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                    text = stringResource(R.string.daily_goals),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_goals))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (goals != null) {
                GoalItem(label = stringResource(R.string.calories), value = "${goals.calories} ${stringResource(R.string.kcal)}", icon = Icons.Default.Star)
                GoalItem(label = stringResource(R.string.protein), value = "${goals.protein.toInt()} ${stringResource(R.string.g)}", icon = Icons.Default.FavoriteBorder)
                GoalItem(label = stringResource(R.string.carbs), value = "${goals.carbs.toInt()} ${stringResource(R.string.g)}", icon = Icons.Default.CheckCircle)
                GoalItem(label = stringResource(R.string.fat), value = "${goals.fat.toInt()} ${stringResource(R.string.g)}", icon = Icons.Default.Info)
            } else {
                Text(
                    text = stringResource(R.string.no_goals_configured),
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
                title = stringResource(R.string.notifications),
                subtitle = if (notificationsEnabled) stringResource(R.string.notifications_enabled) else stringResource(R.string.notifications_disabled),
                onClick = onNotificationsClick
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.language),
                subtitle = selectedLanguage,
                onClick = onLanguageClick
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsItem(
                icon = Icons.Default.Build,
                title = stringResource(R.string.units),
                subtitle = when (selectedUnits) {
                    "metric" -> stringResource(R.string.units_metric)
                    "imperial" -> stringResource(R.string.units_imperial)
                    else -> selectedUnits
                },
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
                title = stringResource(R.string.change_password),
                subtitle = stringResource(R.string.update_password),
                onClick = onChangePassword
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = stringResource(R.string.logout),
                subtitle = stringResource(R.string.logout_subtitle),
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
        title = { Text(stringResource(R.string.edit_nutrition_goals)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text(stringResource(R.string.calories_kcal)) },
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text(stringResource(R.string.protein_g)) },
                    leadingIcon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text(stringResource(R.string.carbs_g)) },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text(stringResource(R.string.fat_g)) },
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
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
        title = { Text(stringResource(R.string.notifications_config)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.meal_reminders))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.notifications_label))
                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggle
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun LanguageDialog(
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onSelect: suspend (String) -> Unit
) {
    val languages = listOf(stringResource(R.string.language_spanish), stringResource(R.string.language_english), stringResource(R.string.language_french), stringResource(R.string.language_german))
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_language)) },
        text = {
            Column {
                languages.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    onSelect(language)
                                }
                            }
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
                Text(stringResource(R.string.cancel))
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
    val unitOptions = listOf(
        "metric" to stringResource(R.string.units_metric),
        "imperial" to stringResource(R.string.units_imperial)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_units)) },
        text = {
            Column {
                unitOptions.forEach { (key, display) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(key) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(display)
                        if (key == selectedUnits) {
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
                Text(stringResource(R.string.cancel))
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
        title = { Text(stringResource(R.string.change_password_title)) },
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
                    label = { Text(stringResource(R.string.current_password)) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        showError = false
                    },
                    label = { Text(stringResource(R.string.new_password)) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        showError = false
                    },
                    label = { Text(stringResource(R.string.confirm_new_password)) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError
                )
                if (showError) {
                    Text(
                        text = stringResource(R.string.passwords_dont_match),
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
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun EditProfileDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, File?) -> Unit
) {
    var name by remember { mutableStateOf(userProfile.name) }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    val context = LocalContext.current

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Convert Uri to File
            val file = ImageUtils.uriToFile(context, it, "profile_image.jpg")
            selectedImageFile = file
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = userProfile.email,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.email_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                // Profile photo selection
                Column {
                    Text(
                        text = stringResource(R.string.select_profile_photo),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.select_image))
                    }
                    if (selectedImageFile != null) {
                        Text(
                            text = stringResource(R.string.image_selected),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(name, selectedImageFile)
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
