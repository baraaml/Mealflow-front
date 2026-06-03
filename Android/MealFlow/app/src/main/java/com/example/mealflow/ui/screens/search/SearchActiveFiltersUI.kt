package com.example.mealflow.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// Import hasAnyFilters

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveFiltersSectionUI(
    selectedIngredients: List<String>,
    selectedRegion: String?,
    selectedSubRegion: String?,
    selectedDietaryTags: List<String>,
    minCalories: Int?,
    maxCalories: Int?,
    maxPrepTime: Int?,
    onRemoveIngredient: (String) -> Unit,
    onRemoveRegion: () -> Unit,
    onRemoveSubRegion: () -> Unit,
    onRemoveDietaryTag: (String) -> Unit,
    onRemoveMinCalories: () -> Unit,
    onRemoveMaxCalories: () -> Unit,
    onRemoveMaxPrepTime: () -> Unit
) {
    val hasActiveFilters = hasAnyFilters( // Make sure hasAnyFilters is imported
        selectedIngredients, selectedRegion, selectedSubRegion,
        selectedDietaryTags, minCalories, maxCalories, maxPrepTime
    )

    AnimatedVisibility(
        visible = hasActiveFilters,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Active Filters",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedIngredients.forEach { ingredient ->
                        SuggestionChip(
                            onClick = { onRemoveIngredient(ingredient) },
                            label = { Text(ingredient) },
                            icon = { Icon(Icons.Default.Clear, "Remove", Modifier.size(16.dp)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    }
                    if (selectedRegion != null) {
                        SuggestionChip(
                            onClick = { onRemoveRegion() },
                            label = { Text("Region: $selectedRegion") },
                            icon = { Icon(Icons.Default.Clear, "Remove", Modifier.size(16.dp)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                    if (selectedSubRegion != null) {
                        SuggestionChip(
                            onClick = { onRemoveSubRegion() },
                            label = { Text("Sub-region: $selectedSubRegion") },
                            icon = { Icon(Icons.Default.Clear, "Remove", Modifier.size(16.dp)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                            )
                        )
                    }
                    selectedDietaryTags.forEach { tag ->
                        SuggestionChip(
                            onClick = { onRemoveDietaryTag(tag) },
                            label = { Text(tag) },
                            icon = { Icon(Icons.Default.Clear, "Remove", Modifier.size(16.dp)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.3f)
                            )
                        )
                    }
                    if (minCalories != null) {
                        SuggestionChip(
                            onClick = { onRemoveMinCalories() },
                            label = { Text("Min cal: $minCalories") },
                            icon = { Icon(Icons.Default.Clear, "Remove", Modifier.size(16.dp)) }
                        )
                    }
                    if (maxCalories != null) {
                        SuggestionChip(
                            onClick = { onRemoveMaxCalories() },
                            label = { Text("Max cal: $maxCalories") },
                            icon = { Icon(Icons.Default.Clear, "Remove", Modifier.size(16.dp)) }
                        )
                    }
                    if (maxPrepTime != null) {
                        SuggestionChip(
                            onClick = { onRemoveMaxPrepTime() },
                            label = { Text("Max time: $maxPrepTime min") },
                            icon = { Icon(Icons.Default.Clear, "Remove", Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }
    }
}