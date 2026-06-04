package com.example.mealflow.ui.components.post

// Required imports for Share Bottom Sheet
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Facebook
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mealflow.R
import com.example.mealflow.data.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    post: Post,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
            modifier = Modifier.fillMaxWidth()
        ) {
            ShareBottomSheetContent(
                post = post,
                onDismiss = onDismiss,
                context = context,
                navController = navController
            )
        }
    }
}

@Composable
fun ShareBottomSheetContent(
    post: Post,
    onDismiss: () -> Unit,
    context: Context,
    navController: NavController
) {
    var shareData by remember { mutableStateOf<SharePostData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Load share links when the component is first displayed
    LaunchedEffect(post.id) {
        sharePostApi( // استخدام sharePostApi بدلاً من sharePostApiWithFallback
            postId = post.id,
            context = context,
            onSuccess = { data ->
                shareData = data
                isLoading = false
                error = null
            },
            onError = { errorMessage ->
                error = errorMessage
                isLoading = false
                shareData = null // تأكد من إعادة تعيين shareData إلى null عند الخطأ
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Share Post",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        // Post Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Author info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    AsyncImage(
                        model = post.author.profilePicture, // تأكد من استخدام الحقل الصحيح
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.avatar11)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = post.author.name.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Post content preview
                Text(
                    text = post.content.take(100) + if (post.content.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Loading, Error, or Share Options
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Generating share links...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Failed to load share links",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                isLoading = true
                                error = null
                                sharePostApi( // استخدام sharePostApi في إعادة المحاولة أيضاً
                                    postId = post.id,
                                    context = context,
                                    onSuccess = { data ->
                                        shareData = data
                                        isLoading = false
                                        error = null
                                    },
                                    onError = { errorMessage ->
                                        error = errorMessage
                                        isLoading = false
                                        shareData = null
                                    }
                                )
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            shareData != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Copy Link
                    item {
                        ShareOptionItem(
                            icon = Icons.Outlined.ContentCopy,
                            title = "Copy Link",
                            subtitle = "Copy post link to clipboard",
                            onClick = {
                                copyToClipboard(context, shareData!!.deepLink, "Link copied to clipboard")
                                onDismiss()
                            }
                        )
                    }

                    // Share via WhatsApp
                    item {
                        ShareOptionItem(
                            icon = Icons.AutoMirrored.Outlined.Message,
                            title = "WhatsApp",
                            subtitle = "Share via WhatsApp",
                            onClick = {
                                openUrl(context, shareData!!.shareLinks.whatsapp)
                                onDismiss()
                            }
                        )
                    }

                    // Share via Telegram
                    item {
                        ShareOptionItem(
                            icon = Icons.AutoMirrored.Outlined.Send,
                            title = "Telegram",
                            subtitle = "Share via Telegram",
                            onClick = {
                                openUrl(context, shareData!!.shareLinks.telegram)
                                onDismiss()
                            }
                        )
                    }

                    // Share via Twitter
                    item {
                        ShareOptionItem(
                            icon = Icons.Outlined.Share,
                            title = "Twitter",
                            subtitle = "Share via Twitter",
                            onClick = {
                                openUrl(context, shareData!!.shareLinks.twitter)
                                onDismiss()
                            }
                        )
                    }

                    // Share via Facebook
                    item {
                        ShareOptionItem(
                            icon = Icons.Outlined.Facebook,
                            title = "Facebook",
                            subtitle = "Share via Facebook",
                            onClick = {
                                openUrl(context, shareData!!.shareLinks.facebook)
                                onDismiss()
                            }
                        )
                    }

                    // Share via Email
                    item {
                        ShareOptionItem(
                            icon = Icons.Outlined.Email,
                            title = "Email",
                            subtitle = "Share via Email",
                            onClick = {
                                openUrl(context, shareData!!.shareLinks.email)
                                onDismiss()
                            }
                        )
                    }

                    // Share via Discord
                    item {
                        ShareOptionItem(
                            icon = Icons.AutoMirrored.Outlined.Chat,
                            title = "Discord",
                            subtitle = "Share via Discord",
                            onClick = {
                                openUrl(context, shareData!!.shareLinks.discord)
                                onDismiss()
                            }
                        )
                    }

                    // More Options (System Share)
                    item {
                        ShareOptionItem(
                            icon = Icons.Outlined.MoreHoriz,
                            title = "More Options",
                            subtitle = "See all sharing options",
                            onClick = {
                                shareViaSystemShare(context, shareData!!.deepLink)
                                onDismiss()
                            }
                        )
                    }

                    // Report Post (if needed)
                    item {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        ShareOptionItem(
                            icon = Icons.Outlined.Report,
                            title = "Report Post",
                            subtitle = "Report inappropriate content",
                            onClick = {
                                // Navigate to report screen or show report dialog
                                // navController.navigate("report_post/${post.id}")
                                onDismiss()
                            },
                            textColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
@Composable
fun ShareOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = textColor
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.7f)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Arrow",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

// Helper functions for sharing
private fun copyToClipboard(context: Context, text: String, message: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Shared Content", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
    }
}

private fun shareViaSystemShare(context: Context, shareLink: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareLink)
        type = "text/plain"
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Share Post"))
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to share", Toast.LENGTH_SHORT).show()
    }
}