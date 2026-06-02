package com.example.mealflow.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mealflow.R
import com.example.mealflow.data.model.Post
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.CommentApiService
import com.example.mealflow.ui.components.post.ShareBottomSheet
import com.example.mealflow.ui.components.post.navigateToPostDetails
import com.example.mealflow.utils.CommentsBottomSheet
import com.example.mealflow.utils.ModernVideoPlayer
import com.example.mealflow.viewModel.InteractionsViewModel
import com.example.mealflow.viewModel.PostLikesViewModel

@Composable
fun PostContent(post: Post) {
    // Flair (tag)
    post.flair?.name?.let {
        AssistChip(
            onClick = { /* Handle tag click */ },
            label = { Text(text = it) },
            modifier = Modifier.padding(bottom = 8.dp),
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        )
    }
    Text(
        text = post.title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    Text(
        text = post.content,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (!post.mediaUrl.isNullOrEmpty()) {
        when (post.mediaType) {
            "IMAGE" -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    shadowElevation = 1.dp
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = post.mediaUrl,
                            error = painterResource(id = R.drawable.default_menu_image_placeholder)
                        ),
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            "VIDEO" -> {
                ModernVideoPlayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp)),
                    videoUrl = post.mediaUrl
                )
            }
        }
    }
}

@Composable
fun PostMetrics(post: Post) {
    // Interaction counter
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Since we don't have like count in the data structure, we can use viewCount
        Text(
            text = "${post.likeCount} Likes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // For comments count, we would ideally have a field in the Post class
        // For now, we're using a placeholder
        Text(
            text = if (post.allowComments) "36 Comments" else "Comments disabled",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Post metadata (e.g., edited status)
    if (post.isEdited) {
        Text(
            text = "Edited" + (post.lastEditedAt?.let { " on $it" } ?: ""),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun PostDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun InteractionButtons(
    post: Post,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    navController: NavController,
    commentApiService: CommentApiService,
    isLiked: Boolean = post.hasLiked // Use post's hasLiked directly as default
) {
    val viewModelInteraction: InteractionsViewModel = viewModel()
    var showCommentsSheet by remember { mutableStateOf(false) }
    var allowComments by remember { mutableStateOf(false) }
    allowComments = post.allowComments

    val context = LocalContext.current
    var userId by rememberSaveable { mutableStateOf<String?>(null) }
    // Fetch the user ID when the component is first created
    LaunchedEffect(Unit) {
        userId = UserPreferencesManager(context).getMyId()
    }
    Log.d("userId","$userId")

    // ⭐ Optimistic UI state for Like
    var isLikedLocal by remember { mutableStateOf(isLiked) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Like button
        TextButton(
            onClick = {
                // Optimistic UI update
                isLikedLocal = !isLikedLocal
                viewModelInteraction.toggleLike(post.id)
                      },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
            val size by animateDpAsState(if (isLikedLocal) 24.dp else 20.dp)

            val color by animateColorAsState(
                targetValue = if (isLikedLocal)
                    Color.Blue
                else
                    MaterialTheme.colorScheme.primary
            )

            val icon = if (isLikedLocal) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp

            Icon(
                imageVector = icon,
                contentDescription = "Like",
                modifier = Modifier.size(size), // Apply the animated size
                tint = color
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Like",
                style = MaterialTheme.typography.labelLarge
            )
        }
        // Comment button
        TextButton(
            onClick = {
                showCommentsSheet = true
                onCommentClick()
            },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Comment",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Comment",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Share button
        TextButton(
            onClick = onShareClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Share",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

    // Comments bottom sheet
    userId?.let {
        CommentsBottomSheet(
            postId = post.id,
            userId = "4b28a0c1-46b8-42f9-b2b6-063fe7b0f3ef",
            isVisible = showCommentsSheet,
            allowComments = allowComments,
            commentApiService = commentApiService,
            onDismiss = { showCommentsSheet = false },
            navController = navController
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun PostInteractions(
    post: Post,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    navController: NavController,
    commentApiService: CommentApiService
) {
    val viewModelInteraction: InteractionsViewModel = viewModel()
    val postLikesViewModel: PostLikesViewModel = viewModel()
    val context = LocalContext.current

    var showCommentsSheet by remember { mutableStateOf(false) }
    var showLikesSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var userId by rememberSaveable { mutableStateOf<String?>(null) }

    val likeCount = rememberSaveable { mutableIntStateOf(post.likeCount) }
    val isLikedLocal = rememberSaveable { mutableStateOf(post.hasLiked) }

    // Fetch user ID once
    LaunchedEffect(Unit) {
        userId = UserPreferencesManager(context).getMyId()
    }

    Column {
        // --- Metrics Row (Likes + Comments + Edited) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Updated Likes text with click functionality
            Text(
                text = "${likeCount.intValue} Likes",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLikedLocal.value)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable {
                    if (likeCount.intValue > 0) { // Only show if there are likes
                        postLikesViewModel.setPostId(post.id) // Set post ID in ViewModel
                        showLikesSheet = true
                    }
                }
            )

            Text(
                text = if (post.allowComments) "${post.commentCount} Comments" else "Comments disabled",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(onClick =
                    {
                        showCommentsSheet = true
                        onCommentClick()
                    }
                )
            )
        }

        if (post.isEdited) {
            Text(
                text = "Edited" + (post.lastEditedAt?.let { " on $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- Buttons Row (Like, Comment, Share) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Like
            TextButton(
                onClick = {
                    isLikedLocal.value = !isLikedLocal.value
                    likeCount.intValue += if (isLikedLocal.value) 1 else -1
                    viewModelInteraction.toggleLike(post.id)
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                val size by animateDpAsState(if (isLikedLocal.value) 24.dp else 20.dp)
                val color by animateColorAsState(
                    if (isLikedLocal.value)
                        Color.Blue
                    else
                        MaterialTheme.colorScheme.primary
                )
                val icon = if (isLikedLocal.value) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp

                Icon(
                    imageVector = icon,
                    contentDescription = "Like",
                    modifier = Modifier.size(size),
                    tint = color
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Like",
                    style = MaterialTheme.typography.labelLarge,
                    color = color
                )
            }

            // Comment
            TextButton(
                onClick = {
                    showCommentsSheet = true
                    onCommentClick()
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Comment",
                    style = MaterialTheme.typography.labelLarge
                )
            }

//            // Share
//            TextButton(
//                onClick = onShareClick,
//                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Outlined.Share,
//                    contentDescription = "Share",
//                    modifier = Modifier.size(20.dp),
//                    tint = MaterialTheme.colorScheme.primary
//                )
//                Spacer(modifier = Modifier.width(6.dp))
//                Text(
//                    text = "Share",
//                    style = MaterialTheme.typography.labelLarge
//                )
//            // Share - تعديل onClick لإظهار Share Bottom Sheet
//            TextButton(
//                onClick = {
//                    showShareSheet = true
//                    onShareClick()
//                },
//                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Outlined.Share,
//                    contentDescription = "Share",
//                    modifier = Modifier.size(20.dp),
//                    tint = MaterialTheme.colorScheme.primary
//                )
//                Spacer(modifier = Modifier.width(6.dp))
//                Text(
//                    text = "Share",
//                    style = MaterialTheme.typography.labelLarge
//                )
//            }
        }

        // Comments Bottom Sheet
        userId?.let {
            CommentsBottomSheet(
                postId = post.id,
                userId = it,
                isVisible = showCommentsSheet,
                allowComments = post.allowComments,
                commentApiService = commentApiService,
                onDismiss = { showCommentsSheet = false },
                navController = navController
            )
        }

        // Likes Bottom Sheet - Pass ViewModel directly
        LikesBottomSheet(
            postId = post.id,
            isVisible = showLikesSheet,
            onDismiss = { showLikesSheet = false },
            navController = navController,
            viewModel = postLikesViewModel // Pass the ViewModel
        )

        // Share Bottom Sheet - إضافة Share Bottom Sheet
        ShareBottomSheet(
            post = post,
            isVisible = showShareSheet,
            onDismiss = { showShareSheet = false },
            navController = navController
        )
    }
}