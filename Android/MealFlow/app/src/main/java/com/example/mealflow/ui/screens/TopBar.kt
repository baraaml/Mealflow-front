package com.example.mealflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mealflow.navigation.Destination
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.logoutApi
import com.example.mealflow.viewModel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchTopBar(
    navController: NavController,
    searchViewModel: SearchViewModel = viewModel()
) {
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchSuggestions by remember { mutableStateOf(false) }

    val userPrefs = remember { UserPreferencesManager(context) }
    var firstname by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }

    // Search states from ViewModel
    val searchType by searchViewModel.searchType.collectAsState()
    val isLoading by searchViewModel.isLoading.collectAsState()
    val errorState by searchViewModel.errorState.collectAsState()

    LaunchedEffect(Unit) {
        firstname = userPrefs.getFirstname()
        profileImageUrl = userPrefs.getMyImageProfile()
    }

    // Handle search query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            showSearchSuggestions = true
            searchViewModel.updateQuery(searchQuery)
        } else {
            showSearchSuggestions = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        // Background with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isSearchActive) 120.dp else 70.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                        )
                    )
                )
                .drawBehind {
                    val borderColor = Color(0x1A6750A4)
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 0.5f
                    )
                }
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        )

        Column {
            // Main TopBar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isSearchActive) {
                    // Left side: Profile and greeting (when search is not active)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false) // This is correct
                    ) {
                        ProfileImage(
                            profileImageUrl = profileImageUrl,
                            size = 45.dp,
                            cornerRadius = 12.dp,
                            onClick = { navController.navigate(Destination.Profile) }
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Hello,",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.3.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = firstname,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                }

                // FIXED: SearchBar with proper constraints
                if (isSearchActive) {
                    // When search is active, give it full width
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = {
                            if (it.length >= 2) {
                                searchViewModel.search(it, searchType)
                                showSearchSuggestions = false
                                navController.navigate(Destination.SearchResults)
                            }
                        },
                        active = showSearchSuggestions,
                        onActiveChange = {
                            showSearchSuggestions = it
                            if (!it) {
                                isSearchActive = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(), // Simple, no conflicting constraints
                        placeholder = {
                            Text(
                                text = "Search posts, users, communities...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            Row {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                            searchViewModel.clearSearch()
                                        },
                                        modifier = Modifier.size(40.dp) // Fixed: proper touch target
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Clear,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        isSearchActive = false
                                        showSearchSuggestions = false
                                        searchQuery = ""
                                    },
                                    modifier = Modifier.size(40.dp) // Fixed: proper touch target
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Cancel",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        colors = SearchBarDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            dividerColor = Color.Transparent
                        ),
                        content = {
                            // Search Suggestions Content
                            SearchSuggestionsContent(
                                searchQuery = searchQuery,
                                onSuggestionClick = { suggestion ->
                                    searchQuery = suggestion
                                    searchViewModel.search(suggestion, searchType)
                                    showSearchSuggestions = false
                                    navController.navigate(Destination.SearchResults)
                                },
                                onSearchTypeChange = { type ->
                                    searchViewModel.updateType(type)
                                },
                                currentSearchType = searchType
                            )
                        }
                    )
                } else {
                    // When search is not active, constrain it properly
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = {
                            if (it.length >= 2) {
                                searchViewModel.search(it, searchType)
                                showSearchSuggestions = false
                                navController.navigate(Destination.SearchResults)
                            }
                        },
                        active = showSearchSuggestions,
                        onActiveChange = {
                            showSearchSuggestions = it
                            if (it) isSearchActive = true
                        },
                        modifier = Modifier
                            .weight(1f) // Use weight instead of fixed width
                            .heightIn(
                                min = 40.dp,
                                max = 56.dp
                            ), // Set reasonable height constraints
                        placeholder = {
                            Text(
                                text = "Search...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        searchQuery = ""
                                        searchViewModel.clearSearch()
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        colors = SearchBarDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            dividerColor = Color.Transparent
                        ),
                        content = {
                            SearchSuggestionsContent(
                                searchQuery = searchQuery,
                                onSuggestionClick = { suggestion ->
                                    searchQuery = suggestion
                                    searchViewModel.search(suggestion, searchType)
                                    showSearchSuggestions = false
                                    navController.navigate(Destination.SearchResults)
                                },
                                onSearchTypeChange = { type ->
                                    searchViewModel.updateType(type)
                                },
                                currentSearchType = searchType
                            )
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right side: Action buttons (hidden when search is active)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Notification button
                        Box(contentAlignment = Alignment.TopEnd) {
                            IconButton(
                                onClick = { /* Handle notifications */ },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Notification badge
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                                    .offset(x = (-2).dp, y = 2.dp)
                            )
                        }

                        // Menu button
                        IconButton(
                            onClick = { showBottomSheet = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(38f, 38f)
                                    )
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Search Type Filter Row (visible when search is active)
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                SearchTypeFilterRow(
                    currentType = searchType,
                    onTypeSelected = { type ->
                        searchViewModel.updateType(type)
                        if (searchQuery.length >= 2) {
                            searchViewModel.search(searchQuery, type)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }

        // Bottom Sheet
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBottomSheet = false
                                navController.navigate(Destination.UpdateProfile)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Profile")
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBottomSheet = false
                                logoutApi(context, navController)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Logout",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSuggestionsContent(
    searchQuery: String,
    onSuggestionClick: (String) -> Unit,
    onSearchTypeChange: (String) -> Unit,
    currentSearchType: String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Recent searches (mock data - replace with actual recent searches)
        if (searchQuery.isEmpty()) {
            item {
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(listOf("pizza recipe", "healthy meals", "cooking tips")) { recentSearch ->
                SearchSuggestionItem(
                    text = recentSearch,
                    icon = Icons.Rounded.History,
                    onClick = { onSuggestionClick(recentSearch) }
                )
            }
        } else {
            // Search suggestions based on query
            item {
                Text(
                    text = "Suggestions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(
                listOf(
                    "$searchQuery in posts",
                    "$searchQuery recipes",
                    "$searchQuery community"
                )
            ) { suggestion ->
                SearchSuggestionItem(
                    text = suggestion,
                    icon = Icons.Rounded.Search,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}

@Composable
fun SearchSuggestionItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SearchTypeFilterRow(
    currentType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchTypes = listOf(
        "all" to "All",
        "posts" to "Posts",
        "users" to "People",
        "communities" to "Communities",
        "comments" to "Comments"
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(searchTypes) { (type, label) ->
            SearchTypeChip(
                label = label,
                isSelected = currentType == type,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTypeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = if (isSelected) 1.5.dp else 0.5.dp
        ),
        modifier = Modifier.height(32.dp)
    )
}