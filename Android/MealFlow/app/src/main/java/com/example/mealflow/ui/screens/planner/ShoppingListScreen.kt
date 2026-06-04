// File: app/src/main/java/com/example/mealflow/ui/screens/ShoppingListScreen.kt
package com.example.mealflow.ui.screens.planner

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealflow.MainActivity
import com.example.mealflow.data.model.CompositeShoppingList
import com.example.mealflow.data.model.EnhancedShoppingListItem
import com.example.mealflow.data.model.ShoppingListItem
import com.example.mealflow.data.model.ShoppingPlan
import com.example.mealflow.navigation.Destination
import com.example.mealflow.ui.components.*
import com.example.mealflow.utils.DateUtils
import com.example.mealflow.viewModel.ShoppingListViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mealflow.utils.ShoppingReminderManager
import java.time.format.DateTimeFormatter

/**
 * Extension function to find the activity from a context
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@SuppressLint("ServiceCast")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel,
    onNavigateToAddCustomItem: () -> Unit,
    onNavigateBack: () -> Unit,
    navController: androidx.navigation.NavController? = null
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Remember activity launcher for exact alarm permission
    val exactAlarmPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // After returning from settings, check if we got the permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                // Permission granted, show success message
                viewModel.clearExactAlarmPermissionRequest()
                viewModel.showSnackbar("Permission granted. You can now set reminders.")
            } else {
                // Permission denied, show error message
                viewModel.showSnackbar("Exact alarm permission denied. Reminders may not work properly.")
            }
        }
    }
    
    // Handle permission request
    LaunchedEffect(uiState.needsExactAlarmPermission) {
        if (uiState.needsExactAlarmPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Get the intent to request the permission
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            exactAlarmPermissionLauncher.launch(intent)
            viewModel.clearExactAlarmPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Enhanced Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Shopping List",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Subtitle for context
                    uiState.activeShoppingPlan?.let { plan ->
                        Text(
                            text = "Plan: ${plan.formatDateRange()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Content
            when {
                uiState.isLoading -> {
                    LoadingScreen()
                }
                uiState.errorMessage != null -> {
                    ErrorScreen(
                        message = uiState.errorMessage!!,
                        onRetry = { viewModel.refreshShoppingList() }
                    )
                }
                uiState.compositeShoppingList == null ||
                        (uiState.compositeShoppingList?.items?.isEmpty() == true &&
                                uiState.compositeShoppingList?.inHouseItems?.isEmpty() == true) -> {
                    EmptyShoppingListMessage(
                        onCreateShoppingListClick = {
                            // Navigate to DaysSelectionScreen to start the meal planning flow
                            navController?.navigate(Destination.DaysSelection)
                        }
                    )
                }
                else -> {
                    ShoppingListContent(
                        compositeList = uiState.compositeShoppingList!!,
                        onTogglePurchased = { item, isPurchased ->
                            viewModel.toggleItemPurchasedStatus(item, isPurchased)
                        },
                        onMoveToInventory = { item ->
                            viewModel.moveItemToInventory(item)
                        },
                        onMoveToNeeded = { item ->
                            viewModel.moveItemFromInventoryToNeeded(item)
                        },
                        viewModel = viewModel
                    )
                }
            }
        }

        // FAB Menu - Enhanced with smoother animations
        FABMenu(
            isExpanded = isFabExpanded,
            onToggleExpanded = { isFabExpanded = !isFabExpanded },
            hasUnpurchasedItems = uiState.compositeShoppingList?.items?.any { !it.isPurchased } == true,
            hasItems = uiState.compositeShoppingList?.let {
                it.items.isNotEmpty() || it.inHouseItems.isNotEmpty()
            } == true,
            onCompleteShoppingDay = {
                viewModel.markShoppingDayComplete()
                isFabExpanded = false
            },
            onDeleteList = {
                showDeleteConfirmation = true
                isFabExpanded = false
            },
            onAddCustomItem = {
                viewModel.prepareToAddCustomItem()
                onNavigateToAddCustomItem()
                isFabExpanded = false
            }
        )

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Delete confirmation dialog - Enhanced with more visual cues
    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            onConfirm = {
                viewModel.clearShoppingList()
                showDeleteConfirmation = false
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
    
    // Reminder Dialog
    if (uiState.showReminderDialog) {
        val shoppingPlan = uiState.activeShoppingPlan
        val dayIndex = uiState.reminderDialogDayIndex
        
        if (shoppingPlan != null && dayIndex < shoppingPlan.shoppingDays.size) {
            val shoppingDateStr = shoppingPlan.shoppingDays[dayIndex]
            val shoppingDate = DateUtils.parseLocalDate(shoppingDateStr)
            
            if (shoppingDate != null) {
                val reminderMetadata = viewModel.getReminderMetadata(dayIndex)
                val existingReminderTimeIndex = reminderMetadata?.reminderTimeIndex
                val existingNotes = reminderMetadata?.notes ?: uiState.reminderDialogNotes
                val existingCustomTime = reminderMetadata?.customTime
                
                ShoppingReminderDialog(
                    onDismiss = { viewModel.dismissReminderDialog() },
                    onSaveReminder = { reminderTimeIndex, notes ->
                        viewModel.setReminder(dayIndex, reminderTimeIndex, notes)
                    },
                    onSaveReminderWithCustomTime = { reminderTimeIndex, notes, customTime ->
                        viewModel.setReminderWithCustomTime(dayIndex, reminderTimeIndex, notes, customTime)
                    },
                    onRemoveReminder = {
                        viewModel.removeReminder(dayIndex)
                    },
                    shoppingDate = shoppingDate,
                    existingReminderTimeIndex = existingReminderTimeIndex,
                    existingNotes = existingNotes,
                    existingCustomTime = existingCustomTime
                )
            }
        }
    }

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
            viewModel.clearSnackbar()
        }
    }
}

@Composable
private fun ShoppingListContent(
    compositeList: CompositeShoppingList,
    onTogglePurchased: (EnhancedShoppingListItem, Boolean) -> Unit,
    onMoveToInventory: (EnhancedShoppingListItem) -> Unit,
    onMoveToNeeded: (ShoppingListItem) -> Unit,
    viewModel: ShoppingListViewModel
) {
    val listState = rememberLazyListState()
    val neededItems = compositeList.items.filter { !it.isPurchased }
    val purchasedItems = compositeList.items.filter { it.isPurchased }
    val inHouseItems = compositeList.inHouseItems
    val shoppingPlan = viewModel.uiState.collectAsState().value.activeShoppingPlan
    
    // Group needed items by shopping day
    val groupedNeededItems = neededItems.groupBy { it.shoppingDayIndex }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp), // Extra padding for FAB
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Shopping list items grouped by shopping day
        if (neededItems.isNotEmpty()) {

            
            // Items organized by shopping day
            groupedNeededItems.keys.sorted().forEach { dayIndex ->
                val dayItems = groupedNeededItems[dayIndex] ?: emptyList()
                if (dayItems.isNotEmpty()) {
                    item(key = "day-header-$dayIndex") {
                        ShoppingDayHeader(
                            dayIndex = dayIndex,
                            shoppingPlan = shoppingPlan,
                            viewModel = viewModel
                        )
                    }
                    
                    items(
                        items = dayItems,
                        key = { "${it.ingredientId ?: "custom"}_${it.name}_${it.unit}_${it.shoppingDayIndex}" }
                    ) { item ->
                        ShoppingListItemCard(
                            item = item,
                            onTogglePurchased = { onTogglePurchased(item, !item.isPurchased) },
                            onMoveToInventory = { onMoveToInventory(item) },
                            viewModel = viewModel
                        )
                    }
                    
                    item(key = "spacer-day-$dayIndex") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Purchased items section
        if (purchasedItems.isNotEmpty()) {
            item(key = "purchased-header") {
                SectionHeader(
                    title = "Purchased Items",
                    subtitle = "${purchasedItems.size} items checked off",
                    icon = Icons.Rounded.Done
                )
            }
            
            items(
                items = purchasedItems,
                key = { "${it.ingredientId ?: "custom"}_${it.name}_${it.unit}_${it.shoppingDayIndex}_purchased" }
            ) { item ->
                ShoppingListItemCard(
                    item = item,
                    onTogglePurchased = { onTogglePurchased(item, !item.isPurchased) },
                    onMoveToInventory = { onMoveToInventory(item) },
                    viewModel = viewModel
                )
            }
            
            item(key = "spacer-purchased") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // In-house items section
        if (inHouseItems.isNotEmpty()) {
            item(key = "inhouse-header") {
                SectionHeader(
                    title = "Items In Stock",
                    subtitle = "${inHouseItems.size} items available",
                    icon = Icons.Rounded.Kitchen
                )
            }
            
            items(
                items = inHouseItems,
                key = { "${it.ingredientId ?: "custom"}_${it.name}_${it.unit}_inhouse" }
            ) { item ->
                InHouseItemCard(
                    item = item,
                    onMoveToNeeded = { onMoveToNeeded(item) },
                    viewModel = viewModel
                )
            }
        }
        
        // Bottom spacer for FAB
        item(key = "bottom-spacer") {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ShoppingDayHeader(
    dayIndex: Int,
    shoppingPlan: ShoppingPlan? = null,
    viewModel: ShoppingListViewModel = viewModel()
) {
    val tripDate = if (shoppingPlan != null && dayIndex < shoppingPlan.shoppingDays.size) {
        DateUtils.formatShortDate(shoppingPlan.shoppingDays[dayIndex])
    } else {
        null
    }
    
    // Parse the shopping date for use in reminder dialog
    val shoppingDate = if (shoppingPlan != null && dayIndex < shoppingPlan.shoppingDays.size) {
        DateUtils.parseLocalDate(shoppingPlan.shoppingDays[dayIndex])
    } else {
        null
    }
    
    // Check if there's an existing reminder
    val hasReminder = shoppingDate != null && viewModel.hasReminder(dayIndex)
    val reminderData = if (hasReminder) viewModel.getReminderMetadata(dayIndex) else null
    
    // Find the date range this shopping trip covers
    val dateRangeInfo = if (shoppingPlan != null && dayIndex < shoppingPlan.shoppingDays.size) {
        val currentTripDate = DateUtils.parseLocalDate(shoppingPlan.shoppingDays[dayIndex])
        val nextTripDate = if (dayIndex + 1 < shoppingPlan.shoppingDays.size) {
            DateUtils.parseLocalDate(shoppingPlan.shoppingDays[dayIndex + 1])
        } else {
            DateUtils.parseLocalDate(shoppingPlan.planEndDate)?.plusDays(1)
        }
        
        if (currentTripDate != null && nextTripDate != null) {
            "Covers meals from ${DateUtils.formatShortDate(DateUtils.formatToISO(currentTripDate))} to ${DateUtils.formatShortDate(DateUtils.formatToISO(nextTripDate.minusDays(1)))}"
        } else {
            null
        }
    } else {
        null
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (tripDate != null) "Trip ${dayIndex + 1} ($tripDate)" else "Trip ${dayIndex + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Rounded.ShoppingBasket,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Add reminder button
            if (shoppingDate != null) {
                IconButton(
                    onClick = { viewModel.showReminderDialog(dayIndex) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (hasReminder) Icons.Filled.Alarm else Icons.Filled.AddAlarm,
                        contentDescription = if (hasReminder) "Edit Reminder" else "Add Reminder",
                        tint = if (hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Show date range info if available
        if (dateRangeInfo != null) {
            Text(
                text = dateRangeInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
        
        // Show reminder card if there's an active reminder
        if (hasReminder && reminderData != null) {
            val (reminderTimeIndex, reminderDate, notes, customTime) = reminderData
            Spacer(modifier = Modifier.height(8.dp))
            
            // Google Keep style reminder pill
            Row(
                modifier = Modifier
                    .padding(start = 12.dp, top = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                    .clickable { viewModel.showReminderDialog(dayIndex) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Format the reminder time based on type
                val reminderText = if (reminderTimeIndex == 3 && customTime != null) {
                    customTime.format(DateTimeFormatter.ofPattern("h:mm a"))
                } else {
                    val (label, _, _) = ShoppingReminderManager.REMINDER_TIMES[reminderTimeIndex]
                    label
                }
                
                Text(
                    text = reminderText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Divider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 1.dp,
            modifier = Modifier.padding(start = 32.dp)
        )
    }
}

// Optimized Shopping List Item Card with performance improvements
@Composable
fun ShoppingListItemCard(
    item: EnhancedShoppingListItem,
    onTogglePurchased: () -> Unit,
    onMoveToInventory: () -> Unit,
    viewModel: ShoppingListViewModel
) {
    // Memoize expensive calculations
    val containerColor = if (item.isPurchased) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (item.isPurchased) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    // Use the enhanced formatter from the ViewModel
    val formattedQuantity = remember(item) {
        viewModel.formatItemQuantity(item)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(width = 1.dp, color = borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTogglePurchased() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = item.isPurchased,
                onCheckedChange = { onTogglePurchased() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
            
            // Name and quantity
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null,
                    color = if (item.isPurchased) 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                
                // Display formatted quantity
                Text(
                    text = formattedQuantity,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // "Have It" button for unpurchased items
            if (!item.isPurchased) {
                TextButton(
                    onClick = onMoveToInventory,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Have It")
                }
            }
        }
    }
}

// Optimized In-House Item Card
@Composable
fun InHouseItemCard(
    item: ShoppingListItem,
    onMoveToNeeded: () -> Unit,
    viewModel: ShoppingListViewModel
) {
    // We need to convert to EnhancedShoppingListItem for formatting
    val enhancedItem = EnhancedShoppingListItem(
        ingredientId = item.ingredientId,
        name = item.name,
        quantity = item.quantity,
        unit = item.unit,
        isPurchased = item.isPurchased,
        isInInventory = item.isInInventory,
        shoppingDayIndex = 0 // Default to first shopping day
    )
    
    // Use the enhanced formatter from the ViewModel
    val formattedQuantity = remember(item) {
        viewModel.formatItemQuantity(enhancedItem)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Display formatted quantity
                Text(
                    text = formattedQuantity,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(
                onClick = onMoveToNeeded,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Need It")
            }
        }
    }
}

@Composable
private fun EmptyShoppingListMessage(
    onCreateShoppingListClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            Text(
                text = "No items in shopping list",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Add items from your meal plan or create custom items",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FilledTonalButton(
                onClick = onCreateShoppingListClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Shopping List")
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun FABMenu(
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    hasUnpurchasedItems: Boolean,
    hasItems: Boolean,
    onCompleteShoppingDay: () -> Unit,
    onDeleteList: () -> Unit,
    onAddCustomItem: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Complete shopping list option
            if (hasUnpurchasedItems) {
                AnimatedFAB(
                    visible = isExpanded,
                    onClick = onCompleteShoppingDay,
                    icon = Icons.Rounded.Done,
                    contentDescription = "Complete shopping day",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            // Delete shopping list option
            if (hasItems) {
                AnimatedFAB(
                    visible = isExpanded,
                    onClick = onDeleteList,
                    icon = Icons.Rounded.Delete,
                    contentDescription = "Delete shopping list",
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            // Add custom item option
            AnimatedFAB(
                visible = isExpanded,
                onClick = onAddCustomItem,
                icon = Icons.Rounded.ShoppingCart,
                contentDescription = "Add custom item",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )

            // Main FAB
            MainFAB(
                isExpanded = isExpanded,
                onClick = onToggleExpanded
            )
        }
    }
}

@Composable
private fun AnimatedFAB(
    visible: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0.8f) + fadeIn(),
        exit = scaleOut(targetScale = 0.8f) + fadeOut()
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.padding(bottom = 12.dp),
            containerColor = containerColor,
            contentColor = contentColor
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
}

@Composable
private fun MainFAB(
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabRotation"
    )

    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = if (isExpanded) "Close menu" else "Open menu",
            modifier = Modifier.rotate(rotation)
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Shopping List") },
        text = { Text("Are you sure you want to delete this shopping list?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper function for formatting quantity - moved outside composable for better performance
private fun Double.formatQuantity(): String {
    return if (this % 1 == 0.0) this.toInt().toString()
    else String.format("%.2f", this).trimEnd('0').trimEnd('.')
}

// Extension function to format the date range of a shopping plan
@RequiresApi(Build.VERSION_CODES.O)
private fun ShoppingPlan.formatDateRange(): String {
    return DateUtils.formatDateRange(this.planStartDate, this.planEndDate)
}