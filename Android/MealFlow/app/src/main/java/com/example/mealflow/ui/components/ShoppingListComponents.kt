package com.example.mealflow.ui.components

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mealflow.data.model.GeneratedShoppingList
import com.example.mealflow.data.model.ShoppingListItem
import com.example.mealflow.data.model.ShoppingSettings
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import kotlin.collections.forEach

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isLoading: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val buttonModifier = Modifier
        .fillMaxWidth()
        .height(48.dp)

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = buttonModifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            // Better contrast for the button
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            // More distinct disabled states
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        )
    ) {
        ButtonContent(text = text, icon = icon, isLoading = isLoading)
    }
}
@Composable
fun ButtonContent(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isLoading: Boolean) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = LocalContentColor.current, // Adapts to button's content color
            strokeWidth = 2.5.dp
        )
    } else {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Medium)
    }
}


@Composable
fun LoadingStateIndicator(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun IngredientReviewSectionHeader() {
    Column(modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)) {
        Text(
            "Review Ingredients",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Check items you already have in your inventory.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Divider(modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun InventoryCheckItemRow(item: ShoppingListItem, onCheckChange: () -> Unit) {
    val cardBackground = when {
        item.isInInventory -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }

    val cardBorder = when {
        item.isInInventory -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onCheckChange),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = cardBorder
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Checkbox(
                checked = item.isInInventory,
                onCheckedChange = { onCheckChange() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isInInventory) TextDecoration.LineThrough else null,
                    color = if (item.isInInventory)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.quantity.formatQuantity()} ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CurrentShoppingListHeader(list: GeneratedShoppingList) {
    Column(modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(
                Icons.Filled.ShoppingCartCheckout,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Your Shopping List",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        InfoRow(icon = Icons.Outlined.Info, text = "Created: ${formatDate(list.generationDate)}")
        Spacer(Modifier.height(4.dp))
        InfoRow(icon = Icons.Outlined.CalendarMonth, text = "Covers: ${formatDate(list.mealPlanStartDate)} to ${formatDate(list.mealPlanEndDate)}")

        val purchasedCount = list.items.count { it.isPurchased }
        val totalCount = list.items.size
        val progress = if (totalCount > 0) purchasedCount.toFloat() / totalCount.toFloat() else 0f

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)), // Or CircleShape
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "$purchasedCount / $totalCount items",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Divider(modifier = Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun EmptyStateMessage(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(56.dp)
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ListSectionHeader(title: String, count: Int, isSecondary: Boolean = false) {
    val headerColor = when {
        isSecondary -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    ) {
        Divider(
            modifier = Modifier
                .weight(0.15f)
                .padding(end = 8.dp),
            color = headerColor.copy(alpha = 0.5f)
        )
        Text(
            "$title ($count)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = headerColor
        )
        Divider(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            color = headerColor.copy(alpha = 0.5f)
        )
    }
}
@Composable
fun ShoppingListItemRow(
    item: ShoppingListItem,
    onCheckChange: () -> Unit
) {
    val rowBackground = when {
        item.isPurchased -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        item.isPurchased -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onCheckChange)
            .background(rowBackground)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Checkbox(
            checked = item.isPurchased,
            onCheckedChange = { onCheckChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.quantity.formatQuantity()} ${item.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PastListsSection(
    pastLists: List<GeneratedShoppingList>,
    isLoading: Boolean,
    onViewListClicked: (GeneratedShoppingList) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = pastLists.isNotEmpty() || isLoading) { if (pastLists.isNotEmpty()) expanded = !expanded }
                    .padding(bottom = if (expanded && pastLists.isNotEmpty()) 8.dp else 0.dp), // Add padding only if expanding
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Past Shopping Lists",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                if (pastLists.isNotEmpty()) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            if (isLoading && pastLists.isEmpty()) {
                LoadingStateIndicator("Loading past lists...")
            } else if (pastLists.isEmpty() && !isLoading) {
                Text(
                    "No past shopping lists available.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded && pastLists.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pastLists.forEach { list ->
                        PastShoppingListItemCard(list = list, onClick = { onViewListClicked(list) })
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PastShoppingListItemCard(list: GeneratedShoppingList, onClick: () -> Unit) {
    OutlinedCard( // Using OutlinedCard for items within the section
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "List from: ${formatDate(list.generationDate)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Covers: ${formatDate(list.mealPlanStartDate)} to ${formatDate(list.mealPlanEndDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${list.items.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward, // Or Icons.Filled.ChevronRight
                contentDescription = "View list",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


// formatDate and formatQuantity helpers remain the same
@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(dateString: String?): String {
    return try {
        dateString?.takeIf { it.isNotBlank() }?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        } ?: "N/A"
    } catch (e: Exception) {
        "Invalid Date"
    }
}

@SuppressLint("DefaultLocale")
fun Double.formatQuantity(): String {
    return if (this == this.toInt().toDouble()) {
        this.toInt().toString()
    } else {
        // Ensure it doesn't show .0 for integers, but shows .5 for halves etc.
        val formatted = String.format("%.2f", this)
        if (formatted.endsWith(".00")) {
            formatted.substring(0, formatted.length - 3)
        } else if (formatted.endsWith("0") && formatted.contains(".")) {
            formatted.substring(0, formatted.length - 1)
        } else {
            formatted
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShoppingSettingsCard(
    settings: ShoppingSettings,
    isLoading: Boolean,
    onNextDateChanged: (LocalDate?) -> Unit,
    onCoverageChanged: (String) -> Unit,
    onPrepareClicked: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val currentNextDate = settings.nextShoppingDate?.let { LocalDate.parse(it) }
    val calendar = Calendar.getInstance()
    currentNextDate?.let { calendar.set(it.year, it.monthValue - 1, it.dayOfMonth) }

    val datePickerDialog = remember { // remember the dialog to avoid recreation
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                onNextDateChanged(LocalDate.of(year, month + 1, dayOfMonth))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000 // Ensure minDate is today
        }
    }


    Card( // Use standard Card for less emphasis than ElevatedCard unless it's a primary action area
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), // Consistent corner rounding
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Shopping Setup",
                style = MaterialTheme.typography.titleMedium, // Title Medium is good for card headers
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            InfoRow(
                icon = Icons.Outlined.EventRepeat,
                text = "Last Trip: ${settings.lastShoppingDate?.let { formatDate(it) } ?: "Not recorded"}"
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = currentNextDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "",
                onValueChange = { /* Read Only */ },
                label = { Text("Next Shopping Date") },
                placeholder = { Text("Tap to select date") },
                leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = if (settings.shoppingCoverageDays == 0) "" else settings.shoppingCoverageDays.toString(),
                onValueChange = { onCoverageChanged(it) },
                label = { Text("Shopping Coverage (days)") },
                placeholder = { Text("e.g., 7") },
                leadingIcon = { Icon(Icons.Outlined.DateRange, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(Modifier.height(20.dp))

            PrimaryActionButton(
                text = "Prepare Ingredients List",
                icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                isLoading = isLoading,
                enabled = !isLoading && settings.nextShoppingDate != null && settings.shoppingCoverageDays > 0,
                onClick = onPrepareClicked
            )
        }
    }
}

