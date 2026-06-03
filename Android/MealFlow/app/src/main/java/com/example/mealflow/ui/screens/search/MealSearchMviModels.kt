package com.example.mealflow.ui.screens.search

import com.example.mealflow.data.model.Meal
import com.example.mealflow.viewModel.MealSearchViewModel

data class MealSearchState(
    val query: String = "",
    val isFiltersExpanded: Boolean = false,
    
    // Metadata Search Queries
    val ingredientSearchQuery: String = "",
    val regionSearchQuery: String = "",
    val subRegionSearchQuery: String = "",
    val dietaryTagSearchQuery: String = "",
    
    // Metadata Results
    val ingredientResults: List<String> = emptyList(),
    val regionResults: List<String> = emptyList(),
    val subRegionResults: List<String> = emptyList(),
    val dietaryTagResults: List<String> = emptyList(),
    
    // Applied Search Parameters (what the API sees)
    val searchParams: MealSearchViewModel.SearchParams = MealSearchViewModel.SearchParams(),
    
    // Draft Search Parameters (what's in the panel before "Apply")
    val draftParams: MealSearchViewModel.SearchParams = MealSearchViewModel.SearchParams(),
    
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val activeFiltersCount: Int get() = (searchParams.ingredients?.size ?: 0) +
            (if (searchParams.region != null) 1 else 0) +
            (if (searchParams.subRegion != null) 1 else 0) +
            (searchParams.dietaryTags?.size ?: 0) +
            (if (searchParams.minCalories != null) 1 else 0) +
            (if (searchParams.maxCalories != null) 1 else 0) +
            (if (searchParams.maxTime != null) 1 else 0)
}

sealed interface MealSearchAction {
    // Primary Search
    data class OnQueryChange(val newQuery: String) : MealSearchAction
    data object OnSearchClick : MealSearchAction
    data object OnRefresh : MealSearchAction
    data object ClearError : MealSearchAction
    data object ToggleFilters : MealSearchAction
    
    // Filter Panel Input Changes (Debounced)
    data class OnIngredientQueryChange(val query: String) : MealSearchAction
    data class OnRegionQueryChange(val query: String) : MealSearchAction
    data class OnSubRegionQueryChange(val query: String) : MealSearchAction
    data class OnDietaryTagQueryChange(val query: String) : MealSearchAction
    
    // Filter Selection Changes (Draft)
    data class OnIngredientModeChange(val mode: String) : MealSearchAction
    data class OnIngredientsChange(val ingredients: List<String>) : MealSearchAction
    data class OnRegionChange(val region: String?) : MealSearchAction
    data class OnSubRegionChange(val subRegion: String?) : MealSearchAction
    data class OnDietaryTagsChange(val tags: List<String>) : MealSearchAction
    data class OnMinCaloriesChange(val calories: Int?) : MealSearchAction
    data class OnMaxCaloriesChange(val calories: Int?) : MealSearchAction
    data class OnMaxTimeChange(val time: Int?) : MealSearchAction
    data class OnSearchTypeChange(val type: String) : MealSearchAction
    data class OnSearchParamsChange(val params: MealSearchViewModel.SearchParams) : MealSearchAction
    
    // Global Actions
    data object ApplyFilters : MealSearchAction
    data object ResetFilters : MealSearchAction
    data class OnMealClick(val meal: Meal) : MealSearchAction
    data class RemoveFilter(val filterType: FilterType, val value: String? = null) : MealSearchAction
}

enum class FilterType {
    INGREDIENT, REGION, SUB_REGION, DIETARY_TAG, MIN_CALORIES, MAX_CALORIES, MAX_TIME
}

sealed interface MealSearchEvent {
    data class NavigateToDetail(val mealId: String) : MealSearchEvent
    data class ShowError(val message: String) : MealSearchEvent
}
