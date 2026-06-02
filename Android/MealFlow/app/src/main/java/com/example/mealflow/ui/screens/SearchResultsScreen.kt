package com.example.mealflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.mealflow.R
import com.example.mealflow.data.model.Post
import com.example.mealflow.data.model.SearchComment
import com.example.mealflow.data.model.SearchCommunity
import com.example.mealflow.data.model.SearchUser
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.CommentApiService
import com.example.mealflow.ui.components.PostUserInCommunity
import com.example.mealflow.ui.components.post.PostFeed
import com.example.mealflow.ui.components.post.navigateToPostDetails
import com.example.mealflow.viewModel.FeedViewModel
import com.example.mealflow.viewModel.PostDropdownViewModel
import com.example.mealflow.viewModel.SearchViewModel
import com.example.mealflow.viewModel.UpdatePostViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchResultsScreen(
    navController: NavController,
    searchViewModel: SearchViewModel = viewModel()
) {
    val searchQuery by searchViewModel.searchQuery.collectAsState()
    val searchType by searchViewModel.searchType.collectAsState()
    val isLoading by searchViewModel.isLoading.collectAsState()
    val errorState by searchViewModel.errorState.collectAsState()

    // Collect paging data
    val searchPosts = searchViewModel.searchPosts.collectAsLazyPagingItems()
    val searchUsers = searchViewModel.searchUsers.collectAsLazyPagingItems()
    val searchComments = searchViewModel.searchComments.collectAsLazyPagingItems()
    val searchCommunities = searchViewModel.searchCommunities.collectAsLazyPagingItems()

    var selectedTabIndex by remember { mutableIntStateOf(
        when (searchType) {
            "all" -> 0
            "posts" -> 1
            "users" -> 2
            "communities" -> 3
            "comments" -> 4
            else -> 0
        }
    ) }

    val tabTitles = listOf("All", "Posts", "People", "Communities", "Comments")
    val tabTypes = listOf("all", "posts", "users", "communities", "comments")

    LaunchedEffect(selectedTabIndex) {
        val newType = tabTypes[selectedTabIndex]
        if (newType != searchType) {
            searchViewModel.updateType(newType)
            if (searchQuery.length >= 2) {
                searchViewModel.search(searchQuery, newType)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Custom Top Bar for Search Results
        SearchResultsTopBar(
            query = searchQuery,
            onQueryChange = { newQuery ->
                searchViewModel.updateQuery(newQuery)
                if (newQuery.length >= 2) {
                    searchViewModel.search(newQuery, searchType)
                }
            },
            onBackClick = { navController.popBackStack() },
            onClearClick = {
                searchViewModel.clearSearch()
            }
        )

        // Search Results Count and Filters
        if (searchQuery.isNotEmpty()) {
            SearchResultsHeader(
                query = searchQuery,
                searchType = searchType,
                resultCount = when (searchType) {
                    "posts" -> searchPosts.itemCount
                    "users" -> searchUsers.itemCount
                    "communities" -> searchCommunities.itemCount
                    "comments" -> searchComments.itemCount
                    else -> 0
                }
            )
        }

        // Error handling
        errorState?.let { error ->
            ErrorMessage(
                message = error,
                onDismiss = { searchViewModel.clearError() }
            )
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Content based on selected tab
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTabIndex) {
                0 -> AllSearchResults(
                    posts = searchPosts,
                    users = searchUsers,
                    communities = searchCommunities,
                    comments = searchComments,
                    navController = navController
                )
                1 -> PostsSearchResults(posts = searchPosts, navController = navController)
                2 -> UsersSearchResults(users = searchUsers, navController = navController)
                3 -> CommunitiesSearchResults(communities = searchCommunities, navController = navController)
                4 -> CommentsSearchResults(comments = searchComments, navController = navController)
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search...",
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
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = onClearClick,
                            modifier = Modifier.size(24.dp)
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
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun SearchResultsHeader(
    query: String,
    searchType: String,
    resultCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Search results for \"$query\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (resultCount > 0) {
            Text(
                text = "$resultCount results found",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AllSearchResults(
    posts: LazyPagingItems<Post>,
    users: LazyPagingItems<SearchUser>,
    communities: LazyPagingItems<SearchCommunity>,
    comments: LazyPagingItems<SearchComment>,
    navController: NavController
) {
    val context = LocalContext.current
    // Initialize the view model
    val viewModel: PostDropdownViewModel = viewModel()
    val viewModelUpdate: UpdatePostViewModel = viewModel()

    val commentApiService = remember { CommentApiService(context) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Posts Section
        if (posts.itemCount > 0) {
            item {
                SectionHeader(title = "Posts", itemCount = posts.itemCount)
            }

            items(minOf(3, posts.itemCount)) { index ->
                posts[index]?.let { post ->
                    if (post.community == null) {
                        PostFeed(
                            post = post,
                            author = post.author,
                            refresh = {
//                            coroutineScope.launch {
//                                refreshData()
//                            }
                            },
                            onCommentClick = { /* Navigate to comments */ },
                            onShareClick = { /* Implement share functionality */ },
                            navController = navController,
                            viewModel = viewModel,
                            viewModelUpdate = viewModelUpdate,
                            commentApiService = commentApiService
                        )
                    } else {
                        PostUserInCommunity(
                            post = post,
                            refresh = {
//                            coroutineScope.launch {
//                                refreshData()
//                            }
                            },
                            navController = navController,
                            isAuthor = true,
                            commentApiService = commentApiService,
                            onLikeClick = { /* Implement like functionality */ },
                            onCommentClick = { /* Navigate to comments */ },
                            onShareClick = { /* Implement share functionality */ }
                        )
                    }
//                    PostSearchItem(
//                        post = post,
//                        onClick = { navigateToPostDetails(navController, post.id) }
//                    )
                }
            }

            if (posts.itemCount > 3) {
                item {
                    ViewMoreButton(
                        text = "View all ${posts.itemCount} posts",
                        onClick = { /* Switch to posts tab */ }
                    )
                }
            }
        }

        // Top Users Section
        if (users.itemCount > 0) {
            item {
                SectionHeader(title = "People", itemCount = users.itemCount)
            }

            items(minOf(3, users.itemCount)) { index ->
                users[index]?.let { user ->
                    UserSearchItem(
                        user = user,
                        navController = navController,
                        onClick = { /* Navigate to user profile */ }
                    )
                }
            }

            if (users.itemCount > 3) {
                item {
                    ViewMoreButton(
                        text = "View all ${users.itemCount} people",
                        onClick = { /* Switch to users tab */ }
                    )
                }
            }
        }

        // Top Communities Section
        if (communities.itemCount > 0) {
            item {
                SectionHeader(title = "Communities", itemCount = communities.itemCount)
            }

            items(minOf(3, communities.itemCount)) { index ->
                communities[index]?.let { community ->
                    CommunitySearchItem(
                        community = community,
                        onClick = { /* Navigate to community */ }
                    )
                }
            }

            if (communities.itemCount > 3) {
                item {
                    ViewMoreButton(
                        text = "View all ${communities.itemCount} communities",
                        onClick = { /* Switch to communities tab */ }
                    )
                }
            }
        }
    }
}

@Composable
fun PostsSearchResults(
    posts: LazyPagingItems<Post>,
    navController: NavController
) {
    val context = LocalContext.current
    // Initialize the view model
    val viewModel: PostDropdownViewModel = viewModel()
    val viewModelUpdate: UpdatePostViewModel = viewModel()

    val commentApiService = remember { CommentApiService(context) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posts.itemCount) { index ->
            posts[index]?.let { post ->
                if (post.community == null) {
                    PostFeed(
                        post = post,
                        author = post.author,
                        refresh = {
//                            coroutineScope.launch {
//                                refreshData()
//                            }
                        },
                        onCommentClick = { /* Navigate to comments */ },
                        onShareClick = { /* Implement share functionality */ },
                        navController = navController,
                        viewModel = viewModel,
                        viewModelUpdate = viewModelUpdate,
                        commentApiService = commentApiService
                    )
                } else {
                    PostUserInCommunity(
                        post = post,
                        refresh = {
//                            coroutineScope.launch {
//                                refreshData()
//                            }
                        },
                        navController = navController,
                        isAuthor = true,
                        commentApiService = commentApiService,
                        onLikeClick = { /* Implement like functionality */ },
                        onCommentClick = { /* Navigate to comments */ },
                        onShareClick = { /* Implement share functionality */ }
                    )
                }
//                PostSearchItem(
//                    post = post,
//                    onClick = { navigateToPostDetails(navController, post.id) }
//                )
            }
        }

        // Handle loading states
        when (posts.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            is LoadState.Error -> {
                item {
                    ErrorMessage(
                        message = "Failed to load more results",
                        onDismiss = { posts.retry() }
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun UsersSearchResults(
    users: LazyPagingItems<SearchUser>,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(users.itemCount) { index ->
            users[index]?.let { user ->
                UserSearchItem(
                    user = user,
                    navController = navController,
                    onClick = { /* Navigate to user profile */ }
                )
            }
        }

        when (users.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            is LoadState.Error -> {
                item {
                    ErrorMessage(
                        message = "Failed to load more results",
                        onDismiss = { users.retry() }
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun CommunitiesSearchResults(
    communities: LazyPagingItems<SearchCommunity>,
    navController: NavController
) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(communities.itemCount) { index ->
            communities[index]?.let { community ->
                CommunitySearchItem(
                    community = community,
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            userPreferencesManager.saveCommunityId(community.id)
                        }
                        navController.navigate("Community Page")
                    }
                )
            }
        }

        when (communities.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            is LoadState.Error -> {
                item {
                    ErrorMessage(
                        message = "Failed to load more results",
                        onDismiss = { communities.retry() }
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun CommentsSearchResults(
    comments: LazyPagingItems<SearchComment>,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(comments.itemCount) { index ->
            comments[index]?.let { comment ->
                CommentSearchItem(
                    comment = comment,
                    onClick = { navigateToPostDetails(navController, comment.post.id) }
                )
            }
        }

        when (comments.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            is LoadState.Error -> {
                item {
                    ErrorMessage(
                        message = "Failed to load more results",
                        onDismiss = { comments.retry() }
                    )
                }
            }
            else -> {}
        }
    }
}

// Search Item Components

@Composable
fun PostSearchItem(
    post: Post,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Post header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Author avatar
                AsyncImage(
                    model = post.author.id,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.profile_default),
                    error = painterResource(R.drawable.profile_default)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${post.author.name} ${post.author.id}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@${post.author.username}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = formatTimeAgo(post.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post title
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Post content preview
            if (post.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.ThumbUp,
                        contentDescription = "Likes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.likeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.commentCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (post.community != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Group,
                            contentDescription = "Community",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.community.name.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchItem(
    user: SearchUser,
    navController: NavController,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var myId by remember { mutableStateOf<String?>(null) }
    var postUserId by remember { mutableStateOf<String?>(null) }
    var isMyPost by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val id = UserPreferencesManager(context).getMyId()
            val postId = user.id
            myId = id
            postUserId = postId
            isMyPost = (id == postId)
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(
                    onClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                UserPreferencesManager(context).saveUserId(postUserId.toString())
                            }
                            if (isMyPost == true) {
                                navController.navigate("Profile Page")
                            } else {
                                navController.navigate("User Page")
                            }
                        }
                    }
                )
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            AsyncImage(
                model = user.profilePicture,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                placeholder = painterResource(R.drawable.profile_default),
                error = painterResource(R.drawable.profile_default)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.name} ${user.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (user.bio?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // User stats
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${user._count.posts} posts",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${user._count.followers} followers",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Follow button
            OutlinedButton(
                onClick = { /* Handle follow/unfollow */ },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (user.isFollowing) MaterialTheme.colorScheme.primary
                    else Color.Transparent,
                    contentColor = if (user.isFollowing) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(
                    1.dp,
                    if (user.isFollowing) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = if (user.isFollowing) "Following" else "Follow",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CommunitySearchItem(
    community: SearchCommunity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Community image
            AsyncImage(
                model = community.image,
                contentDescription = "Community Image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                placeholder = painterResource(R.drawable.profile_default),
                error = painterResource(R.drawable.profile_default)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = community.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Community stats
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Group,
                            contentDescription = "Members",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${community.memberCount} members",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Article,
                            contentDescription = "Posts",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${community.postCount} posts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Privacy indicator
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (community.privacy == "PUBLIC") Icons.Rounded.Public
                        else Icons.Rounded.Lock,
                        contentDescription = "Privacy",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = community.privacy.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Join button
            OutlinedButton(
                onClick = { /* Handle join */ },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "Join",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CommentSearchItem(
    comment: SearchComment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Comment header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = comment.author.profilePicture,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.profile_default),
                    error = painterResource(R.drawable.profile_default)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${comment.author.name} ${comment.author.lastName}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@${comment.author.username}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = formatTimeAgo(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comment content
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Post context
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "In: ${comment.post.title}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "by ${comment.post.author.name} ${comment.post.author.lastName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (comment.post.community != null) {
                        Text(
                            text = "in ${comment.post.community.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment stats
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.ThumbUp,
                    contentDescription = "Likes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = comment.likeCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper Components

@Composable
fun SectionHeader(
    title: String,
    itemCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "$itemCount results",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ViewMoreButton(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Utility function for time formatting
fun formatTimeAgo(timestamp: String): String {
    // Implement time ago formatting logic
    return "2h ago" // Placeholder
}