package com.example.mealflow.ui.components



import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mealflow.R
import com.example.mealflow.data.model.*
import com.example.mealflow.utils.rememberHapticFeedback
import com.example.mealflow.viewModel.MealViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PlannedMealCard(
    meal: Meal,
    plannedMealInfo: PlannedMealInfo? = null,
    onClick: () -> Unit,
    animationDelay: Long = 0L,
    modifier: Modifier = Modifier
) {
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

    Card(
        modifier = modifier
            .width(200.dp)
            .height(300.dp) // Increased height for better badge layout
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.translationY = translateY
            }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clickable {
                if (!meal.hasLoadingError) {
                    onClick()
                }
            }
            .semantics { contentDescription = "Meal: ${meal.name}" },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Enhanced Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp) // Adjusted for better proportions
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                AsyncImage(
                    model = meal.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.neptune_placeholder_48,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.neptune_placeholder_48),
                    error = painterResource(id = R.drawable.neptune_placeholder_48)
                )

                // Gradient overlay for better badge visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.15f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Error overlay for meals with loading errors
                if (meal.hasLoadingError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red.copy(alpha = 0.1f))
                    )
                }

                // Time badge (top left)
                val totalTime = meal.preparationTime + meal.cookingTime
                if (totalTime > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        EnhancedBadge(
                            icon = Icons.Default.Timer,
                            text = "$totalTime min",
                            backgroundColor = Color.Black.copy(alpha = 0.75f),
                            contentColor = Color.White
                        )
                    }
                }

                // Status badges (top right)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Meal type badge (if planned)
                    plannedMealInfo?.mealType?.let { mealType ->
                        EnhancedBadge(
                            text = mealType.replaceFirstChar { it.uppercase() },
                            backgroundColor = getMealTypeColor(mealType),
                            contentColor = Color.White,
                            icon = Icons.Filled.Restaurant,
                            modifier = Modifier
                        )
                    }

                    // Row of circular status badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Cooked status (highest priority)
                        if (meal.isCooked == true || plannedMealInfo?.isCooked == true) {
                            CircularBadge(
                                icon = Icons.Default.Done,
                                backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.95f),
                                contentColor = Color.White
                            )
                        }
                        // Planned status
                        else if (plannedMealInfo?.isPlanned == true) {
                            CircularBadge(
                                icon = Icons.Default.Schedule,
                                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                                contentColor = Color.White
                            )
                        }

                        // Favorited status
                        if (meal.isFavorited) {
                            CircularBadge(
                                icon = Icons.Default.Favorite,
                                backgroundColor = Color(0xFFE91E63).copy(alpha = 0.95f),
                                contentColor = Color.White
                            )
                        }

                        // Data quality indicators
                        if (meal.hasLoadingError) {
                            CircularBadge(
                                icon = Icons.Default.Error,
                                backgroundColor = Color(0xFFFF5722).copy(alpha = 0.95f),
                                contentColor = Color.White
                            )
                        } else if (meal.isPartialData) {
                            CircularBadge(
                                icon = Icons.Default.Warning,
                                backgroundColor = Color(0xFFFF9800).copy(alpha = 0.95f),
                                contentColor = Color.White
                            )
                        }
                    }
                }
            }

            // Enhanced Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(110.dp) // Adjusted for better spacing
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title with better typography
                Text(
                    text = meal.name.takeIf { it.isNotBlank() } ?: "Unnamed Meal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Enhanced tags and info row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tags section
                    if (meal.tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            meal.tags.take(2).forEach { tag ->
                                EnhancedTagChip(text = tag)
                            }
                            if (meal.tags.size > 2) {
                                Text(
                                    text = "+${meal.tags.size - 2}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Rating section
                    if (meal.rating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB000),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = String.format("%.1f", meal.rating),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }


            }
        }
    }
}
@Composable
fun MealCard(
    meal: Meal,
    onClick: () -> Unit,
    animationDelay: Long = 0L,
    modifier: Modifier = Modifier
) {
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

    Card(
        modifier = modifier
            .width(160.dp)  // Reduced from 200.dp
            .height(220.dp) // Reduced from 280.dp
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.translationY = translateY
            }
            .shadow(
                elevation = 4.dp,  // Reduced from 8.dp
                shape = RoundedCornerShape(16.dp), // Reduced from 20.dp
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clickable {
                if (!meal.hasLoadingError) {
                    onClick()
                }
            }
            .semantics { contentDescription = "Meal: ${meal.name}" },
        shape = RoundedCornerShape(16.dp), // Reduced from 20.dp
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Enhanced Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp) // Reduced from 180.dp
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) // Reduced from 20.dp
            ) {
                AsyncImage(
                    model = meal.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.neptune_placeholder_48,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.neptune_placeholder_48),
                    error = painterResource(id = R.drawable.neptune_placeholder_48)
                )

                // Gradient overlay for better text visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Error overlay for meals with loading errors
                if (meal.hasLoadingError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red.copy(alpha = 0.1f))
                    )
                }

                // Enhanced badges row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), // Reduced from 12.dp
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Time badge
                    val totalTime = meal.preparationTime + meal.cookingTime
                    if (totalTime > 0) {
                        EnhancedBadge(
                            icon = Icons.Default.Timer,
                            text = "$totalTime min",
                            backgroundColor = Color.Black.copy(alpha = 0.7f),
                            contentColor = Color.White,
                            smallSize = true // Add this parameter to EnhancedBadge
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Status badges
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { // Reduced spacing from 6.dp
                        if (meal.isFavorited) {
                            CircularBadge(
                                icon = Icons.Default.Favorite,
                                backgroundColor = Color.Red.copy(alpha = 0.9f),
                                contentColor = Color.White,
                                size = 24.dp // Add size parameter to CircularBadge
                            )
                        }
                        if (meal.isCooked == true) {
                            CircularBadge(
                                icon = Icons.Default.Done,
                                backgroundColor = Color.Green.copy(alpha = 0.9f),
                                contentColor = Color.White,
                                size = 24.dp // Add size parameter to CircularBadge
                            )
                        }

                        // Data quality indicators
                        if (meal.hasLoadingError) {
                            CircularBadge(
                                icon = Icons.Default.Error,
                                backgroundColor = Color(0xFFFF5722).copy(alpha = 0.9f),
                                contentColor = Color.White,
                                size = 24.dp // Add size parameter to CircularBadge
                            )
                        } else if (meal.isPartialData) {
                            CircularBadge(
                                icon = Icons.Default.Warning,
                                backgroundColor = Color(0xFFFF9800).copy(alpha = 0.9f),
                                contentColor = Color.White,
                                size = 24.dp // Add size parameter to CircularBadge
                            )
                        }
                    }
                }
            }

            // Enhanced Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(80.dp) // Reduced from 100.dp
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp), // Reduced from 16.dp
                verticalArrangement = Arrangement.spacedBy(4.dp) // Reduced from 8.dp
            ) {
                // Title with better typography
                Text(
                    text = meal.name.takeIf { it.isNotBlank() } ?: "Unnamed Meal",
                    style = MaterialTheme.typography.titleSmall.copy( // Changed from titleMedium
                        fontSize = 14.sp, // Reduced from 16.sp
                        lineHeight = 18.sp // Reduced from 20.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Enhanced tags and rating row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tags section
                    if (meal.tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            meal.tags.take(1).forEach { tag -> // Take only 1 tag instead of 2
                                EnhancedTagChip(text = tag, smallSize = true) // Add smallSize parameter
                            }
                            if (meal.tags.size > 1) {
                                Text(
                                    text = "+${meal.tags.size - 1}", // Changed from -2 to -1
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Rating section
                    if (meal.rating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB000),
                                modifier = Modifier.size(12.dp) // Reduced from 14.dp
                            )
                            Text(
                                text = String.format("%.1f", meal.rating),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedMealCarousel(
    meals: List<Meal>,
    onMealClick: (Meal) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()

    // Enhanced scroll end detection with haptic feedback
    LaunchedEffect(lazyListState) {
        snapshotFlow {
            val layoutInfo = lazyListState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val visibleItemsInfo = layoutInfo.visibleItemsInfo

            if (visibleItemsInfo.isNotEmpty() && totalItemsCount > 0) {
                val lastVisibleItem = visibleItemsInfo.last()
                lastVisibleItem.index == totalItemsCount - 1
            } else {
                false
            }
        }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && meals.isNotEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            }
    }

    Column(modifier = modifier.fillMaxWidth()) {

        LazyRow(
            state = lazyListState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(meals.size) { index ->
                val meal = meals[index]
                MealCard(
                    meal = meal,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMealClick(meal)
                    },
                    animationDelay = index * 75L // Slightly longer stagger for better effect
                )
            }
        }
    }
}

// Data class for planned meal information
data class PlannedMealInfo(
    val isPlanned: Boolean = false,
    val isCooked: Boolean = false,
    val mealType: String? = null
)

// Data quality helper functions
object MealDataQuality {
    fun getMealDataQualityLevel(meal: Meal): DataQualityLevel {
        return when {
            meal.hasLoadingError -> DataQualityLevel.ERROR
            meal.isPartialData -> DataQualityLevel.PARTIAL
            isMealDataIncomplete(meal) -> DataQualityLevel.INCOMPLETE
            else -> DataQualityLevel.COMPLETE
        }
    }

    private fun isMealDataIncomplete(meal: Meal): Boolean {
        return meal.name.isBlank() ||
               meal.ingredients.isEmpty() ||
               meal.instructions.isEmpty() ||
               (meal.preparationTime == 0 && meal.cookingTime == 0)
    }

    fun shouldShowRetryOption(meal: Meal): Boolean {
        return meal.hasLoadingError || meal.isPartialData
    }

    fun getDataQualityMessage(level: DataQualityLevel): String {
        return when (level) {
            DataQualityLevel.ERROR -> "Failed to load meal data"
            DataQualityLevel.PARTIAL -> "Some data may be missing"
            DataQualityLevel.INCOMPLETE -> "Limited information available"
            DataQualityLevel.COMPLETE -> ""
        }
    }
}

enum class DataQualityLevel {
    COMPLETE, INCOMPLETE, PARTIAL, ERROR
}

@Composable
fun DataQualityStatusBar(
    viewModel: MealViewModel,
    modifier: Modifier = Modifier
) {
    val dataQualityStats by remember {
        derivedStateOf { viewModel.getDataQualityStats() }
    }
    val (errorCount, partialCount, totalCount) = dataQualityStats

    // Only show if there are any data quality issues
    if (errorCount > 0 || partialCount > 0) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = when {
                    errorCount > 0 -> MaterialTheme.colorScheme.errorContainer
                    partialCount > 0 -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = when {
                            errorCount > 0 -> Icons.Default.Error
                            partialCount > 0 -> Icons.Default.Warning
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when {
                            errorCount > 0 -> MaterialTheme.colorScheme.error
                            partialCount > 0 -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )

                    Column {
                        Text(
                            text = when {
                                errorCount > 0 -> "Some meals failed to load"
                                partialCount > 0 -> "Some meals have limited data"
                                else -> "Data status"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = buildString {
                                if (errorCount > 0) append("$errorCount failed")
                                if (errorCount > 0 && partialCount > 0) append(", ")
                                if (partialCount > 0) append("$partialCount partial")
                                append(" of $totalCount meals")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                if (errorCount > 0) {
                    Button(
                        onClick = { viewModel.refreshFailedMeals() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "Retry",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

// Create a helper function to convert PlannedMeal to PlannedMealInfo
@Composable
fun PlannedMealCarousel(
    meals: List<Meal>,
    plannedMealInfoList: List<PlannedMealInfo?> = emptyList(),
    onMealClick: (Meal) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()

    // Enhanced scroll end detection with haptic feedback
    LaunchedEffect(lazyListState) {
        snapshotFlow {
            val layoutInfo = lazyListState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val visibleItemsInfo = layoutInfo.visibleItemsInfo

            if (visibleItemsInfo.isNotEmpty() && totalItemsCount > 0) {
                val lastVisibleItem = visibleItemsInfo.last()
                lastVisibleItem.index == totalItemsCount - 1
            } else {
                false
            }
        }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd && meals.isNotEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            }
    }

    Column(modifier = modifier.fillMaxWidth()) {

        LazyRow(
            state = lazyListState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(meals.size) { index ->
                val meal = meals[index]
                val plannedInfo = plannedMealInfoList.getOrNull(index)

                PlannedMealCard(
                    meal = meal,
                    plannedMealInfo = plannedInfo,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMealClick(meal)
                    },
                    animationDelay = index * 75L
                )
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
@Composable
fun EnhancedBadge(
    icon: ImageVector? = null,
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    smallSize: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        contentColor = contentColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = if (smallSize) 6.dp else 8.dp, vertical = if (smallSize) 2.dp else 4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(if (smallSize) 12.dp else 14.dp)
                )
                Spacer(modifier = Modifier.width(if (smallSize) 2.dp else 4.dp))
            }
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = if (smallSize) 10.sp else 12.sp
                ),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
@Composable
fun CircularBadge(
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(size * 0.55f) // Proportional to the badge size
        )
    }
}
@Composable
private fun EnhancedTagChip(
    text: String,
    smallSize: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = text.take(8) + if (text.length > 8) "..." else "",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = if (smallSize) 8.sp else 10.sp
            ),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = if (smallSize) 4.dp else 6.dp, vertical = if (smallSize) 2.dp else 3.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
// Reusable badge chip component
@Composable
fun BadgeChip(
    text: String,
    color: Color,
    textColor: Color,
    icon: ImageVector? = null,
    iconTint: Color = textColor,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = color,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
fun LoadingCarouselHomePage() { // Renamed for clarity
    Box(
        modifier = Modifier.fillMaxWidth().height(220.dp), // Match PlannedMealCardHomePage height
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyStatePlannedMeals(message: String, buttonText: String, onButtonClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp) // Match PlannedMealCardHomePage height
            .padding(horizontal = 24.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.RestaurantMenu, // More relevant icon
                contentDescription = message,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onButtonClick) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(buttonText)
            }
        }
    }
}

@Composable
fun EmptyStateGenericHomePage(message: String) { // Renamed for clarity
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp) // Match PlannedMealCardHomePage height
            .padding(horizontal = 24.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}
