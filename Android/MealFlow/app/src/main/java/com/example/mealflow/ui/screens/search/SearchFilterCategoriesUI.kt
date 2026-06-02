package com.example.mealflow.ui.screens // Or your new package

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Import filter content composables: IngredientsFilterContentUI, RegionFilterContentUI etc.

@Composable
fun FilterCategoriesSectionUI(
    searchType: String,
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
    onMaxPrepTimeChange: (Int?) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        val shouldShowIngredients = searchType == "ingredients"
        var ingredientsExpanded by remember { mutableStateOf(shouldShowIngredients) }
        LaunchedEffect(shouldShowIngredients) {
            if (shouldShowIngredients) ingredientsExpanded = true
            // Decide if you want to auto-collapse it if searchType changes away from "ingredients"
             else ingredientsExpanded = false
        }
        FilterCategoryUI(
            title = "Ingredients",
            isExpanded = ingredientsExpanded,
            onToggleExpand = { ingredientsExpanded = !ingredientsExpanded }
        ) {
            IngredientsFilterContentUI( // Use new composable
                ingredientMode, onIngredientModeChange, ingredientSearchQuery,
                onIngredientSearchQueryChange, ingredientResults, selectedIngredients,
                onSelectedIngredientsChange
            )
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        var showRegions by remember { mutableStateOf(false) }
        FilterCategoryUI(
            title = "Regions",
            isExpanded = showRegions,
            onToggleExpand = { showRegions = !showRegions }
        ) {
            RegionFilterContentUI( // Use new composable
                regionSearchQuery, onRegionSearchQueryChange, regionResults,
                selectedRegion, onSelectedRegionChange
            )
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        var showSubRegions by remember { mutableStateOf(false) }
        FilterCategoryUI(
            title = "Sub-Regions",
            isExpanded = showSubRegions,
            onToggleExpand = { showSubRegions = !showSubRegions }
        ) {
            SubRegionFilterContentUI( // Use new composable
                subRegionSearchQuery, onSubRegionSearchQueryChange, subRegionResults,
                selectedSubRegion, onSelectedSubRegionChange
            )
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        var showDietaryTags by remember { mutableStateOf(false) }
        FilterCategoryUI(
            title = "Dietary Preferences",
            isExpanded = showDietaryTags,
            onToggleExpand = { showDietaryTags = !showDietaryTags }
        ) {
            DietaryTagsFilterContentUI( // Use new composable
                dietaryTagSearchQuery, onDietaryTagSearchQueryChange, dietaryTagResults,
                selectedDietaryTags, onSelectedDietaryTagsChange
            )
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        var showAdvancedFilters by remember { mutableStateOf(false) }
        FilterCategoryUI(
            title = "Advanced Filters",
            isExpanded = showAdvancedFilters,
            onToggleExpand = { showAdvancedFilters = !showAdvancedFilters }
        ) {
            AdvancedFiltersContentUI( // Use new composable
                minCalories, onMinCaloriesChange, maxCalories, onMaxCaloriesChange,
                maxPrepTime, onMaxPrepTimeChange
            )
        }
    }
}

@Composable
fun FilterCategoryUI(
    title: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onToggleExpand() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse $title" else "Expand $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                content()
            }
        }
    }
}