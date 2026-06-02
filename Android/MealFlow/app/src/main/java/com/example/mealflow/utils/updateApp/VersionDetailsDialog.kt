package com.example.mealflow.utils.updateApp

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.mealflow.ui.screens.SettingsButton
import com.example.mealflow.utils.HapticFeedback
import com.example.mealflow.utils.collectAsState

@Composable
fun VersionCheckDialog(
    versionState: VersionViewModel.VersionState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    context: Context
) {
    when (versionState) {
        is VersionViewModel.VersionState.Idle -> {
            LoadingVersionDialog(onDismiss = onDismiss)
        }

        is VersionViewModel.VersionState.Loading -> {
            LoadingVersionDialog(onDismiss = onDismiss)
        }

        is VersionViewModel.VersionState.Success -> {
            SuccessVersionDialog(
                successState = versionState,
                onDismiss = onDismiss,
                context = context
            )
        }

        is VersionViewModel.VersionState.Error -> {
            ErrorVersionDialog(
                errorMessage = versionState.message,
                onDismiss = onDismiss,
                onRetry = onRetry
            )
        }
    }
}

@Composable
private fun LoadingVersionDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Checking...") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Checking for the latest version...")
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun SuccessVersionDialog(
    successState: VersionViewModel.VersionState.Success,
    onDismiss: () -> Unit,
    context: Context
) {
    val versionData = successState.versionData
    val versionMessage = getVersionComparisonMessage(successState.versionComparison)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Version information")
                Spacer(modifier = Modifier.width(8.dp))
                VersionIcon(successState.versionComparison)
            }
        },
        text = {
            VersionDialogContent(
                successState = successState,
                versionData = versionData,
                versionMessage = versionMessage
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = {
            if (successState.needsUpdate) {
                UpdateButton(
                    versionComparison = successState.versionComparison,
                    onDismiss = onDismiss,
                    context = context
                )
            }
        }
    )
}

@Composable
private fun ErrorVersionDialog(
    errorMessage: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("خطأ")
            }
        },
        text = {
            Text("An error occurred while verifying the version:\n$errorMessage")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    )
}

@Composable
private fun VersionIcon(versionComparison: VersionViewModel.VersionComparison) {
    when (versionComparison) {
        VersionViewModel.VersionComparison.UP_TO_DATE -> {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.Green,
                modifier = Modifier.size(20.dp)
            )
        }
        VersionViewModel.VersionComparison.UPDATE_AVAILABLE -> {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        VersionViewModel.VersionComparison.FORCE_UPDATE -> {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Yellow,
                modifier = Modifier.size(20.dp)
            )
        }
        VersionViewModel.VersionComparison.UNSUPPORTED -> {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun VersionDialogContent(
    successState: VersionViewModel.VersionState.Success,
    versionData: VersionData, // Replace with actual type
    versionMessage: String
) {
    Column {
        // Version information card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (successState.versionComparison) {
                    VersionViewModel.VersionComparison.UP_TO_DATE -> Color.Green.copy(alpha = 0.1f)
                    VersionViewModel.VersionComparison.UPDATE_AVAILABLE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    VersionViewModel.VersionComparison.FORCE_UPDATE -> Color.Yellow.copy(alpha = 0.1f)
                    VersionViewModel.VersionComparison.UNSUPPORTED -> Color.Red.copy(alpha = 0.1f)
                }
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = versionMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (successState.versionComparison) {
                        VersionViewModel.VersionComparison.UP_TO_DATE -> Color.Green
                        VersionViewModel.VersionComparison.UPDATE_AVAILABLE -> MaterialTheme.colorScheme.primary
                        VersionViewModel.VersionComparison.FORCE_UPDATE -> Color.Yellow
                        VersionViewModel.VersionComparison.UNSUPPORTED -> Color.Red
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Version details
        Text("Application name : ${versionData.appName}", style = MaterialTheme.typography.bodyMedium)
        Text("Current version : ${successState.currentAppVersion}", style = MaterialTheme.typography.bodyMedium)
        Text("Latest version : ${versionData.latestVersion}", style = MaterialTheme.typography.bodyMedium)
        Text("Minimum supported : ${versionData.minSupportedVersion}", style = MaterialTheme.typography.bodyMedium)

        if (successState.needsUpdate) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Update message : ", fontWeight = FontWeight.Bold)
            Text(versionData.updateMessage, style = MaterialTheme.typography.bodySmall)

            if (versionData.whatsNew.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("What's new : ", fontWeight = FontWeight.Bold)
                versionData.whatsNew.forEach { update ->
                    Text("• $update", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun UpdateButton(
    versionComparison: VersionViewModel.VersionComparison,
    onDismiss: () -> Unit,
    context: Context
) {
    TextButton(
        onClick = {
            val url = "https://mealflow.ddns.net/download"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            onDismiss()
        }
    ) {
        Text(
            when (versionComparison) {
                VersionViewModel.VersionComparison.FORCE_UPDATE -> "Forced update"
                VersionViewModel.VersionComparison.UNSUPPORTED -> "Update required"
                else -> "Download the update"
            }
        )
    }
}

// Helper function - you might need to move this to ViewModel or create it
private fun getVersionComparisonMessage(versionComparison: VersionViewModel.VersionComparison): String {
    // Implementation depends on your ViewModel logic
    return when (versionComparison) {
        VersionViewModel.VersionComparison.UP_TO_DATE -> "Your app is updated to the latest version."
        VersionViewModel.VersionComparison.UPDATE_AVAILABLE -> "A newer version is available."
        VersionViewModel.VersionComparison.FORCE_UPDATE -> "The app must be updated to continue."
        VersionViewModel.VersionComparison.UNSUPPORTED -> "Current app version is not supported"
    }
}

// Updated main component
@Composable
fun VersionCheckButton(
    viewModel: VersionViewModel,
    hapticFeedback: HapticFeedback
) {
    val currentVersion = viewModel.getCurrentAppVersion()
    val versionState by viewModel.versionState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    SettingsButton(
        title = "App Version",
        subtitle = "Check for updates : $currentVersion",
        icon = Icons.Default.Info,
        onClick = {
//            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.checkAppVersion()
            showDialog = true
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