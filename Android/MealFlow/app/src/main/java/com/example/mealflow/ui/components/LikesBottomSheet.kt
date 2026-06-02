package com.example.mealflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.PostLike
import com.example.mealflow.viewModel.PostLikesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikesBottomSheet(
    postId: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    navController: NavController,
    viewModel: PostLikesViewModel // Accept ViewModel as parameter
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Remove the viewModel creation since it's passed as parameter
    // val viewModel: PostLikesViewModel = viewModel()

    // Remove the LaunchedEffect for setPostId since it's already set in PostInteractions
    // LaunchedEffect(postId) {
    //     viewModel.setPostId(postId)
    // }

    val likes = viewModel.postLikesFlow.collectAsLazyPagingItems()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorState by viewModel.errorState.collectAsState()

    if (isVisible) {
        val bottomSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        LaunchedEffect(isVisible) {
            if (isVisible) {
                bottomSheetState.show()
            } else {
                bottomSheetState.hide()
            }
        }

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
            modifier = Modifier.fillMaxSize()
        ) {
            LikesSheetContent(
                likes = likes,
                isLoading = isLoading,
                errorState = errorState,
                onDismiss = onDismiss,
                onRefresh = { viewModel.refresh() },
                onUserClick = { userId ->
                    coroutineScope.launch {
                        UserPreferencesManager(context).saveUserId(userId)
                        val myId = UserPreferencesManager(context).getMyId()
                        val isMyProfile = (myId == userId)

                        if (isMyProfile) {
                            navController.navigate("Profile Page")
                        } else {
                            navController.navigate("User Page")
                        }
                        onDismiss()
                    }
                },
                onClearError = { viewModel.clearError() }
            )
        }
    }
}

@Composable
private fun LikesSheetContent(
    likes: LazyPagingItems<PostLike>,
    isLoading: Boolean,
    errorState: String?,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onUserClick: (String) -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp, max = 600.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Likes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error handling
        errorState?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        onClearError()
                        onRefresh()
                    }) {
                        Text("Retry")
                    }
                }
            }
        }

        // Content
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(likes.itemCount) { index ->
                likes[index]?.let { like ->
                    LikeUserItem(
                        like = like,
                        onClick = { onUserClick(like.user.id) }
                    )
                }
            }

            // Loading states
            when (likes.loadState.refresh) {
                is LoadState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is LoadState.Error -> {
                    item {
                        ErrorRetryItem(
                            message = "Failed to load likes",
                            onRetry = { likes.retry() }
                        )
                    }
                }
                else -> {}
            }

            when (likes.loadState.append) {
                is LoadState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                    }
                }
                is LoadState.Error -> {
                    item {
                        ErrorRetryItem(
                            message = "Failed to load more",
                            onRetry = { likes.retry() }
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun LikeUserItem(
    like: PostLike,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // State management for user identification
    var myId by remember { mutableStateOf<String?>(null) }
    var likeUserId by remember { mutableStateOf<String?>(null) }
    var isMyLike by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        myId = UserPreferencesManager(context).getMyId()
        likeUserId = like.user.id
        isMyLike = (myId == likeUserId)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(like.user.profilePicture)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // User Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildString {
                        like.user.name?.let { append(it) }
                        like.user.lastName?.let {
                            if (isNotEmpty()) append(" ")
                            append(it)
                        }
                        if (isEmpty()) append(like.user.username)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Show indicator if it's user's own like
                if (isMyLike == true) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(You)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = "@${like.user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Like timestamp
        Text(
            text = formatTimeAgo(like.createdAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorRetryItem(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Retry")
            }
        }
    }
}