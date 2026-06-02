//package com.example.mealflow
//
//
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material3.FilledIconButton
//import androidx.compose.material3.IconButtonDefaults
//import androidx.compose.runtime.remember
//
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.animation.animateColorAsState
//import androidx.compose.animation.core.*
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.setValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Done
//import androidx.compose.material.icons.filled.Star
//import androidx.compose.material.icons.filled.Timer
//import androidx.compose.ui.semantics.contentDescription
//import androidx.compose.ui.semantics.semantics
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import coil.compose.AsyncImage
//import com.example.mealflow.data.model.Meal
//import com.example.mealflow.R
//import kotlinx.coroutines.delay
//import android.os.Build
//import android.util.Log
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.material.icons.filled.Refresh
//import androidx.compose.material3.Button
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Snackbar
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.snapshotFlow
//import com.example.mealflow.data.model.Interactions
//import com.example.mealflow.data.model.MealIngredient
//import com.example.mealflow.data.model.User
//import com.example.mealflow.utils.rememberHapticFeedback
//
//@Composable
//fun SectionHeader(
//    title: String,
//    onViewAll: () -> Unit = {},
//    showViewAll: Boolean = true
//) {
//    val haptic = rememberHapticFeedback()
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = title,
//            style = MaterialTheme.typography.titleLarge.copy(
//                fontWeight = FontWeight.Bold
//            ),
//            color = MaterialTheme.colorScheme.onBackground
//        )
//
//        if (showViewAll) {
//            Text(
//                text = "See All",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.tertiary,
//                modifier = Modifier
//                    .clickable {
//                        haptic.lightClick() // Provide selection haptic feedback
//                        onViewAll()
//                    }
//                    .padding(vertical = 4.dp, horizontal = 8.dp)
//            )
//        }
//    }
//}
//@Composable
//fun EnhancedMealCarousel(
//    meals: List<Meal>,
//    onMealClick: (Meal) -> Unit
//) {
//    val haptic = rememberHapticFeedback()
//    val lazyListState = rememberLazyListState()
//
//    // Track if we've reached the end for haptic feedback
//    LaunchedEffect(lazyListState) {
//        snapshotFlow {
//            // Check if we've reached the end of the list
//            val layoutInfo = lazyListState.layoutInfo
//            val totalItemsCount = layoutInfo.totalItemsCount
//            val visibleItemsInfo = layoutInfo.visibleItemsInfo
//
//            if (visibleItemsInfo.isNotEmpty()) {
//                val lastVisibleItem = visibleItemsInfo.last()
//                lastVisibleItem.index == totalItemsCount - 1 &&
//                        lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset
//            } else {
//                false
//            }
//        }.collect { isAtEnd ->
//            if (isAtEnd && meals.isNotEmpty()) {
//                // Provide haptic feedback when reaching the end
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    haptic.scrollEnd()
//                } else {
//                    haptic.lightClick()
//                }
//            }
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(top = 8.dp)
//    ) {
//        LazyRow(
//            state = lazyListState,
//            contentPadding = PaddingValues(horizontal = 19.dp),
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            items(meals.size) { index ->
//                val meal = meals[index]
//                EnhancedMealCard(
//                    meal = meal,
//                    onClick = {
//                        haptic.lightClick() // Add haptic feedback on card click
//                        onMealClick(meal)
//                    },
//                    animationDelay = index * 50L // Staggered animation
//                )
//            }
//        }
//    }
//}
//@Composable
//fun EnhancedEmptyState(message: String) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(180.dp)
//            .padding(horizontal = 24.dp)
//            .background(
//                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
//                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
//            ),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Icon(
//                imageVector = Icons.Default.Refresh,
//                contentDescription = "Empty state",
//                modifier = Modifier.size(32.dp),
//                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = message,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
//            )
//        }
//    }
//}
//
//@Composable
//fun EnhancedErrorSnackbar(
//    message: String,
//    onDismiss: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Snackbar(
//        modifier = modifier,
//        containerColor = MaterialTheme.colorScheme.errorContainer,
//        contentColor = MaterialTheme.colorScheme.onErrorContainer,
//        action = {
//            TextButton(onClick = onDismiss) {
//                Text(
//                    "Dismiss",
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
//        }
//    ) {
//        Text(message)
//    }
//}
//
//@Composable
//fun MealCard(
//    meal: Meal,
//    isPlanned: Boolean?,
//    isCooked: Boolean?,
//    onClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//            .clickable(onClick = onClick),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        shape = RoundedCornerShape(16.dp)
//    ) {
//        Column(modifier = Modifier.fillMaxWidth()) {
//            AsyncImage(
//                model = meal.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.neptune_placeholder_48,
//                contentDescription = meal.name,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(160.dp)
//                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
//                contentScale = ContentScale.Crop,
//                placeholder = painterResource(id = R.drawable.neptune_placeholder_48),
//                error = painterResource(id = R.drawable.neptune_placeholder_48)
//            )
//
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        text = meal.name.takeIf { it.isNotBlank() } ?: "Unnamed Meal",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                    // Display checkmarks based on status
//                    Text(
//                        text = when {
//                            isCooked == true -> "✓✓"
//                            isPlanned == true -> "✓"
//                            else -> ""
//                        },
//                        color = if (isCooked == true) Color.Green else if (isPlanned == true) Color.Yellow else Color.Transparent,
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(6.dp))
//
//                Text(
//                    text = meal.description?.takeIf { it.isNotBlank() } ?: "No description available",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
//                    maxLines = 3,
//                    overflow = TextOverflow.Ellipsis
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    meal.tags.firstOrNull()?.let { tag ->
//                        Box(
//                            modifier = Modifier
//                                .background(
//                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
//                                    shape = RoundedCornerShape(8.dp)
//                                )
//                                .padding(horizontal = 8.dp, vertical = 4.dp)
//                        ) {
//                            Text(
//                                text = tag,
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.primary
//                            )
//                        }
//                    }
//
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            imageVector = Icons.Default.Star,
//                            contentDescription = "Rating",
//                            tint = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.size(16.dp)
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "${meal.rating}",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurface
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//@Composable
//fun EnhancedMealCard(
//    meal: Meal,
//    onClick: () -> Unit,
//    animationDelay: Long = 0L
//) {
//    // Single animation state controller
//    var visible by remember { mutableStateOf(false) }
//
//    // Trigger animation after delay
//    LaunchedEffect(Unit) {
//        delay(animationDelay)
//        visible = true
//    }
//
//    // Combined animation specs
//    val enterTransition = remember {
//        tween<Float>(
//            durationMillis = 300,
//            easing = FastOutSlowInEasing
//        )
//    }
//
//    // Animate alpha and scale together
//    val alpha by animateFloatAsState(
//        targetValue = if (visible) 1f else 0f,
//        animationSpec = enterTransition,
//        label = "cardFade"
//    )
//
//    val scale by animateFloatAsState(
//        targetValue = if (visible) 1f else 0.95f,
//        animationSpec = enterTransition,
//        label = "cardScale"
//    )
//
//    Card(
//        modifier = Modifier
//            .width(180.dp)
//            .height(240.dp)
//            .graphicsLayer {
//                this.alpha = alpha
//                this.scaleX = scale
//                this.scaleY = scale
//            }
//            .clickable { onClick() }
//            .semantics { contentDescription = "Meal: ${meal.name}" },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        shape = RoundedCornerShape(16.dp)
//    ) {
//        Box(modifier = Modifier.fillMaxSize()) {
//            // Meal Image
//            AsyncImage(
//                model = meal.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.neptune_placeholder_48,
//                contentDescription = null,
//                modifier = Modifier.fillMaxSize(),
//                contentScale = ContentScale.Crop,
//                placeholder = painterResource(id = R.drawable.neptune_placeholder_48),
//                error = painterResource(id = R.drawable.neptune_placeholder_48)
//            )
//
//            // Time indicator
//            val totalTime = meal.preparationTime + meal.cookingTime
//            if (totalTime > 0) {
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.TopStart)
//                        .padding(8.dp)
//                        .background(
//                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f),
//                            shape = RoundedCornerShape(12.dp)
//                        )
//                        .padding(horizontal = 8.dp, vertical = 4.dp)
//                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            imageVector = Icons.Default.Timer,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.tertiary,
//                            modifier = Modifier.size(16.dp)
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "$totalTime min",
//                            style = MaterialTheme.typography.bodySmall,
//                            fontWeight = FontWeight.Medium,
//                            color = MaterialTheme.colorScheme.onSecondary
//                        )
//                    }
//                }
//            }
//
//            // Info section
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .background(
//                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
//                    )
//                    .padding(12.dp)
//            ) {
//                Text(
//                    text = meal.name.takeIf { it.isNotBlank() } ?: "Unnamed Meal",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//
//                Spacer(modifier = Modifier.height(6.dp))
//
//                if (meal.tags.isNotEmpty()) {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        // Simplified tag display
//                        meal.tags.take(2).forEach { tag ->
//                            SimpleTagChip(text = tag)
//                        }
//
//                        if (meal.tags.size > 2) {
//                            Text(
//                                text = "+${meal.tags.size - 2}",
//                                style = MaterialTheme.typography.labelSmall,
//                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                            )
//                        }
//                    }
//                }
//
//                if (meal.isCooked == true) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 4.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Done,
//                            contentDescription = null,
//                            tint = Color.Green,
//                            modifier = Modifier.size(14.dp)
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "Cooked",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = Color.Green
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun SimpleTagChip(text: String) {
//    Box(
//        modifier = Modifier
//            .background(
//                color = MaterialTheme.colorScheme.surfaceVariant,
//                shape = RoundedCornerShape(8.dp)
//            )
//            .padding(horizontal = 6.dp, vertical = 2.dp)
//    ) {
//        Text(
//            text = text,
//            style = MaterialTheme.typography.labelSmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis
//        )
//    }
//}
//
//
//
//fun getSampleMeal(id: Int = 1, name: String = "Sample Meal $id"): Meal {
//    return Meal(
//        mealId = id,
//        name = name,
//        description = "This is a delicious sample meal. It's very easy to make and everyone loves it.",
//        imageUrl = "https://www.themealdb.com/images/media/meals/svprys1511176755.jpg", // A generic placeholder image
//        region = "Generic",
//        tags = listOf("Quick", "Easy", "Dinner"),
//        dietaryTags = listOf("Vegetarian"),
//        preparationTime = 15,
//        cookingTime = 30,
//        totalTime = 45, // This should ideally be prep + cook, ensure consistency or calculate
//        servings = 4,
//        caloriesPerServing = 350.0,
//        createdBy = User(userId = "user123", username = "Chef Bot"),
//        isFavorited = false,
//        isSaved = false,
//        notes = emptyList(),
//        interactions = Interactions(views = 100, likes = 20),
//        createdAt = "2023-01-01T12:00:00Z",
//        updatedAt = "2023-01-01T12:00:00Z",
//        isPlanned = false,
//        isCooked = false,
//        ingredients = listOf(
//            MealIngredient(id = 1, phrase = "2 cups flour"),
//            MealIngredient(id = 2, phrase = "1 cup sugar")
//        ),
//        instructions = listOf("Mix ingredients.", "Bake for 30 minutes."),
//        cookware = listOf("Mixing Bowl", "Oven Pan"),
//        rating = 4.5,
//        reviewsCount = 15,
//        communityId = null,
//        matchingIngredients = 2
//    )
//}
//
//val sampleMealsForPreview: List<Meal> = listOf(
//    getSampleMeal(1, "Pasta Carbonara")
//        .copy(
//            tags = listOf("Italian", "Pasta", "Dinner"),
//            preparationTime = 10,
//            cookingTime = 15,
//            rating = 4.8,
//            isCooked = true,
//            imageUrl = "https://www.themealdb.com/images/media/meals/llcbn01574260722.jpg"
//        ),
//    getSampleMeal(2, "Chicken Salad")
//        .copy(
//            tags = listOf("Healthy", "Lunch", "Quick"),
//            preparationTime = 20,
//            cookingTime = 0,
//            rating = 4.2,
//            isPlanned = true,
//            imageUrl = "https://www.themealdb.com/images/media/meals/tqrrsq1511723764.jpg"
//        ),
//    getSampleMeal(3, "Vegetable Stir-fry")
//        .copy(
//            name = "Vegetable Stir-fry with Extra Long Name for Testing Ellipsis",
//            tags = listOf("Vegan", "Asian", "Healthy", "Quick", "Low Carb"),
//            description = "A very long description that should ideally wrap or be ellipsized to test the text overflow capabilities of the card component. This meal is packed with nutrients and flavors, making it a perfect choice for a healthy and satisfying dinner. It includes a variety of colorful vegetables such as broccoli, carrots, bell peppers, and snap peas, all stir-fried to perfection in a savory sauce.",
//            preparationTime = 15,
//            cookingTime = 10,
//            rating = 4.5,
//            imageUrl = "https://www.themealdb.com/images/media/meals/1529444830.jpg"
//        ),
//    getSampleMeal(4, "Blueberry Pancakes")
//        .copy(
//            name = "Delicious Blueberry Pancakes for Breakfast",
//            tags = listOf("Breakfast", "Sweet", "Classic"),
//            preparationTime = 10,
//            cookingTime = 20,
//            rating = 4.9,
//            imageUrl = "" // Test placeholder image logic
//        ),
//    getSampleMeal(5, "No Image Meal")
//        .copy(
//            imageUrl = null, // Test null image URL
//            tags = listOf("Simple"),
//            preparationTime = 5,
//            cookingTime = 5
//        )
//)
//
//// --- Previews ---
//
//// MaterialTheme wrapper for all previews
//@Composable
//fun MealFlowPreviewTheme(content: @Composable () -> Unit) {
//    MaterialTheme { // Replace with your app's actual theme if available, e.g., MealFlowTheme
//        content()
//    }
//}
//
//
//
//
//// Previews for EnhancedMealCard
//@Preview(name = "EnhancedMealCard - Default", showBackground = true)
//@Composable
//fun EnhancedMealCardPreview() {
//    MealFlowPreviewTheme {
//        EnhancedMealCard(
//            meal = sampleMealsForPreview[2].copy(isCooked = false), // Vegetable Stir-fry
//            onClick = {},
//            animationDelay = 0L
//        )
//    }
//}
//
//@Preview(name = "EnhancedMealCard - Cooked", showBackground = true)
//@Composable
//fun EnhancedMealCardCookedPreview() {
//    MealFlowPreviewTheme {
//        AdvancedMealCard(
//            meal = sampleMealsForPreview[1].copy(isPlanned = true), // Pasta Carbonara
//            onClick = {},
//            animationDelay = 0L
//        )
//    }
//}
//
//
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//fun AdvancedMealCard(
//    meal: Meal,
//    onClick: () -> Unit,
//    animationDelay: Long = 0L
//) {
//    // Animation state controller
//    var visible by remember { mutableStateOf(false) }
//
//    // Trigger animation after delay
//    LaunchedEffect(Unit) {
//        delay(animationDelay)
//        visible = true
//    }
//
//    // Animation specs for smooth appearance
//    val enterTransition = remember {
//        tween<Float>(
//            durationMillis = 300,
//            easing = FastOutSlowInEasing
//        )
//    }
//
//    // Combined animations for cohesive motion
//    val alpha by animateFloatAsState(
//        targetValue = if (visible) 1f else 0f,
//        animationSpec = enterTransition,
//        label = "cardFade"
//    )
//
//    val scale by animateFloatAsState(
//        targetValue = if (visible) 1f else 0.95f,
//        animationSpec = enterTransition,
//        label = "cardScale"
//    )
//
//    Card(
//        modifier = Modifier
//            .width(180.dp)
//            .height(240.dp)
//            .graphicsLayer {
//                this.alpha = alpha
//                this.scaleX = scale
//                this.scaleY = scale
//            }
//            .clickable { onClick() }
//            .semantics { contentDescription = "Meal: ${meal.name}" },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        shape = RoundedCornerShape(16.dp),
//        // Use theme colors for card background
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface
//        )
//    ) {
//        Column(modifier = Modifier.fillMaxSize()) {
//            // Image container with proper sizing
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(140.dp)
//                    .background(MaterialTheme.colorScheme.surfaceVariant)
//            ) {
//                // Meal Image
//                AsyncImage(
//                    model = meal.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.neptune_placeholder_48,
//                    contentDescription = null,
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.Crop,
//                    placeholder = painterResource(id = R.drawable.neptune_placeholder_48),
//                    error = painterResource(id = R.drawable.neptune_placeholder_48)
//                )
//
//                // Status indicators in top corners
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    // Time indicator on the left
//                    val totalTime = meal.preparationTime + meal.cookingTime
//                    if (totalTime > 0) {
//                        Box(
//                            modifier = Modifier
//                                .background(
//                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
//                                    shape = RoundedCornerShape(12.dp)
//                                )
//                                .padding(horizontal = 8.dp, vertical = 4.dp)
//                        ) {
//                            Row(verticalAlignment = Alignment.CenterVertically) {
//                                Icon(
//                                    imageVector = Icons.Default.Timer,
//                                    contentDescription = null,
//                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
//                                    modifier = Modifier.size(14.dp)
//                                )
//                                Spacer(modifier = Modifier.width(4.dp))
//                                Text(
//                                    text = "$totalTime min",
//                                    style = MaterialTheme.typography.labelSmall,
//                                    fontWeight = FontWeight.Medium,
//                                    color = MaterialTheme.colorScheme.onSecondaryContainer
//                                )
//                            }
//                        }
//                    }
//
//                    // Cooked status indicator on the right
//                    if (meal.isCooked == true) {
//                        Box(
//                            modifier = Modifier
//                                .background(
//                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
//                                    shape = RoundedCornerShape(12.dp)
//                                )
//                                .padding(horizontal = 8.dp, vertical = 4.dp)
//                        ) {
//                            Row(verticalAlignment = Alignment.CenterVertically) {
//                                Icon(
//                                    imageVector = Icons.Default.Done,
//                                    contentDescription = null,
//                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
//                                    modifier = Modifier.size(14.dp)
//                                )
//                                Spacer(modifier = Modifier.width(4.dp))
//                                Text(
//                                    text = "Cooked",
//                                    style = MaterialTheme.typography.labelSmall,
//                                    color = MaterialTheme.colorScheme.onPrimaryContainer
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Content section with proper padding and spacing
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(12.dp)
//            ) {
//                // Meal name with proper styling
//                Text(
//                    text = meal.name.takeIf { it.isNotBlank() } ?: "Unnamed Meal",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Tags with better layout and styling
//                if (meal.tags.isNotEmpty()) {
//                    FlowRow(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(4.dp),
//                        verticalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        // Show up to 3 tags, using better flow layout
//                        meal.tags.take(3).forEach { tag ->
//                            TagChip(text = tag)
//                        }
//
//                        // More elegant overflow indicator
//                        if (meal.tags.size > 3) {
//                            Box(
//                                modifier = Modifier
//                                    .background(
//                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .padding(horizontal = 6.dp, vertical = 2.dp)
//                            ) {
//                                Text(
//                                    text = "+${meal.tags.size - 3}",
//                                    style = MaterialTheme.typography.labelSmall,
//                                    color = MaterialTheme.colorScheme.tertiary
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun TagChip(text: String) {
//    Box(
//        modifier = Modifier
//            .background(
//                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
//                shape = RoundedCornerShape(8.dp)
//            )
//            .padding(horizontal = 6.dp, vertical = 2.dp)
//    ) {
//        Text(
//            text = text,
//            style = MaterialTheme.typography.labelSmall,
//            color = MaterialTheme.colorScheme.onSecondaryContainer,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis
//        )
//    }
//}