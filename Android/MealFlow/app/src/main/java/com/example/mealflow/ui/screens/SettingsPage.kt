package com.example.mealflow.ui.screens

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mealflow.navigation.NavRoutes
import com.example.mealflow.utils.updateApp.VersionCheckDialog
import com.example.mealflow.utils.updateApp.VersionViewModel
import com.example.mealflow.viewModel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    navController: NavController,
    onNavigateBack: () -> Unit = { navController.popBackStack() }
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )

    val scrollState = rememberScrollState()
    val hapticFeedback = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            SettingsTopBar(
                onNavigateBack = onNavigateBack,
                hapticFeedback = hapticFeedback
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AppearanceSection(
                viewModel = viewModel,
                hapticFeedback = hapticFeedback
            )

            NotificationsSection(
                viewModel = viewModel,
                hapticFeedback = hapticFeedback
            )

            PreferencesSection(
                viewModel = viewModel,
                hapticFeedback = hapticFeedback
            )

            AccountSection(
                navController = navController,
                hapticFeedback = hapticFeedback
            )

            SupportSection(
                navController = navController,
                hapticFeedback = hapticFeedback
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    onNavigateBack: () -> Unit,
    hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    TopAppBar(
        title = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNavigateBack()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    )
}

@Composable
private fun AppearanceSection(
    viewModel: SettingsViewModel,
    hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = viewModel.themeSetting,
            onThemeSelected = { theme ->
                viewModel.setTheme(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    SettingsSection(
        title = "Appearance",
        icon = Icons.Default.Palette,
        gradient = Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            )
        )
    ) {
        SettingsButton(
            title = "Theme",
            subtitle = "Choose between light, dark, or system default",
            icon = Icons.Default.DarkMode,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                showThemeDialog = true
            }
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val themes = listOf("Light", "Dark", "System")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column {
                themes.forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(text = theme)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun NotificationsSection(
    viewModel: SettingsViewModel,
    hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    SettingsSection(
        title = "Notifications",
        icon = Icons.Default.Notifications,
        gradient = Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
    ) {
        SettingsSwitch(
            title = "Push Notifications",
            subtitle = "Get timely meal reminders and helpful updates",
            icon = Icons.Default.NotificationsActive,
            checked = viewModel.notificationsEnabled,
            onCheckedChange = { enabled ->
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.toggleNotifications(enabled)
            }
        )
    }
}

@Composable
private fun PreferencesSection(
    viewModel: SettingsViewModel,
    hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    SettingsSection(
        title = "Preferences",
        icon = Icons.Default.Tune,
        gradient = Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
    ) {
        SettingsSwitch(
            title = "Haptic Feedback",
            subtitle = "Feel vibrations when interacting with the app",
            icon = Icons.Default.Vibration,
            checked = viewModel.hapticFeedbackEnabled,
            onCheckedChange = { enabled ->
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.toggleHapticFeedback(enabled)
            }
        )
    }
}

@Composable
private fun AccountSection(
    navController: NavController,
    hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    SettingsSection(
        title = "Account & Privacy",
        icon = Icons.Default.AccountCircle,
        gradient = Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
            )
        )
    ) {
        SettingsButton(
            title = "Profile",
            subtitle = "Manage your personal information",
            icon = Icons.Default.Person,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                navController.navigate(NavRoutes.ProfilePage.route)
            }
        )

        SettingsButton(
            title = "Privacy Policy",
            subtitle = "Read our privacy policy",
            icon = Icons.Default.Policy,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                navController.navigate("privacy_policy")
            }
        )

        SettingsButton(
            title = "Terms of Service",
            subtitle = "Read our terms of service",
            icon = Icons.Default.Description,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                navController.navigate("terms_of_service")
            }
        )
    }
}

@Composable
private fun SupportSection(
    navController: NavController,
    hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    SettingsSection(
        title = "Support",
        icon = Icons.AutoMirrored.Filled.Help,
        gradient = Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            )
        )
    ) {
        SettingsButton(
            title = "Help Center",
            subtitle = "Get help and contact support",
            icon = Icons.AutoMirrored.Filled.ContactSupport,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                navController.navigate("help_center")
            }
        )
        SettingsButton(
            title = "Report a Bug",
            subtitle = "Let us know about a technical issue",
            icon = Icons.Default.BugReport,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                navController.navigate("report_bug")
            }
        )
        val viewModel : VersionViewModel = viewModel()
        val currentVersion = viewModel.getCurrentAppVersion()
        val versionState by viewModel.versionState.collectAsState()
        var showDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current

        SettingsButton(
            title = "Check Updates",
            subtitle = "Tap to check for newer versions",
            icon = Icons.Default.Update,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.checkAppVersion()
                showDialog = true
            },
            showChevron = false
        )

        SettingsButton(
            title = "App Version",
            subtitle = "Version $currentVersion",
            icon = Icons.Default.Info,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                // TODO: Show version details
            },
            showChevron = false
        )

        if (showDialog) {
            // Launch version check if in idle state
            if (versionState is VersionViewModel.VersionState.Idle) {
                LaunchedEffect(Unit) {
                    viewModel.checkAppVersion()
                }
            }
            VersionCheckDialog(
                versionState = versionState,
                onDismiss = { showDialog = false },
                onRetry = { viewModel.checkAppVersion() },
                context = context
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    gradient: Brush,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        brush = gradient,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Section Content Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(4.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "scale_animation"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                role = Role.Switch,
                onClickLabel = if (checked) "Disable $title" else "Enable $title"
            ) {
                isPressed = true
                onCheckedChange(!checked)
            }
            .semantics {
                role = Role.Switch
                contentDescription = "$title: ${if (checked) "enabled" else "disabled"}"
            },
        color = if (checked) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (checked) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun SettingsButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    showChevron: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "scale_animation"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                role = Role.Button,
                onClickLabel = "Open $title"
            ) {
                isPressed = true
                onClick()
            }
            .semantics {
                role = Role.Button
                contentDescription = "Navigate to $title"
            },
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}