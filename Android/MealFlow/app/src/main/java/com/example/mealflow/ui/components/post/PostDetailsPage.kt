package com.example.mealflow.ui.components.post

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mealflow.data.model.Post
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.CommentApiService
import com.example.mealflow.network.SinglePostApiService
import com.example.mealflow.utils.CommentsViewModel
import com.example.mealflow.utils.CommentsViewModelFactory
import com.example.mealflow.utils.CommentInput
import com.example.mealflow.utils.CommentItem
import com.example.mealflow.utils.collectAsState
import com.example.mealflow.viewModel.PostDropdownViewModel
import com.example.mealflow.viewModel.UpdatePostViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(
    postId: String,
    navController: NavController,
    commentApiService: CommentApiService
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // ViewModels
    val viewModel: PostDropdownViewModel = viewModel()
    val viewModelUpdate: UpdatePostViewModel = viewModel()

    // Comments ViewModel
    val factory = remember { CommentsViewModelFactory(commentApiService) }
    val commentViewModel: CommentsViewModel = viewModel(factory = factory)

    // State
    var post by remember { mutableStateOf<Post?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<com.example.mealflow.network.CommentItem?>(null) }

    // Comments state
    val comments by commentViewModel.comments.collectAsState()
    val isCommentsLoading by commentViewModel.isLoading.collectAsState()
    val hasMore by commentViewModel.hasMore.collectAsState()
    val nextCursor by commentViewModel.nextCursor.collectAsState()

    // API Service
    val singlePostApiService = remember { SinglePostApiService(context) }

    // Get user ID
    LaunchedEffect(Unit) {
        userId = UserPreferencesManager(context).getMyId() ?: ""
    }

    // Load post data and comments
    LaunchedEffect(postId) {
        scope.launch {
            try {
                isLoading = true
                val response = singlePostApiService.fetchSinglePost(postId)
                if (response.success) {
                    post = response.data
                    // Load comments after post is loaded
                    commentViewModel.loadComments(postId)
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Error loading post: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Refresh function
    val refresh: () -> Unit = {
        scope.launch {
            try {
                val response = singlePostApiService.fetchSinglePost(postId)
                if (response.success) {
                    post = response.data
                    commentViewModel.loadComments(postId) // Refresh comments too
                }
            } catch (e: Exception) {
                // Handle error silently for refresh
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Post Details",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                when {
                    isLoading -> {
                        PostDetailsLoadingState()
                    }
                    errorMessage != null -> {
                        PostDetailsErrorState(
                            errorMessage = errorMessage!!,
                            onRetry = {
                                errorMessage = null
                                scope.launch {
                                    try {
                                        isLoading = true
                                        val response = singlePostApiService.fetchSinglePost(postId)
                                        if (response.success) {
                                            post = response.data
                                            commentViewModel.loadComments(postId)
                                        } else {
                                            errorMessage = response.message
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error loading post: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    }
                    post != null -> {
                        // Post content in a card with padding
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column {
                                // Determine if this is a community post or profile post
                                if (post!!.communityId != null) {
                                    // Community post
                                    PostCommunityDetails(
                                        post = post!!,
                                        navController = navController,
                                        viewModel = viewModel,
                                        viewModelUpdate = viewModelUpdate,
                                        commentApiService = commentApiService,
                                        refresh = refresh
                                    )
                                } else {
                                    // Profile post
                                    PostProfileDetails(
                                        post = post!!,
                                        navController = navController,
                                        viewModel = viewModel,
                                        viewModelUpdate = viewModelUpdate,
                                        commentApiService = commentApiService,
                                        refresh = refresh
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Comments Section (only show if post is loaded and allows comments)
            if (post != null && post!!.allowComments) {
                item {
                    // Comments Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${comments.size} Comments",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }

                // Comment Input
                item {
                    CommentInput(
                        replyingTo = replyingToComment,
                        onCancelReply = { replyingToComment = null },
                        onPostComment = { content ->
                            commentViewModel.postComment(
                                postId = postId,
                                content = content,
                                parentId = replyingToComment?.id
                            ) {
                                replyingToComment = null
                            }
                        }
                    )
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }

                // Comments List
                if (isCommentsLoading && comments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    // Organize comments into parent-child structure
                    val parentComments = comments.filter { it.parentId == null }
                    val childComments = comments.filter { it.parentId != null }
                        .groupBy { it.parentId }

                    items(parentComments) { comment ->
                        CommentItem(
                            comment = comment,
                            userId = userId,
                            onEdit = { commentId, newContent ->
                                commentViewModel.updateComment(commentId, newContent) {}
                            },
                            onDelete = { commentId ->
                                commentViewModel.deleteComment(commentId)
                            },
                            onReply = {
                                replyingToComment = comment
                                // Scroll to comment input
                                scope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            },
                            childComments = childComments[comment.id] ?: emptyList(),
                            navController = navController
                        )
                    }

                    // Load more comments button
                    if (hasMore) {
                        item {
                            if (isCommentsLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                TextButton(
                                    onClick = {
                                        commentViewModel.loadComments(postId, nextCursor)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Load more comments",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Empty state for comments
                if (comments.isEmpty() && !isCommentsLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No comments yet. Be the first to comment!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Add bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PostDetailsLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading post...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PostDetailsErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Error Loading Post",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
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