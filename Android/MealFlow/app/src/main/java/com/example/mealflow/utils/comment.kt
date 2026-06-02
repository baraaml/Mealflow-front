package com.example.mealflow.utils

import android.content.Context
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mealflow.R
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.CommentApiService
import com.example.mealflow.network.CommentItem
import com.example.mealflow.network.toggleLikeComment
import com.example.mealflow.ui.components.formatTimeAgo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentsViewModel(private val commentApiService: CommentApiService) : ViewModel() {
    private val _comments = MutableStateFlow<List<CommentItem>>(emptyList())
    val comments: StateFlow<List<CommentItem>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _nextCursor = MutableStateFlow<String?>(null)
    val nextCursor: StateFlow<String?> = _nextCursor.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    fun loadComments(postId: String, cursor: String? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            val response = commentApiService.getPostComments(postId, cursor = cursor)
            if (response != null) {
                if (cursor == null) {
                    _comments.value = response.data.comments
                } else {
                    _comments.value = _comments.value + response.data.comments
                }
                _nextCursor.value = response.data.pagination.nextCursor
                _hasMore.value = response.data.pagination.hasMore
            }
            _isLoading.value = false
        }
    }

    fun postComment(postId: String, content: String, parentId: String? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val response = commentApiService.createComment(postId, content, parentId)
            if (response != null && response.success) {
                // Reload comments to see the new comment
                loadComments(postId)
                onSuccess()
            }
        }
    }

    fun updateComment(commentId: String, newContent: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val response = commentApiService.updateComment(commentId, newContent)
            if (response != null && response.success) {
                // Update the comment in the list
                _comments.value = _comments.value.map {
                    if (it.id == commentId) {
                        it.copy(content = newContent, updatedAt = response.data.updatedAt)
                    } else {
                        it
                    }
                }
                onSuccess()
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            val success = commentApiService.deleteComment(commentId)
            if (success) {
                // Remove the comment from the list
                _comments.value = _comments.value.filter { it.id != commentId }
            }
        }
    }
    fun toggleCommentLike(context: Context, commentId: String) {
        viewModelScope.launch {
            // Optimistic update before API call
            val currentComments = _comments.value
            val commentIndex = currentComments.indexOfFirst { it.id == commentId }

            if (commentIndex != -1) {
                val comment = currentComments[commentIndex]
                val optimisticUpdate = comment.copy(
                    hasLiked = !comment.hasLiked,
                    likeCount = if (comment.hasLiked)
                        maxOf(0, comment.likeCount - 1) // Prevent negative like count
                    else
                        comment.likeCount + 1
                )

                val updatedList = currentComments.toMutableList().apply {
                    set(commentIndex, optimisticUpdate)
                }
                _comments.value = updatedList

                // Then make the actual API call
                try {
                    val response = toggleLikeComment(context, commentId)
                    response?.let { result ->
                        if (!result.success) {
                            // Revert to original state if API call fails
                            _comments.value = currentComments
                        }
                    } ?: run {
                        // Revert if response is null
                        _comments.value = currentComments
                    }
                } catch (e: Exception) {
                    // Revert on exception
                    _comments.value = currentComments
                    // Could show error message if needed
                    // Toast.makeText(context, "Failed to update like status", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// Helper extension function to get StateFlow as State in Compose
@Composable
fun <T> StateFlow<T>.collectAsState(): androidx.compose.runtime.State<T> {
    val state = remember { mutableStateOf(value) }
    LaunchedEffect(this) {
        collect { state.value = it }
    }
    return state
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    postId: String,
    userId: String,
    isVisible: Boolean,
    allowComments: Boolean,
    commentApiService: CommentApiService,
    onDismiss: () -> Unit,
    navController: NavController
) {
    // Create a factory for the ViewModel
    val factory = remember { CommentsViewModelFactory(commentApiService) }
    // Get ViewModel using Factory
    val commentViewModel: CommentsViewModel = viewModel(factory = factory)
    // Create bottom sheet state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val comments by commentViewModel.comments.collectAsState()
    val isLoading by commentViewModel.isLoading.collectAsState()
    val hasMore by commentViewModel.hasMore.collectAsState()
    val nextCursor by commentViewModel.nextCursor.collectAsState()

    var isFirstLoad by remember { mutableStateOf(true) }

    // State for tracking which comment we're replying to
    var replyingToComment by remember { mutableStateOf<CommentItem?>(null) }

    LaunchedEffect(Unit) {
        delay(2000L)
        isFirstLoad = false
    }

    // Load comments when the sheet becomes visible
    LaunchedEffect(postId, isVisible) {
        if (isVisible) {
            commentViewModel.loadComments(postId)
        }
    }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = Modifier
                .fillMaxWidth(),
            dragHandle = {
                // Add a handle to make it more visible
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp) // Add padding at the bottom to avoid cutoff
            ) {
                // Header with comment count and close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${comments.size} comments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "close"
                        )
                    }
                }

                HorizontalDivider()

                if (isFirstLoad) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Organize comments into parent-child structure
                    val parentComments = comments.filter { it.parentId == null }
                    val childComments = comments.filter { it.parentId != null }
                        .groupBy { it.parentId }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        // Show parent comments with their replies
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
                                },
                                // Pass child comments
                                childComments = childComments[comment.id] ?: emptyList(),
                                navController = navController
                            )
                        }

                        item {
                            if (isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else if (hasMore) {
                                TextButton(
                                    onClick = { commentViewModel.loadComments(postId, nextCursor) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text("Load more comments")
                                }
                            }
                        }
                    }
                }

                if(allowComments)
                {
                    // Comment input field with reply info
                    CommentInput(
                        replyingTo = replyingToComment,
                        onCancelReply = { replyingToComment = null },
                        onPostComment = { content ->
                            commentViewModel.postComment(
                                postId = postId,
                                content = content,
                                parentId = replyingToComment?.id
                            ) {
                                // Reset reply state after successful post
                                replyingToComment = null
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentItem(
    comment: CommentItem,
    userId: String,
    onEdit: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onReply: () -> Unit,
    childComments: List<CommentItem> = emptyList(),
    indentLevel: Int = 0,
    navController: NavController
) {
    val isCurrentUserAuthor = comment.authorId == userId
    var showOptions by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editedComment by remember { mutableStateOf(comment.content) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val viewModel: CommentsViewModel = viewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var myId by remember { mutableStateOf<String?>(null) }
    var postUserId by remember { mutableStateOf<String?>(null) }
    var isMyPost by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        myId = UserPreferencesManager(context).getMyId()
        postUserId = comment.author.id
        isMyPost = (myId == postUserId)
    }

    val imageUrl = comment.author.profilePicture?.takeIf { it.isNotBlank() }
        ?: R.drawable.profile_default

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Add indentation for replies
            if (indentLevel > 0) {
                Spacer(modifier = Modifier.width((indentLevel * 16).dp))
            }

            // User avatar
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "User image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(
                        onClick = {
                            coroutineScope.launch {
                                UserPreferencesManager(context).saveUserId(postUserId.toString())
                                if (isMyPost == true) {
                                    navController.navigate("Profile Page")
                                } else {
                                    navController.navigate("User Page")
                                }
                            }
                        }
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        // Author name
                        Text(
                            text = comment.author.name ?: "User",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (isEditing) {
                            // Edit mode
                            OutlinedTextField(
                                value = editedComment,
                                onValueChange = { editedComment = it },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        onEdit(comment.id, editedComment)
                                        isEditing = false
                                    }
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { isEditing = false }) {
                                    Text("Cancel")
                                }
                                TextButton(
                                    onClick = {
                                        onEdit(comment.id, editedComment)
                                        isEditing = false
                                    }
                                ) {
                                    Text("Save")
                                }
                            }
                        } else {
                            // Display comment
                            Text(
                                text = if (comment.isDeleted) "This comment has been deleted." else comment.content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Comment actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like button
                    LikeButton(
                        isLiked = comment.hasLiked,
                        likeCount = comment.likeCount,
                        onClick = { viewModel.toggleCommentLike(context, comment.id) }
                    )

//                    // Reply button - only show for non-deleted comments
//                    if (!comment.isDeleted) {
//                        TextButton(
//                            onClick = { onReply() }
//                        ) {
//                            Text(
//                                text = "Reply",
//                                style = MaterialTheme.typography.labelMedium,
//                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                            )
//                        }
//                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Time ago
                    Text(
                        text = formatTimeAgo(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    // Options menu for current user's comments
                    if (isCurrentUserAuthor && !comment.isDeleted) {
                        Box {
                            IconButton(
                                onClick = { showOptions = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            if (showOptions) {
                                ModalBottomSheet(
                                    onDismissRequest = { showOptions = false },
                                    sheetState = sheetState,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        // Edit Option
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    isEditing = true
                                                    showOptions = false
                                                }
                                                .padding(vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Edit", style = MaterialTheme.typography.bodyLarge)
                                        }

                                        HorizontalDivider()

                                        // Delete Option
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    showDeleteDialog = true
                                                    showOptions = false
                                                }
                                                .padding(vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Delete",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }

                            // Confirmation Dialog
                            if (showDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteDialog = false },
                                    title = { Text("Confirm Deletion") },
                                    text = { Text("Are you sure you want to delete this comment?") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            onDelete(comment.id)
                                            showDeleteDialog = false
                                        }) {
                                            Text("Delete", color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            showDeleteDialog = false
                                        }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
//                            if (showOptions) {
//                                ModalBottomSheet(
//                                    onDismissRequest = { showOptions = false },
//                                    sheetState = sheetState
//                                ) {
//                                    Column(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(16.dp)
//                                    ) {
//                                        TextButton(onClick = {
//                                            isEditing = true
//                                            showOptions = false
//                                        }) {
//                                            Text("Edit", style = MaterialTheme.typography.bodyLarge)
//                                        }
//
//                                        TextButton(onClick = {
//                                            onDelete(comment.id)
//                                            showOptions = false
//                                        }) {
//                                            Text("Delete", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
//                                        }
//                                    }
//                                }
//                            }
                        }
                    }
                }
            }
        }

        // Show like count if available
        if (comment.likeCount > 0) {
            Row(
                modifier = Modifier
                    .padding(start = 48.dp, top = 4.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ThumbUp,
                    contentDescription = "Likes",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = comment.likeCount.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Display child comments (replies) with increased indent level
        if (childComments.isNotEmpty()) {
            childComments.forEach { childComment ->
                CommentItem(
                    comment = childComment,
                    userId = userId,
                    onEdit = onEdit,
                    onDelete = onDelete,
                    onReply = onReply,
                    indentLevel = indentLevel + 1,
                    navController= navController
                )
            }
        }
    }
}

@Composable
fun CommentInput(
    replyingTo: CommentItem? = null,
    onCancelReply: () -> Unit = {},
    onPostComment: (String) -> Unit
) {
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        // Show reply info if replying to a comment
        if (replyingTo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Replying to ${replyingTo.author.name ?: "User"}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onCancelReply,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel reply",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Comment input field
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            placeholder = {
                Text(
                    if (replyingTo != null) "Write a reply..."
                    else "Write a comment..."
                )
            },
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (commentText.isNotBlank()) {
                        onPostComment(commentText)
                        commentText = ""
                        focusManager.clearFocus()
                    }
                }
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onPostComment(commentText)
                            commentText = ""
                            focusManager.clearFocus()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "send",
                        tint = if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        )
    }
}

/**
 * Factory to create CommentsViewModel with CommentApiService passed to it
 * */
class CommentsViewModelFactory(private val commentApiService: CommentApiService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentsViewModel::class.java)) {
            return CommentsViewModel(commentApiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun LikeButton(
    isLiked: Boolean,
    likeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use interactionSource without indication for custom animations
    val interactionSource = remember { MutableInteractionSource() }
    val transition = updateTransition(targetState = isLiked, label = "LikeButtonTransition")

    // Animate color based on liked state
    val iconColor by transition.animateColor(
        label = "IconColorAnimation",
        transitionSpec = { spring(stiffness = Spring.StiffnessLow) }
    ) { liked ->
        if (liked) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    // Animate scale for heart icon
    val scale by transition.animateFloat(
        label = "ScaleAnimation",
        transitionSpec = {
            if (targetState) {
                spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            } else {
                spring(stiffness = Spring.StiffnessLow)
            }
        }
    ) {
        if (it) 1.2f else 1f
    }

    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Using null instead of deprecated rememberRipple
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0f) // Transparent surface using alpha
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                contentDescription = if (isLiked) "Unlike" else "Like",
                tint = iconColor,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )

//            AnimatedVisibility(
//                visible = likeCount > 0,
//                enter = fadeIn() + scaleIn(),
//                exit = fadeOut() + scaleOut()
//            ) {
//                Text(
//                    text = likeCount.toString(),
//                    style = MaterialTheme.typography.labelMedium,
//                    color = iconColor
//                )
//            }
        }
    }
}