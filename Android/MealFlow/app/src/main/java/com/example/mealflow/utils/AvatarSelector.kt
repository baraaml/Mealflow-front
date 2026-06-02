package com.example.mealflow.utils

// Existing imports
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.mealflow.R
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarSelector(
    context: Context,
    onAvatarSelected: (Uri) -> Unit
) {
    val avatarResources = listOf(
        R.drawable.avatar,
        R.drawable.avatar1,
        R.drawable.avatar2,
        R.drawable.avatar3,
        R.drawable.avatar4,
        R.drawable.avatar5,
        R.drawable.avatar6,
        R.drawable.avatar7,
        R.drawable.avatar8,
        R.drawable.avatar9,
        R.drawable.avatar10,
        R.drawable.avatar11,
        )

    var showDialog by remember { mutableStateOf(false) }

    // Button to open avatar selector
    Button(
        onClick = { showDialog = true },
        modifier = Modifier
            .padding(top = 8.dp)
            .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4E79E3),
            contentColor = Color.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Avatar",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Choose Avatar")
        }
    }

    // Avatar selection dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Avatar") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(avatarResources) { resId ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .border(2.dp, Color(0xFF4E79E3), CircleShape)
                                .clickable {
                                    val uri = drawableToUri(context, resId)
                                    uri?.let {
                                        onAvatarSelected(it)
                                        showDialog = false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = "Avatar option",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Converts a drawable resource to a content URI
 */
fun drawableToUri(context: Context, drawableResId: Int): Uri? {
    return try {
        val file = drawableToFile(context, drawableResId)
        file?.let { FileProvider.getUriForFile(context, "${context.packageName}.provider", it) }
    } catch (e: Exception) {
        Log.e("DrawableToUri", "Error converting drawable to URI: ${e.localizedMessage}")
        null
    }
}

/**
 * Converts a drawable resource to a File stored in the app's cache directory
 */
fun drawableToFile(context: Context, drawableResId: Int, fileName: String = "avatar_${drawableResId}.jpg"): File? {
    return try {
        val drawable = ContextCompat.getDrawable(context, drawableResId)
        val bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> {
                // Convert any drawable type to bitmap
                val width = drawable?.intrinsicWidth ?: 200
                val height = drawable?.intrinsicHeight ?: 200
                val bitmap = createBitmap(width, height)
                val canvas = Canvas(bitmap)
                drawable?.setBounds(0, 0, canvas.width, canvas.height)
                drawable?.draw(canvas)
                bitmap
            }
        }

        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()
        file
    } catch (e: Exception) {
        Log.e("DrawableToFile", "Error converting drawable to file: ${e.localizedMessage}")
        null
    }
}