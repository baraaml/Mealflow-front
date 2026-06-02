package com.example.mealflow.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.rememberAsyncImagePainter
import com.example.mealflow.R
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.CommentApiService
import com.example.mealflow.network.FollowUserResponse
import com.example.mealflow.network.UnfollowUserResponse
import com.example.mealflow.network.UserApiService
import com.example.mealflow.network.UserFollowService
import com.example.mealflow.network.UserProfile
import com.example.mealflow.network.joinCommunityApi
import com.example.mealflow.ui.components.AppendEmptyState
import com.example.mealflow.ui.components.AppendErrorState
import com.example.mealflow.ui.components.AppendLoadingState
import com.example.mealflow.ui.components.CardCommunityProfile
import com.example.mealflow.ui.components.EmptyState
import com.example.mealflow.ui.components.ErrorState
import com.example.mealflow.ui.components.LoadingState
import com.example.mealflow.ui.components.ModernDescriptionSectionProfile
import com.example.mealflow.ui.components.ModernFollowersAndFollowingCount
import com.example.mealflow.ui.components.PostUser
import com.example.mealflow.ui.components.PostUserInCommunity
import com.example.mealflow.viewModel.GetUserCommunitiesViewModel
import com.example.mealflow.viewModel.MealSearchViewModel
import com.example.mealflow.viewModel.UserPostViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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

