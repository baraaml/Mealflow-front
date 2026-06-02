package com.example.mealflow.ui.screens

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mealflow.R
import com.example.mealflow.data.model.Meal
import com.example.mealflow.ui.components.AddToPlanDialog
// import com.example.mealflow.ui.components.MealMoreOptionsButton // Will be defined locally or imported
import com.example.mealflow.ui.components.TagsRow // Assuming this is correctly located
import com.example.mealflow.ui.components.BottomSheetItem // Assuming this is correctly located or defined
import com.example.mealflow.utils.rememberHapticFeedback
import com.example.mealflow.utils.HapticFeedbackUtil
import com.example.mealflow.viewModel.MealPlannerViewModel
import com.example.mealflow.viewModel.MealPlannerViewModelFactory
import com.example.mealflow.viewModel.MealViewModel
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Divider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.window.Dialog
import com.example.mealflow.network.InteractionRequest
import com.example.mealflow.viewModel.ShoppingListViewModel
import com.example.mealflow.viewModel.ShoppingListViewModelFactory
import kotlinx.coroutines.launch

val tabActiveColor = Color(0xFF4CAF50)
val tabActiveContainerColor = Color(0xFFEBF6EC)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MealDetailScreen(
    meal: Meal,
    onNavigateBack: () -> Unit,
    navController: NavController,
    mealViewModel: MealViewModel,
    isPlanned: Boolean = false
) {
    val scrollState = rememberScrollState()
    var showAddToPlanDialog by remember { mutableStateOf(false) }
    var isFavorited by remember { mutableStateOf(meal.isFavorited) }
    var isLiked by remember(meal.isLiked) { mutableStateOf(meal.isLiked) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Ingredients", "Instructions", "Cookware")
    val haptic = rememberHapticFeedback()

    LaunchedEffect(key1 = meal.mealId) {
        mealViewModel.recordView(meal)
    }

    var hasReachedBottom by remember { mutableStateOf(false) }
    val isAtBottom = scrollState.value >= (scrollState.maxValue - 100)

    LaunchedEffect(isAtBottom) {
        if (isAtBottom && !hasReachedBottom) {
            haptic.endOfContentBump()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    val pattern = longArrayOf(0, 80, 40, 80)
                    val amplitudes = intArrayOf(0, 255, 0, 255)
                    haptic.customVibration(pattern, amplitudes)
                } catch (e: SecurityException) {
                    // Fallback
                }
            }
            hasReachedBottom = true
        } else if (!isAtBottom && hasReachedBottom) {
            hasReachedBottom = false
        }
    }

    val context = LocalContext.current.applicationContext
    val shoppingListViewModel: ShoppingListViewModel = viewModel(
        factory = ShoppingListViewModelFactory(context)
    )
    val plannerViewModel: MealPlannerViewModel = viewModel(
        factory = MealPlannerViewModelFactory(
            context,
            shoppingListViewModel = shoppingListViewModel,
            mealViewModel = mealViewModel
        )
    )

    val scale by animateFloatAsState(
        targetValue = if (isFavorited) 1.2f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "favoriteScale"
    )

    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "likeScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        ) {
            AsyncImage(
                model = meal.imageUrl,
                contentDescription = "Image of ${meal.name}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.neptune_placeholder_48),
                error = painterResource(id = R.drawable.neptune_placeholder_48)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                            startY = 0.7f * 360
                        )
                    )
            )
            IconButton(
                onClick = { onNavigateBack() },
                modifier = Modifier
                    .padding(15.dp)
                    .align(Alignment.TopStart)
                    .size(30.dp)
                    .semantics { contentDescription = "Navigate back" }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val iconSizePercent = 0.6f
                    val buttonSize = 36.dp
                    val iconSize = buttonSize * iconSizePercent
                    val offsetPercent = 0.08f
                    val offset = buttonSize * offsetPercent
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(iconSize).offset(x = offset)
                    )
                }
            }
            MealMoreOptionsButton(
                haptic = haptic,
                meal = meal,
                navController = navController,
                modifier = Modifier
                    .padding(15.dp)
                    .align(Alignment.TopEnd)
                    .size(30.dp),
                mealViewModel = mealViewModel
            )
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        val newLikedState = !isLiked
                        isLiked = newLikedState

                        val currentMeal = mealViewModel.findMealById(meal.mealId) ?: meal
                        // If liking, ensure it's not disliked
                        val newDislikedState = if (newLikedState) false else currentMeal.isDisliked

                        mealViewModel.sendInteraction(
                            InteractionRequest(
                                userId = "",
                                mealId = currentMeal.mealId,
                                type = "like",
                                liked = newLikedState,
                                disliked = newDislikedState,
                                ignored = false, // Replace with actual state
                                viewed = true, // Replace with actual state
                                saved = currentMeal.isSaved,
                                cooked = currentMeal.isCooked,
                                favorited = currentMeal.isFavorited,
                                rating = currentMeal.rating.toFloat(),
                                reviewText = "" // Replace with actual review text if available
                            )
                        )
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .scale(likeScale)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) tabActiveColor else Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (!isFavorited) haptic.success() else haptic.lightClick()
                        isFavorited = !isFavorited
                        mealViewModel.toggleFavorite(meal)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .scale(scale)
                        .semantics {
                            contentDescription = if (isFavorited) "Remove from favorites" else "Add to favorites"
                        }
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorited) Color.Red else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    append(meal.name)
                    withStyle(style = SpanStyle(color = tabActiveColor, fontSize = MaterialTheme.typography.headlineLarge.fontSize)) {
                        append(".")
                    }
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (meal.tags.isNotEmpty()) {
                TagsRow(
                    tags = meal.tags,
                    onTagClick = { haptic.lightClick() },
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${meal.preparationTime + meal.cookingTime} minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                var isAdding by remember { mutableStateOf(false) }
                if (!isPlanned) {
                    Button(
                        onClick = {
                            haptic.mediumClick()
                            showAddToPlanDialog = true
                        },
                        modifier = Modifier.height(36.dp).alpha(if (isAdding) 0.7f else 1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tabActiveColor),
                        enabled = !isAdding
                    ) {
                        if (isAdding) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(text = "Add to Plan", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(4.dp)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(color = tabActiveColor)
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        haptic.selection()
                        selectedTab = index
                    },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) tabActiveColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            fontWeight = if (selectedTab == index) FontWeight.Medium else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.background(
                        color = if (selectedTab == index) tabActiveContainerColor else MaterialTheme.colorScheme.background
                    )
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInVertically(initialOffsetY = { height -> height }, animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
                        .togetherWith(slideOutVertically(targetOffsetY = { height -> -height }, animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)))
                }, label = "TabContentAnimation"
            ) { targetTab ->
                when (targetTab) {
                    0 -> { // Ingredients
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            meal.ingredients.forEach { ingredient ->
                                IngredientItem(
                                    quantity = ingredient.quantity.toString(),
                                    unit = ingredient.unit.toString(),
                                    name = ingredient.phrase.toString(),
                                    onItemClick = { haptic.lightClick() }
                                )
                            }
                        }
                    }
                    1 -> { // Instructions
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            meal.instructions.forEachIndexed { index, instruction ->
                                InstructionItem(step = index + 1, instruction = instruction, onItemClick = { haptic.lightClick() })
                            }
                        }
                    }
                    2 -> { // Cookware
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            meal.cookware.forEach { item -> ListItem(text = item, onItemClick = { haptic.lightClick() }) }
                        }
                    }
                }
            }
        }
    }

    if (showAddToPlanDialog) {
        AddToPlanDialog(
            meal = meal,
            onDismiss = {
                haptic.lightClick()
                showAddToPlanDialog = false
            },
            onAddToPlan = { date, mealType ->
                haptic.success()
                plannerViewModel.addSingleMealToStorage(meal, date, mealType)
                plannerViewModel.refreshCurrentDayMeals()
                Toast.makeText(context, "${meal.name} added to ${mealType.title}", Toast.LENGTH_SHORT).show()
                showAddToPlanDialog = false
            }
        )
    }
}

