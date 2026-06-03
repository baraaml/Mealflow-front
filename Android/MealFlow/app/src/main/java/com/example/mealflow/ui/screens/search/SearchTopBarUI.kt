package com.example.mealflow.ui.screens.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun SearchTopBarUI(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    activeFiltersCount: Int,
    isFiltersExpanded: Boolean,
    onToggleFilters: () -> Unit,
    onSearch: () -> Unit,
    onNavigateBack: (() -> Unit)? = null // Optional back navigation callback
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
                placeholder = { Text("Search meals...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                shape = RoundedCornerShape(24.dp)
            )

            Box(modifier = Modifier.padding(start = 8.dp)) {
                IconButton(onClick = onToggleFilters) {
                    Icon(
                        if (isFiltersExpanded) Icons.Filled.FilterList else Icons.Outlined.FilterAlt,
                        contentDescription = if (isFiltersExpanded) "Hide filters" else "Show filters"
                    )
                }

                if (activeFiltersCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                    ) {
                        Text(text = activeFiltersCount.toString())
                    }
                }
            }
        }
    }
}