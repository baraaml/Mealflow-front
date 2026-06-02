import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.mealflow.R
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.FollowRelationship
import com.example.mealflow.ui.screens.AppBarMembers
import com.example.mealflow.viewModel.MyFollowingViewModel
import com.example.mealflow.viewModel.UserFollowingViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

enum class FollowingPageState {
    LOADING,
    SUCCESS,
    ERROR,
    EMPTY
}

@Composable
fun FollowingPage(
    navController: NavController,
    viewModel: MyFollowingViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val followingPagingItems = viewModel.followingFlow.collectAsLazyPagingItems()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState() // تغيير من حالة محلية إلى ViewModel

    var pageState by remember { mutableStateOf(FollowingPageState.LOADING) }

    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = isLoading
    )

    // Determine page state based on loading state and data
    LaunchedEffect(followingPagingItems.loadState, followingPagingItems.itemCount) {
        val loadState = followingPagingItems.loadState

        pageState = when {
            loadState.refresh is LoadState.Loading -> FollowingPageState.LOADING
            loadState.refresh is LoadState.Error -> FollowingPageState.ERROR
            followingPagingItems.itemCount == 0 -> FollowingPageState.EMPTY
            else -> FollowingPageState.SUCCESS
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppBarMembers(
            title = "Following",
            onBackClick = { navController.popBackStack() }
        )

        SearchBar(
            query = searchQuery ?: "",
            onQueryChange = { viewModel.updateSearchQuery(it) }, // استخدام ViewModel
            onClearQuery = { viewModel.clearSearch() } // استخدام ViewModel
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
                    FollowingPageState.LOADING -> {
                        LoadingState(modifier = Modifier.align(Alignment.Center))
                    }
                    FollowingPageState.SUCCESS -> {
                        if (followingPagingItems.itemCount == 0 && !searchQuery.isNullOrEmpty()) {
                            EmptySearchState(
                                searchQuery = searchQuery!!,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            FollowingList(
                                followingPagingItems = followingPagingItems,
                                onUserClick = { followRelationship ->
                                    coroutineScope.launch {
                                        val userPrefs = UserPreferencesManager(context)
                                        userPrefs.saveUserId(followRelationship.following.id)
                                        navController.navigate("User Page")
                                    }
                                }
                            )
                        }
                    }
                    FollowingPageState.ERROR -> {
                        ErrorState(
                            message = errorState ?: "Failed to load following",
                            onRetry = { viewModel.refresh() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    FollowingPageState.EMPTY -> {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun UserFollowingPage(
    navController: NavController,
    viewModel: UserFollowingViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val followingPagingItems = viewModel.followingFlow.collectAsLazyPagingItems()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState() // تغيير من حالة محلية إلى ViewModel

    var pageState by remember { mutableStateOf(FollowingPageState.LOADING) }

    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = isLoading
    )

    // Determine page state based on loading state and data
    LaunchedEffect(followingPagingItems.loadState, followingPagingItems.itemCount) {
        val loadState = followingPagingItems.loadState

        pageState = when {
            loadState.refresh is LoadState.Loading -> FollowingPageState.LOADING
            loadState.refresh is LoadState.Error -> FollowingPageState.ERROR
            followingPagingItems.itemCount == 0 -> FollowingPageState.EMPTY
            else -> FollowingPageState.SUCCESS
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppBarMembers(
            title = "Following",
            onBackClick = { navController.popBackStack() }
        )

        SearchBar(
            query = searchQuery ?: "",
            onQueryChange = { viewModel.updateSearchQuery(it) }, // استخدام ViewModel
            onClearQuery = { viewModel.clearSearch() } // استخدام ViewModel
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
                    FollowingPageState.LOADING -> {
                        LoadingState(modifier = Modifier.align(Alignment.Center))
                    }
                    FollowingPageState.SUCCESS -> {
                        if (followingPagingItems.itemCount == 0 && !searchQuery.isNullOrEmpty()) {
                            EmptySearchState(
                                searchQuery = searchQuery!!,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            FollowingList(
                                followingPagingItems = followingPagingItems,
                                onUserClick = { followRelationship ->
                                    coroutineScope.launch {
                                        val userPrefs = UserPreferencesManager(context)
                                        userPrefs.saveUserId(followRelationship.following.id)
                                        navController.navigate("User Page")
                                    }
                                }
                            )
                        }
                    }
                    FollowingPageState.ERROR -> {
                        ErrorState(
                            message = errorState ?: "Failed to load following",
                            onRetry = { viewModel.refresh() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    FollowingPageState.EMPTY -> {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun FollowingList(
    followingPagingItems: androidx.paging.compose.LazyPagingItems<FollowRelationship>,
    onUserClick: (FollowRelationship) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(followingPagingItems.itemCount) { index ->
            val followRelationship = followingPagingItems[index]
            followRelationship?.let {
                FollowingUserItem(
                    followRelationship = it,
                    onUserClick = { onUserClick(it) }
                )

                if (index < followingPagingItems.itemCount - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Show loading footer when appending data
        if (followingPagingItems.loadState.append is LoadState.Loading) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    onBackClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun SearchBar(
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
        placeholder = {
            Text("Search following...")
        },
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
                        contentDescription = "Clear"
                    )
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
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
        })
    )
}

@Composable
fun FollowingUserItem(
    followRelationship: FollowRelationship,
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
                model = followRelationship.following.profilePicture,
                contentDescription = "Profile picture of ${followRelationship.following.name}",
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
                val fullName = "${followRelationship.following.name ?: ""} ${followRelationship.following.lastName ?: ""}".trim()
                if (fullName.isNotEmpty()) {
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // عرض username إذا كان متوفراً
                followRelationship.following.username?.let { username ->
                    Text(
                        text = "@$username",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // عرض تاريخ المتابعة
                Text(
                    text = "Following since ${formatDate(followRelationship.createdAt)}",
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

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Loading following...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Failed to load following",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "No Following",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No followers",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You are not currently following any users.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun EmptySearchState(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "No Results",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No results found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "There are no users following that match \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
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