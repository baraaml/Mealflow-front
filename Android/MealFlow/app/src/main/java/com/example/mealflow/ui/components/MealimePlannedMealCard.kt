package com.example.mealflow.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.mealflow.R
import com.example.mealflow.data.model.Meal
import kotlinx.coroutines.delay

/**
 * A meal card component in Mealime style that also shows planned meal information.
 * Displays a square photo with the meal name below it, and indicators for meal type
 * (breakfast, lunch, dinner) and planned status.
 */
@Composable
fun MealimePlannedMealCard(
    meal: Meal,
    plannedMealInfo: PlannedMealInfo? = null,
    onClick: () -> Unit,
    animationDelay: Long = 0L,
    modifier: Modifier = Modifier
) {
    // Animation states
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay)
        visible = true
    }

    val enterTransition = remember {
        tween<Float>(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        )
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = enterTransition,
        label = "cardFade"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.92f,
        animationSpec = enterTransition,
        label = "cardScale"
    )

    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = enterTransition,
        label = "cardTranslate"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.translationY = translateY
            }
            .semantics { contentDescription = "Meal: ${meal.name}" }
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        // Square image container with rounded corners
        Box(modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f), // Perfect square aspect ratio
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                // Image with loading/error states
                SubcomposeAsyncImage(
                    model = meal.imageUrl,
                    contentDescription = "Image of ${meal.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth(),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.default_menu_image_placeholder),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                )
            }

            // Meal type badge (if planned)
            plannedMealInfo?.mealType?.let { mealType ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    EnhancedBadge(
                        text = mealType.replaceFirstChar { it.uppercase() },
                        backgroundColor = getMealTypeColor(mealType),
                        contentColor = Color.White,
                        icon = Icons.Filled.Restaurant,
                        smallSize = true
                    )
                }
            }

            // Time badge
            val totalTime = meal.preparationTime + meal.cookingTime
            if (totalTime > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    EnhancedBadge(
                        text = "$totalTime min",
                        backgroundColor = Color.Black.copy(alpha = 0.75f),
                        contentColor = Color.White,
                        icon = Icons.Default.Timer,
                        smallSize = true
                    )
                }
            }
            
            // Status indicator (cooked/planned)
            if (plannedMealInfo?.isCooked == true || plannedMealInfo?.isPlanned == true) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    CircularBadge(
                        icon = if (plannedMealInfo.isCooked) Icons.Default.Done else Icons.Default.Schedule,
                        backgroundColor = if (plannedMealInfo.isCooked) 
                            Color(0xFF4CAF50) 
                        else 
                            MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        size = 24.dp
                    )
                }
            }
        }

        // Meal name below the image
        Text(
            text = meal.name,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 2.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        
        // Status text (only if there's space and it's planned)
        plannedMealInfo?.let {
            if (it.isPlanned || it.isCooked) {
                val statusText = when {
                    it.isCooked -> "Cooked"
                    it.isPlanned -> "Planned"
                    else -> null
                }
                
                statusText?.let { text ->
                    Text(
                        text = text,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            it.isCooked -> Color(0xFF4CAF50)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Helper function to get color based on meal type
@Composable
private fun getMealTypeColor(mealType: String): Color {
    return when (mealType.lowercase()) {
        "breakfast" -> Color(0xFFFF9800) // Orange
        "lunch" -> Color(0xFF2196F3) // Blue
        "dinner" -> Color(0xFF9C27B0) // Purple
        "snack" -> Color(0xFF4CAF50) // Green
        else -> MaterialTheme.colorScheme.primary
    }
} 