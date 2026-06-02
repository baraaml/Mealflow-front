package com.example.mealflow.ui.screens // Or your new package

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Keep this for the primary items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.example.mealflow.data.model.Meal
import com.example.mealflow.ui.components.MealimeStyleCard // Updated import

@Composable
fun ResultsSectionUI(
    lazyMeals: LazyPagingItems<Meal>,
    searchQuery: String,
    activeFiltersCount: Int,
    onMealClick: (Meal) -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp) // Apply consistent padding
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Fixed 2 columns for consistent sizing
                contentPadding = PaddingValues(12.dp), // Even padding all around
                horizontalArrangement = Arrangement.spacedBy(16.dp), // More horizontal spacing
                verticalArrangement = Arrangement.spacedBy(20.dp),   // More vertical spacing for name below
                modifier = Modifier.weight(1f) // Make LazyVerticalGrid take available space
            ) {
                items(
                    count = lazyMeals.itemCount,
                    key = { index -> lazyMeals.peek(index)?.mealId ?: index } // Use mealId as key
                ) { index ->
                    val meal = lazyMeals[index]
                    if (meal != null) {
                        MealimeStyleCard( // Changed to MealimeStyleCard
                            meal = meal,
                            onClick = { onMealClick(meal) }
                        )
                    }
                }

                // Handle Append state (loading more items)
                lazyMeals.apply {
                    when (loadState.append) {
                        is LoadState.Loading -> {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is LoadState.Error -> {
                            val e = loadState.append as LoadState.Error
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Error loading more items: ${e.error.localizedMessage}", textAlign = TextAlign.Center)
                                    Button(onClick = { retry() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                        is LoadState.NotLoading -> Unit // Do nothing
                    }
                }
            }
        }

        // Handle Refresh state (initial load or manual refresh)
        lazyMeals.apply {
            when (loadState.refresh) {
                is LoadState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is LoadState.Error -> {
                    val e = loadState.refresh as LoadState.Error
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${e.error.localizedMessage}", textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { retry() }) { Text("Retry") }
                        }
                    }
                }
                is LoadState.NotLoading -> {
                    if (itemCount == 0) { // Empty state after a successful load
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = "No meals found",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No meals found", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                            if (searchQuery.isNotEmpty() || activeFiltersCount > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when {
                                        searchQuery.isNotEmpty() && activeFiltersCount > 0 -> "Try adjusting your search or filters"
                                        searchQuery.isNotEmpty() -> "Try different search keywords"
                                        else -> "Try adjusting your filters"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}