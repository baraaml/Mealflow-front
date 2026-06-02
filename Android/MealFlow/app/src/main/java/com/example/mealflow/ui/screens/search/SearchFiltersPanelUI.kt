package com.example.mealflow.ui.screens // Or your new package

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Import other necessary components like ActiveFiltersSectionUI, FilterCategoriesSectionUI, hasAnyFilters

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnhancedFiltersPanelUI(
    searchType: String,
    onSearchTypeChange: (String) -> Unit,
    ingredientMode: String,
    onIngredientModeChange: (String) -> Unit,
    ingredientSearchQuery: String,
    onIngredientSearchQueryChange: (String) -> Unit,
    ingredientResults: List<String>,
    selectedIngredients: List<String>,
    onSelectedIngredientsChange: (List<String>) -> Unit,
    regionSearchQuery: String,
    onRegionSearchQueryChange: (String) -> Unit,
    regionResults: List<String>,
    selectedRegion: String?,
    onSelectedRegionChange: (String?) -> Unit,
    subRegionSearchQuery: String,
    onSubRegionSearchQueryChange: (String) -> Unit,
    subRegionResults: List<String>,
    selectedSubRegion: String?,
    onSelectedSubRegionChange: (String?) -> Unit,
    dietaryTagSearchQuery: String,
    onDietaryTagSearchQueryChange: (String) -> Unit,
    dietaryTagResults: List<String>,
    selectedDietaryTags: List<String>,
    onSelectedDietaryTagsChange: (List<String>) -> Unit,
    minCalories: Int?,
    onMinCaloriesChange: (Int?) -> Unit,
    maxCalories: Int?,
    onMaxCaloriesChange: (Int?) -> Unit,
    maxPrepTime: Int?,
    onMaxPrepTimeChange: (Int?) -> Unit,
    onClearAllFilters: () -> Unit,
    onApplyFilters: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (hasAnyFilters( // Make sure hasAnyFilters is imported
                        selectedIngredients, selectedRegion, selectedSubRegion,
                        selectedDietaryTags, minCalories, maxCalories, maxPrepTime
                    )) {
                    TextButton(onClick = onClearAllFilters) {
                        Text("Clear All")
                    }
                }
            }



            ActiveFiltersSectionUI( // Use the new composable
                selectedIngredients = selectedIngredients,
                selectedRegion = selectedRegion,
                selectedSubRegion = selectedSubRegion,
                selectedDietaryTags = selectedDietaryTags,
                minCalories = minCalories,
                maxCalories = maxCalories,
                maxPrepTime = maxPrepTime,
                onRemoveIngredient = { ingredient ->
                    onSelectedIngredientsChange(selectedIngredients - ingredient)
                },
                onRemoveRegion = { onSelectedRegionChange(null) },
                onRemoveSubRegion = { onSelectedSubRegionChange(null) },
                onRemoveDietaryTag = { tag ->
                    onSelectedDietaryTagsChange(selectedDietaryTags - tag)
                },
                onRemoveMinCalories = { onMinCaloriesChange(null) },
                onRemoveMaxCalories = { onMaxCaloriesChange(null) },
                onRemoveMaxPrepTime = { onMaxPrepTimeChange(null) }
            )

            FilterCategoriesSectionUI( // Use the new composable
                searchType = searchType,
                ingredientMode = ingredientMode,
                onIngredientModeChange = onIngredientModeChange,
                ingredientSearchQuery = ingredientSearchQuery,
                onIngredientSearchQueryChange = onIngredientSearchQueryChange,
                ingredientResults = ingredientResults,
                selectedIngredients = selectedIngredients,
                onSelectedIngredientsChange = onSelectedIngredientsChange,
                regionSearchQuery = regionSearchQuery,
                onRegionSearchQueryChange = onRegionSearchQueryChange,
                regionResults = regionResults,
                selectedRegion = selectedRegion,
                onSelectedRegionChange = onSelectedRegionChange,
                subRegionSearchQuery = subRegionSearchQuery,
                onSubRegionSearchQueryChange = onSubRegionSearchQueryChange,
                subRegionResults = subRegionResults,
                selectedSubRegion = selectedSubRegion,
                onSelectedSubRegionChange = onSelectedSubRegionChange,
                dietaryTagSearchQuery = dietaryTagSearchQuery,
                onDietaryTagSearchQueryChange = onDietaryTagSearchQueryChange,
                dietaryTagResults = dietaryTagResults,
                selectedDietaryTags = selectedDietaryTags,
                onSelectedDietaryTagsChange = onSelectedDietaryTagsChange,
                minCalories = minCalories,
                onMinCaloriesChange = onMinCaloriesChange,
                maxCalories = maxCalories,
                onMaxCaloriesChange = onMaxCaloriesChange,
                maxPrepTime = maxPrepTime,
                onMaxPrepTimeChange = onMaxPrepTimeChange
            )

            Button(
                onClick = onApplyFilters,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Apply Filters")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Filters")
            }
        }
    }
}