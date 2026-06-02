package com.example.mealflow.ui.screens.planner

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mealflow.data.model.DayPlan
import com.example.mealflow.data.model.MealType
import com.example.mealflow.data.model.PlannedMeal
import com.example.mealflow.navigation.NavRoutes
import com.example.mealflow.utils.DateUtils
import com.example.mealflow.viewModel.MealPlannerViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllPlansScreen(
    viewModel: MealPlannerViewModel,
    navController: NavController,
    onNavigateBack: () -> Unit,
    onAddMealToDate: (date: String, mealType: MealType) -> Unit,
) {
    val currentMonthString by viewModel.currentDisplayMonthString.collectAsState()
    val selectedWeek by viewModel.selectedWeek.collectAsState()
    val weeklyPlans by viewModel.weeklyPlans.collectAsState()
    val numberOfWeeks by viewModel.numberOfWeeksInMonth.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    val filteredPlans = remember(weeklyPlans, searchQuery) {
        if (searchQuery.isBlank()) {
            weeklyPlans
        } else {
            weeklyPlans.filter { dayPlan ->
                val searchLower = searchQuery.lowercase()
                val allMeals = dayPlan.breakfastMeals + dayPlan.lunchMeals +
                        dayPlan.dinnerMeals + dayPlan.snackMeals + dayPlan.otherMeals

                allMeals.any { plannedMeal ->
                    plannedMeal.meal.name.lowercase().contains(searchLower) ||
                            plannedMeal.meal.description?.lowercase()?.contains(searchLower) == true
                } || DateUtils.formatDateWithDay(dayPlan.date).lowercase().contains(searchLower)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.selectWeek(viewModel.selectedWeek.value)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 2.dp,
                modifier = Modifier.animateContentSize()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    // Top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Meal Plans",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { showSearch = !showSearch }) {
                            Icon(
                                imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (showSearch) "Close search" else "Search"
                            )
                        }
                    }

                    // Search bar
                    AnimatedVisibility(
                        visible = showSearch,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search meals or dates...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            }
                        )
                    }

                    // Month navigation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousMonth() }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                        }
                        Text(
                            currentMonthString,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { viewModel.nextMonth() }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                        }
                    }

                    // Week selector
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        items(numberOfWeeks) { index ->
                            val weekNum = index + 1
                            WeekButton(
                                weekNumber = weekNum,
                                isSelected = selectedWeek == weekNum,
                                onClick = { viewModel.selectWeek(weekNum) }
                            )
                        }
                    }
                }
            }

            // Content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                filteredPlans.isEmpty() -> {
                    EmptyState(
                        message = if (searchQuery.isNotBlank()) "No meals found for '$searchQuery'." else "No meals planned for this week.",
                        icon = if (searchQuery.isNotBlank()) Icons.Default.SearchOff else Icons.Default.DateRange
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        flingBehavior = ScrollableDefaults.flingBehavior()
                    ) {
                        items(filteredPlans, key = { it.date }) { dayPlan ->
                            AllEnhancedDaySection(
                                date = dayPlan.date,
                                plannedMealsOnDate = dayPlan.breakfastMeals + dayPlan.lunchMeals + dayPlan.dinnerMeals + dayPlan.snackMeals + dayPlan.otherMeals,
                                onAddBreakfastClick = { onAddMealToDate(dayPlan.date, MealType.BREAKFAST) },
                                onAddLunchClick = { onAddMealToDate(dayPlan.date, MealType.LUNCH) },
                                onAddDinnerClick = { onAddMealToDate(dayPlan.date, MealType.DINNER) },
                                onAddSnackClick = { onAddMealToDate(dayPlan.date, MealType.SNACK) },
                                onAddOtherClick = { onAddMealToDate(dayPlan.date, MealType.OTHER) },
                                onMealCardClick = { mealId: String ->
                                    navController.navigate(NavRoutes.MealDetailPage.createMealDetailRoute(mealId))
                                },
                                onRemoveMealClick = { plannedMealId: String ->
                                    viewModel.deletePlannedMeal(plannedMealId)
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekButton(weekNumber: Int, isSelected: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = if (isSelected) ButtonDefaults.buttonElevation(2.dp) else null
    ) {
        Text(
            text = "Week $weekNumber",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun EmptyState(message: String, icon: ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AllEnhancedDaySection(
    date: String,
    plannedMealsOnDate: List<PlannedMeal>,
    onAddBreakfastClick: () -> Unit,
    onAddLunchClick: () -> Unit,
    onAddDinnerClick: () -> Unit,
    onAddSnackClick: () -> Unit,
    onAddOtherClick: () -> Unit,
    onMealCardClick: (String) -> Unit,
    onRemoveMealClick: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isToday = DateUtils.isToday(date)

    val mealTypesInOrder = listOf(
        MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK, MealType.OTHER
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = if (isToday) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Day Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = DateUtils.formatDateWithDay(date),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isToday) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            "Today",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Meal Sections
            mealTypesInOrder.forEach { mealType ->
                val mealsForType = plannedMealsOnDate.filter { it.getMealTypeEnum() == mealType }
                MealTypeSection(
                    mealType = mealType,
                    meals = mealsForType,
                    onAddClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        when (mealType) {
                            MealType.BREAKFAST -> onAddBreakfastClick()
                            MealType.LUNCH -> onAddLunchClick()
                            MealType.DINNER -> onAddDinnerClick()
                            MealType.SNACK -> onAddSnackClick()
                            MealType.OTHER -> onAddOtherClick()
                        }
                    },
                    onMealCardClick = onMealCardClick,
                    onRemoveMealClick = onRemoveMealClick
                )
            }
        }
    }
}

@Composable
fun MealTypeSection(
    mealType: MealType,
    meals: List<PlannedMeal>,
    onAddClick: () -> Unit,
    onMealCardClick: (String) -> Unit,
    onRemoveMealClick: (String) -> Unit
) {
    var mealToDelete by remember { mutableStateOf<PlannedMeal?>(null) }

    if (mealToDelete != null) {
        AlertDialog(
            onDismissRequest = { mealToDelete = null },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to remove \"${mealToDelete!!.meal.name}\" from your plan?") },
            confirmButton = {
                Button(
                    onClick = {
                        mealToDelete!!.id.let { onRemoveMealClick(it) }
                        mealToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { mealToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = mealType.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (meals.isEmpty()) {
                IconButton(onClick = onAddClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        contentDescription = "Add ${mealType.title}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (meals.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                meals.forEach { plannedMeal ->
                    PlannedMealCard(
                        plannedMeal = plannedMeal,
                        onClick = { onMealCardClick(plannedMeal.meal.mealId) },
                        onRemove = { mealToDelete = plannedMeal }
                    )
                }
            }
        } else {
            // Placeholder for empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No ${mealType.title.lowercase()} planned",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannedMealCard(
    plannedMeal: PlannedMeal,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Card(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = plannedMeal.meal.imageUrl,
                contentDescription = plannedMeal.meal.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plannedMeal.meal.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRemove()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove meal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}