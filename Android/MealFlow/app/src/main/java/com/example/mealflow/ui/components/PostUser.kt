package com.example.mealflow.ui.components

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.mealflow.R
import com.example.mealflow.data.model.Post
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.data.model.Author
import com.example.mealflow.network.CommentApiService
import com.example.mealflow.network.UserProfile
import com.example.mealflow.ui.components.post.navigateToPostDetails
import com.example.mealflow.viewModel.InteractionsViewModel
import com.example.mealflow.viewModel.PostDropdownViewModel
import com.example.mealflow.viewModel.UpdatePostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun PostUser(
    post: Post,
    userProfile: UserProfile,
    isAuthor: Boolean,
    refresh: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    navController: NavController,
    commentApiService: CommentApiService,
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current

    // Format the post creation time
    val formattedDate = remember(post.createdAt) {
        formatTimeAgo(post.createdAt)
    }
    // State to show the dropdown menu
    var expanded by remember { mutableStateOf(false) }

    val viewModel: PostDropdownViewModel = viewModel()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // User information and time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable(onClick = {navigateToPostDetails(navController, post.id)}),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // User profile and time
                UserProfileInfo(userProfile, formattedDate)
            }
//            // User information and time
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 12.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Box(
//                        modifier = Modifier
//                            .size(48.dp)
//                            .clip(CircleShape)
//                            .border(
//                                width = 2.dp,
//                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
//                                shape = CircleShape
//                            )
//                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
//                    ) {
//                        Image(
//                            painter = rememberAsyncImagePainter(
//                                model = userProfile.profilePicture ?: "https://photobasement.com/wp-content/uploads/2017/04/this-is-a-photo.jpg",
//                                error = painterResource(R.drawable.apple_logo_icon)
//                            ),
//                            contentDescription = "User Avatar",
//                            modifier = Modifier
//                                .size(45.dp)
//                                .clip(CircleShape) // Make sure the image itself is also circular
//                                .align(Alignment.Center),
//                            contentScale = ContentScale.Crop // Already set to Crop, which is good
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.width(12.dp))
//
//                    Column {
//                        Text(
//                            text = userProfile.name ?: "user",
//                            style = MaterialTheme.typography.titleMedium.copy(
//                                fontWeight = FontWeight.SemiBold
//                            )
//                        )
//                        Text(
//                            text = formattedDate,
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//
////                if(isAuthor)
////                {
////                    Row(
////                        horizontalArrangement = Arrangement.spacedBy(8.dp),
////                        verticalAlignment = Alignment.CenterVertically
////                    ) {
////                        IconButton(
////                            onClick = {
////                                expanded = !expanded
////                            },
////                            modifier = Modifier
////                                .size(36.dp)
////                                .clip(CircleShape)
////                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
////                        ) {
////                            Icon(
////                                imageVector = Icons.Rounded.MoreVert,
////                                contentDescription = "More Options",
////                                modifier = Modifier.size(20.dp),
////                                tint = MaterialTheme.colorScheme.onSurfaceVariant
////                            )
////                        }
////
////                        // Dropdown menu for more options
////                        DropdownMenu(
////                            expanded = expanded,
////                            onDismissRequest = { expanded = false }
////                        ) {
////                            DropdownMenuItem(
////                                text = { Text("Edit Post") },
////                                onClick = {
////                                    expanded = false
////                                }
////                            )
////                            DropdownMenuItem(
////                                text = {
////                                    Text("Delete Post")
////                                    if (viewModel.isDeleting) {
////                                        CircularProgressIndicator()
////                                    }
////                                },
////                                onClick = {
////                                    expanded = false
////                                    viewModel.deletePost(
////                                        postId =  post.id,
////                                        refresh = {refresh()}
////                                    )
////                                },
////                                enabled = !viewModel.isDeleting
////                            )
////                        }
////                    }
////                }
//            }

            // Post content
            PostContent(post)

            PostDivider()

            PostInteractions(
                post,
                commentApiService =  commentApiService,
                onCommentClick = onCommentClick,
                onShareClick = onShareClick,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostUser1(
    post: Post,
    userProfile: UserProfile,
    refresh: () -> Unit = {},
    viewModel: PostDropdownViewModel,
    viewModelUpdate: UpdatePostViewModel,
    commentApiService: CommentApiService,
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    context: Context,
    navController: NavController
) {
    val interactionSource = remember { MutableInteractionSource() }

    val showOptionsSheet = remember { mutableStateOf(false) }
    val showEditSheet = remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf(false) }
    val formattedDate = remember(post.createdAt) { formatTimeAgo(post.createdAt) }

    var isLiked by remember { mutableStateOf(post.hasLiked) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User information and time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable(onClick = {navigateToPostDetails(navController, post.id)}),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // User profile and time
                UserProfileInfo(userProfile, formattedDate)

                // Options button
                OptionsButton(onClick = { showOptionsSheet.value = true })
            }

            // Post content
            PostContent(post)

            // Divider
            PostDivider()
            var myId by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                myId = UserPreferencesManager(context).getMyId()
            }

            PostInteractions(
                post,
                commentApiService =  commentApiService,
                onCommentClick = onCommentClick,
                onShareClick = onShareClick,
                navController = navController

            )
        }
    }

    // Edit bottom sheet
    if (showEditSheet.value) {
        EditPostBottomSheet(
            post = post,
            onDismiss = { showEditSheet.value = false },
            onSubmit = { updatedTitle, updatedContent, updatedFlair, isHidden, allowComments, mediaUri, mediaType ->
                // Update the view model with all the new data
                viewModelUpdate.title = updatedTitle
                viewModelUpdate.content = updatedContent
                viewModelUpdate.flair = updatedFlair
                viewModelUpdate.mediaUri = mediaUri
                viewModelUpdate.mediaType = mediaType

                viewModelUpdate.updatePost(
                    context = context,
                    postId = post.id,
                    refresh = {refresh()}
                )

                viewModelUpdate.resetState()
                showEditSheet.value = false
            }
        )
    }

    // Post options sheet
    if (showOptionsSheet.value) {
        PostOptionsSheet(
            post = post,
            showOptionsSheet = showOptionsSheet,
            showEditSheet = showEditSheet,
            viewModel = viewModel,
            navController = navController,
            refresh,
            onDismiss = { showOptionsSheet.value = false }
        )
    }
}

