package com.example.mealflow.ui.screens // Or your new package e.g., com.example.mealflow.ui.screens.searchpage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.mealflow.data.model.Meal
import com.example.mealflow.ui.screens.search.SearchTopBarUI
import com.example.mealflow.viewModel.MealViewModel
import com.example.mealflow.viewModel.MealSearchViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/**
 * SearchPage implementing multi-level search based on the MealFlow API
 * with a modern UI approach
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, FlowPreview::class, ExperimentalLayoutApi::class)
@Composable
fun SearchPage(
    onMealClick: (Meal) -> Unit,
    mealViewModel: MealViewModel, // Renamed to avoid conflict, used for filter options
    mealSearchViewModel: MealSearchViewModel = viewModel(), // For paged search results
    navController: NavController,
    returnToPlanner: Boolean = false // Added nav argument
) {
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Primary search state
    var searchQuery by remember { mutableStateOf("") }
    var isFiltersExpanded by remember { mutableStateOf(false) }
    var activeFiltersCount by remember { mutableIntStateOf(0) }

    // Search type state - initialize from MealViewModel's searchType
    val mealViewModelSearchType by mealViewModel.searchType.collectAsState()
    var searchType by remember { mutableStateOf(mealViewModelSearchType) } // Start with the value from MealViewModel

    // Tracking state for API loading
    // isLoading and errorMessage will now primarily be derived from LazyPagingItems.loadState
    // val isLoading by mealSearchViewModel.isLoading.collectAsState() // From MealSearchViewModel
    // val searchResults by mealViewModel.searchResults.collectAsState() // Replaced by paged results
    val lazyMealItems = mealSearchViewModel.pagedMeals.collectAsLazyPagingItems()

    // Ingredient search options
    var ingredientMode by remember { mutableStateOf("any") } // any, all, exact
    var ingredientSearchQuery by remember { mutableStateOf("") }
    var selectedIngredients by remember { mutableStateOf<List<String>>(emptyList()) }
    var ingredientResults by remember { mutableStateOf<List<String>>(emptyList()) }

    // Metadata filters
    var regionSearchQuery by remember { mutableStateOf("") }
    var subRegionSearchQuery by remember { mutableStateOf("") }
    var dietaryTagSearchQuery by remember { mutableStateOf("") }

    var selectedRegion by remember { mutableStateOf<String?>(null) }
    var selectedSubRegion by remember { mutableStateOf<String?>(null) }
    var selectedDietaryTags by remember { mutableStateOf<List<String>>(emptyList()) }

    var regionResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var subRegionResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var dietaryTagResults by remember { mutableStateOf<List<String>>(emptyList()) }

    // Additional filters
    var minCalories by remember { mutableStateOf<Int?>(null) }
    var maxCalories by remember { mutableStateOf<Int?>(null) }
    var maxPrepTime by remember { mutableStateOf<Int?>(null) }

    // Calculate active filters count
    LaunchedEffect(selectedIngredients, selectedRegion, selectedSubRegion, selectedDietaryTags,
        minCalories, maxCalories, maxPrepTime) {
        activeFiltersCount = selectedIngredients.size +
                (if (selectedRegion != null) 1 else 0) +
                (if (selectedSubRegion != null) 1 else 0) +
                selectedDietaryTags.size +
                (if (minCalories != null) 1 else 0) +
                (if (maxCalories != null) 1 else 0) +
                (if (maxPrepTime != null) 1 else 0)
    }

    // Initial data load when not returning to planner (i.e., direct navigation to search)
    LaunchedEffect(returnToPlanner, mealViewModelSearchType) {
        if (!returnToPlanner) { // Only load initial if not for planner selection
            // Update local search type with the one from ViewModel
            searchType = mealViewModelSearchType
            
            // Log for debugging
            println("Initiating search with type: $mealViewModelSearchType")
            
            mealSearchViewModel.search(
                MealSearchViewModel.SearchParams(
                    searchType = mealViewModelSearchType, // Use the type from MealViewModel
                    queryText = null, // Initially no query
                    trending = if (mealViewModelSearchType == "trending") true else null,
                    minCalories = 0,
                    maxCalories = 10000,
                    maxTime = 100000
                )
            )
        }
        // Load filter options regardless
        coroutineScope.launch {
            // Use mealViewModel for fetching filter options
            mealViewModel.repository.getRegions(filter = "").onSuccess { regions ->
                regionResults = regions.map { it.name }
            }
            mealViewModel.repository.getSubRegions(filter = "").onSuccess { subRegions ->
                subRegionResults = subRegions.map { it.name }
            }
            mealViewModel.repository.getDietaryTags(filter = "").onSuccess { dietaryTags ->
                dietaryTagResults = dietaryTags.map { it.name }
            }
        }
    }

    // Effect to handle metadata search when query changes
    LaunchedEffect(regionSearchQuery) {
        if (regionSearchQuery.isNotEmpty() && regionSearchQuery.length >= 2) {
            coroutineScope.launch {
                mealViewModel.repository.getRegions(filter = regionSearchQuery).onSuccess { regions ->
                    regionResults = regions.map { it.name }
                }
            }
        } else if (regionSearchQuery.isEmpty()) {
            coroutineScope.launch {
                mealViewModel.repository.getRegions(filter = "").onSuccess { regions ->
                    regionResults = regions.map { it.name }
                }
            }
        }
    }

    LaunchedEffect(subRegionSearchQuery) {
        if (subRegionSearchQuery.isNotEmpty() && subRegionSearchQuery.length >= 2) {
            coroutineScope.launch {
                mealViewModel.repository.getSubRegions(filter = subRegionSearchQuery).onSuccess { subRegions ->
                    subRegionResults = subRegions.map { it.name }
                }
            }
        } else if (subRegionSearchQuery.isEmpty()) {
            coroutineScope.launch {
                mealViewModel.repository.getSubRegions(filter = "").onSuccess { subRegions ->
                    subRegionResults = subRegions.map { it.name }
                }
            }
        }
    }

    LaunchedEffect(dietaryTagSearchQuery) {
        if (dietaryTagSearchQuery.isNotEmpty() && dietaryTagSearchQuery.length >= 2) {
            coroutineScope.launch {
                mealViewModel.repository.getDietaryTags(filter = dietaryTagSearchQuery).onSuccess { dietaryTags ->
                    dietaryTagResults = dietaryTags.map { it.name }
                }
            }
        } else if (dietaryTagSearchQuery.isEmpty()) {
            coroutineScope.launch {
                mealViewModel.repository.getDietaryTags(filter = "").onSuccess { dietaryTags ->
                    dietaryTagResults = dietaryTags.map { it.name }
                }
            }
        }
    }

    LaunchedEffect(ingredientSearchQuery) {
        if (ingredientSearchQuery.length >= 2) {
            delay(300)
            mealViewModel.repository.getIngredients(filter = ingredientSearchQuery)
                .onSuccess { ingredientsResponseList ->
                    ingredientResults = ingredientsResponseList.map { it.name }.distinct()
                }
                .onFailure { error ->
                    ingredientResults = emptyList()
                }
        } else {
            ingredientResults = emptyList()
        }
    }

    // Debounced search for the main query text
    LaunchedEffect(searchQuery) { // Listen to external changes to searchQuery
        snapshotFlow { searchQuery }
            .debounce(500)
            .collect { query ->
                if (query.isNotBlank() && query.length >= 2) {
                    executeSearch(
                        mealSearchViewModel = mealSearchViewModel,
                        searchType = searchType, // Use current searchType
                        searchQuery = query,
                        selectedIngredients = selectedIngredients,
                        ingredientMode = ingredientMode,
                        selectedRegion = selectedRegion,
                        selectedSubRegion = selectedSubRegion,
                        selectedDietaryTags = selectedDietaryTags,
                        minCalories = minCalories,
                        maxCalories = maxCalories,
                        maxPrepTime = maxPrepTime
                    )
                } else if (query.isBlank() && activeFiltersCount == 0 && !returnToPlanner) {
                    // If query becomes blank, no filters, and not for planner, fetch general results.
                    mealSearchViewModel.search(MealSearchViewModel.SearchParams(searchType = "filters"))
                } else if (query.isBlank() && activeFiltersCount > 0) {
                    // If query is blank but filters are active, execute search with filters only
                    executeSearch(
                        mealSearchViewModel = mealSearchViewModel,
                        searchType = "filters", // Usually filters when query is blank but other filters exist
                        searchQuery = "",
                        selectedIngredients = selectedIngredients,
                        ingredientMode = ingredientMode,
                        selectedRegion = selectedRegion,
                        selectedSubRegion = selectedSubRegion,
                        selectedDietaryTags = selectedDietaryTags,
                        minCalories = minCalories,
                        maxCalories = maxCalories,
                        maxPrepTime = maxPrepTime
                    )
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar - Directly integrated without Scaffold
        SearchTopBarUI(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onClearSearch = {
                searchQuery = ""
                if (activeFiltersCount == 0 && !returnToPlanner) {
                    mealSearchViewModel.search(MealSearchViewModel.SearchParams(searchType = "filters"))
                }
            },
            activeFiltersCount = activeFiltersCount,
            isFiltersExpanded = isFiltersExpanded,
            onToggleFilters = { isFiltersExpanded = !isFiltersExpanded },
            onSearch = { // This is for keyboard action search
                keyboardController?.hide()
                focusManager.clearFocus()
                executeSearch(
                    mealSearchViewModel = mealSearchViewModel,
                    searchType = searchType, // Use current searchType
                    searchQuery = searchQuery,
                    selectedIngredients = selectedIngredients,
                    ingredientMode = ingredientMode,
                    selectedRegion = selectedRegion,
                    selectedSubRegion = selectedSubRegion,
                    selectedDietaryTags = selectedDietaryTags,
                    minCalories = minCalories,
                    maxCalories = maxCalories,
                    maxPrepTime = maxPrepTime
                )
            },
            onNavigateBack = if (returnToPlanner) { { navController.popBackStack() } } else null
        )

        AnimatedVisibility(
            visible = isFiltersExpanded,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
        ) {
            EnhancedFiltersPanelUI(
                searchType = searchType,
                onSearchTypeChange = { searchType = it },
                ingredientMode = ingredientMode,
                onIngredientModeChange = { ingredientMode = it },
                ingredientSearchQuery = ingredientSearchQuery,
                onIngredientSearchQueryChange = { ingredientSearchQuery = it },
                ingredientResults = ingredientResults,
                selectedIngredients = selectedIngredients,
                onSelectedIngredientsChange = { selectedIngredients = it },
                regionSearchQuery = regionSearchQuery,
                onRegionSearchQueryChange = { regionSearchQuery = it },
                regionResults = regionResults,
                selectedRegion = selectedRegion,
                onSelectedRegionChange = { selectedRegion = it },
                subRegionSearchQuery = subRegionSearchQuery,
                onSubRegionSearchQueryChange = { subRegionSearchQuery = it },
                subRegionResults = subRegionResults,
                selectedSubRegion = selectedSubRegion,
                onSelectedSubRegionChange = { selectedSubRegion = it },
                dietaryTagSearchQuery = dietaryTagSearchQuery,
                onDietaryTagSearchQueryChange = { dietaryTagSearchQuery = it },
                dietaryTagResults = dietaryTagResults,
                selectedDietaryTags = selectedDietaryTags,
                onSelectedDietaryTagsChange = { selectedDietaryTags = it },
                minCalories = minCalories,
                onMinCaloriesChange = { minCalories = it },
                maxCalories = maxCalories,
                onMaxCaloriesChange = { maxCalories = it },
                maxPrepTime = maxPrepTime,
                onMaxPrepTimeChange = { maxPrepTime = it },
                onClearAllFilters = {
                    searchType = "filters" // Reset to default search type
                    ingredientMode = "any"
                    ingredientSearchQuery = ""
                    regionSearchQuery = ""
                    subRegionSearchQuery = ""
                    dietaryTagSearchQuery = ""
                    selectedIngredients = emptyList() // Clear all filters
                    selectedRegion = null
                    selectedSubRegion = null
                    selectedDietaryTags = emptyList()
                    minCalories = null
                    maxCalories = null
                    maxPrepTime = null
                    // Trigger a search with no filters if the query is also blank
                    if (searchQuery.isBlank() && !returnToPlanner) {
                        mealSearchViewModel.search(MealSearchViewModel.SearchParams(searchType = "filters"))
                    }
                },
                onApplyFilters = {
                    isFiltersExpanded = false // Collapse panel after applying
                    executeSearch(
                        mealSearchViewModel = mealSearchViewModel,
                        searchType = searchType, // Use current searchType
                        searchQuery = searchQuery,
                        selectedIngredients = selectedIngredients,
                        ingredientMode = ingredientMode,
                        selectedRegion = selectedRegion,
                        selectedSubRegion = selectedSubRegion,
                        selectedDietaryTags = selectedDietaryTags,
                        minCalories = minCalories,
                        maxCalories = maxCalories,
                        maxPrepTime = maxPrepTime
                    )
                }
            )
        }

        ResultsSectionUI(
            lazyMeals = lazyMealItems,
            searchQuery = searchQuery,
            activeFiltersCount = activeFiltersCount,
            onMealClick = onMealClick,
            // Error message and clear error can be handled via loadState or a separate StateFlow from mealSearchViewModel
            // For now, let's pass the retry action from lazyMealItems
            onRetry = { lazyMealItems.retry() }
        )
    }

    LaunchedEffect(Unit) {
        if (searchQuery.isBlank() && !returnToPlanner) { // Auto-focus only if not returning to planner and query is blank
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}