// File: app/src/main/java/com/example/mealflow/ui/screens/PlanConfigScreen.kt
package com.example.mealflow.ui.screens.planner

import android.app.DatePickerDialog
import android.os.Build
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.ui.graphics.Brush
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.model.MealType
import com.example.mealflow.data.model.PlannedMeal
import com.example.mealflow.utils.DateUtils
import com.example.mealflow.viewModel.MealPlannerViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanConfigScreen(
    daysCount: Int,
    viewModel: MealPlannerViewModel,
    onNavigateBack: () -> Unit,
    onSavePlanClick: () -> Unit,
    onAddMealToDay: (date: String, mealType: MealType) -> Unit
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    // Initialize startDate from the MealPlannerViewModel's currentDate
    var startDate by remember { mutableStateOf(viewModel.currentDate.value) }
    val endDate by remember(startDate, daysCount) {
        mutableStateOf(startDate.plusDays(daysCount - 1L))
    }

    val planDates = remember(startDate, daysCount) {
        DateUtils.generatePlanDates(DateUtils.formatToISO(startDate), daysCount)
    }

    val newPlanMeals by viewModel.newPlanMeals.collectAsState()

    // Calculate progress more accurately
    val totalMealSlots = planDates.size * 3 // Focus on main meals
    val filledMealSlots = newPlanMeals.count {
        it.getMealTypeEnum() in listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)
    }

    val calendar = Calendar.getInstance()
    val startDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            startDate = LocalDate.of(year, month + 1, day)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Animated save button visibility
    var showSaveButton by remember { mutableStateOf(false) }
    LaunchedEffect(filledMealSlots) {
        delay(300)
        showSaveButton = filledMealSlots > 0
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Enhanced Header with better typography and spacing
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
                        IconButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateBack()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Title and subtitle
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Configure Your Plan",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val startDateFormatted = DateUtils.formatDateConcise(DateUtils.formatToISO(startDate))
                            val endDateFormatted = DateUtils.formatDateConcise(DateUtils.formatToISO(endDate))

                            Text(
                                "$startDateFormatted - $endDateFormatted",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Enhanced Progress Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Progress",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "$filledMealSlots/$totalMealSlots meals",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { if (totalMealSlots > 0) filledMealSlots.toFloat() / totalMealSlots else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // Main Content
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                flingBehavior = ScrollableDefaults.flingBehavior()
            ) {
                items(planDates, key = { it }) { date ->
                    val mealsForThisDate = newPlanMeals.filter { it.date == date }
                    EnhancedDaySection(
                        date = date,
                        plannedMealsOnDate = mealsForThisDate,
                        onAddBreakfastClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddMealToDay(date, MealType.BREAKFAST)
                        },
                        onAddLunchClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddMealToDay(date, MealType.LUNCH)
                        },
                        onAddDinnerClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddMealToDay(date, MealType.DINNER)
                        },
                        onAddSnackClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddMealToDay(date, MealType.SNACK)
                        },
                        onAddOtherClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddMealToDay(date, MealType.OTHER)
                        },
                        onMealCardClick = { mealId ->
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Handle meal card click
                        },
                        onRemoveMealClick = { plannedMealId ->
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.removeMealFromNewPlan(plannedMealId)
                        }
                    )
                }

                // Add padding at the bottom for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Floating Action Button with enhanced animation
        AnimatedVisibility(
            visible = showSaveButton,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn() + scaleIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring()
            ) + fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.saveNewPlanToStorage()
                    onSavePlanClick()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(Icons.Filled.Save, contentDescription = "Save Plan")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Save Plan",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EnhancedDaySection(
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
    // Get the day name and concise date format
    val dayName = try {
        val localDate = DateUtils.parseLocalDate(date)
        localDate?.dayOfWeek?.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
    } catch (e: Exception) {
        null
    }

    val conciseDate = DateUtils.formatDateConcise(date)
    val formattedDate = if (dayName != null) "$dayName" else conciseDate

    val breakfast = plannedMealsOnDate.find { it.getMealTypeEnum() == MealType.BREAKFAST }
    val lunch = plannedMealsOnDate.find { it.getMealTypeEnum() == MealType.LUNCH }
    val dinner = plannedMealsOnDate.find { it.getMealTypeEnum() == MealType.DINNER }
    val snack = plannedMealsOnDate.find { it.getMealTypeEnum() == MealType.SNACK }
    val other = plannedMealsOnDate.find { it.getMealTypeEnum() == MealType.OTHER }

    val mainMealsCount = listOf(breakfast, lunch, dinner).count { it != null }
    val totalMealsCount = plannedMealsOnDate.size

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Enhanced Day Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = conciseDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Completion Badge
                Surface(
                    color = if (mainMealsCount == 3) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (mainMealsCount == 3) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (mainMealsCount == 3) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$mainMealsCount/3",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (mainMealsCount == 3) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Meals - Consistent Grid Layout
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Breakfast
                MealSlotCard(
                    mealType = MealType.BREAKFAST,
                    plannedMeal = breakfast,
                    onAddClick = onAddBreakfastClick,
                    onCardClick = { breakfast?.meal?.mealId?.let { onMealCardClick(it.toString()) } },
                    onRemoveClick = { breakfast?.id?.let { onRemoveMealClick(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Lunch
                MealSlotCard(
                    mealType = MealType.LUNCH,
                    plannedMeal = lunch,
                    onAddClick = onAddLunchClick,
                    onCardClick = { lunch?.meal?.mealId?.let { onMealCardClick(it.toString()) } },
                    onRemoveClick = { lunch?.id?.let { onRemoveMealClick(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Dinner
                MealSlotCard(
                    mealType = MealType.DINNER,
                    plannedMeal = dinner,
                    onAddClick = onAddDinnerClick,
                    onCardClick = { dinner?.meal?.mealId?.let { onMealCardClick(it.toString()) } },
                    onRemoveClick = { dinner?.id?.let { onRemoveMealClick(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Snacks Section (Collapsible)
            if (snack != null || other != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Snacks & Others",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (snack != null) {
                        MealSlotCard(
                            mealType = MealType.SNACK,
                            plannedMeal = snack,
                            onAddClick = onAddSnackClick,
                            onCardClick = { snack.meal?.mealId?.let { onMealCardClick(it.toString()) } },
                            onRemoveClick = { snack.id?.let { onRemoveMealClick(it) } },
                            modifier = Modifier.weight(1f),
                            isCompact = true
                        )
                    }

                    if (other != null) {
                        MealSlotCard(
                            mealType = MealType.OTHER,
                            plannedMeal = other,
                            onAddClick = onAddOtherClick,
                            onCardClick = { other.meal?.mealId?.let { onMealCardClick(it.toString()) } },
                            onRemoveClick = { other.id?.let { onRemoveMealClick(it) } },
                            modifier = Modifier.weight(1f),
                            isCompact = true
                        )
                    }
                }
            } else {
                // Add snacks button
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealSlotCard(
                        mealType = MealType.SNACK,
                        plannedMeal = null,
                        onAddClick = onAddSnackClick,
                        onCardClick = {},
                        onRemoveClick = {},
                        modifier = Modifier.weight(1f),
                        isCompact = true
                    )

                    MealSlotCard(
                        mealType = MealType.OTHER,
                        plannedMeal = null,
                        onAddClick = onAddOtherClick,
                        onCardClick = {},
                        onRemoveClick = {},
                        modifier = Modifier.weight(1f),
                        isCompact = true
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MealSlotCard(
    mealType: MealType,
    plannedMeal: PlannedMeal?,
    onAddClick: () -> Unit,
    onCardClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val mealIcon = when (mealType) {
        MealType.BREAKFAST -> Icons.Default.WbSunny
        MealType.LUNCH -> Icons.Default.Restaurant
        MealType.DINNER -> Icons.Default.NightsStay
        MealType.SNACK -> Icons.Default.Icecream
        MealType.OTHER -> Icons.Default.MoreHoriz
    }

    val typeColor = when(mealType) {
        MealType.BREAKFAST -> Color(0xFFFF9800) // Orange
        MealType.LUNCH -> Color(0xFF2196F3) // Blue
        MealType.DINNER -> Color(0xFF9C27B0) // Purple
        MealType.SNACK -> Color(0xFF4CAF50) // Green
        MealType.OTHER -> Color(0xFF607D8B) // Blue Grey
    }

    if (plannedMeal != null) {
        // Meal Card with consistent design
        Card(
            onClick = onCardClick,
            modifier = modifier.height(if (isCompact) 80.dp else 120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = BorderStroke(1.dp, typeColor.copy(alpha = 0.5f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Meal Type Icon
                    Surface(
                        color = typeColor.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(if (isCompact) 40.dp else 48.dp)
                    ) {
                        Icon(
                            mealIcon,
                            contentDescription = mealType.title,
                            tint = typeColor,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (isCompact) 10.dp else 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Meal Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            mealType.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = typeColor,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            plannedMeal.meal?.name ?: "Unknown Meal",
                            style = if (isCompact) MaterialTheme.typography.bodySmall
                            else MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            maxLines = if (isCompact) 1 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Remove button
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .padding(4.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove meal",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Add Meal Button with consistent design
        OutlinedCard(
            onClick = onAddClick,
            modifier = modifier.height(if (isCompact) 80.dp else 120.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, typeColor.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = typeColor.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(if (isCompact) 32.dp else 40.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add ${mealType.title}",
                        tint = typeColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (isCompact) 8.dp else 10.dp)
                    )
                }

                if (!isCompact) {
                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            "Add ${mealType.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = typeColor,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Plan your meal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}