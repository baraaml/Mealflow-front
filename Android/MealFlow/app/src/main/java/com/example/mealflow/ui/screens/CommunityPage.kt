package com.example.mealflow.ui.screens

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mealflow.navigation.Destination
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.rememberAsyncImagePainter
import com.example.mealflow.R
import com.example.mealflow.data.model.Post
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.CommentApiService
import com.example.mealflow.network.deleteCommunity
import com.example.mealflow.ui.components.AppendEmptyState
import com.example.mealflow.ui.components.AppendErrorState
import com.example.mealflow.ui.components.AppendLoadingState
import com.example.mealflow.ui.components.BottomSheetOption
import com.example.mealflow.ui.components.EmptyState
import com.example.mealflow.ui.components.ErrorState
import com.example.mealflow.ui.components.LoadingState
import com.example.mealflow.ui.components.ModernDeleteCommunityDialog
import com.example.mealflow.ui.components.ModernDescriptionSection
import com.example.mealflow.ui.components.ModernHeaderSectionFixed
import com.example.mealflow.ui.components.ModernMembersAndRecipesCount
import com.example.mealflow.ui.components.ModernRecipeCard
import com.example.mealflow.ui.components.ModernTitleSection
import com.example.mealflow.ui.components.PostUserInCommunity
    import com.example.mealflow.ui.screens.search.MealSearchAction
import com.example.mealflow.ui.components.RecipeItem
import com.example.mealflow.viewModel.MealSearchViewModel
import com.example.mealflow.viewModel.PostsCommunityViewModel
import com.example.mealflow.viewModel.SingleCommunityViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

