package com.example.mealflow.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.mealflow.R
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.model.MealType
import kotlinx.coroutines.delay


@Composable
fun MealimePlanCard(
    meal: Meal,
    mealType: MealType,
    onCardClick: () -> Unit,
    onCookClick: () -> Unit,
    isCooked: Boolean = false,
    isPlanned: Boolean = false,
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
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = enterTransition,
        label = "cardFade"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = enterTransition,
        label = "cardScale"
    )

    // Get meal type color
    val typeColor = getMealTypeColor(mealType.title)
    
    // Status color
    val statusColor = when {
        isCooked -> Color(0xFF4CAF50) // Green
        isPlanned -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
            .semantics { contentDescription = "Meal: ${meal.name}" }
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCooked) 0.5.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCooked) 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f) 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top row with meal type and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Meal type indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when(mealType) {
                            MealType.BREAKFAST -> Icons.Filled.WbSunny
                            MealType.LUNCH -> Icons.Filled.LunchDining
                            MealType.DINNER -> Icons.Filled.DinnerDining
                            MealType.SNACK -> Icons.Filled.Icecream
                            MealType.OTHER -> Icons.Filled.MoreHoriz
                        },
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = mealType.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = typeColor
                    )
                }
                
                // Status indicator
                val statusText = when {
                    isCooked -> "Cooked"
                    isPlanned -> "Planned"
                    else -> null
                }
                
                statusText?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCooked) Icons.Rounded.CheckCircle else Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Square image - with FIXED height and width for consistent sizing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp) // Fixed height for consistency
            ) {
                SubcomposeAsyncImage(
                    model = meal.imageUrl,
                    contentDescription = "Image of ${meal.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when(mealType) {
                                    MealType.BREAKFAST -> Icons.Filled.WbSunny
                                    MealType.LUNCH -> Icons.Filled.LunchDining
                                    MealType.DINNER -> Icons.Filled.DinnerDining
                                    MealType.SNACK -> Icons.Filled.Icecream
                                    MealType.OTHER -> Icons.Filled.MoreHoriz
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                )

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
                            icon = Icons.Filled.Timer,
                            smallSize = true
                        )
                    }
                }
            }

            // Meal name
            Text(
                text = meal.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            
            // Cook button (only if planned but not cooked yet)
            if (isPlanned && !isCooked) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onCookClick,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Mark as Cooked", 
                        style = MaterialTheme.typography.labelSmall
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