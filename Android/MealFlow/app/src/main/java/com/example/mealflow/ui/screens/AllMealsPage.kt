package com.example.mealflow.ui.screens

import androidx.compose.foundation.clickable // For clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight // For FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.mealflow.R
import com.example.mealflow.data.model.Meal
import androidx.compose.ui.layout.ContentScale
import com.example.mealflow.ui.components.DataQualityStatusBar
import com.example.mealflow.viewModel.AllMealsViewModel
import com.example.mealflow.viewModel.MealViewModel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.mealflow.ui.components.MealimeStyleCard
import kotlinx.coroutines.FlowPreview
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun AllMealsPage(
    title: String,
    onMealClick: (Meal) -> Unit,
    onBackClick: () -> Unit,
    allMealsViewModel: AllMealsViewModel = viewModel(
        factory = AllMealsViewModel.Factory(
            application = LocalContext.current.applicationContext as android.app.Application,
            sectionTitle = title
        )
    ),
    mealViewModel: MealViewModel? = null
) {
    var isGridView by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val lazyMealItems: LazyPagingItems<Meal> = allMealsViewModel.pagedMeals.collectAsLazyPagingItems()

    // Debounce search query changes to avoid excessive recompositions or API calls if backend search is used
    LaunchedEffect(searchQuery) {
        snapshotFlow { searchQuery }
            .debounce(300)
            .distinctUntilChanged()
            .collect { query ->
            }
    }

    // Local filtering on the currently loaded PagingData items
    // This provides instant feedback while typing.
    val filteredMealsSnapshot = remember(lazyMealItems.itemSnapshotList, searchQuery) {
        val currentItems = lazyMealItems.itemSnapshotList.items
        if (searchQuery.isBlank()) {
            currentItems // No local filtering if search query is blank
        } else {
            currentItems.filter { meal ->
                meal.name.contains(searchQuery, ignoreCase = true) ||
                        meal.tags.any { it.contains(searchQuery, ignoreCase = true) } ||
                        meal.description?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { isGridView = !isGridView }) {
                    Icon(
                        imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                        contentDescription = if (isGridView) "Switch to list view" else "Switch to grid view"
                    )
                }
            }
        )

        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onClearQuery = {
                searchQuery = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Data Quality Status Bar
        mealViewModel?.let { viewModel ->
            DataQualityStatusBar(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Handle initial load state
            when (val refreshState = lazyMealItems.loadState.refresh) {
                is LoadState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is LoadState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${refreshState.error.localizedMessage}", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { lazyMealItems.retry() }) { Text("Retry") }
                        }
                    }
                }
                is LoadState.NotLoading -> {
                    if (lazyMealItems.itemCount == 0 && searchQuery.isBlank()) {
                        // True empty state from backend (no items for the section)
                        EmptyStateAllMeals(message = "No meals found for \"$title\".")
                    } else if (filteredMealsSnapshot.isEmpty() && searchQuery.isNotBlank()) {
                        // Empty state due to local search filter
                        EmptyStateAllMeals(message = "No meals match your search \"$searchQuery\".")
                    } else {
                        // Display content (Grid or List)
                        if (isGridView) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Determine which list to show based on search query
                                val itemsToShow = if (searchQuery.isBlank()) null else filteredMealsSnapshot

                                if (itemsToShow != null) { // Showing locally filtered results
                                    items(itemsToShow.size, key = { index -> itemsToShow[index].mealId }) { index ->
                                        MealimeStyleCard(
                                            meal = itemsToShow[index], 
                                            onClick = { 
                                                if (itemsToShow[index].hasLoadingError && mealViewModel != null) {
                                                    mealViewModel.retryFailedMeal(itemsToShow[index].mealId)
                                                } else {
                                                    onMealClick(itemsToShow[index])
                                                }
                                            }
                                        )
                                    }
                                } else { // Showing paginated items directly
                                    items(lazyMealItems.itemCount, key = { index -> lazyMealItems.peek(index)?.mealId ?: index }) { index ->
                                        lazyMealItems[index]?.let { meal ->
                                            MealimeStyleCard(
                                                meal = meal, 
                                                onClick = { 
                                                    if (meal.hasLoadingError && mealViewModel != null) {
                                                        mealViewModel.retryFailedMeal(meal.mealId)
                                                    } else {
                                                        // Preload next few meals for smoother navigation
                                                        if (mealViewModel != null) {
                                                            val nextMeals = (1..3).mapNotNull { i -> 
                                                                lazyMealItems.peek(index + i)?.mealId 
                                                            }
                                                            if (nextMeals.isNotEmpty()) {
                                                                mealViewModel.preloadMealDetails(nextMeals)
                                                            }
                                                        }
                                                        onMealClick(meal)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                // Append loading state (only if not locally filtering)
                                if (searchQuery.isBlank()) {
                                    lazyMealItems.loadState.append.let { appendState ->
                                        when (appendState) {
                                            is LoadState.Loading -> {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                                        CircularProgressIndicator()
                                                    }
                                                }
                                            }
                                            is LoadState.Error -> {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("Error: ${appendState.error.localizedMessage}", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                                        Button(onClick = { lazyMealItems.retry() }) { Text("Retry") }
                                                    }
                                                }
                                            }
                                            else -> Unit
                                        }
                                    }
                                }
                            }
                        } else { // List View
                            LazyColumn(
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val itemsToShow = if (searchQuery.isBlank()) null else filteredMealsSnapshot

                                if (itemsToShow != null) { // Showing locally filtered results
                                    items(itemsToShow.size, key = { index -> itemsToShow[index].mealId }) { index ->
                                        MealItemRow(
                                            meal = itemsToShow[index], 
                                            onMealClick = { meal ->
                                                if (meal.hasLoadingError && mealViewModel != null) {
                                                    mealViewModel.retryFailedMeal(meal.mealId)
                                                } else {
                                                    onMealClick(meal)
                                                }
                                            }
                                        )
                                    }
                                } else { // Showing paginated items directly
                                    items(lazyMealItems.itemCount, key = { index -> lazyMealItems.peek(index)?.mealId ?: index }) { index ->
                                        lazyMealItems[index]?.let { meal ->
                                            MealItemRow(
                                                meal = meal, 
                                                onMealClick = { m ->
                                                    if (m.hasLoadingError && mealViewModel != null) {
                                                        mealViewModel.retryFailedMeal(m.mealId)
                                                    } else {
                                                        onMealClick(m)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                // Append loading state (only if not locally filtering)
                                if (searchQuery.isBlank()) {
                                    lazyMealItems.loadState.append.let { appendState ->
                                        when (appendState) {
                                            is LoadState.Loading -> {
                                                item {
                                                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                                        CircularProgressIndicator()
                                                    }
                                                }
                                            }
                                            is LoadState.Error -> {
                                                item {
                                                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("Error: ${appendState.error.localizedMessage}", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                                        Button(onClick = { lazyMealItems.retry() }) { Text("Retry") }
                                                    }
                                                }
                                            }
                                            else -> Unit
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateAllMeals(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.RestaurantMenu, // Or SearchOff if more appropriate
            contentDescription = message,
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search meals...") }, // Fixed placeholder text
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        singleLine = true,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge // More rounded for modern look
    )
}

@Composable
fun MealItemRow(
    meal: Meal,
    onMealClick: (Meal) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMealClick(meal) },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Square image with fixed size
            Card(
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AsyncImageWithPlaceholder(
                        model = meal.imageUrl,
                        contentDescription = "Image of ${meal.name}",
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Time badge overlay on the image
                    if ((meal.preparationTime + meal.cookingTime) > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = MaterialTheme.shapes.extraSmall,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Timer,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${meal.preparationTime + meal.cookingTime}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Right side: Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Meal name
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Description if available
                meal.description?.takeIf { it.isNotBlank() }?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // Tags row
                if (meal.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        meal.tags.take(3).forEach { tag ->
                            Surface(
                                modifier = Modifier.padding(end = 4.dp),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        // Show +X more if there are more tags
                        if (meal.tags.size > 3) {
                            Text(
                                text = "+${meal.tags.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                
                // Rating if available
                if (meal.rating > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            val starAlpha = if (index < meal.rating) 1f else 0.3f
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = starAlpha)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(meal.rating),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AsyncImageWithPlaceholder(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage( // Directly use AsyncImage from coil.compose
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop, // Crop to fill bounds
        modifier = modifier,
        error = painterResource(id = R.drawable.neptune_placeholder_48), // Your placeholder drawable
        placeholder = painterResource(id = R.drawable.neptune_placeholder_48)
    )
}