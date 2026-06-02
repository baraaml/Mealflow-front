package com.example.mealflow.ui.screens.setup

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mealflow.utils.AvatarSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPhotosScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    context: Context,
    viewModel: SetupProfileViewModel = viewModel()
) {
    val darkBackground = Color(0xFF121212)
    val blueText = Color(0xFF4E79E3)
    val darkGray = Color(0xFF1E1E1E)
    val lightGray = Color(0xFF9E9E9E)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Progress Bar
            SetupTopBar(
                currentStep = 3,
                totalSteps = 3,
                onBackClick = { navController.popBackStack() },
                onSkipClick = { viewModel.skipSetupPhotosScreen(navController) }
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Text(
                    text = "Add Photos & Bio",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Personalize your profile with photos and a bio",
                    color = lightGray,
                    style = TextStyle(fontSize = 16.sp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Cover Picture Section
                Text(
                    text = "Cover Photo",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(darkGray)
                        .clickable { coverPictureLauncher.launch("image/*") }
                ) {
                    viewModel.coverPictureUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Cover Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = "Add Cover Photo",
                                tint = lightGray,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add Cover Photo",
                                color = lightGray,
                                style = TextStyle(fontSize = 14.sp)
                            )
                        }
                    }

                    // Remove button if image is selected
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

                Spacer(modifier = Modifier.height(24.dp))

                // Profile Picture Section
                Text(
                    text = "Profile Photo",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile picture
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(darkGray)
                            .clickable { profilePictureLauncher.launch("image/*") }
                    ) {
                        viewModel.profilePictureUri?.let { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } ?: run {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Add Profile Photo",
                                tint = lightGray,
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.Center)
                            )
                        }

                        // Remove button if image is selected
                        viewModel.profilePictureUri?.let {
                            IconButton(
                                onClick = { viewModel.clearProfilePicture() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
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

                    Column {
                        Button(
                            onClick = { profilePictureLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = blueText
                            ),
                            border = BorderStroke(1.dp, blueText),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Choose Photo")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Avatar selector
                        AvatarSelector(
                            context = context,
                            onAvatarSelected = { uri ->
                                viewModel.setProfilePicture(uri)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bio Section
                Text(
                    text = "Bio",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                BasicTextField(
                    value = viewModel.bio,
                    onValueChange = { viewModel.bio = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(darkGray)
                        .padding(16.dp),
                    decorationBox = { innerTextField ->
                        if (viewModel.bio.isEmpty()) {
                            Text(
                                text = "Tell us about yourself...",
                                color = lightGray,
                                style = TextStyle(fontSize = 16.sp)
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Complete Setup Button
                Button(
                    onClick = {
                        viewModel.photoInfoSetup(context, navController)
                              },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = blueText,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Complete Setup",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skip Button
                TextButton(
                    onClick = { viewModel.skipSetupPhotosScreen(navController) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading
                ) {
                    Text(
                        text = "Skip for now",
                        color = lightGray,
                        style = TextStyle(fontSize = 16.sp)
                    )
                }
            }
        }

        // Full screen loading overlay
        if (viewModel.isLoading) {
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