package com.example.mealflow.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mealflow.data.model.Meal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * A horizontal carousel component that displays meals in the Mealime style.
 * Each card can be swiped up (like) or down (ignore).
 */
@Composable
fun MealimeCarousel(
    meals: List<Meal>,
    onMealClick: (Meal) -> Unit,
    onSwipeUp: (Meal) -> Unit,   // For 'like'
    onSwipeDown: (Meal) -> Unit, // For 'ignore'
    isLoading: Boolean = false,
    cardWidth: Int = 140
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (meals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No meals available",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = meals,
                    key = { it.mealId } // Use a stable key for better performance
                ) { meal ->
                    SwipeableMealCard(
                        meal = meal,
                        onClick = { onMealClick(meal) },
                        onSwipeUp = { onSwipeUp(meal) },
                        onSwipeDown = { onSwipeDown(meal) },
                        modifier = Modifier.width(cardWidth.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeableMealCard(
    meal: Meal,
    onClick: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val swipeThreshold = 150f // Pixels to swipe before triggering action

    // State for visual feedback
    val scale by animateFloatAsState(targetValue = if (offsetY.value != 0f) 0.95f else 1f, label = "scale")

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        coroutineScope.launch {
                            val isSwipeActioned = when {
                                offsetY.value > swipeThreshold -> {
                                    onSwipeDown()
                                    true
                                }
                                offsetY.value < -swipeThreshold -> {
                                    onSwipeUp()
                                    true
                                }
                                else -> false
                            }

                            if (isSwipeActioned) {
                                launch { offsetY.snapTo(0f) }
                                launch { rotation.snapTo(0f) }
                            } else {
                                // Animate back to the original position with a jiggle
                                launch {
                                    offsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = 0.4f, // bouncy
                                            stiffness = 400f
                                        )
                                    )
                                }
                                launch {
                                    rotation.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = 0.4f,
                                            stiffness = 400f
                                        )
                                    )
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            // Animate back with a jiggle on cancellation
                            offsetY.animateTo(0f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f))
                            rotation.animateTo(0f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f))
                        }
                    }
                ) { _, dragAmount ->
                    coroutineScope.launch {
                        offsetY.snapTo(offsetY.value + dragAmount)
                        rotation.snapTo((offsetY.value / 40).coerceIn(-15f, 15f))
                    }
                }
            }
            .graphicsLayer(
                translationY = offsetY.value,
                scaleX = scale,
                scaleY = scale,
                rotationZ = rotation.value
            )
    ) {
        // Background icons that appear during swipe
        val alphaUp by animateFloatAsState(targetValue = if (offsetY.value < -10) abs(offsetY.value / swipeThreshold) else 0f, label = "alphaUp")
        val alphaDown by animateFloatAsState(targetValue = if (offsetY.value > 10) abs(offsetY.value / swipeThreshold) else 0f, label = "alphaDown")

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Like",
            tint = Color.Green.copy(alpha = alphaUp),
            modifier = Modifier
                .align(Alignment.Center)
                .scale(2f * alphaUp)
        )

        Icon(
            imageVector = Icons.Default.ThumbDown,
            contentDescription = "Ignore",
            tint = Color.Red.copy(alpha = alphaDown),
            modifier = Modifier
                .align(Alignment.Center)
                .scale(2f * alphaDown)
        )

        MealimeStyleCard(
            meal = meal,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * A horizontal carousel component that displays planned meals in the Mealime style.
 * Each card shows a square image with the name underneath, plus meal type and planned/cooked status.
 */
@Composable
fun PlannedMealimeCarousel(
    meals: List<Meal>,
    plannedMealInfoList: List<PlannedMealInfo?> = emptyList(),
    onMealClick: (Meal) -> Unit,
    isLoading: Boolean = false,
    cardWidth: Int = 150 // Slightly wider for the planned meal badges
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (meals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No meals available",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(meals.size) { index ->
                    val meal = meals[index]
                    val plannedMealInfo = plannedMealInfoList.getOrNull(index)

                    MealimePlannedMealCard(
                        meal = meal,
                        plannedMealInfo = plannedMealInfo,
                        onClick = { onMealClick(meal) },
                        animationDelay = index * 75L,
                        modifier = Modifier.width(cardWidth.dp)
                    )
                }
            }
        }
    }
} 