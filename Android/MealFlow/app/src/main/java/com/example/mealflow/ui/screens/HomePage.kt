package com.example.mealflow.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mealflow.data.model.Meal
import com.example.mealflow.navigation.NavRoutes
import com.example.mealflow.network.InteractionRequest
import com.example.mealflow.ui.components.EmptyStateGenericHomePage
import com.example.mealflow.ui.components.EmptyStatePlannedMeals
import com.example.mealflow.ui.components.EnhancedMealCarousel
import com.example.mealflow.ui.components.ErrorSnackbar
import com.example.mealflow.ui.components.LoadingCarouselHomePage
import com.example.mealflow.ui.components.MealimeCarousel
import com.example.mealflow.ui.components.PlannedMealCard
import com.example.mealflow.ui.components.PlannedMealCarousel
import com.example.mealflow.ui.components.PlannedMealimeCarousel
import com.example.mealflow.ui.components.PlannedMealInfo
import com.example.mealflow.ui.components.SectionHeader
import com.example.mealflow.ui.components.TelegramStyleDrawer
import com.example.mealflow.utils.rememberHapticFeedback
import com.example.mealflow.viewModel.MealPlannerViewModel
import com.example.mealflow.viewModel.MealViewModel
import java.time.LocalDate
import java.util.Calendar
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePage(
    userName: String = "User",
    onMealClick: (Meal) -> Unit, // For clicking on backend-sourced meals
    onPlannedMealClick: (String) -> Unit, // For clicking on locally planned meals (pass meal ID from PlannedMeal.meal.mealId)
    mealViewModel: MealViewModel,
    mealPlannerViewModel: MealPlannerViewModel,
    navController: NavController
) {
    var isDrawerOpen by remember { mutableStateOf(false) }
    val isRefreshing by mealViewModel.isLoading.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            mealViewModel.refreshMeals()
            mealPlannerViewModel.refreshCurrentDayMeals()
        }
    )

    TelegramStyleDrawer(
        isOpen = isDrawerOpen,
        onOpenChange = { isDrawerOpen = it },
        navController = navController
    ) {
        val calendar = Calendar.getInstance()
        val greeting = when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }

        // States from MealViewModel (for trending, recommendations from backend)
        val isLoadingBackendMeals by mealViewModel.isLoading.collectAsState()
        val errorMessageBackend by mealViewModel.errorMessage.collectAsState()
        // Assuming MealViewModel has these distinct flows now:
        val trendingMeals by mealViewModel.trendingMealsVM.collectAsState()
        val personalizedMeals by mealViewModel.collaborativeMealsVM.collectAsState()

        // States from MealPlannerViewModel (for locally planned meals for today)
        LaunchedEffect(Unit) {
            mealPlannerViewModel.setCurrentDate(LocalDate.now()) // Ensure "Today's Plan" shows today's meals
        }
        val locallyPlannedMealsForToday by mealPlannerViewModel.currentDayMeals.collectAsState()

        // Error handling for backend meals
        var errorShown by remember { mutableStateOf(false) }
        LaunchedEffect(errorMessageBackend) {
            if (!errorMessageBackend.isNullOrBlank()) {
                errorShown = false // Reset to allow showing new errors
            }
        }

        // Load backend-sourced sections (if not handled by ViewModel's init)
        LaunchedEffect(key1 = "loadBackendDataForHome") {
            if (trendingMeals.isEmpty()) mealViewModel.fetchTrendingMeals()
            if (personalizedMeals.isEmpty()) mealViewModel.fetchCollaborativeMeals()
        }

        Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Greeting section with menu button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$greeting,",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { isDrawerOpen = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Menu",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Section 1: Locally Planned Meals for Today
                SectionHeader(
                    title = "Today's Plan",
                    onViewAll = { navController.navigate(NavRoutes.PlannerPage.route) },
                    showViewAll = true
                )
                if (locallyPlannedMealsForToday.isNotEmpty()) {
                    // Extract meals and planned info from the planned meals
                    val meals = locallyPlannedMealsForToday.take(6).map { it.meal }
                    val plannedMealInfoList = locallyPlannedMealsForToday.take(6).map { plannedMeal ->
                        PlannedMealInfo(
                            isPlanned = true,
                            isCooked = plannedMeal.isCooked, // Assuming this exists on your planned meal object
                            mealType = plannedMeal.mealType, // Assuming this exists on your planned meal object
                        )
                    }

                    PlannedMealimeCarousel(
                        meals = meals,
                        plannedMealInfoList = plannedMealInfoList,
                        onMealClick = { meal ->
                            onPlannedMealClick(meal.mealId.toString())
                        }
                    )
                } else {
                    EmptyStatePlannedMeals(
                        message = "No plans for today!",
                        buttonText = "Plan Some Meals",
                        onButtonClick = { navController.navigate(NavRoutes.PlannerPage.route) }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: Trending Meals (from backend)
                SectionHeader(
                    title = "Trending",
                    onViewAll = { navController.navigate(NavRoutes.AllMealsPage.createAllMealsRoute("Trending")) },
                    showViewAll = true
                )

                // Preload meal details for trending meals
                LaunchedEffect(trendingMeals) {
                    if (trendingMeals.isNotEmpty()) {
                        mealViewModel.preloadMealDetails(trendingMeals.map { it.mealId })
                    }
                }
                
                MealimeCarousel(
                    meals = trendingMeals,
                    onMealClick = onMealClick,
                    onSwipeUp = { meal -> mealViewModel.likeMeal(meal) },
                    onSwipeDown = { meal -> mealViewModel.ignoreMeal(meal) },
                    isLoading = isLoadingBackendMeals && trendingMeals.isEmpty()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Section 3: Recommendations (from backend)
                SectionHeader(
                    title = "Recommended",
                    onViewAll = { navController.navigate(NavRoutes.AllMealsPage.createAllMealsRoute("Recommended")) },
                    showViewAll = true
                )

                // Preload meal details for personalized meals
                LaunchedEffect(personalizedMeals) {
                    if (personalizedMeals.isNotEmpty()) {
                        mealViewModel.preloadMealDetails(personalizedMeals.map { it.mealId })
                    }
                }
                
                MealimeCarousel(
                    meals = personalizedMeals,
                    onMealClick = onMealClick,
                    onSwipeUp = { meal -> mealViewModel.likeMeal(meal) },
                    onSwipeDown = { meal -> mealViewModel.ignoreMeal(meal) },
                    isLoading = isLoadingBackendMeals && personalizedMeals.isEmpty()
                )

                Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for FAB clearance
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
            if (!errorMessageBackend.isNullOrBlank() && !errorShown) {
                ErrorSnackbar(
                    message = errorMessageBackend ?: "Unknown error",
                    onDismiss = {
                        errorShown = true
                        mealViewModel.clearErrorMessage()
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        }
    }
}