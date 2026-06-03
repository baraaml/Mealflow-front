package com.example.mealflow.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.mealflow.R
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.CommentApiService
import com.example.mealflow.network.MyProfileApiService
import com.example.mealflow.network.UserProfile
import com.example.mealflow.network.joinCommunityApi
import com.example.mealflow.network.logoutApi
import com.example.mealflow.ui.components.AppendEmptyState
import com.example.mealflow.ui.components.AppendErrorState
import com.example.mealflow.ui.components.AppendLoadingState
import com.example.mealflow.ui.components.BottomSheetOption
import com.example.mealflow.ui.components.CardCommunityProfile
import com.example.mealflow.ui.components.EmptyState
import com.example.mealflow.ui.components.ErrorState
import com.example.mealflow.ui.components.LoadingState
import com.example.mealflow.ui.components.ModernDescriptionSectionProfile
import com.example.mealflow.ui.components.ModernFollowersAndFollowingCount
import com.example.mealflow.ui.components.PostUser1
import com.example.mealflow.ui.components.PostUserInCommunity
import com.example.mealflow.ui.screens.search.MealSearchAction
import com.example.mealflow.viewModel.MealSearchViewModel
import com.example.mealflow.viewModel.MyCommunitiesViewModel
import com.example.mealflow.viewModel.PostDropdownViewModel
import com.example.mealflow.viewModel.PostsViewModel
import com.example.mealflow.viewModel.UpdatePostViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

// Constants for animation
private val COLLAPSE_THRESHOLD = 120.dp
private val HEADER_HEIGHT = 250.dp
private val PROFILE_IMAGE_SIZE = 120.dp
private val PROFILE_IMAGE_SIZE_COLLAPSED = 40.dp
private val HEADER_IMAGE_HEIGHT = 180.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//fun ProfilePage(navController: NavController) {
fun ProfilePage(
    navController: NavController,
    postsViewModel: PostsViewModel,
    communitiesViewModel: MyCommunitiesViewModel,
    mealSearchViewModel: MealSearchViewModel,
    viewModel: PostDropdownViewModel,
    viewModelUpdate: UpdatePostViewModel,
    commentApiService: CommentApiService,
    userPreferencesManager: UserPreferencesManager
) {
    val context = LocalContext.current

//    // Initialize the view model
//    val postsViewModel: PostsViewModel = viewModel()
//    val communitiesViewModel: MyCommunitiesViewModel = viewModel()
//    val mealSearchViewModel: MealSearchViewModel = viewModel()
//    val viewModel: PostDropdownViewModel = viewModel()
//    val viewModelUpdate: UpdatePostViewModel = viewModel()
//
//    val commentApiService = remember { CommentApiService(context) }
//
//    val userPreferencesManager = remember { UserPreferencesManager(context) }

    // Get paging items
    val posts = postsViewModel.pagedPosts.collectAsLazyPagingItems()
    val communities = communitiesViewModel.communitiesPagingData.collectAsLazyPagingItems()


    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // LazyColumnState to track scroll position for collapsing header effect
    val lazyListState = rememberLazyListState()

    // Collect meals as LazyPagingItems
    val meals = mealSearchViewModel.pagedMeals.collectAsLazyPagingItems()

    // Calculate scroll state for animations with pixel values
    val scrollInPx = with(density) { COLLAPSE_THRESHOLD.toPx() }
    val headerHeightPx = with(density) { HEADER_HEIGHT.toPx() }

    // Calculate scroll progress (0 to 1)
    val scrollProgress by remember {
        derivedStateOf {
            val scrollPosition = min(
                1f,
                max(
                    0f, lazyListState.firstVisibleItemScrollOffset / scrollInPx +
                            lazyListState.firstVisibleItemIndex
                )
            )
            scrollPosition
        }
    }

    // Calculate various animation values based on scroll progress
    val headerHeightFactor by animateFloatAsState(
        targetValue = 1f - scrollProgress,
        animationSpec = tween(durationMillis = 300)
    )

    val headerAlpha by animateFloatAsState(
        targetValue = 1f - scrollProgress * 1.5f,
        animationSpec = tween(durationMillis = 300)
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (scrollProgress > 0.3f) min(1f, (scrollProgress - 0.3f) * 3f) else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    // Profile image animation values
    val profileImageSize by animateFloatAsState(
        targetValue = if (scrollProgress > 0.3f)
            max(
                PROFILE_IMAGE_SIZE_COLLAPSED.value,
                PROFILE_IMAGE_SIZE.value * (1f - scrollProgress * 1.2f)
            )
        else PROFILE_IMAGE_SIZE.value,
        animationSpec = tween(durationMillis = 300)
    )

    // State to hold user profile data
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State for bottom sheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen by remember { mutableStateOf(false) }

    // State for dropdown menu
    var isRefreshing by remember { mutableStateOf(false) }

    // Track if we've reached the end of available posts
    val hasMorePosts = remember { mutableStateOf(true) }
    val hasMoreCommunities = remember { mutableStateOf(true) }

    suspend fun fetchUserProfile() {
        // Your logic to fetch the user profile
        val result = MyProfileApiService(context).fetchMyProfile()

        // Handling the result
        if (result.success) {
            userProfile = result.data
            isLoading = false
        } else {
            errorMessage = result.message
            isLoading = false
        }
    }

    // Function to refresh all data
    suspend fun refreshData() {
        isRefreshing = true
        fetchUserProfile()
        posts.refresh() // Refresh the paging data
        meals.refresh()
        postsViewModel.refresh()
        communitiesViewModel.refresh()
//        postsViewModel.loadPosts(reset = true)
//        communitiesViewModel.loadCommunities(reset = true)
        hasMorePosts.value = true
        hasMoreCommunities.value = true
        delay(1000) // Small delay to show refresh animation
        isRefreshing = false
    }

    // Handle search
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val userId = userPreferencesManager.getMyId()
            Log.d("t", "user id: $userId")
            mealSearchViewModel.onAction(MealSearchAction.OnSearchParamsChange(MealSearchViewModel.SearchParams(userId = userId)))
            fetchUserProfile()
        }
//        val userId = userPreferencesManager.getMyId()
//        Log.d("t","user id : $userId")
//        // Initial search with default parameters
//        mealSearchViewModel.search(MealSearchViewModel.SearchParams(userId = userId))
    }
