package com.example.mealflow.ui.screens

import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.mealflow.network.MyProfileApiService
import com.example.mealflow.network.UserProfile
import com.example.mealflow.utils.AvatarSelector
import com.example.mealflow.viewModel.ProfilePageViewModel
import com.example.mealflow.viewModel.UpdateProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

//----------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    context: Context,
) {
    val blueText = Color(0xFF4E79E3)

    val viewModel = viewModel<UpdateProfileViewModel>()
    val viewModelProfile = viewModel<ProfilePageViewModel>()
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    viewModel.isProfile = true

//    suspend fun fetchUserProfile() {
//        isLoading = true // Set the upload status to true before starting the process
//
//        // Your logic to fetch the user profile
//        val result = MyProfileApiService(context).fetchMyProfile()
//
//        // Handling the result
//        if (result.success) {
//            userProfile = result.data
//        } else {
//            errorMessage = result.message
//        }
//
//        isLoading = false // Set the upload status to false after the process is finished
//    }

    // Fetch user profile data when the screen loads
    LaunchedEffect(key1 = true) {
        isLoading = true
        val result = MyProfileApiService(context).fetchMyProfile()
        if (result.success) {
            userProfile = result.data
            // Initialize the viewModel with the fetched profile data
            userProfile?.let { profile ->
                viewModel.initWithProfile(profile)
            }
        } else {
            errorMessage = result.message
        }
        isLoading = false
    }

    // Setup image pickers
    val profilePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.setProfilePicture(uri)
    }

    val coverPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.setCoverPicture(uri)
    }

    // Effect to show error messages
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.errorMessage = null
        }
    }

    // Effect to show success messages
    LaunchedEffect(viewModel.successMessage) {
        viewModel.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.successMessage = null
        }
    }

    // Date picker setup
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
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
                    text = "Update Profile",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )

                // Save button with integrated loading indicator
                Button(
                    onClick = {
                        viewModel.updateProfile(context, navController)
                    },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = blueText,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
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

            // Cover picture
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                // Cover picture
                viewModel.coverPictureUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Cover Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: viewModel.userProfile?.let { profile ->
                    if (!profile.coverPicture.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(profile.coverPicture),
                            contentDescription = "Cover Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Change cover picture button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { coverPictureLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Change Cover Picture",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Delete cover picture button (if a new one is selected)
                viewModel.coverPictureUri?.let {
                    IconButton(
                        onClick = { viewModel.clearCoverPicture() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove cover picture",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Profile picture
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                // Profile picture
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                ) {
                    viewModel.profilePictureUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: viewModel.userProfile?.let { profile ->
                        if (!profile.profilePicture.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(profile.profilePicture),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Change profile picture button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(blueText)
                        .clickable { profilePictureLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Change Profile Picture",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Delete profile picture button (if a new one is selected)
                viewModel.profilePictureUri?.let {
                    IconButton(
                        onClick = { viewModel.clearProfilePicture() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 12.dp, y = (-8).dp)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove profile picture",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Avatar selector button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                AvatarSelector(
                    context = context,
                    onAvatarSelected = { uri ->
                        viewModel.setProfilePicture(uri)
                    }
                )
            }

            // Profile form fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Name field
                ProfileField(
                    label = "First Name",
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Last Name field
                ProfileField(
                    label = "Last Name",
                    value = viewModel.lastName,
                    onValueChange = { viewModel.lastName = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Birth Date field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showDatePicker = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Birth Date",
                        tint = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (viewModel.birthDate.isNotBlank()) {
                            // Format the date for display (assuming ISO format from backend)
                            try {
                                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                                val date = inputFormat.parse(viewModel.birthDate)
                                outputFormat.format(date ?: Date())
                            } catch (e: Exception) {
                                Log.d("UpdateProfileScreen","UpdateProfileScreen : $e")
                                viewModel.birthDate
                            }
                        } else "Select Birth Date",
                        style = TextStyle(fontSize = 16.sp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gender field
                val genderOptions = listOf("Male", "Female", "Other")
                Column {
                    Text(
                        text = "Gender",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        color = Color.White,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        genderOptions.forEach { option ->
                            val isSelected = viewModel.gender.equals(option, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.gender = option },
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = blueText,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Height field (numeric input)
                ProfileField(
                    label = "Height (cm)",
                    value = viewModel.height?.toString() ?: "",
                    onValueChange = { viewModel.setHeight(it) },
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Weight field (numeric input)
                ProfileField(
                    label = "Weight (kg)",
                    value = viewModel.weight?.toString() ?: "",
                    onValueChange = { viewModel.setWeight(it) },
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bio field (multi-line)
                Text(
                    text = "Bio",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .imePadding()
                ) {
                    OutlinedTextField(
                        value = viewModel.bio,
                        onValueChange = { viewModel.bio = it },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        placeholder = { Text("Enter your bio...") },
                        maxLines = 5,
                        singleLine = false
                    )
                }
            }

            // Add extra padding at the bottom for better scrolling
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Date picker dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = Date(millis)
                                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                format.timeZone = TimeZone.getTimeZone("UTC")
                                viewModel.birthDate = format.format(date)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDatePicker = false }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Full screen loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = blueText)
            }
        }
    }
}
@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
        },
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun UpdateProfileScreenPreview() {
    UpdateProfileScreen(
        navController = rememberNavController(),
        snackbarHostState = remember { SnackbarHostState() },
        context = LocalContext.current
    )
}