@Composable
fun userId(): String {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    var userId by remember { mutableStateOf("N/A") }

    LaunchedEffect(Unit) {
        userId = userPreferencesManager.getUserIdString()
    }
    // Collect userId using collectAsState
//    val userId = userPreferencesManager.getUserId().first().toString()
    Log.e("UserPosts", "Token $userId")
    return userId
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPage(
    communitiesViewModel: GetUserCommunitiesViewModel,
    mealSearchViewModel: MealSearchViewModel,
    postsViewModel: UserPostViewModel,
    commentApiService: CommentApiService,
    userPreferencesManager: UserPreferencesManager,
    navController: NavController
)
{
    val context = LocalContext.current

//    // Initialize the view model
//    val communitiesViewModel: GetUserCommunitiesViewModel = viewModel()
//    val mealSearchViewModel: MealSearchViewModel = viewModel()
//    val postsViewModel: UserPostViewModel = viewModel()
//
//    val commentApiService = remember { CommentApiService(context) }
//    val userPreferencesManager = remember { UserPreferencesManager(context) }

    // Get paging items
    val posts = postsViewModel.postsFlow.collectAsLazyPagingItems()
    val communities = communitiesViewModel.communitiesFlow.collectAsLazyPagingItems()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // LazyColumnState to track scroll position for collapsing header effect
    val lazyListState = rememberLazyListState()

    // Calculate scroll state for animations with pixel values
    val scrollInPx = with(density) { COLLAPSE_THRESHOLD.toPx() }
    val headerHeightPx = with(density) { HEADER_HEIGHT.toPx() }

    // Collect meals as LazyPagingItems
    val meals = mealSearchViewModel.pagedMeals.collectAsLazyPagingItems()

    // Handle search
    LaunchedEffect(true) {
        withContext(Dispatchers.IO) {
            val userId = userPreferencesManager.getUserId().first()
            Log.d("t","user id : $userId")
            // Initial search with default parameters
            mealSearchViewModel.search(MealSearchViewModel.SearchParams(userId = userId))
        }
    }

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

    // FIXED: Derive isFollowing from userProfile instead of storing separately
    // This ensures isFollowing is always in sync with the latest userProfile data
    val isFollowing = userProfile?.isFollowing ?: false
    Log.d("isfollowing", "is : ${userProfile?.isFollowing}")

    // State for dropdown menu
    var expanded by remember { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(false) }

    // Track if we've reached the end of available posts
    val hasMorePosts = remember { mutableStateOf(true) }
    val hasMoreCommunities = remember { mutableStateOf(true) }

    suspend fun fetchUserProfile() {
        withContext(Dispatchers.IO) {
            val result = UserApiService(context).fetchUser()

            // Switch back to Main thread for UI updates
            withContext(Dispatchers.Main) {
                if (result.success) {
                    userProfile = result.data
                    isLoading = false
                } else {
                    errorMessage = result.message
                    isLoading = false
                }
            }
        }
    }
//    suspend fun fetchUserProfile() {
//        // Your logic to fetch the user profile
//        val result = UserApiService(context).fetchUser()
//
//        // Handling the result
//        if (result.success) {
//            userProfile = result.data
//            isLoading = false
//        } else {
//            errorMessage = result.message
//            isLoading = false
//        }
//    }

    // Function to refresh all data
    suspend fun refreshData() {
        isRefreshing = true
        withContext(Dispatchers.IO) {
            fetchUserProfile()
        }
        posts.refresh() // Refresh the paging data
        meals.refresh()
        communities.refresh()
        postsViewModel.refresh()
        hasMorePosts.value = true
        hasMoreCommunities.value = true
        delay(1000) // Small delay to show refresh animation
        isRefreshing = false
    }

    // Fetch user profile data and posts when the screen loads
    LaunchedEffect(key1 = true) {
        withContext(Dispatchers.IO) {
            fetchUserProfile()
        }
    }

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
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White
                )
            }
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
                                    // Follow/Following button
                                    // When rendering the FollowButton:
                                    FollowButton(
                                        userId = userProfile?.id ?: "",
                                        isFollowing = isFollowing,
                                        onFollowStateChange = { newState ->
                                            // Update the userProfile object with the new following state
                                            userProfile = userProfile?.copy(isFollowing = newState)
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
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
                                    userProfile?.bio ?: "Loading........"
                                )
                                ModernFollowersAndFollowingCount(
                                    followersCount = userProfile?._count?.followers ?: 0,
                                    followingCount = userProfile?._count?.following ?: 0,
                                    isUser = true,
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
                                        modifier = Modifier.padding(vertical = 12.dp)
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
                                            PostUser(
                                                post = it,
                                                userProfile = profile,
                                                isAuthor = false,
                                                commentApiService = commentApiService,
                                                onCommentClick = { /* Navigate to comments */ },
                                                onShareClick = { /* Implement share functionality */ },
                                                navController = navController
                                            )
                                        }
                                    } else {
                                        PostUserInCommunity(
                                            post = it,
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
                                        preparationTime = meal.preparationTime,
                                        calories = meal.caloriesPerServing.toInt() ,
                                        dietaryTags = meal.dietaryTags as List<String>,
                                        rating = meal.rating.toFloat() ,
                                        onClick = {
//                                            CoroutineScope(Dispatchers.IO).launch {
//                                                userPreferencesManager.saveMealId(
//                                                    meal.mealId.toString()
//                                                )
//                                            }
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
                            text = ("${userProfile?.name ?: ""} \n${userProfile?.lastName ?: ""}") ,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = if (titleAlpha <= 0.5f) 16.dp else 0.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                        modifier = Modifier.size(40.dp)
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
    }
}


@Composable
fun FollowButton(
    userId: String,
    isFollowing: Boolean,
    onFollowStateChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userFollowService = UserFollowService(context)

    Button(
        onClick = {
            // remember the old state for rollback
            val oldState = isFollowing
            // immediately toggle UI
            onFollowStateChange(!oldState)

            coroutineScope.launch {
                val result = try {
                    if (oldState) {
                        userFollowService.unfollowUser(userId = userId)
                    } else {
                        userFollowService.followUser(userId = userId)
                    }
                } catch (e: Exception) {
                    null
                }

                val success = when (result) {
                    is FollowUserResponse -> result.success
                    is UnfollowUserResponse -> result.success
                    else -> false
                }

                if (!success) {
                    onFollowStateChange(oldState)
                    val action = if (oldState) "Unfollow" else "Follow"
                    Toast.makeText(context, "Failed to implement $action", Toast.LENGTH_SHORT).show()
                }
                else {
                    // you may show a success toast if you like:
                    // Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        },
        colors = ButtonDefaults.buttonColors(Color(0xFFFFA500)),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(
            horizontal = 32.dp,
            vertical = 8.dp
        ),
        modifier = Modifier.width(140.dp)
    ) {
        Text(
            // Make sure the text is always consistent with the current isFollowing state
            text = if (isFollowing) "Following" else "Follow",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}