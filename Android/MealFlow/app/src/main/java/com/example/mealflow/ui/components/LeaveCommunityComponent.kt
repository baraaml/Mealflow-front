package com.example.mealflow.ui.components

import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mealflow.navigation.Destination
import com.example.mealflow.data.model.SingleCommunity
import com.example.mealflow.network.hasNewAdmin
import com.example.mealflow.network.isCommunityDeleted
import com.example.mealflow.network.joinCommunityApi
import com.example.mealflow.network.leaveCommunityApi

@Composable
fun JoinLeaveCommunityButton(
    modifier: Modifier = Modifier,
    community: SingleCommunity?, // Your community data class
    navController: NavController,
    onCommunityStatusChanged: () -> Unit = {}, // Callback to refresh community data
) {
    val context = LocalContext.current
    var showLeaveDialog by remember { mutableStateOf(false) }
    // Optimistic UI state
    var optimisticIsMember by remember(community?.isMember) {
        mutableStateOf(community?.isMember ?: false)
    }
    var optimisticMemberCount by remember(community?.members?.size) {
        mutableStateOf(community?.members?.size ?: 0)
    }

    // Reset optimistic state when community data changes
    LaunchedEffect(community?.isMember, community?.members?.size) {
        optimisticIsMember = community?.isMember ?: false
        optimisticMemberCount = community?.members?.size ?: 0
    }

    // Determine button text using optimistic values
    val buttonText = if (optimisticIsMember) "Leave" else "Join"

    // Button colors based on optimistic state
    val buttonColors = if (optimisticIsMember) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }

    Button(
        onClick = {
            if (optimisticIsMember) {
                // Show confirmation dialog for leaving
                showLeaveDialog = true
            } else {
                // Optimistic update for joining
                optimisticIsMember = true
                optimisticMemberCount += 1

                joinCommunityApi(
                    idCommunity = community?.id ?: "",
                    context = context
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        colors = buttonColors,
        enabled = community?.id != null,
        modifier = modifier
    ) {
        Text(
            text = buttonText,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }

    // Leave confirmation dialog
    if (showLeaveDialog) {
        LeaveCommunityDialog(
            community = community,
            onConfirm = {
                showLeaveDialog = false

                // Optimistic update for leaving
                val wasLastMember = optimisticMemberCount == 1
                optimisticIsMember = false
                if (!wasLastMember) {
                    optimisticMemberCount -= 1
                }

                leaveCommunityApi(
                    idCommunity = community?.id ?: "",
                    context = context,
                    onSuccess = { response ->
                        onCommunityStatusChanged()

                        // Handle special scenarios
                        when {
                            response.isCommunityDeleted() -> {
                                // Community was deleted, might need to navigate back
                                navController.navigate(Destination.CommunityHome)
                            }
                            response.hasNewAdmin() -> {
                                // Show info about new admin if needed
                                Toast.makeText(context, "New admin has been assigned", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onError = {
                        // Revert optimistic changes on error
                        optimisticIsMember = true
                        if (!wasLastMember) {
                            optimisticMemberCount += 1
                        }

                        // Show error message
                        Toast.makeText(context, "Failed to leave community", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onDismiss = {
                showLeaveDialog = false
            },
            countMember = optimisticMemberCount // Use optimistic member count
        )
    }
}

@Composable
fun LeaveCommunityDialog(
    community: SingleCommunity?,
    countMember: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    when {
        community?.isAdmin == false -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = "Leave Community",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to leave ${community.name}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Leave")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }

        countMember == 1 -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = "Delete Community",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to leave ${community?.name}?\nThe community will be permanently deleted as you are the last member.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete Community")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }

        countMember > 1 && community?.hasMultipleAdmins == true -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = "Leave Community",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to leave ${community.name}?\nOther admins will remain in charge.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Leave")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }

        countMember > 1 && community?.hasMultipleAdmins == false -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = "Leave Community",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to leave ${community.name}?\nA new admin will be automatically promoted.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Leave")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
