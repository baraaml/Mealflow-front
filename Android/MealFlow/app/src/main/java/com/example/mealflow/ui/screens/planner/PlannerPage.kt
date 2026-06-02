package com.example.mealflow.ui.screens.planner

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.model.MealType
import com.example.mealflow.data.model.PlannedMeal
import com.example.mealflow.data.repository.MealPlannerRepository
import com.example.mealflow.ui.components.AddToPlanDialog
import com.example.mealflow.ui.components.MealPlanCard
import com.example.mealflow.ui.components.MealimePlanCard
import com.example.mealflow.ui.components.PulsatingEffect
import com.example.mealflow.navigation.NavRoutes
import com.example.mealflow.ui.animations.EnhancedAnimations.EnhancedPulsatingEffect
import com.example.mealflow.utils.DateUtils
import com.example.mealflow.viewModel.MealPlannerViewModel
import com.example.mealflow.viewModel.MealViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background

/**
 * Merged PlannerPage that includes both the date navigation and meal planning functionality
 */
@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlannerPage(
    viewModel: MealPlannerViewModel,
    mealViewModel: MealViewModel,
    navController: NavHostController,
    onNavigateToMealDetails: (String) -> Unit,
    onPlanMealsClick: () -> Unit,
    onSeeAllPlansClick: () -> Unit
) {
    // State collection
    val currentDateForNavigator by viewModel.currentDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val errorMessage by viewModel.errorState.collectAsState()
    val currentDayMeals by viewModel.currentDayMeals.collectAsState()

    // Computed properties
    val isCurrentDateToday = currentDateForNavigator == LocalDate.now()
    val cookedMealsCount = currentDayMeals.count { it.isCooked }
    val totalMealsCount = currentDayMeals.size
    val progressPercentage = if (totalMealsCount > 0) cookedMealsCount.toFloat() / totalMealsCount else 0f

    // FAB menu expansion state
    var isFabExpanded by remember { mutableStateOf(false) }

    // Initialize current date and ensure meal data is loaded
    LaunchedEffect(currentDateForNavigator) {
        viewModel.setCurrentDate(currentDateForNavigator)
    }

    LaunchedEffect(Unit) {
        if (viewModel.currentDate.value == LocalDate.MIN ||
            (currentDayMeals.isEmpty() && viewModel.currentDate.value != LocalDate.now())) {
            viewModel.setCurrentDate(LocalDate.now())
        } else {
            viewModel.setCurrentDate(viewModel.currentDate.value)
        }
    }

    // Dialog state management
    var mealForDialog by remember { mutableStateOf<Meal?>(null) }
    var initialDateForDialogState by remember { mutableStateOf(currentDateForNavigator) }

    val backStackEntry = navController.currentBackStackEntry
    val mealIdState = backStackEntry?.savedStateHandle?.getLiveData<String>("mealIdForDialog")?.observeAsState()
    val initialDateState = backStackEntry?.savedStateHandle?.getLiveData<String>("initialDateForDialog")?.observeAsState()

    val receivedMealIdForDialog = mealIdState?.value
    val receivedInitialDateStrForDialog = initialDateState?.value

    LaunchedEffect(receivedMealIdForDialog, receivedInitialDateStrForDialog) {
        if (receivedMealIdForDialog != null && receivedInitialDateStrForDialog != null) {
            val meal = mealViewModel.findMealById(receivedMealIdForDialog)
            if (meal != null) {
                mealForDialog = meal
                initialDateForDialogState = DateUtils.parseLocalDate(receivedInitialDateStrForDialog) ?: currentDateForNavigator
            }
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("mealIdForDialog")
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("initialDateForDialog")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Page Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                ) {
                    Column {
                        Text(
                            text = "Meal Planner",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isLoading) {
                        EnhancedPulsatingEffect(
                            minScale = 0.6f,
                            maxScale = 1.0f,
                            minAlpha = 0.3f,
                            maxAlpha = 0.7f,
                            durationMs = 1000,
                            content = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.Center),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date Navigator
                DateNavigator(
                    currentDate = currentDateForNavigator,
                    onPreviousDay = { viewModel.navigateToPreviousDay() },
                    onNextDay = { viewModel.navigateToNextDay() },
                    isToday = isCurrentDateToday
                )

                // Main Content Area
                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading && currentDayMeals.isEmpty()) {
                        LoadingMealsContent()
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Progress indicator for today's meals (only if meals exist and it's today)
                            if (currentDayMeals.isNotEmpty() && isCurrentDateToday) {
                                MealProgressIndicator(
                                    progressPercentage = progressPercentage,
                                    cookedCount = cookedMealsCount,
                                    totalCount = totalMealsCount
                                )
                            }

                            // Main meal content
                            if (currentDayMeals.isEmpty()) {
                                EmptyStateContentPlanMeals(
                                    onPlanMealsClick = onPlanMealsClick,
                                    isToday = isCurrentDateToday,
                                    navController = navController
                                )
                            } else {
                                val groupedMeals = currentDayMeals.groupBy { it.getMealTypeEnum() }
                                MealsListContentPlanMeals(
                                    groupedMeals = groupedMeals,
                                    onPlanMealsClick = onPlanMealsClick,
                                    onMealCardClick = onNavigateToMealDetails,
                                    onMarkAsCooked = { plannedMealId ->
                                        viewModel.toggleCookedStatus(plannedMealId)
                                    },
                                    onDeleteMeal = { plannedMealId ->
                                        viewModel.deletePlannedMeal(plannedMealId)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Semi-transparent scrim when FAB menu is expanded
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                        .pointerInput(Unit) {
                            detectTapGestures { isFabExpanded = false }
                        }
                )
            }

            // Expandable FAB menu
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .zIndex(2f),
                horizontalAlignment = Alignment.End
            ) {
                // Add Meal option
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = scaleIn(initialScale = 0.8f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.8f) + fadeOut()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (currentDayMeals.isEmpty()) {
                                // Navigate to DaysSelectionScreen with the current date the user is viewing
                                navController.navigate(NavRoutes.DaysSelectionScreen.route)
                            } else {
                                onPlanMealsClick()
                            }
                            isFabExpanded = false
                        },
                        modifier = Modifier.padding(bottom = 16.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Restaurant,
                                contentDescription = "Add Meal"
                            )
                        },
                        text = { Text("Add Meal") }
                    )
                }

                // See All Plans option
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = scaleIn(initialScale = 0.8f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.8f) + fadeOut()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            onSeeAllPlansClick()
                            isFabExpanded = false
                        },
                        modifier = Modifier.padding(bottom = 16.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = "See All Plans"
                            )
                        },
                        text = { Text("All Plans") }
                    )
                }

                // Main FAB
                val rotation by animateFloatAsState(
                    targetValue = if (isFabExpanded) 45f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "fabRotation"
                )

                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Menu",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

            // Error Snackbar
            errorMessage?.let { error ->
                var showError by remember { mutableStateOf(true) }

                if (showError) {
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                            .imePadding()
                            .zIndex(3f),
                        action = {
                            TextButton(onClick = { showError = false }) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text(error)
                    }
                }
            }

            // AddToPlanDialog
            if (mealForDialog != null) {
                AddToPlanDialog(
                    meal = mealForDialog!!,
                    initialDate = initialDateForDialogState,
                    onDismiss = { mealForDialog = null },
                    onAddToPlan = { actualDate, actualMealType ->
                        viewModel.addSingleMealToStorage(mealForDialog!!, actualDate, actualMealType)
                        mealForDialog = null
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateNavigator(
    currentDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    isToday: Boolean
) {
    // Debug logging
    LaunchedEffect(currentDate) {
        println("DateNavigator received date: $currentDate")
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = "Navigate to previous day" }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val displayText = remember(currentDate, isToday) {
                    if (isToday) {
                        "Today"
                    } else {
                        currentDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                    }
                }

                Text(
                    text = displayText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                if (!isToday) {
                    val dayOfWeekText = remember(currentDate) {
                        currentDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    }

                    Text(
                        text = dayOfWeekText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onNextDay,
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = "Navigate to next day" }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MealProgressIndicator(
    progressPercentage: Float,
    cookedCount: Int,
    totalCount: Int
) {
    val cachedProgressPercentage = remember(progressPercentage) { progressPercentage }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Today's Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "$cookedCount/$totalCount meals",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                val animatedProgress by animateFloatAsState(
                    targetValue = cachedProgressPercentage,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = LinearEasing
                    ),
                    label = "progressAnimation"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color = MaterialTheme.colorScheme.primary)
                )
            }

            if (cachedProgressPercentage == 1f) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "All meals completed today!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateContentPlanMeals(
    onPlanMealsClick: () -> Unit,
    isToday: Boolean,
    navController: NavHostController // Add navController parameter
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(350.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isToday) "Plan Today's Meals" else "No Meals Planned",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    if (isToday)
                        "Start planning your meals for today to stay on track with your nutrition goals!"
                    else
                        "No meals planned for this day. Add some meals to your plan!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                )

                Button(
                    onClick = {
                        // Navigate to DaysSelectionScreen when no meals are planned
                        navController.navigate(NavRoutes.DaysSelectionScreen.route)
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Plan Meals")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MealsListContentPlanMeals(
    groupedMeals: Map<MealType, List<PlannedMeal>>,
    onPlanMealsClick: () -> Unit,
    onMealCardClick: (String) -> Unit,
    onMarkAsCooked: (String) -> Unit,
    onDeleteMeal: (String) -> Unit
) {
    var mealToDelete by remember { mutableStateOf<PlannedMeal?>(null) }
    val coroutineScope = rememberCoroutineScope()

    if (mealToDelete != null) {
        AlertDialog(
            onDismissRequest = { mealToDelete = null },
            title = { Text("Delete Meal") },
            text = { Text("Are you sure you want to delete this meal from your plan?") },
            confirmButton = {
                Button(onClick = {
                    mealToDelete?.let {
                        onDeleteMeal(it.id)
                    }
                    mealToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { mealToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        groupedMeals.forEach { (mealType, mealsForType) ->
            item {
                MealTypeHeader(mealType = mealType)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (mealsForType.isEmpty()) {
                item {
                    Text(
                        text = "No meals planned for ${mealType.name}.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(mealsForType, key = { it.id }) { plannedMeal ->
                    val dismissState = rememberSwipeToDismissBoxState()

                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                            when (dismissState.currentValue) {
                                SwipeToDismissBoxValue.EndToStart -> onMarkAsCooked(plannedMeal.id)
                                SwipeToDismissBoxValue.StartToEnd -> mealToDelete = plannedMeal
                                else -> {}
                            }
                            dismissState.reset()
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .animateItemPlacement(),
                        backgroundContent = {
                            SwipeBackground(dismissState = dismissState)
                        }
                    ) {
                        MealimePlanCard(
                            meal = plannedMeal.meal,
                            mealType = plannedMeal.getMealTypeEnum(),
                            onCardClick = { onMealCardClick(plannedMeal.meal.mealId) },
                            onCookClick = { onMarkAsCooked(plannedMeal.id) },
                            isCooked = plannedMeal.isCooked,
                            isPlanned = true
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val direction = dismissState.dismissDirection

    val color = when (direction) {
        SwipeToDismissBoxValue.EndToStart -> Color(0xFF4CAF50)
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.error
        else -> Color.Transparent
    }

    val alignment = when (direction) {
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterStart
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    val icon = when (direction) {
        SwipeToDismissBoxValue.EndToStart -> Icons.Rounded.Check
        SwipeToDismissBoxValue.StartToEnd -> Icons.Rounded.Delete
        else -> null
    }

    val scale by animateFloatAsState(
        targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "SwipeIconScale"
    )

    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(48.dp)
                    .background(color, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Header for meal types (e.g., Breakfast, Lunch).
 * Displays the meal type title with a corresponding icon.
 */
@Composable
fun MealTypeHeader(mealType: MealType) {
    // Implementation of MealTypeHeader composable
}

@Composable
fun LoadingMealsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {}

        repeat(3) {
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    val sharedTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by sharedTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
        tonalElevation = 1.dp
    ) {}
}