// Definitions and animation constants
private val COLLAPSE_THRESHOLD = 120.dp
private val HEADER_HEIGHT = 250.dp
private val PROFILE_IMAGE_SIZE = 120.dp
private val PROFILE_IMAGE_SIZE_COLLAPSED = 40.dp
private val HEADER_IMAGE_HEIGHT = 180.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPage(
    communityViewModel: SingleCommunityViewModel,
    commentApiService: CommentApiService,
    userPreferencesManager: UserPreferencesManager,
    navController: NavController
) {
    val context = LocalContext.current
//    val userPreferencesManager = remember { UserPreferencesManager(context) }
//    val commentApiService = remember { CommentApiService(context) }
    val communityId by userPreferencesManager.getCommunityId().collectAsState(initial = "Loading...")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var isSheetOpen by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val community = communityViewModel.community
    val isLoading = communityViewModel.isLoading
    val errorMessage = communityViewModel.errorMessage
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    var userId by remember { mutableStateOf("N/A") }

    LaunchedEffect(Unit) {
        userId = userPreferencesManager.getMyId()
    }

    // Initialize the view model
    val postsViewModel: PostsCommunityViewModel = viewModel()
    val mealSearchViewModel: MealSearchViewModel = viewModel()
    val posts = postsViewModel.postsFlow.collectAsLazyPagingItems()

    // Shared variable for dropdown list
    var expanded by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val lazyListState = rememberLazyListState()

    // Calculate scroll values for animation
    val scrollInPx = with(density) { COLLAPSE_THRESHOLD.toPx() }
    val headerHeightPx = with(density) { HEADER_HEIGHT.toPx() }

    // Collect meals as LazyPagingItems
    val meals = mealSearchViewModel.pagedMeals.collectAsLazyPagingItems()

    // Handle search
    LaunchedEffect(true) {
        val communityId = userPreferencesManager.getCommunityId().first()
        Log.d("community Id","community Id : $communityId")
        // Initial search with default parameters
        mealSearchViewModel.onAction(MealSearchAction.OnSearchParamsChange(MealSearchViewModel.SearchParams(communityId = communityId)))
    }

    // Calculate the pass ratio from 0 to 1
    val scrollProgress by remember {
        derivedStateOf {
            val scrollPosition = min(
                1f,
                max(0f, lazyListState.firstVisibleItemScrollOffset / scrollInPx +
                        lazyListState.firstVisibleItemIndex)
            )
            scrollPosition
        }
    }

    // Apply different animation values based on scroll rate
    val headerHeightFactor by animateFloatAsState(
        targetValue = 1f - scrollProgress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "headerHeightFactor"
    )

    val headerAlpha by animateFloatAsState(
        targetValue = 1f - scrollProgress * 1.5f,
        animationSpec = tween(durationMillis = 300),
        label = "headerAlpha"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (scrollProgress > 0.3f) min(1f, (scrollProgress - 0.3f) * 3f) else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "titleAlpha"
    )

    // Profile picture animation values
    val profileImageSize by animateFloatAsState(
        targetValue = if (scrollProgress > 0.3f)
            max(PROFILE_IMAGE_SIZE_COLLAPSED.value, PROFILE_IMAGE_SIZE.value * (1f - scrollProgress * 1.2f))
        else PROFILE_IMAGE_SIZE.value,
        animationSpec = tween(durationMillis = 300),
        label = "profileImageSize"
    )

    // Implement a function to update data when dragging down
    suspend fun refreshData() {
        isRefreshing = true
        communityViewModel.loadSingleCommunity()
        posts.refresh() // Refresh the paging data
        meals.refresh()
        postsViewModel.refresh()
        delay(1000) // Small delay to show refresh animation
        isRefreshing = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Change the position of the drop-down menu to the top of the screen so that it appears directly below the three dots button.
        if (community?.isAdmin == true) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            ) {
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
                // Background image with parallax effect
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HEADER_IMAGE_HEIGHT)
                        .graphicsLayer {
                            // Parallax effect for background
                            translationY = -scrollProgress * 50f
                            alpha = max(0f, 1f - scrollProgress * 1.5f)
                        }
                ) {
                    // Header background image
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = community?.image
                                ?: "https://images.unsplash.com/photo-1504674900247-0877df9cc836",
                            error = painterResource(R.drawable.loading_placeholder),
                            placeholder = painterResource(R.drawable.loading_placeholder)
                        ),
                        contentDescription = "Header background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(radius = (8 * scrollProgress).dp)
                    )

                    // Partially transparent layer that becomes darker as you scroll
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

                // Include title in app bar on scroll
                if (scrollProgress > 0.5f) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = titleAlpha))
                            .alpha(titleAlpha)
                            .zIndex(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }

                        Text(
                            text = community?.name ?: "Community",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Display the menu button only if the user is an administrator
                        if (community?.isAdmin == true) {
                            IconButton(
                                onClick = { showBottomSheet = true}
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More"
                                )
                            }
                        }
                    }
                }

                // Main content
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .zIndex(0f)
                ) {
                    // LaunchedEffect at the beginning of the LazyColumn
                    item {
                        LaunchedEffect(key1 = true) {
                            communityViewModel.loadSingleCommunity()
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            // Display main content - We always display all three sections regardless of the loading status.
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 80.dp)
                            ) {
                                // Add the necessary space to the header
                                Spacer(modifier = Modifier.height(200.dp))

                                // The three required sections - always visible even during loading
                                ModernTitleSection(community,navController)

                                ModernDescriptionSection(community)

                                ModernMembersAndRecipesCount(community, navController)

                                // Tab Row - Only appears when the download is complete and there are no errors
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
                                    var isAuthor = false

                                    if (it.authorId == userId) {
                                        isAuthor = true
                                    }
                                    Log.d(
                                        "it.authorId",
                                        "📩 Send Request: responseBody=${it.authorId}"
                                    )
                                    Log.d(
                                        "userId",
                                        "📩 Send Request: responseBody=${userId}"
                                    )

                                    PostUserInCommunity(
                                        post = it,
                                        refresh = {
                                            coroutineScope.launch {
                                                refreshData()
                                            }
                                        },
                                        navController = navController,
                                        isAuthor = isAuthor,
                                        commentApiService = commentApiService,
                                        onLikeClick = { /* Implement like functionality */ },
                                        onCommentClick = { /* Navigate to comments */ },
                                        onShareClick = { /* Implement share functionality */ }
                                    )
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
                                            navController.navigate(Destination.MealDetail(meal.mealId))
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
                    }
                }
                // Overlay the header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp * headerHeightFactor)
                        .graphicsLayer {
                            alpha = headerAlpha
                        }
                        .zIndex(if (scrollProgress < 0.5f) 1f else 0f)
                ) {
                    ModernHeaderSectionFixed(community, navController, scrollProgress) {
                        expanded = it
                    }
                }
            }
        }

        // Overlay the header and add the three dots button to it
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp * headerHeightFactor)
                .graphicsLayer {
                    alpha = headerAlpha
                }
                .zIndex(if (scrollProgress < 0.5f) 1f else 0f)
        ) {
            // Add the three dots button to the header
            if (community?.isAdmin == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            ModernHeaderSectionFixed(community, navController, scrollProgress) {
                showBottomSheet = it
            }
        }

        if(community?.isMember == true)
        {
            // زر الإضافة
            FloatingActionButton(
                onClick = { isSheetOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // الـ Bottom Sheet
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
                        title = "Set Admin",
                        onClick = {
                            showBottomSheet = false
                            navController.navigate(Destination.SetAdmin)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    BottomSheetOption(
                        icon = Icons.Outlined.Delete,
                        title = "Remove Member",
                        onClick = {
                            showBottomSheet = false
                            navController.navigate(Destination.RemoveMembers)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    BottomSheetOption(
                        icon = Icons.Outlined.Update,
                        title = "Update Community",
                        onClick = {
                            showBottomSheet = false
                            navController.navigate(Destination.UpdateCommunity)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    BottomSheetOption(
                        icon = Icons.Outlined.Delete,
                        title = "Delete Community",
                        onClick = {
                            showBottomSheet = false
                            showDialog = true
                        }
                    )
                }
            }
        }

        // الـ Bottom Sheet
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
                            navController.navigate(Destination.PostCreation(communityId = communityId))
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
                            navController.navigate(Destination.RecipeCreation(communityId = communityId))
                        }
                    )
                }
            }
        }

        // Delete confirmation dialog
        if (showDialog) {
            ModernDeleteCommunityDialog(
                onDismiss = { showDialog = false },
                onConfirm = {
                    val result = deleteCommunity(context)
                    if (result?.success == true) {
                        navController.navigate(Destination.CommunityHome)
                    }
                }
            )
        }
    }
}