//    // Fetch user profile data and posts when the screen loads
//    LaunchedEffect(key1 = true) {
//        withContext(Dispatchers.IO) {
//            fetchUserProfile()
//        }
////        fetchUserProfile()
////        postsViewModel.loadPosts(reset = true)
////        communitiesViewModel.loadCommunities(reset = true)
//    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Add another menu button for when viewing the profile before scrolling
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .size(40.dp)
                .align(Alignment.TopEnd)
                .alpha(1f - titleAlpha)
                .zIndex(2f) // Ensure it's above other elements
        ) {
            MoreOptionsBottomSheet(navController = navController, logoutApi = ::logoutApi)
        }
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                coroutineScope.launch {
                    refreshData()
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Image with parallax effect (Samsung Food style)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HEADER_IMAGE_HEIGHT)
                        .graphicsLayer {
                            // Parallax effect for the background
                            translationY = -scrollProgress * 50f
                            alpha = max(0f, 1f - scrollProgress * 1.5f)
                        }
                ) {
                    // Header background image
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = userProfile?.coverPicture ?:R.drawable.neptune_placeholder_48,
                            error = painterResource(R.drawable.loading_placeholder),
                            placeholder = painterResource(R.drawable.loading_placeholder)
                        ),
                        contentDescription = "Header background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(radius = (8 * scrollProgress).dp)
                    )

                    // Semi-transparent overlay that gets darker as you scroll
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(
                                    alpha = 0.2f + (scrollProgress * 0.3f)
                                )
                            )
                    )
                }

                // Main content
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .zIndex(0f) // Set base z-index
                ) {
                    // User profile header - Samsung Food style (profile image on left, name & stats on right)
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(HEADER_HEIGHT * headerHeightFactor)
                        ) {
                            // Samsung Food style layout with profile image on left
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 80.dp),  // More space for the top bar
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = userProfile?.profilePicture ?: R.drawable.profile_default,
                                        error = painterResource(R.drawable.loading_placeholder),
                                        placeholder = painterResource(R.drawable.loading_placeholder)
                                    ),
                                    contentDescription = "Profile image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(profileImageSize.dp)
                                        .alpha(headerAlpha)
                                        .clip(CircleShape)
                                        .border(
                                            width = 3.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                // User info column - right side of profile image
                                Column(
                                    modifier = Modifier
                                        .alpha(headerAlpha)
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = "${userProfile?.name ?: ""} \n${userProfile?.lastName ?: ""}",
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily(Font(R.font.sfpro))
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            // Move stats below the profile image - removed black background as requested
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 200.dp)
                                    .padding(vertical = 12.dp)
                                    .alpha(headerAlpha),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Location row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.LocationOn,
                                            contentDescription = "Location",
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            "Location",
                                            fontSize = 14.sp
                                        )
                                    }

                                    // Following count
                                    Text(
                                        "${userProfile?._count?.following ?: 0} Following",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    // Followers count
                                    Text(
                                        "${userProfile?._count?.followers ?: 0} Followers",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Bio section moved below the profile row
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 250.dp)
                                    .alpha(headerAlpha),
                                horizontalAlignment = Alignment.Start
                            ) {
                                // Bio text
                                Text(
                                    userProfile?.bio ?: "Simple recipes that taste delicious. 😋",
                                    color = Color.Black, // Changed to black for white mode
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Content card with rounded top corners
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        ) {
                            Column {
                                ModernDescriptionSectionProfile(
                                    userProfile?.bio ?: "Bio........"
                                )
                                ModernFollowersAndFollowingCount(
                                    followersCount = userProfile?._count?.followers ?: 0,
                                    followingCount = userProfile?._count?.following ?: 0,
                                    isUser = false,
                                    navController
                                )

                                // Tab Row
                                TabRow(
                                    selectedTabIndex = selectedTabIndex,
                                    modifier = Modifier.fillMaxWidth(),
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = Color(0xFFFFA500),
                                    indicator = { tabPositions -> },
                                    divider = {
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = Color.LightGray.copy(alpha = 0.3f)
                                        )
                                    }
                                ) {
                                    Tab(
                                        selected = selectedTabIndex == 0,
                                        onClick = { selectedTabIndex = 0 },
                                        modifier = Modifier
                                            .padding(vertical = 12.dp)
                                    ) {
                                        Text(
                                            "Activity",
                                            color = if (selectedTabIndex == 0) tabActiveColor else Color.Gray,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                    Tab(
                                        selected = selectedTabIndex == 1,
                                        onClick = { selectedTabIndex = 1 },
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    ) {
                                        Text(
                                            "Recipes",
                                            color = if (selectedTabIndex == 1) tabActiveColor else Color.Gray,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                    Tab(
                                        selected = selectedTabIndex == 2,
                                        onClick = { selectedTabIndex = 2 },
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    ) {
                                        Text(
                                            "Community",
                                            color = if (selectedTabIndex == 2) tabActiveColor else Color.Gray,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // Conditionally add post items directly to main LazyColumn when Activity tab is selected
                    if (selectedTabIndex == 0) {
                        // Loading state for posts
                        if (posts.loadState.refresh is LoadState.Loading) {
                            // Loading state
                            item {
                                LoadingState()
                            }
                        } else if (posts.loadState.refresh is LoadState.Error && posts.itemCount == 0) {
                            // Error state
                            item {
                                ErrorState(
                                    message = "Failed to load conversation posts",
                                    onRetry = { posts.refresh() })
                            }
                        } else if (posts.itemCount == 0) {
                            // Empty state
                            item { EmptyState(message = "No posts yet") }
                        } else {
                            // Posts items
                            items(
                                count = posts.itemCount,
                                key = posts.itemKey { it.id }
                            ) { index ->
                                val post = posts[index]
                                post?.let {
                                    if (it.community == null) {
                                        userProfile?.let { profile ->
                                            PostUser1(
                                                post = it,
                                                userProfile = profile,
                                                refresh = {
                                                    coroutineScope.launch {
                                                        refreshData()
                                                    }
                                                },
                                                onCommentClick = { /* Navigate to comments */ },
                                                onShareClick = { /* Implement share functionality */ },
                                                context = context,
                                                viewModel = viewModel,
                                                viewModelUpdate = viewModelUpdate,
                                                commentApiService = commentApiService,
                                                navController = navController
                                            )
                                        }
                                    } else {
                                        PostUserInCommunity(
                                            post = it,
                                            refresh = {
                                                coroutineScope.launch {
                                                    refreshData()
                                                }
                                            },
                                            navController = navController,
                                            isAuthor = true,
                                            commentApiService = commentApiService,
                                            onLikeClick = { /* Implement like functionality */ },
                                            onCommentClick = { /* Navigate to comments */ },
                                            onShareClick = { /* Implement share functionality */ }
                                        )
                                    }
                                }
                            }
                            // ✅ No more posts
                            if (
                                posts.loadState.append is LoadState.NotLoading &&
                                posts.loadState.refresh !is LoadState.Loading &&
                                posts.itemCount > 0 &&
                                posts.loadState.append.endOfPaginationReached
                            ) {
                                item { AppendEmptyState(message = "No more posts") }
                            }
                        }
                        // Append loading state
                        if (posts.loadState.append is LoadState.Loading) {
                            item { AppendLoadingState() }
                        } else if (posts.loadState.append is LoadState.Error) {
                            item { AppendErrorState(message = "Error loading more posts") }
                        }
                    } else if (selectedTabIndex == 1) {
                        // Meals tab content
                        if (meals.loadState.refresh is LoadState.Loading) {
                            item {
                                LoadingState()
                            }
                        } else if (meals.loadState.refresh is LoadState.Error && meals.itemCount == 0) {
                            item {
                                ErrorState(
                                    message = "An error occurred while loading meals",
                                    onRetry = { meals.refresh() })
                            }
                        } else if (meals.itemCount == 0) {
                            item {
                                EmptyState(message = "No meals yet")
                            }
                        } else {
                            items(
                                count = meals.itemCount,
                                key = meals.itemKey { it.mealId }
                            ) { index ->
                                val meal = meals[index]
                                meal?.let {
                                    CardMealProfile(
                                        mealName = meal.name.toString(),
                                        imageUrl = meal.imageUrl.toString(),
                                        preparationTime = meal.preparationTime ,
                                        calories = meal.caloriesPerServing.toInt() ,
                                        dietaryTags = meal.dietaryTags as List<String>,
                                        rating = meal.rating.toFloat() ,
                                        onClick = {
                                            navController.navigate("meal_detail/${meal.mealId}")
                                        },
                                        onSaveClick = {
//                                            saveMealApi(
//                                                meal.id.toString(),
//                                                context
//                                            )
                                        },
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                            if (
                                meals.loadState.append is LoadState.NotLoading &&
                                meals.loadState.refresh !is LoadState.Loading &&
                                meals.itemCount > 0 &&
                                meals.loadState.append.endOfPaginationReached
                            ) {
                                item { AppendEmptyState(message = "No more meals.") }
                            }
                        }
                        if (meals.loadState.append is LoadState.Loading) {
                            item { AppendLoadingState() }
                        } else if (meals.loadState.append is LoadState.Error) {
                            item { AppendErrorState(message = "Error loading more meals.") }
                        }
                    } else if (selectedTabIndex == 2) {
                        if (communities.loadState.refresh is LoadState.Loading) {
                            // Loading state
                            item {
                                LoadingState()
                            }
                        } else if (communities.loadState.refresh is LoadState.Error && communities.itemCount == 0) {
                            // Error state
                            item {
                                ErrorState(
                                    message = "Failed to load communities.",
                                    onRetry = { communities.refresh() })
                            }
                        } else if (communities.itemCount == 0) {
                            // Empty state
                            item { EmptyState(message = "No communities yet") }
                        } else {
                            items(
                                count = communities.itemCount,
                                key = communities.itemKey { it.community.id }
                            ) { index ->
                                val community = communities[index]
                                community?.let {
                                    CardCommunityProfile(
                                        communityName = community.community.name.toString(),
                                        imageUrl = community.community.image.toString(),
                                        members = community.community._count.members, // Replace with actual count when available
                                        recipes = 3, // Replace with actual count when available
                                        onJoinClick = {
                                            joinCommunityApi(
                                                community.community.id.toString(),
                                                context
                                            )
                                        },
                                        onClick = {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                userPreferencesManager.saveCommunityId(
                                                    community.community.id.toString()
                                                )
                                            }
                                            navController.navigate("Community Page")
                                        },
                                        isMember = true,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                            // Data expiration status
                            if (
                                communities.loadState.append is LoadState.NotLoading &&
                                communities.loadState.refresh !is LoadState.Loading &&
                                communities.itemCount > 0 &&
                                communities.loadState.append.endOfPaginationReached
                            ) {
                                item { AppendEmptyState(message = "No more communities.") }
                            }
                        }

                        // Loading status
                        if (communities.loadState.append is LoadState.Loading) {
                            item { AppendLoadingState() }
                        }
                        // Error status
                        else if (communities.loadState.append is LoadState.Error) {
                            item { AppendErrorState(message = "Error loading more communities.") }
                        }
                    }
                }

                // Top App Bar that appears when scrolling - removed white space above as requested
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .alpha(titleAlpha)
                        .zIndex(1f), // Ensure top bar is above other elements
                    color = MaterialTheme.colorScheme.surface.copy(
                        alpha = min(
                            1f,
                            titleAlpha * 2f
                        )
                    ),
                    shadowElevation = if (titleAlpha > 0.01f) 4.dp else 0.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }

                        // Animated profile image that appears in the toolbar when scrolled
                        if (titleAlpha > 0.5f) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = userProfile?.profilePicture ?: R.drawable.profile_default,
                                    error = painterResource(R.drawable.loading_placeholder),
                                    placeholder = painterResource(R.drawable.loading_placeholder)
                                ),
                                contentDescription = "Profile image small",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(32.dp)
                                    .alpha(min(1f, (titleAlpha - 0.5f) * 2f))
                                    .clip(CircleShape)
                                    .border(1.dp, Color.White, CircleShape)
                            )
                        }

                        Text(
//                            text = (userProfile?.name +" "+ userProfile?.lastName) ,
                            text = ("${userProfile?.name ?: ""} \n${userProfile?.lastName ?: ""}") ,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = if (titleAlpha <= 0.5f) 16.dp else 0.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        MoreOptionsBottomSheet(navController = navController, logoutApi = ::logoutApi)

                    }
                }

                // Back button that's visible at the top when not scrolled
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(8.dp)
                        .size(40.dp)
                        .align(Alignment.TopStart)
                        .alpha(1f - titleAlpha)
                ) {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // FloatingActionButton for creating new content
        FloatingActionButton(
            onClick = {
                isSheetOpen = true
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(1f), // Ensure FAB is above other elements
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(24.dp)
            )
        }

        // Modal Bottom Sheet for creating new content
        if (isSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { isSheetOpen = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    BottomSheetOption(
                        icon = Icons.AutoMirrored.Outlined.Comment,
                        title = "Create Post",
                        onClick = {
                            isSheetOpen = false
                            navController.navigate("PostCreationPage")
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    BottomSheetOption(
                        icon = Icons.Outlined.Restaurant,
                        title = "Create Recipe",
                        onClick = {
                            isSheetOpen = false
                            navController.navigate("RecipeCreationPage")
                        }
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsBottomSheet(
    navController: NavController,
    logoutApi: (Context, NavController) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    // Button to open the Bottom Sheet
    IconButton(
        onClick = { showBottomSheet = true },
        modifier = Modifier
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More options",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

    // The Bottom Sheet itself
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {

                BottomSheetOption(
                    icon = Icons.Outlined.Settings,
                    title = "Update Profile",
                    onClick = {
                        showBottomSheet = false
                        navController.navigate("Update Profile")
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                BottomSheetOption(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title =  "Logout",
                    onClick = {
                        showBottomSheet = false
                        logoutApi(context, navController)
                    }
                )
            }
        }
    }
}
// Helper function to save meal to favorites - implement according to your API
private fun saveMealToFavorites(mealId: String, context: Context) {
    // Implement your API call to save a meal to favorites
    Toast.makeText(context, "Meal saved to favorites", Toast.LENGTH_SHORT).show()
}

@Composable
fun CardMealProfile(
    mealName: String,
    imageUrl: String,
    preparationTime: Int,
    calories: Int,
    dietaryTags: List<String>,
    rating: Float,
    onClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSaved: Boolean = false
) {
    var saved by remember { mutableStateOf(isSaved) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Meal image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current).data(
                        data = imageUrl
                    ).apply(block = fun ImageRequest.Builder.() {
                        crossfade(true)
                        placeholder(android.R.drawable.ic_menu_gallery)
                        error(android.R.drawable.ic_menu_report_image)
                    }).build()),
                    contentDescription = "Meal image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Rating badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", rating),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Save button
                IconButton(
                    onClick = {
                        saved = !saved
                        onSaveClick()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (saved) "Unsave meal" else "Save meal",
                        tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Meal details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Meal name
                Text(
                    text = mealName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Meal metadata (time & calories)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Preparation time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Preparation time",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$preparationTime min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Calories
                    Text(
                        text = "$calories kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dietary tags
                if (dietaryTags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        dietaryTags.take(3).forEach { tag ->
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = tag,
                                        fontSize = 10.sp
                                    )
                                },
                                modifier = Modifier.padding(end = 4.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                        if (dietaryTags.size > 3) {
                            Text(
                                text = "+${dietaryTags.size - 3}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}