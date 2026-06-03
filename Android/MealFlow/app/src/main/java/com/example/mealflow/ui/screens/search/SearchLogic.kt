package com.example.mealflow.ui.screens.search

import com.example.mealflow.viewModel.MealSearchViewModel

fun hasAnyFilters(
    selectedIngredients: List<String>,
    selectedRegion: String?,
    selectedSubRegion: String?,
    selectedDietaryTags: List<String>,
    minCalories: Int?,
    maxCalories: Int?,
    maxPrepTime: Int?
): Boolean {
    return selectedIngredients.isNotEmpty() ||
            selectedRegion != null ||
            selectedSubRegion != null ||
            selectedDietaryTags.isNotEmpty() ||
            minCalories != null ||
            maxCalories != null ||
            maxPrepTime != null
}

fun executeSearch(
    mealSearchViewModel: MealSearchViewModel,
    searchType: String,
    searchQuery: String,
    selectedIngredients: List<String>,
    ingredientMode: String,
    selectedRegion: String?,
    selectedSubRegion: String?,
    selectedDietaryTags: List<String>,
    minCalories: Int?,
    maxCalories: Int?,
    maxPrepTime: Int?
) {
    val hasTextQuery = searchQuery.isNotBlank()
    val activeFiltersPresent = hasAnyFilters(
        selectedIngredients, selectedRegion, selectedSubRegion, selectedDietaryTags,
        minCalories, maxCalories, maxPrepTime
    )

    val params = MealSearchViewModel.SearchParams(
        searchType = if (hasTextQuery && (searchType == "semantic" || searchType == "filters")) {
            searchType
        } else if (activeFiltersPresent) {
            "filters"
        } else {
            searchType
        },
        queryText = if (searchQuery.isNotBlank()) searchQuery else null,
        ingredients = if (selectedIngredients.isNotEmpty()) selectedIngredients else null,
        ingredientMode = if (selectedIngredients.isNotEmpty()) ingredientMode else null,
        region = selectedRegion,
        subRegion = selectedSubRegion,
        dietaryTags = if (selectedDietaryTags.isNotEmpty()) selectedDietaryTags else null,
        minCalories = minCalories,
        maxCalories = maxCalories,
        maxTime = maxPrepTime
    )
    mealSearchViewModel.onAction(MealSearchAction.OnSearchParamsChange(params))
}
