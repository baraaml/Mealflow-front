package com.example.mealflow.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mealflow.ui.screens.createCommunity.getCategories
import com.example.mealflow.viewModel.SingleCommunityViewModel
import com.example.mealflow.viewModel.UpdateCommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCommunityPage(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    context: Context,
    communityId: String? = null
) {
    val updateViewModel = viewModel<UpdateCommunityViewModel>()
    val singleCommunityViewModel = viewModel<SingleCommunityViewModel>()

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    // Initialize categories in viewmodel
    LaunchedEffect(Unit) {
        updateViewModel.initializeCategories()
    }

    // Fetch community data when the screen loads
    LaunchedEffect(communityId) {
        if (communityId != null) {
            isLoading = true
            errorMessage = null
            try {
                singleCommunityViewModel.loadSingleCommunity()
            } catch (e: Exception) {
                errorMessage = "Failed to load community: ${e.message}"
                isLoading = false
            }
        } else {
            // No community ID provided
            errorMessage = "No community ID provided"
            isLoading = false
        }
    }

    // Observe SingleCommunityViewModel state changes
    LaunchedEffect(
        singleCommunityViewModel.community,
        singleCommunityViewModel.isLoading,
        singleCommunityViewModel.errorMessage
    ) {
        // Update local loading state based on SingleCommunityViewModel
        isLoading = singleCommunityViewModel.isLoading

        // Handle successful community load
        singleCommunityViewModel.community?.let { community ->
            updateViewModel.initWithSingleCommunity(community)
            errorMessage = null // Clear any previous errors
        }

        // Handle error from SingleCommunityViewModel
        singleCommunityViewModel.errorMessage?.let { error ->
            errorMessage = error
            isLoading = false
        }
    }

    // Setup image picker
    val communityImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        updateViewModel.setCommunityImage(uri)
    }

    // Effect to show error messages from UpdateViewModel
    LaunchedEffect(updateViewModel.errorMessage) {
        updateViewModel.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            updateViewModel.clearErrorMessage()
        }
    }

    // Effect to show success messages
    LaunchedEffect(updateViewModel.successMessage) {
        updateViewModel.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            updateViewModel.clearSuccessMessage()
        }
    }

    // Category Selection Dialog
    if (showCategoryDialog) {
        CategorySelectionDialog(
            availableCategories = updateViewModel.availableCategories,
            selectedCategories = updateViewModel.selectedCategories,
            onCategoriesSelected = { selected ->
                updateViewModel.updateSelectedCategories(selected)
            },
            onDismiss = { showCategoryDialog = false }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Show loading overlay when either loading community data or updating
        if (isLoading || updateViewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4E79E3))
            }
        }

        // Show error message if there's an error loading community data
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.Center),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            errorMessage = null
                            if (communityId != null) {
                                isLoading = true
                                singleCommunityViewModel.loadSingleCommunity()
                            }
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }
            return@Box // Don't show the rest of the UI if there's an error
        }

        // Only show the main content if we have community data and no errors
        if (!isLoading && updateViewModel.singleCommunity != null) {
            // Content area with scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }

                    Text(
                        text = "Update Community",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    )

                    // Save button with integrated loading indicator
                    Button(
                        onClick = {
                            updateViewModel.updateCommunity(context, navController)
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4E79E3),
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        ),
                        enabled = !updateViewModel.isLoading && updateViewModel.canEditCommunity()
                    ) {
                        if (updateViewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "Save")
                        }
                    }
                }

                // Permission check message
                if (!updateViewModel.canEditCommunity()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "You don't have permission to edit this community. Only admins can make changes.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Community image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // Display current image or selected image
                    updateViewModel.imageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Community Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: updateViewModel.getCurrentImageUrl()?.let { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Community Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "No Image",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                    }

                    // Change image button (only if user can edit)
                    if (updateViewModel.canEditCommunity()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable { communityImageLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = "Change Community Image",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Delete selected image button
                        updateViewModel.imageUri?.let {
                            IconButton(
                                onClick = { updateViewModel.clearCommunityImage() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove image",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Community stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${updateViewModel.getMemberCount()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Members",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${updateViewModel.getPostsCount()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Posts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Form fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Community name
                    OutlinedTextField(
                        value = updateViewModel.name,
                        onValueChange = { updateViewModel.name = it },
                        label = { Text("Community Name") },
                        enabled = updateViewModel.canEditCommunity(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    OutlinedTextField(
                        value = updateViewModel.description,
                        onValueChange = { updateViewModel.description = it },
                        label = { Text("Description") },
                        enabled = updateViewModel.canEditCommunity(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(16.dp))

//                    // Categories Section - Replace the text input with category selection
//                    Text(
//                        text = "Categories",
//                        style = MaterialTheme.typography.labelMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Selected categories display using LazyRow correctly
//                    LazyRow(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        items(count = updateViewModel.selectedCategories.size) { index ->
//                            val category = updateViewModel.selectedCategories[index]
//                            AssistChip(
//                                onClick = {
//                                    if (updateViewModel.canEditCommunity()) {
//                                        updateViewModel.removeCategory(category)
//                                    }
//                                },
//                                label = { Text(text = category) },
//                                trailingIcon = if (updateViewModel.canEditCommunity()) {
//                                    {
//                                        Icon(
//                                            imageVector = Icons.Default.Close,
//                                            contentDescription = "Remove category",
//                                            modifier = Modifier.size(16.dp)
//                                        )
//                                    }
//                                } else null,
//                                enabled = updateViewModel.canEditCommunity()
//                            )
//                        }
//                    }

//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Add categories button
//                    if (updateViewModel.canEditCommunity()) {
//                        OutlinedButton(
//                            onClick = { showCategoryDialog = true },
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Add,
//                                contentDescription = "Add categories",
//                                modifier = Modifier.size(18.dp)
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text("Select Categories")
//                        }
//                    }
                }

                // Add extra padding at the bottom
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CategorySelectionDialog(
    availableCategories: Map<String, List<String>>,
    selectedCategories: List<String>,
    onCategoriesSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedCategories by remember { mutableStateOf(selectedCategories.toSet()) }
    val maxCategories = 3

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Select Categories")
                Text(
                    text = "${tempSelectedCategories.size}/$maxCategories selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (tempSelectedCategories.size >= maxCategories)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp)
            ) {
                availableCategories.forEach { (groupName, categories) ->
                    item {
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(count = categories.size) { index ->
                        val category = categories[index]
                        val isSelected = tempSelectedCategories.contains(category)
                        val canSelect = isSelected || tempSelectedCategories.size < maxCategories

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = canSelect) {
                                    if (canSelect) {
                                        tempSelectedCategories = if (isSelected) {
                                            tempSelectedCategories - category
                                        } else {
                                            tempSelectedCategories + category
                                        }
                                    }
                                }
                                .padding(vertical = 4.dp)
                                .alpha(if (canSelect) 1f else 0.5f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                enabled = canSelect,
                                onCheckedChange = { isChecked ->
                                    if (canSelect) {
                                        tempSelectedCategories = if (isChecked) {
                                            tempSelectedCategories + category
                                        } else {
                                            tempSelectedCategories - category
                                        }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category,
                                color = if (canSelect)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCategoriesSelected(tempSelectedCategories.toList())
                    onDismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
