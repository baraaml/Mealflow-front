// File: app/src/main/java/com/example/mealflow/ui/screens/AddCustomItemScreen.kt
// MODIFY existing file
package com.example.mealflow.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mealflow.ui.components.SegmentedButton
import com.example.mealflow.viewModel.ShoppingListViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomItemScreen(
    viewModel: ShoppingListViewModel,
    onAddClickAndNavigateBack: () -> Unit,
    onCancelClick: () -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
    val itemToEdit = viewModel.uiState.collectAsState().value.customItemToEdit
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("unit") }
    var selectedType by remember { mutableStateOf("I Need") }

    // Input validation states
    var isNameError by remember { mutableStateOf(false) }
    var isQuantityError by remember { mutableStateOf(false) }
    var isUnitError by remember { mutableStateOf(false) }
    
    // Animation states
    val cardElevation by animateDpAsState(
        targetValue = if (isNameError || isQuantityError || isUnitError) 8.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cardElevation"
    )
    
    // Update local state when itemToEdit changes
    LaunchedEffect(itemToEdit) {
        if (itemToEdit != null) {
            name = itemToEdit.name
            quantity = itemToEdit.quantity.toString()
            unit = itemToEdit.unit
            selectedType = if (itemToEdit.isPurchased) "Purchased" else "I Need"
        } else {
            name = ""
            quantity = "1"
            unit = "unit"
            selectedType = "I Need"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (itemToEdit != null) "Edit Item" else "Add Custom Item",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptic.lightClick()
                            viewModel.prepareToAddCustomItem()
                        onCancelClick()
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
            Text(
                        text = if (itemToEdit != null) "Edit Your Item" else "Add a New Item",
                style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (itemToEdit != null) 
                            "Update the details of your shopping item" 
                        else 
                            "Enter the details of the item you want to add to your shopping list",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Item Name
            OutlinedTextField(
                value = name,
                        onValueChange = { 
                            name = it
                            isNameError = false
                        },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                        isError = isNameError,
                        supportingText = {
                            if (isNameError) {
                                Text("Please enter an item name")
                            }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Label,
                                contentDescription = null,
                                tint = if (isNameError) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorLabelColor = MaterialTheme.colorScheme.error,
                            errorCursorColor = MaterialTheme.colorScheme.error,
                            errorSupportingTextColor = MaterialTheme.colorScheme.error,
                            errorLeadingIconColor = MaterialTheme.colorScheme.error
                        )
                    )

                    // Quantity and Unit Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                OutlinedTextField(
                    value = quantity,
                            onValueChange = { 
                                if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                    quantity = it
                                    isQuantityError = false
                                }
                            },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                            isError = isQuantityError,
                            supportingText = {
                                if (isQuantityError) {
                                    Text("Please enter a valid number")
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Numbers,
                                    contentDescription = null,
                                    tint = if (isQuantityError) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                )
                            },
                    keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                errorLabelColor = MaterialTheme.colorScheme.error,
                                errorCursorColor = MaterialTheme.colorScheme.error,
                                errorSupportingTextColor = MaterialTheme.colorScheme.error,
                                errorLeadingIconColor = MaterialTheme.colorScheme.error
                            )
                        )

                OutlinedTextField(
                    value = unit,
                            onValueChange = { 
                                unit = it
                                isUnitError = false
                            },
                    label = { Text("Unit (e.g., kg, pcs)") },
                    modifier = Modifier.weight(1f),
                            isError = isUnitError,
                            supportingText = {
                                if (isUnitError) {
                                    Text("Please enter a unit")
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Scale,
                                    contentDescription = null,
                                    tint = if (isUnitError) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                )
                            },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                errorLabelColor = MaterialTheme.colorScheme.error,
                                errorCursorColor = MaterialTheme.colorScheme.error,
                                errorSupportingTextColor = MaterialTheme.colorScheme.error,
                                errorLeadingIconColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }

                    // Status Selection
                    Column {
                        Text(
                            "Status",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
            SegmentedButton(
                options = listOf("I Need", "Purchased"),
                selectedOption = selectedType,
                            onOptionSelected = { 
                                haptic.lightClick()
                                selectedType = it 
                            },
                            modifier = Modifier.fillMaxWidth()
            )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.lightClick()
                        viewModel.prepareToAddCustomItem()
                        onCancelClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        // Validate inputs
                        isNameError = name.isBlank()
                        isQuantityError = quantity.toDoubleOrNull() == null
                        isUnitError = unit.isBlank()

                        if (!isNameError && !isQuantityError && !isUnitError) {
                            haptic.success()
                        val qtyDouble = quantity.toDoubleOrNull() ?: 1.0
                        val isPurchased = selectedType == "Purchased"

                        viewModel.addOrUpdateCustomItem(
                            name = name,
                            quantity = qtyDouble,
                            unit = unit,
                            isPurchased = isPurchased,
                            isInInventory = selectedType == "Purchased"
                        )
                            focusManager.clearFocus()
                        onAddClickAndNavigateBack()
                        } else {
                            haptic.error()
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        if (itemToEdit != null) "Update Item" else "Add Item",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// Helper class for haptic feedback
private class HapticFeedback {
    fun lightClick() {
        // Implement haptic feedback for light clicks
    }
    
    fun success() {
        // Implement haptic feedback for success
    }
    
    fun error() {
        // Implement haptic feedback for errors
    }
}
        // Implement haptic feedback for errors
