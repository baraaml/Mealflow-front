package com.example.mealflow.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mealflow.navigation.Destination
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.mealflow.data.model.AllCommunities
import com.example.mealflow.data.model.AllCommunitiesCategory
import com.example.mealflow.data.model.AllCommunitiesMember
import com.example.mealflow.data.model.AllCommunitiesUser
import com.example.mealflow.data.model.CommunityRole
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.joinCommunityApi
import com.example.mealflow.ui.components.AppendEmptyState
import com.example.mealflow.ui.components.AppendErrorState
import com.example.mealflow.ui.components.AppendLoadingState
import com.example.mealflow.ui.components.EmptyState
import com.example.mealflow.ui.components.ErrorState
import com.example.mealflow.ui.components.LoadingState
import com.example.mealflow.ui.theme.MealFlowTheme
import com.example.mealflow.viewModel.AllCommunitiesViewModel
import com.example.mealflow.viewModel.MyCommunitiesViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCommunitiesScreen(
    navController: NavController,
    viewModel: AllCommunitiesViewModel = viewModel()
) {
    val communities = viewModel.communitiesPagingData.collectAsLazyPagingItems()
    val isLoading = communities.loadState.refresh is LoadState.Loading
    val errorMessage by viewModel.errorState.collectAsState("")
    val context = LocalContext.current

    // Use remember saveable to persist this state across navigation events
    var hasInitiallyLoaded by rememberSaveable { mutableStateOf(false) }
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    // Track refreshing state
    var isRefreshing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Function to refresh data
    suspend fun refreshData() {
        isRefreshing = true
        communities.refresh()
        viewModel.refresh()
        delay(500) // Small delay to show refresh animation
        isRefreshing = false
    }

    // Only load initial communities if they haven't been loaded before
    LaunchedEffect(Unit) {
        if (!hasInitiallyLoaded) {
            Log.d("AllCommunitiesScreen", "Calling loadInitialCommunities() for the first time")
            viewModel.loadInitialCommunities()
            hasInitiallyLoaded = true
        } else {
            Log.d("AllCommunitiesScreen", "Skipping loadInitialCommunities() - already loaded")
        }
    }

    // Error handling
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Log.e("AllCommunitiesScreen", "Error: $it")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Custom Top Bar using Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "All Communities",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Content with SwipeRefresh
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                coroutineScope.launch {
                    refreshData()
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 220.dp),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        // Show communities count if available
                        if (communities.itemCount > 0) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Communities (${communities.itemCount})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Loading state
                        if (communities.loadState.refresh is LoadState.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                LoadingState()
                            }
                        } else if (communities.loadState.refresh is LoadState.Error && communities.itemCount == 0) {
                            // Error state
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ErrorState(
                                    message = "Failed to load communities",
                                    onRetry = { communities.refresh() }
                                )
                            }
                        } else if (communities.itemCount == 0) {
                            // Empty state
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EmptyState(message = "No communities found")
                            }
                        } else {
                            // Communities items
                            items(
                                count = communities.itemCount,
                                key = { index ->
                                    val community = communities[index]
                                    community?.let { "community_${it.id}" } ?: "community_index_${index}"
                                }
                            ) { index ->
                                val community = communities[index]
                                community?.let {
                                    CommunityCard(
                                        community = it,
                                        onCommunityClick = { communityId ->
                                            CoroutineScope(Dispatchers.IO).launch {
                                                userPreferencesManager.saveCommunityId(communityId)
                                            }
                                            navController.navigate(Destination.CommunityPage)
                                        },
                                        onJoinClick = { communityId ->
                                            joinCommunityApi(communityId, context)
                                            // Handle join community action
                                            Log.d("AllCommunitiesScreen", "Join community: $communityId")
                                        }
                                    )
                                }
                            }

                            // End of pagination reached
                            if (
                                communities.loadState.append is LoadState.NotLoading &&
                                communities.loadState.refresh !is LoadState.Loading &&
                                communities.itemCount > 0 &&
                                communities.loadState.append.endOfPaginationReached
                            ) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    AppendEmptyState(message = "No more communities")
                                }
                            }
                        }

                        // Append loading state
                        if (communities.loadState.append is LoadState.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                AppendLoadingState()
                            }
                        } else if (communities.loadState.append is LoadState.Error) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                AppendErrorState(
                                    message = "Error loading more communities",
                                    onRetry = { communities.retry() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCommunitiesScreen(
    navController: NavController,
    viewModel: MyCommunitiesViewModel = viewModel()
) {
    val communities = viewModel.communitiesPagingData.collectAsLazyPagingItems()
    val isLoading = communities.loadState.refresh is LoadState.Loading
    val errorMessage by viewModel.errorState.collectAsState("")
    val context = LocalContext.current

    // Use remember saveable to persist this state across navigation events
    var hasInitiallyLoaded by rememberSaveable { mutableStateOf(false) }
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    // Track refreshing state
    var isRefreshing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Function to refresh data
    suspend fun refreshData() {
        isRefreshing = true
        communities.refresh()
        viewModel.refresh()
        delay(500) // Small delay to show refresh animation
        isRefreshing = false
    }

    // Only load initial communities if they haven't been loaded before
    LaunchedEffect(Unit) {
        if (!hasInitiallyLoaded) {
            Log.d("AllCommunitiesScreen", "Calling loadInitialCommunities() for the first time")
            viewModel.loadInitialCommunities()
            hasInitiallyLoaded = true
        } else {
            Log.d("AllCommunitiesScreen", "Skipping loadInitialCommunities() - already loaded")
        }
    }

    // Error handling
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Log.e("MyCommunitiesScreen", "Error: $it")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Custom Top Bar using Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "My Communities",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Content with SwipeRefresh
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                coroutineScope.launch {
                    refreshData()
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 220.dp),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        // Show communities count if available
                        if (communities.itemCount > 0) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Communities (${communities.itemCount})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Loading state
                        if (communities.loadState.refresh is LoadState.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                LoadingState()
                            }
                        } else if (communities.loadState.refresh is LoadState.Error && communities.itemCount == 0) {
                            // Error state
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ErrorState(
                                    message = "Failed to load communities",
                                    onRetry = { communities.refresh() }
                                )
                            }
                        } else if (communities.itemCount == 0) {
                            // Empty state
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EmptyState(message = "No communities found")
                            }
                        } else {
                            // Communities items
                            items(
                                count = communities.itemCount,
                                key = { index ->
                                    val community = communities[index]
                                    community?.let { "community_${it.community.id}" } ?: "community_index_${index}"
                                }
                            ) { index ->
                                val community = communities[index]
                                community?.let {
                                    MyCommunityCard(
                                        community = it,
                                        onCommunityClick = { communityId ->
                                            CoroutineScope(Dispatchers.IO).launch {
                                                userPreferencesManager.saveCommunityId(communityId)
                                            }
                                            navController.navigate(Destination.CommunityPage)
                                        },
                                        onJoinClick = { communityId ->
                                            joinCommunityApi(communityId, context)
                                            // Handle join community action
                                            Log.d("AllCommunitiesScreen", "Join community: $communityId")
                                        }
                                    )
                                }
                            }

                            // End of pagination reached
                            if (
                                communities.loadState.append is LoadState.NotLoading &&
                                communities.loadState.refresh !is LoadState.Loading &&
                                communities.itemCount > 0 &&
                                communities.loadState.append.endOfPaginationReached
                            ) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    AppendEmptyState(message = "No more communities")
                                }
                            }
                        }

                        // Append loading state
                        if (communities.loadState.append is LoadState.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                AppendLoadingState()
                            }
                        } else if (communities.loadState.append is LoadState.Error) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                AppendErrorState(
                                    message = "Error loading more communities",
                                    onRetry = { communities.retry() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAllCommunitiesScreen() {
    val navController = rememberNavController()
    MealFlowTheme {
        AllCommunitiesScreen(navController = navController)
    }
}

@Composable
fun CommunityCard(
    community: AllCommunities,
    onCommunityClick: (String) -> Unit,
    onJoinClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        onClick = { onCommunityClick(community.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Community image with join button
            Box(
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            ) {
                // Community image
                AsyncImage(
                    model = community.image ?: "https://photobasement.com/wp-content/uploads/2017/04/this-is-a-photo.jpg",
                    contentDescription = "Community image: ${community.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Transparent layer for contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )

//                // Join button (only show if not a member)
//                if (!community.isMember) {
//                    Button(
//                        onClick = { onJoinClick(community.id) },
//                        modifier = Modifier
//                            .padding(16.dp)
//                            .align(Alignment.TopEnd)
//                            .heightIn(min = 40.dp)
//                            .widthIn(min = 80.dp)
//                            .shadow(
//                                elevation = 4.dp,
//                                shape = RoundedCornerShape(16.dp),
//                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
//                            ),
//                        shape = RoundedCornerShape(16.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = MaterialTheme.colorScheme.primary,
//                            contentColor = MaterialTheme.colorScheme.onPrimary
//                        ),
//                        elevation = ButtonDefaults.buttonElevation(
//                            defaultElevation = 2.dp,
//                            pressedElevation = 6.dp,
//                            hoveredElevation = 4.dp
//                        ),
//                        contentPadding = PaddingValues(
//                            horizontal = 20.dp,
//                            vertical = 12.dp
//                        )
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.Center
//                        ) {
//                            Text(
//                                text = "Join",
//                                style = MaterialTheme.typography.labelLarge.copy(
//                                    fontWeight = FontWeight.SemiBold,
//                                    letterSpacing = 0.5.sp
//                                ),
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                        }
//                    }
//                }

                // Admin badge (if user is admin)
                if (community.isAdmin) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Admin",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Community Information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon and number of members
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${community.members.size} member${if (community.members.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Privacy indicator
                    Icon(
                        imageVector = if (community.privacy == "PUBLIC") Icons.Default.Public else Icons.Default.Lock,
                        contentDescription = community.privacy,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = community.privacy.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Categories (if available)
                if (community.categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        community.categories.take(2).forEach { category ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        if (community.categories.size > 2) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+${community.categories.size - 2}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityCard() {
    val sampleCommunity = AllCommunities(
        id = "1",
        name = "Food Lovers Community",
        description = "A community for people who love cooking and sharing recipes with each other.",
        image = null,
        privacy = "PUBLIC",
        mealCreationPermission = "ALL",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        categories = listOf(
            AllCommunitiesCategory(id = "1", name = "Cooking"),
            AllCommunitiesCategory(id = "2", name = "Recipes"),
            AllCommunitiesCategory(id = "3", name = "Healthy Food"),
            AllCommunitiesCategory(id = "4", name = "Desserts")
        ),
        members = listOf(
            AllCommunitiesMember(
                role = "MEMBER",
                joinedAt = "2024-01-01T00:00:00Z",
                user = AllCommunitiesUser(id = "1", name = "John Doe", username = "john")
            ),
            AllCommunitiesMember(
                role = "MEMBER",
                joinedAt = "2024-01-01T00:00:00Z",
                user = AllCommunitiesUser(id = "2", name = "Jane Smith", username = "jane")
            )
        ),
        owner = AllCommunitiesUser(id = "1", name = "John Doe", username = "john"),
        isMember = false,
        isAdmin = false
    )

    MealFlowTheme {
        CommunityCard(
            community = sampleCommunity,
            onCommunityClick = { },
            onJoinClick = { }
        )
    }
}

@Composable
fun MyCommunityCard(
    community: CommunityRole,
    onCommunityClick: (String) -> Unit,
    onJoinClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        onClick = { onCommunityClick(community.community.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Community image with join button
            Box(
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            ) {
                // Community image
                AsyncImage(
                    model = community.community.image ?: "https://photobasement.com/wp-content/uploads/2017/04/this-is-a-photo.jpg",
                    contentDescription = "Community image: ${community.community.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Transparent layer for contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )

//                // Admin badge (if user is admin)
//                if (community.community.isAdmin) {
//                    Box(
//                        modifier = Modifier
//                            .padding(16.dp)
//                            .align(Alignment.TopStart)
//                            .background(
//                                color = MaterialTheme.colorScheme.errorContainer,
//                                shape = RoundedCornerShape(8.dp)
//                            )
//                            .padding(horizontal = 8.dp, vertical = 4.dp)
//                    ) {
//                        Text(
//                            text = "Admin",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.onErrorContainer,
//                            fontWeight = FontWeight.Medium
//                        )
//                    }
//                }
            }

            // Community Information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = community.community.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon and number of members
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(
//                        text = "${community.members.size} member${if (community.members.size != 1) "s" else ""}",
//                        style = MaterialTheme.typography.bodySmall
//                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Privacy indicator
                    Icon(
                        imageVector = if (community.community.privacy== "PUBLIC") Icons.Default.Public else Icons.Default.Lock,
                        contentDescription = community.community.privacy,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = community.community.privacy.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall
                    )
                }

//                // Categories (if available)
//                if (community.categories.isNotEmpty()) {
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(4.dp),
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        community.categories.take(2).forEach { category ->
//                            Box(
//                                modifier = Modifier
//                                    .background(
//                                        color = MaterialTheme.colorScheme.primaryContainer,
//                                        shape = RoundedCornerShape(6.dp)
//                                    )
//                                    .padding(horizontal = 6.dp, vertical = 2.dp)
//                            ) {
//                                Text(
//                                    text = category.name,
//                                    style = MaterialTheme.typography.labelSmall,
//                                    color = MaterialTheme.colorScheme.onPrimaryContainer
//                                )
//                            }
//                        }
//
//                        if (community.categories.size > 2) {
//                            Box(
//                                modifier = Modifier
//                                    .background(
//                                        color = MaterialTheme.colorScheme.surfaceVariant,
//                                        shape = RoundedCornerShape(6.dp)
//                                    )
//                                    .padding(horizontal = 6.dp, vertical = 2.dp)
//                            ) {
//                                Text(
//                                    text = "+${community.categories.size - 2}",
//                                    style = MaterialTheme.typography.labelSmall,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                                )
//                            }
//                        }
//                    }
//                }
            }
        }
    }
}