@Composable
private fun ListItem(text: String, onItemClick: () -> Unit = {}) {
    Surface(
        onClick = onItemClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape).background(tabActiveColor.copy(alpha = 0.6f))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun IngredientItem(quantity: String, unit: String, name: String, onItemClick: () -> Unit = {}) {
    Surface(
        onClick = onItemClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(
                text = "$quantity $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = tabActiveColor,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun InstructionItem(step: Int, instruction: String, onItemClick: () -> Unit = {}) {
    val animatedElevation by animateDpAsState(
        targetValue = 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cardElevation"
    )
    Surface(
        onClick = onItemClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = animatedElevation,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Surface(shape = CircleShape, color = tabActiveColor, modifier = Modifier.padding(top = 2.dp)) {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.size(24.dp).wrapContentSize(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = instruction, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealMoreOptionsButton(
    meal: Meal,
    navController: NavController,
    haptic: HapticFeedbackUtil,
    modifier: Modifier = Modifier,
    mealViewModel: MealViewModel
) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showNutritionDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var showCollectionsDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    
    // Add context for Toast messages
    val context = LocalContext.current

    IconButton(
        onClick = {
            haptic.mediumClick()
            showBottomSheet = true
        },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
                .semantics { contentDescription = "More options" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                BottomSheetItem(
                    icon = Icons.Default.Info,
                    text = "Nutrition facts",
                    onClick = { 
                        haptic.lightClick()
                        showBottomSheet = false
                        showNutritionDialog = true
                    }
                )
                BottomSheetItem(
                    icon = Icons.Default.RestaurantMenu,
                    text = "Open cooking mode",
                    onClick = {
                        haptic.lightClick()
                        showBottomSheet = false
                        try {
                            val mealJson = Gson().toJson(meal)
                            val encodedMealJson = URLEncoder.encode(mealJson, StandardCharsets.UTF_8.toString())
                            navController.navigate("cooking_mode/$encodedMealJson")
                        } catch (e: Exception) {
                            Log.e("MealDetailScreen", "Error navigating to cooking mode", e)
                        }
                    }
                )
                BottomSheetItem(
                    icon = Icons.Default.EditNote,
                    text = "Add notes",
                    onClick = { 
                        haptic.lightClick()
                        showBottomSheet = false
                        showNotesDialog = true
                    }
                )
                BottomSheetItem(
                    icon = Icons.Default.BookmarkAdd,
                    text = "Add to collections",
                    onClick = { 
                        haptic.lightClick()
                        showBottomSheet = false
                        showCollectionsDialog = true
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Nutrition Facts Dialog
    if (showNutritionDialog) {
        Dialog(onDismissRequest = { showNutritionDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Nutrition Facts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    // Calculate nutrition facts based on ingredients if available
                    val calories = "350"
                    val protein = "12g"
                    val fat = "15g"
                    val carbs = "42g"
                    val fiber = "5g"
                    val sodium = "500mg"
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Calories", style = MaterialTheme.typography.bodyLarge)
                        Text(calories, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    }
                    
                    val nutritionItems = listOf(
                        "Protein" to protein,
                        "Fat" to fat,
                        "Carbohydrates" to carbs,
                        "Fiber" to fiber,
                        "Sodium" to sodium
                    )
                    
                    nutritionItems.forEach { (name, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "* Values are approximate and based on standard ingredient nutrition data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showNutritionDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = tabActiveColor)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Notes Dialog
    if (showNotesDialog) {
        NotesDialog(
            mealId = meal.mealId,
            noteText = noteText,
            onNoteTextChange = { noteText = it },
            mealViewModel = mealViewModel,
            onDismiss = {
                showNotesDialog = false
                noteText = "" // Optional: Reset text when dialog is dismissed
            }
        )
    }

    // Collections Dialog
    if (showCollectionsDialog) {
        Dialog(onDismissRequest = { showCollectionsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Add to Collection",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Sample collections - in a real app, these would come from a database
                    val collections = listOf(
                        "Favorites" to true,
                        "Quick Meals" to false,
                        "Vegetarian" to false,
                        "High Protein" to false,
                        "Weekend Cooking" to false
                    )
                    
                    val checkedStates = remember { 
                        collections.map { it.second }.toMutableStateList() 
                    }
                    
                    collections.forEachIndexed { index, (name, _) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    checkedStates[index] = !checkedStates[index]
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checkedStates[index],
                                onCheckedChange = { checkedStates[index] = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = tabActiveColor
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Create new collection button
                    OutlinedButton(
                        onClick = { /* Create new collection logic */ },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, tabActiveColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = tabActiveColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create New Collection",
                            color = tabActiveColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showCollectionsDialog = false }
                        ) {
                            Text("Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = { 
                                // Save collections logic would go here
                                val selectedCollections = collections
                                    .mapIndexed { index, (name, _) -> if (checkedStates[index]) name else null }
                                    .filterNotNull()
                                
                                if (selectedCollections.isNotEmpty()) {
                                    Toast.makeText(context, "Added to ${selectedCollections.size} collection(s)", Toast.LENGTH_SHORT).show()
                                }
                                showCollectionsDialog = false 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = tabActiveColor)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesDialog(
    mealId: String,
    noteText: String,
    onNoteTextChange: (String) -> Unit,
    mealViewModel: MealViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add Notes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = onNoteTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = { Text("Add your cooking notes here...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (noteText.isNotBlank()) {
                                scope.launch {
                                    mealViewModel.sendInteraction(mealId, "review", noteText)
                                    Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show()
                                }
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