@Composable
fun ActivityContent(
    posts: LazyPagingItems<Post>,
    userId: String,
    navController: NavController,
    coroutineScope: CoroutineScope,
    refreshData: suspend () -> Unit
) {
    val context = LocalContext.current
    val commentApiService = remember { CommentApiService(context) }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Loading state for posts
        if (posts.loadState.refresh is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (posts.loadState.refresh is LoadState.Error && posts.itemCount == 0) {
            // Error state
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error loading posts",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Failed to load conversation posts",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                posts.refresh()
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFFFFA500))
                        ) {
                            Text("Retry", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        } else if (posts.itemCount == 0) {
            // Empty state
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No posts yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Posts items
            items(
                count = posts.itemCount,
                key = posts.itemKey { it.id }
            ) { index ->
                val post = posts[index]
                post?.let {
                    var isAuthor = false

                    if(it.authorId == userId) {
                        isAuthor = true
                    }

                    PostUserInCommunity(
                        post = it,
                        refresh = {
                            coroutineScope.launch {
                                refreshData()
                            }
                        },
                        navController = navController,
                        isAuthor = isAuthor,
                        commentApiService = commentApiService,
                        onLikeClick = { /* Implement like functionality */ },
                        onCommentClick = { /* Navigate to comments */ },
                        onShareClick = { /* Implement share functionality */ }
                    )
                }
            }
            // ✅ No more posts
            if (
                posts.loadState.append is LoadState.NotLoading &&
                posts.loadState.refresh !is LoadState.Loading &&
                posts.itemCount > 0 &&
                posts.loadState.append.endOfPaginationReached
            ) {
                item {
                    Text(
                        text = "No more posts",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        // Append loading state
        if (posts.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else if (posts.loadState.append is LoadState.Error) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error loading more posts",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { posts.retry() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipesContent() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Column {
                repeat(8) {
                    ModernRecipeCard(
                        RecipeItem(
                            "Creamy Garlic Shrimp Pasta",
                            "https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg"
                        )
                    )
                }
            }
        }
    }
}