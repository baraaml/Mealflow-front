package com.example.mealflow.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mealflow.navigation.Destination
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
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

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

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

        FollowersSearchBar(
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
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (pageState) {
                    FollowersPageState.LOADING -> FollowersLoadingState(modifier = Modifier.align(Alignment.Center))
                    FollowersPageState.SUCCESS -> {
                        if (followersPagingItems.itemCount == 0 && !searchQuery.isNullOrEmpty()) {
                            FollowersEmptySearchState(searchQuery = searchQuery!!, modifier = Modifier.align(Alignment.Center))
                        } else {
                            FollowersList(
                                followersPagingItems = followersPagingItems,
                                onUserClick = { followerRelationship ->
                                    coroutineScope.launch {
                                        val userPrefs = UserPreferencesManager(context)
                                        userPrefs.saveUserId(followerRelationship.follower.id)
                                        navController.navigate(Destination.User)
                                    }
                                }
                            )
                        }
                    }
                    FollowersPageState.ERROR -> FollowersErrorState(message = errorState ?: "Failed to load followers", onRetry = { viewModel.refresh() }, modifier = Modifier.align(Alignment.Center))
                    FollowersPageState.EMPTY -> FollowersEmptyState(modifier = Modifier.align(Alignment.Center))
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

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

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

        FollowersSearchBar(
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
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (pageState) {
                    FollowersPageState.LOADING -> FollowersLoadingState(modifier = Modifier.align(Alignment.Center))
                    FollowersPageState.SUCCESS -> {
                        if (followersPagingItems.itemCount == 0 && !searchQuery.isNullOrEmpty()) {
                            FollowersEmptySearchState(searchQuery = searchQuery!!, modifier = Modifier.align(Alignment.Center))
                        } else {
                            FollowersList(
                                followersPagingItems = followersPagingItems,
                                onUserClick = { followerRelationship ->
                                    coroutineScope.launch {
                                        val userPrefs = UserPreferencesManager(context)
                                        userPrefs.saveUserId(followerRelationship.follower.id)
                                        navController.navigate(Destination.User)
                                    }
                                }
                            )
                        }
                    }
                    FollowersPageState.ERROR -> FollowersErrorState(message = errorState ?: "Failed to load followers", onRetry = { viewModel.refresh() }, modifier = Modifier.align(Alignment.Center))
                    FollowersPageState.EMPTY -> FollowersEmptyState(modifier = Modifier.align(Alignment.Center))
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

        if (followersPagingItems.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(followerRelationship.follower.profilePicture)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile picture",
                modifier = Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                placeholder = painterResource(R.drawable.loading_placeholder),
                error = painterResource(R.drawable.profile_default),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val fullName = "${followerRelationship.follower.name ?: ""} ${followerRelationship.follower.lastName ?: ""}".trim()
                if (fullName.isNotEmpty()) {
                    Text(text = fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                followerRelationship.follower.username?.let { username ->
                    Text(text = "@$username", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(text = "Follower since ${followerRelationship.createdAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }

            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Navigate", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun FollowersLoadingState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Loading followers...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FollowersErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.Default.Person, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Failed to load followers", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Retry") }
    }
}

@Composable
fun FollowersEmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.Default.Person, contentDescription = "No Followers", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "No followers", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "You do not have any followers yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
    }
}

@Composable
fun FollowersEmptySearchState(searchQuery: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.Default.Search, contentDescription = "No Results", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "No results found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "There are no followers that match \"$searchQuery\"", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
    }
}

@Composable
fun FollowersSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .focusRequester(focusRequester)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        placeholder = { Text("Search followers...") },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}
