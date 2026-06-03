package com.example.mealflow.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientsFilterContentUI(
    ingredientMode: String,
    onIngredientModeChange: (String) -> Unit,
    ingredientSearchQuery: String,
    onIngredientSearchQueryChange: (String) -> Unit,
    ingredientResults: List<String>,
    selectedIngredients: List<String>,
    onSelectedIngredientsChange: (List<String>) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = ingredientMode == "any", onClick = { onIngredientModeChange("any") }, label = { Text("Any") })
            FilterChip(selected = ingredientMode == "all", onClick = { onIngredientModeChange("all") }, label = { Text("All") })
            FilterChip(selected = ingredientMode == "exact", onClick = { onIngredientModeChange("exact") }, label = { Text("Exact") })
        }
        OutlinedTextField(
            value = ingredientSearchQuery,
            onValueChange = onIngredientSearchQueryChange,
            placeholder = { Text("Search ingredients...") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
        if (ingredientResults.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) { // Reduced height for compactness
                items(ingredientResults) { ingredient ->
                    ListItem(
                        headlineContent = { Text(ingredient) },
                        trailingContent = {
                            Checkbox(
                                checked = selectedIngredients.contains(ingredient),
                                onCheckedChange = { checked ->
                                    val newList = if (checked) selectedIngredients + ingredient else selectedIngredients - ingredient
                                    onSelectedIngredientsChange(newList)
                                }
                            )
                        },
                        modifier = Modifier.clickable {
                            val newList = if (selectedIngredients.contains(ingredient)) selectedIngredients - ingredient else selectedIngredients + ingredient
                            onSelectedIngredientsChange(newList)
                        }
                    )
                    Divider()
                }
            }
        } else if (ingredientSearchQuery.isNotEmpty()) {
            Text("No ingredients matching \"$ingredientSearchQuery\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun RegionFilterContentUI(
    regionSearchQuery: String,
    onRegionSearchQueryChange: (String) -> Unit,
    regionResults: List<String>,
    selectedRegion: String?,
    onSelectedRegionChange: (String?) -> Unit
) {
    Column {
        OutlinedTextField(
            value = regionSearchQuery,
            onValueChange = onRegionSearchQueryChange,
            placeholder = { Text("Search regions...") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
        if (regionResults.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                items(regionResults) { region ->
                    ListItem(
                        headlineContent = { Text(region) },
                        trailingContent = { RadioButton(selected = selectedRegion == region, onClick = { onSelectedRegionChange(region) }) },
                        modifier = Modifier.clickable { onSelectedRegionChange(region) }
                    )
                    Divider()
                }
            }
        } else if (regionSearchQuery.isNotEmpty()) {
            Text("No regions matching \"$regionSearchQuery\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun SubRegionFilterContentUI(
    subRegionSearchQuery: String,
    onSubRegionSearchQueryChange: (String) -> Unit,
    subRegionResults: List<String>,
    selectedSubRegion: String?,
    onSelectedSubRegionChange: (String?) -> Unit
) {
    Column {
        OutlinedTextField(
            value = subRegionSearchQuery,
            onValueChange = onSubRegionSearchQueryChange,
            placeholder = { Text("Search sub-regions...") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
        if (subRegionResults.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                items(subRegionResults) { subRegion ->
                    ListItem(
                        headlineContent = { Text(subRegion) },
                        trailingContent = { RadioButton(selected = selectedSubRegion == subRegion, onClick = { onSelectedSubRegionChange(subRegion) }) },
                        modifier = Modifier.clickable { onSelectedSubRegionChange(subRegion) }
                    )
                    Divider()
                }
            }
        } else if (subRegionSearchQuery.isNotEmpty()) {
            Text("No sub-regions matching \"$subRegionSearchQuery\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DietaryTagsFilterContentUI(
    dietaryTagSearchQuery: String,
    onDietaryTagSearchQueryChange: (String) -> Unit,
    dietaryTagResults: List<String>,
    selectedDietaryTags: List<String>,
    onSelectedDietaryTagsChange: (List<String>) -> Unit
) {
    Column {
        OutlinedTextField(
            value = dietaryTagSearchQuery,
            onValueChange = onDietaryTagSearchQueryChange,
            placeholder = { Text("Search dietary preferences...") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filteredTags = dietaryTagResults.filter { it.contains(dietaryTagSearchQuery, ignoreCase = true) }.sorted()
            filteredTags.forEach { tag ->
                FilterChip(
                    selected = selectedDietaryTags.contains(tag),
                    onClick = {
                        val newTags = if (selectedDietaryTags.contains(tag)) selectedDietaryTags - tag else selectedDietaryTags + tag
                        onSelectedDietaryTagsChange(newTags)
                    },
                    label = { Text(tag) },
                    leadingIcon = if (selectedDietaryTags.contains(tag)) { { Icon(Icons.Default.Clear, "Remove", Modifier.size(18.dp)) } } else null
                )
            }
            if (filteredTags.isEmpty() && dietaryTagSearchQuery.isNotEmpty()) {
                Text("No dietary preferences matching \"$dietaryTagSearchQuery\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun AdvancedFiltersContentUI(
    minCalories: Int?,
    onMinCaloriesChange: (Int?) -> Unit,
    maxCalories: Int?,
    onMaxCaloriesChange: (Int?) -> Unit,
    maxPrepTime: Int?,
    onMaxPrepTimeChange: (Int?) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = minCalories?.toString() ?: "",
                onValueChange = { value -> onMinCaloriesChange(value.toIntOrNull()) },
                modifier = Modifier.weight(1f),
                label = { Text("Min Calories") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )
            OutlinedTextField(
                value = maxCalories?.toString() ?: "",
                onValueChange = { value -> onMaxCaloriesChange(value.toIntOrNull()) },
                modifier = Modifier.weight(1f),
                label = { Text("Max Calories") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
        OutlinedTextField(
            value = maxPrepTime?.toString() ?: "",
            onValueChange = { value -> onMaxPrepTimeChange(value.toIntOrNull()) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Max Prep Time (min)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
    }
}