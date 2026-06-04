package com.example.mealflow.ui.components.post

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mealflow.navigation.Destination
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mealflow.R
import com.example.mealflow.data.model.Post
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.CommentApiService
import com.example.mealflow.ui.components.*
import com.example.mealflow.viewModel.PostDropdownViewModel
import com.example.mealflow.viewModel.UpdatePostViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCommunityDetails(
    post: Post,
    navController: NavController,
    viewModel: PostDropdownViewModel,
    viewModelUpdate: UpdatePostViewModel,
    commentApiService: CommentApiService,
    refresh: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val showOptionsSheet = rememberSaveable { mutableStateOf(false) }
    val showEditSheet = rememberSaveable { mutableStateOf(false) }
    val formattedDate = rememberSaveable(post.createdAt) { formatTimeAgo(post.createdAt) }

    var myId by remember { mutableStateOf<String?>(null) }
    var isMyPost by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        myId = UserPreferencesManager(context).getMyId()
        isMyPost = (myId == post.author.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Community and Author Header
            CommunityPostHeader(
                post = post,
                formattedDate = formattedDate,
                isMyPost = isMyPost,
                navController = navController,
                onOptionsClick = { showOptionsSheet.value = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
            PostContent(post)

            // Divider
            PostDivider()

            // Post interactions
            PostInteractions(
                post = post,
                commentApiService = commentApiService,
                onCommentClick = { },
                onShareClick = { },
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
                viewModelUpdate.title = updatedTitle
                viewModelUpdate.content = updatedContent
                viewModelUpdate.flair = updatedFlair
                viewModelUpdate.mediaUri = mediaUri
                viewModelUpdate.mediaType = mediaType

                viewModelUpdate.updatePost(
                    context = context,
                    postId = post.id,
                    refresh = { refresh() }
                )

                viewModelUpdate.resetState()
                showEditSheet.value = false
            }
        )
    }

    // Post options sheet
    if (showOptionsSheet.value && isMyPost) {
        PostOptionsSheet(
            post = post,
            showOptionsSheet = showOptionsSheet,
            showEditSheet = showEditSheet,
            viewModel = viewModel,
            navController = navController,
            refresh = refresh,
            onDismiss = { showOptionsSheet.value = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostProfileDetails(
    post: Post,
    navController: NavController,
    viewModel: PostDropdownViewModel,
    viewModelUpdate: UpdatePostViewModel,
    commentApiService: CommentApiService,
    refresh: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val showOptionsSheet = rememberSaveable { mutableStateOf(false) }
    val showEditSheet = rememberSaveable { mutableStateOf(false) }
    val formattedDate = rememberSaveable(post.createdAt) { formatTimeAgo(post.createdAt) }

    var myId by remember { mutableStateOf<String?>(null) }
    var isMyPost by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        myId = UserPreferencesManager(context).getMyId()
        isMyPost = (myId == post.author.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Profile Post Header
            ProfilePostHeader(
                post = post,
                formattedDate = formattedDate,
                isMyPost = isMyPost,
                navController = navController,
                onOptionsClick = { showOptionsSheet.value = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
            PostContent(post)

            // Divider
            PostDivider()

            // Post interactions
            PostInteractions(
                post = post,
                commentApiService = commentApiService,
                onCommentClick = { },
                onShareClick = { },
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
                viewModelUpdate.title = updatedTitle
                viewModelUpdate.content = updatedContent
                viewModelUpdate.flair = updatedFlair
                viewModelUpdate.mediaUri = mediaUri
                viewModelUpdate.mediaType = mediaType

                viewModelUpdate.updatePost(
                    context = context,
                    postId = post.id,
                    refresh = { refresh() }
                )

                viewModelUpdate.resetState()
                showEditSheet.value = false
            }
        )
    }

    // Post options sheet
    if (showOptionsSheet.value && isMyPost) {
        PostOptionsSheet(
            post = post,
            showOptionsSheet = showOptionsSheet,
            showEditSheet = showEditSheet,
            viewModel = viewModel,
            navController = navController,
            refresh = refresh,
            onDismiss = { showOptionsSheet.value = false }
        )
    }
}

@Composable
fun CommunityPostHeader(
    post: Post,
    formattedDate: String,
    isMyPost: Boolean,
    navController: NavController,
    onOptionsClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Community name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    // Navigate to community page
                    post.communityId?.let { communityId ->
                        coroutineScope.launch {
                            UserPreferencesManager(context).saveCommunityId(communityId)
                            navController.navigate(Destination.CommunityPage)
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Community",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.community?.name ?: "Community",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Author and time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        UserPreferencesManager(context).saveUserId(post.author.id)
                        if (isMyPost) {
                            navController.navigate(Destination.Profile)
                        } else {
                            navController.navigate(Destination.User)
                        }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(post.author.profilePicture?:"https://via.placeholder.com/150")
                            .crossfade(true)
                            .error(R.drawable.apple_logo_icon)
                            .build(),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = post.author.name.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (isMyPost) {
            OptionsButton(onClick = onOptionsClick)
        }
    }
}

@Composable
fun ProfilePostHeader(
    post: Post,
    formattedDate: String,
    isMyPost: Boolean,
    navController: NavController,
    onOptionsClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    coroutineScope.launch {
                        UserPreferencesManager(context).saveUserId(post.author.id)
                        if (isMyPost) {
                            navController.navigate(Destination.Profile)
                        } else {
                            navController.navigate(Destination.User)
                        }
                    }
                }
        ) {
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
                        .data(post.author.profilePicture?:"https://via.placeholder.com/150")
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
                    text = post.author.name.toString() +" " + post.author.lastName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isMyPost) {
            OptionsButton(onClick = onOptionsClick)
        }
    }
}

fun navigateToPostDetails(navController: NavController, postId: String) {
    navController.navigate(Destination.PostDetails(postId))
}