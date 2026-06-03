package com.example.mealflow.ui.screens.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.mealflow.data.model.Meal
import com.example.mealflow.ui.utils.ObserveAsEvents
import com.example.mealflow.viewModel.MealSearchViewModel
import com.example.mealflow.viewModel.MealViewModel
import kotlinx.coroutines.FlowPreview
import org.koin.androidx.compose.koinViewModel

/**
 * Performance-optimized SearchPage using MVI and unified state management.
 */
@Composable
fun SearchPage(
    onMealClick: (Meal) -> Unit,
    mealViewModel: MealViewModel,
    mealSearchViewModel: MealSearchViewModel = koinViewModel(),
    navController: NavController,
    returnToPlanner: Boolean = false
) {
    val state by mealSearchViewModel.state.collectAsStateWithLifecycle()
    val lazyMealItems = mealSearchViewModel.pagedMeals.collectAsLazyPagingItems()

    ObserveAsEvents(mealSearchViewModel.events) { event ->
        when (event) {
            is MealSearchEvent.NavigateToDetail -> {
                // Outer navigation handled by onMealClick for consistency
            }
            is MealSearchEvent.ShowError -> {
                // Potentially trigger a snackbar in the Scaffold below
            }
        }
    }

    MealSearchScreen(
        state = state,
        lazyMealItems = lazyMealItems,
        onAction = mealSearchViewModel::onAction,
        onMealClick = onMealClick,
        navController = navController,
        returnToPlanner = returnToPlanner,
        mealViewModelSearchType = mealViewModel.searchType.collectAsState().value
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, FlowPreview::class, ExperimentalLayoutApi::class)
@Composable
fun MealSearchScreen(
    state: MealSearchState,
    lazyMealItems: LazyPagingItems<Meal>,
    onAction: (MealSearchAction) -> Unit,
    onMealClick: (Meal) -> Unit,
    navController: NavController,
    returnToPlanner: Boolean,
    mealViewModelSearchType: String
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Initial data load - Sync with external search type if needed
    LaunchedEffect(returnToPlanner, mealViewModelSearchType) {
        if (!returnToPlanner) {
            onAction(MealSearchAction.OnSearchTypeChange(mealViewModelSearchType))
            onAction(MealSearchAction.ApplyFilters)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            SearchTopBarUI(
                searchQuery = state.query,
                onSearchQueryChange = { onAction(MealSearchAction.OnQueryChange(it)) },
                onClearSearch = { onAction(MealSearchAction.OnQueryChange("")) },
                activeFiltersCount = state.activeFiltersCount,
                isFiltersExpanded = state.isFiltersExpanded,
                onToggleFilters = { onAction(MealSearchAction.ToggleFilters) },
                onSearch = { 
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onAction(MealSearchAction.OnSearchClick)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Active filters chips (Stateless)
                ActiveFiltersSectionUI(
                    selectedIngredients = state.searchParams.ingredients ?: emptyList(),
                    selectedRegion = state.searchParams.region,
                    selectedSubRegion = state.searchParams.subRegion,
                    selectedDietaryTags = state.searchParams.dietaryTags ?: emptyList(),
                    minCalories = state.searchParams.minCalories,
                    maxCalories = state.searchParams.maxCalories,
                    maxPrepTime = state.searchParams.maxTime,
                    onRemoveIngredient = { onAction(MealSearchAction.RemoveFilter(FilterType.INGREDIENT, it)) },
                    onRemoveRegion = { onAction(MealSearchAction.RemoveFilter(FilterType.REGION)) },
                    onRemoveSubRegion = { onAction(MealSearchAction.RemoveFilter(FilterType.SUB_REGION)) },
                    onRemoveDietaryTag = { onAction(MealSearchAction.RemoveFilter(FilterType.DIETARY_TAG, it)) },
                    onRemoveMinCalories = { onAction(MealSearchAction.RemoveFilter(FilterType.MIN_CALORIES)) },
                    onRemoveMaxCalories = { onAction(MealSearchAction.RemoveFilter(FilterType.MAX_CALORIES)) },
                    onRemoveMaxPrepTime = { onAction(MealSearchAction.RemoveFilter(FilterType.MAX_TIME)) }
                )

                // Results list
                ResultsSectionUI(
                    lazyMeals = lazyMealItems,
                    searchQuery = state.query,
                    activeFiltersCount = state.activeFiltersCount,
                    onMealClick = { meal ->
                        onAction(MealSearchAction.OnMealClick(meal))
                        onMealClick(meal)
                    },
                    onRetry = { onAction(MealSearchAction.OnRefresh) }
                )
            }

            // Filters Overlay (Stateless)
            if (state.isFiltersExpanded) {
                EnhancedFiltersPanelUI(
                    searchType = state.draftParams.searchType,
                    onSearchTypeChange = { onAction(MealSearchAction.OnSearchTypeChange(it)) },
                    ingredientMode = state.draftParams.ingredientMode ?: "any",
                    onIngredientModeChange = { onAction(MealSearchAction.OnIngredientModeChange(it)) },
                    ingredientSearchQuery = state.ingredientSearchQuery,
                    onIngredientSearchQueryChange = { onAction(MealSearchAction.OnIngredientQueryChange(it)) },
                    ingredientResults = state.ingredientResults,
                    selectedIngredients = state.draftParams.ingredients ?: emptyList(),
                    onSelectedIngredientsChange = { onAction(MealSearchAction.OnIngredientsChange(it)) },
                    regionSearchQuery = state.regionSearchQuery,
                    onRegionSearchQueryChange = { onAction(MealSearchAction.OnRegionQueryChange(it)) },
                    regionResults = state.regionResults,
                    selectedRegion = state.draftParams.region,
                    onSelectedRegionChange = { onAction(MealSearchAction.OnRegionChange(it)) },
                    subRegionSearchQuery = state.subRegionSearchQuery,
                    onSubRegionSearchQueryChange = { onAction(MealSearchAction.OnSubRegionQueryChange(it)) },
                    subRegionResults = state.subRegionResults,
                    selectedSubRegion = state.draftParams.subRegion,
                    onSelectedSubRegionChange = { onAction(MealSearchAction.OnSubRegionChange(it)) },
                    dietaryTagSearchQuery = state.dietaryTagSearchQuery,
                    onDietaryTagSearchQueryChange = { onAction(MealSearchAction.OnDietaryTagQueryChange(it)) },
                    dietaryTagResults = state.dietaryTagResults,
                    selectedDietaryTags = state.draftParams.dietaryTags ?: emptyList(),
                    onSelectedDietaryTagsChange = { onAction(MealSearchAction.OnDietaryTagsChange(it)) },
                    minCalories = state.draftParams.minCalories,
                    onMinCaloriesChange = { onAction(MealSearchAction.OnMinCaloriesChange(it)) },
                    maxCalories = state.draftParams.maxCalories,
                    onMaxCaloriesChange = { onAction(MealSearchAction.OnMaxCaloriesChange(it)) },
                    maxPrepTime = state.draftParams.maxTime,
                    onMaxPrepTimeChange = { onAction(MealSearchAction.OnMaxTimeChange(it)) },
                    onApplyFilters = { onAction(MealSearchAction.ApplyFilters) },
                    onClearAllFilters = { onAction(MealSearchAction.ResetFilters) }
                )
            }
        }
    }
}