@Composable
fun UserProfileInfo(userProfile: UserProfile, formattedDate: String) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                    shape = CircleShape
                )
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(userProfile.profilePicture ?: "https://photobasement.com/wp-content/uploads/2017/04/this-is-a-photo.jpg")
                    .crossfade(true)
                    .error(R.drawable.apple_logo_icon)
                    .build(),
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = userProfile.name ?: "user",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OptionsButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Icon(
            imageVector = Icons.Rounded.MoreVert,
            contentDescription = "More Options",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InteractionCounts(likesCount: Int, commentsCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$likesCount Likes", // Replace with actual likes count when available
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "0 Comments", // Replace with actual comments count when available
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostOptionsSheet(
    post: Post,
    showOptionsSheet: MutableState<Boolean>,
    showEditSheet: MutableState<Boolean>,
    viewModel: PostDropdownViewModel,
    navController: NavController,
    refresh: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Edit option
            BottomSheetOption(
                icon = Icons.Rounded.Edit,
                title = "Edit Post",
                onClick = {
                    onDismiss()
                    showEditSheet.value = true
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            BottomSheetOption(
                icon = Icons.Rounded.Delete,
                title = "Delete Post",
                onClick = {
                    onDismiss()
                    viewModel.deletePost(
                        postId =  post.id,
                        refresh = {refresh()}
                    )
                }
            )
        }
    }
}
@Composable
fun BottomSheetOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(title)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostBottomSheet(
    post: Post,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Boolean, Boolean, Uri?, String?) -> Unit
) {
    var title by remember { mutableStateOf(post.title) }
    var content by remember { mutableStateOf(post.content) }
    var flair by remember { mutableStateOf(post.flair?.name ?: "") }
    var isHidden by remember { mutableStateOf(post.isHidden) }
    var allowComments by remember { mutableStateOf(post.allowComments) }

    var isMediaRemoved by remember { mutableStateOf(false) }


    // Media handling
    var mediaUri by remember { mutableStateOf<Uri?>(null) }
    var mediaType by remember { mutableStateOf(post.mediaType) }
    var showMediaOptions by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            mediaUri = it
            // Try to determine if it's an image or video
            context.contentResolver.getType(it)?.let { mimeType ->
                mediaType = when {
                    mimeType.startsWith("image/") -> "IMAGE"
                    mimeType.startsWith("video/") -> "VIDEO"
                    else -> "NONE"
                }
            }
        }
    }

    var showTagDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Edit Post", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showTagDialog = true }
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
                    text = if (flair.isBlank()) "Add tags & flair (required)" else flair,
                    color = if (flair.isBlank()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Media section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showMediaOptions = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Media",
                    tint = Color.Gray
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = when {
                        mediaUri != null -> "New media selected"
                        post.mediaUrl != null -> "Current ${post.mediaType?.lowercase()?.capitalize() ?: ""} (click to change)"
                        else -> "Add photo or video"
                    },
                    color = if (mediaUri == null && post.mediaUrl == null) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Display preview of current or new media
            if ((mediaUri != null || post.mediaUrl != null) && !isMediaRemoved) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        mediaUri != null -> {
                            // Show newly selected media
                            if (mediaType == "IMAGE") {
                                AsyncImage(
                                    model = mediaUri,
                                    contentDescription = "Selected Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (mediaType == "VIDEO") {
                                // Video thumbnail or icon
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.VideoLibrary,
                                        contentDescription = "Video",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Video selected")
                                }
                            }
                        }
                        post.mediaUrl != null -> {
                            // Show existing media
                            if (post.mediaType == "IMAGE") {
                                AsyncImage(
                                    model = post.mediaUrl,
                                    contentDescription = "Post Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (post.mediaType == "VIDEO") {
                                // Video thumbnail or icon
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.VideoLibrary,
                                        contentDescription = "Video",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Current video")
                                }
                            }
                        }
                    }

                    // Add remove button overlay
                    if (mediaUri != null || post.mediaUrl != null) {
                        IconButton(
                            onClick = {
                                mediaUri = null
                                if (post.mediaUrl != null) {
                                    mediaType = "NONE"  // Mark to remove existing media
                                }
                                isMediaRemoved = true
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(36.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove media",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Additional options
            Text("Post Settings", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = allowComments,
                    onCheckedChange = { allowComments = it }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Allow Comments")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isHidden,
                    onCheckedChange = { isHidden = it }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Hidden Post")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSubmit(title, content, flair, isHidden, allowComments, mediaUri, mediaType)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save")
            }
        }
    }

    // Media options dialog
    if (showMediaOptions) {
        Dialog(onDismissRequest = { showMediaOptions = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Add Media",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            launcher.launch("image/*")
                            showMediaOptions = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Photo")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            launcher.launch("video/*")
                            showMediaOptions = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Video")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { showMediaOptions = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    if (showTagDialog) {
        Dialog(onDismissRequest = { showTagDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Tag",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val availableTags = listOf(
                        "Review", "Must-Try!", "Delicious!", "Not Recommended",
                        "Cooking Success", "Cooking Fail", "Question", "Discussion",
                        "Meal Prep", "Food Photography", "Kitchen Hack / Tip"
                    )

                    availableTags.forEach { tag ->
                        val isSelected = flair == tag
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    flair = if (isSelected) "" else tag
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(tag)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTagDialog = false }) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { showTagDialog = false }
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}
// Helper function to format the time as "X time ago"
fun formatTimeAgo(dateString: String): String {
    try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        val date = format.parse(dateString) ?: return "Unknown time"
        val now = Date()
        val diff = now.time - date.time

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} minutes ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            diff < 2592000000 -> "${diff / 86400000} days ago"
            diff < 31536000000 -> "${diff / 2592000000} months ago"
            else -> "${diff / 31536000000} years ago"
        }
    } catch (e: Exception) {
        return "Unknown time : $e"
    }
}