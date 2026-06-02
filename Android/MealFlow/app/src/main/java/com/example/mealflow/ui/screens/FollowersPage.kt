package com.example.mealflow.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.mealflow.R
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.FollowerRelationship
import com.example.mealflow.viewModel.FollowersViewModel
import com.example.mealflow.viewModel.UserFollowersViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

enum class FollowersPageState {
    LOADING,
    SUCCESS,
    ERROR,
    EMPTY
}

@Composable
fun FollowersPage(
    navController: NavController,
    viewModel: FollowersViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val followersPagingItems = viewModel.followersFlow.collectAsLazyPagingItems()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var pageState by remember { mutableStateOf(FollowersPageState.LOADING) }

    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = isLoading
    )

    // Determine page state based on loading state and data
    LaunchedEffect(followersPagingItems.loadState, followersPagingItems.itemCount) {
        val loadState = followersPagingItems.loadState

        pageState = when {
            loadState.refresh is LoadState.Loading -> FollowersPageState.LOADING
            loadState.refresh is LoadState.Error -> FollowersPageState.ERROR
            followersPagingItems.itemCount == 0 -> FollowersPageState.EMPTY
            else -> FollowersPageState.SUCCESS
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppBarMembers(
            title = "Followers",
            onBackClick = { navController.popBackStack() }
        )

        SearchBar(
            query = searchQuery ?: "",
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onClearQuery = { viewModel.clearSearch() }
        )

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refresh() },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (pageState) {
                    FollowersPageState.LOADING -> {
                        LoadingState(modifier = Modifier.align(Alignment.Center))
                    }
                    FollowersPageState.SUCCESS -> {
                        if (followersPagingItems.itemCount == 0 && !searchQuery.isNullOrEmpty()) {
                            EmptySearchState(
                                searchQuery = searchQuery!!,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            FollowersList(
                                followersPagingItems = followersPagingItems,
                                onUserClick = { followerRelationship ->
                                    coroutineScope.launch {
                                        val userPrefs = UserPreferencesManager(context)
                                        userPrefs.saveUserId(followerRelationship.follower.id)
                                        navController.navigate("User Page")
                                    }
                                }
                            )
                        }
                    }
                    FollowersPageState.ERROR -> {
                        ErrorState(
                            message = errorState ?: "Failed to load followers",
                            onRetry = { viewModel.refresh() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    FollowersPageState.EMPTY -> {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun UserFollowersPage(
    navController: NavController,
    viewModel: UserFollowersViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val followersPagingItems = viewModel.followersFlow.collectAsLazyPagingItems()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var pageState by remember { mutableStateOf(FollowersPageState.LOADING) }

    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = isLoading
    )

    // Determine page state based on loading state and data
    LaunchedEffect(followersPagingItems.loadState, followersPagingItems.itemCount) {
        val loadState = followersPagingItems.loadState

        pageState = when {
            loadState.refresh is LoadState.Loading -> FollowersPageState.LOADING
            loadState.refresh is LoadState.Error -> FollowersPageState.ERROR
            followersPagingItems.itemCount == 0 -> FollowersPageState.EMPTY
            else -> FollowersPageState.SUCCESS
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppBarMembers(
            title = "Followers",
            onBackClick = { navController.popBackStack() }
        )

        SearchBar(
            query = searchQuery ?: "",
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onClearQuery = { viewModel.clearSearch() }
        )

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refresh() },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (pageState) {
                    FollowersPageState.LOADING -> {
                        LoadingState(modifier = Modifier.align(Alignment.Center))
                    }
                    FollowersPageState.SUCCESS -> {
                        if (followersPagingItems.itemCount == 0 && !searchQuery.isNullOrEmpty()) {
                            EmptySearchState(
                                searchQuery = searchQuery!!,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            FollowersList(
                                followersPagingItems = followersPagingItems,
                                onUserClick = { followerRelationship ->
                                    coroutineScope.launch {
                                        val userPrefs = UserPreferencesManager(context)
                                        userPrefs.saveUserId(followerRelationship.follower.id)
                                        navController.navigate("User Page")
                                    }
                                }
                            )
                        }
                    }
                    FollowersPageState.ERROR -> {
                        ErrorState(
                            message = errorState ?: "Failed to load followers",
                            onRetry = { viewModel.refresh() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    FollowersPageState.EMPTY -> {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun FollowersList(
    followersPagingItems: androidx.paging.compose.LazyPagingItems<FollowerRelationship>,
    onUserClick: (FollowerRelationship) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(followersPagingItems.itemCount) { index ->
            val followerRelationship = followersPagingItems[index]
            followerRelationship?.let {
                FollowerUserItem(
                    followerRelationship = it,
                    onUserClick = { onUserClick(it) }
                )

                if (index < followersPagingItems.itemCount - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Show loading footer when appending data
        if (followersPagingItems.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
fun FollowerUserItem(
    followerRelationship: FollowerRelationship,
    onUserClick: () -> Unit
) {
    Card(
        onClick = onUserClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = followerRelationship.follower.profilePicture,
                contentDescription = "Profile picture of ${followerRelationship.follower.name}",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                placeholder = painterResource(R.drawable.loading_placeholder),
                error = painterResource(R.drawable.profile_default),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                val fullName = "${followerRelationship.follower.name ?: ""} ${followerRelationship.follower.lastName ?: ""}".trim()
                if (fullName.isNotEmpty()) {
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // عرض username إذا كان متوفراً
                followerRelationship.follower.username?.let { username ->
                    Text(
                        text = "@$username",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // عرض تاريخ المتابعة
                Text(
                    text = "Follower since ${formatDate(followerRelationship.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate to profile",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


// Helper function to format the date from ISO format to something more readable
private fun formatDate(isoDate: String): String {
    // You could implement a proper date formatter here
    // For simplicity, just return a shortened version
    return try {
        // Just get the date part for now
        isoDate.split("T").firstOrNull()?.let { date ->
            val parts = date.split("-")
            if (parts.size == 3) {
                "${parts[2]}/${parts[1]}/${parts[0]}"
            } else {
                isoDate
            }
        } ?: isoDate
    } catch (e: Exception) {
        Log.d("formatDate","e: $e")
        isoDate
    }
}