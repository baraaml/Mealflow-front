package com.example.mealflow.ui.screens // Or your new package

import com.example.mealflow.viewModel.MealSearchViewModel
import com.example.mealflow.viewModel.MealViewModel // Keep for filter options if needed

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
    mealSearchViewModel: MealSearchViewModel, // Changed to MealSearchViewModel
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
            // If there's a text query, "semantic" or "filters" (acting like text search) are good.
            // If user explicitly chose "ingredients" but also typed, backend logic for searchType="ingredients" with query_text applies.
            searchType
        } else if (activeFiltersPresent) {
            "filters" // Default to "filters" if only filters are present
        } else {
            searchType // Use the selected searchType, could be "filters" for general browsing
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
    mealSearchViewModel.search(params)
}