package com.example.mealflow.ui.screens

import android.content.Context
import android.widget.MediaController
import android.widget.VideoView
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.mealflow.viewModel.CreatePostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreationPage(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    context: Context,
    communityId: String? = null
) {
    val darkBackground = Color(0xFF121212)
    val blueText = Color(0xFF4E79E3)
    val darkGray = Color(0xFF1E1E1E)

    val viewModel = viewModel<CreatePostViewModel>()

    var showTagsDialog by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // State to track if media is an image or video
    var isImage by remember { mutableStateOf<Boolean?>(null) }

    // Setup image and video pickers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.setMedia(uri, "IMAGE")
        isImage = true
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.setMedia(uri, "VIDEO")
        isImage = false
    }

    // Effect to show error messages
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Content area
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top bar with fixed loading button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                // Post button with integrated loading indicator
                Button(
                    onClick = {
                        viewModel.createPost(context, communityId, navController)
                    },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = blueText,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    enabled = !viewModel.isLoading && viewModel.title.isNotBlank() &&
                            viewModel.content.isNotBlank() && viewModel.flair.isNotBlank()
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Post")
                    }
                }
            }

            // Post content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .weight(1f)
            ) {
                // Title field
                Text(
                    text = "Title",
                    color = Color.White,
                    style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Title input field
                BasicTextField(
                    value = viewModel.title,
                    onValueChange = { viewModel.title = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(darkGray, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Add tags field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(darkGray)
                        .clickable { showTagsDialog = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = "Tags",
                        tint = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = viewModel.flair.ifBlank { "Add tags & flair (required)" },
                        color = if (viewModel.flair.isBlank()) Color.Gray else Color.White,
                        style = TextStyle(fontSize = 14.sp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Body text field
                Text(
                    text = "Body text (required)",
                    color = Color.White,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Body text input
                BasicTextField(
                    value = viewModel.content,
                    onValueChange = { viewModel.content = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(darkGray, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Show selected media
                viewModel.mediaUri?.let { uri ->
                    if (viewModel.mediaType == "IMAGE") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            // Delete button
                            IconButton(
                                onClick = { viewModel.clearMedia() },
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
                    } else if (viewModel.mediaType == "VIDEO") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            AndroidView(
                                factory = { context ->
                                    VideoView(context).apply {
                                        setVideoURI(uri)
                                        setMediaController(MediaController(context).apply {
                                            setAnchorView(this@apply)
                                        })
                                        start()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            // Delete button
                            IconButton(
                                onClick = { viewModel.clearMedia() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove video",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom action toolbar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = darkGray),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
//                    ActionIcon(Icons.Outlined.Link, "Format")
//                    {
//                        /*ToDo*/
//                    }
                    ActionIcon(Icons.Outlined.Image, "Image") {
                        imagePickerLauncher.launch("image/*")
                    }
                    ActionIcon(Icons.Outlined.VideoLibrary, "Video") {
                        videoPickerLauncher.launch("video/*")
                    }
//                    ActionIcon(Icons.AutoMirrored.Outlined.List, "List")
//                    {
//                        /*ToDo*/
//                    }
                    ActionIcon(Icons.Outlined.EmojiEmotions, "Emoji")
                    {
                        showEmojiPicker = true
                    }
                }
            }
        }

        // Tags Dialog
        if (showTagsDialog) {
            Dialog(onDismissRequest = { showTagsDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = darkBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Select Tag",
                            color = Color.White,
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val availableTags = listOf(
                            "Review", "Must-Try!", "Delicious!", "Not Recommended",
                            "Cooking Success", "Cooking Fail", "Question", "Discussion",
                            "Meal Prep", "Food Photography", "Kitchen Hack / Tip"
                        )

                        availableTags.forEach { tag ->
                            val isSelected = viewModel.flair == tag
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        viewModel.flair = if (isSelected) "" else tag
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = blueText,
                                        unselectedColor = Color.Gray
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = tag,
                                    color = Color.White,
                                    style = TextStyle(fontSize = 16.sp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showTagsDialog = false }) {
                                Text(
                                    text = "Cancel",
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { showTagsDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = blueText
                                )
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }
            }
        }
        // Emoji Picker Dialog
        if (showEmojiPicker) {
            Dialog(onDismissRequest = { showEmojiPicker = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = darkBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Select Emoji",
                            color = Color.White,
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Common emojis grid
                        val emojis = listOf(
                            "😀", "😂", "😍", "🥰", "😊",
                            "👍", "👎", "❤️", "🔥", "🎉",
                            "🍕", "🍔", "🍗", "🥗", "🍰",
                            "🍎", "🥑", "🍓", "🥦", "🧀"
                        )

                        Column {
                            for (i in emojis.indices step 5) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (j in 0 until 5) {
                                        if (i + j < emojis.size) {
                                            Text(
                                                text = emojis[i + j],
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clickable {
                                                        viewModel.content += emojis[i + j]
                                                        showEmojiPicker = false
                                                    }
                                                    .wrapContentSize(Alignment.Center),
                                                fontSize = 24.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showEmojiPicker = false }) {
                                Text(
                                    text = "Cancel",
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionIcon(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFF2A2A2A))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PostCreationScreenPreview() {
    PostCreationPage(
        navController = rememberNavController(),
        snackbarHostState = remember { SnackbarHostState() },
        context = LocalContext.current
    )
}