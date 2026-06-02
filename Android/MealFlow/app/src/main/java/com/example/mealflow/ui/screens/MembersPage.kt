package com.example.mealflow.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mealflow.R
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.CommunityMember
import com.example.mealflow.network.UserMember
import com.example.mealflow.utils.collectAsState
import com.example.mealflow.viewModel.CommunityMembersViewModel
import com.example.mealflow.viewModel.SingleCommunityViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


enum class MembersPageState {
    LOADING,
    SUCCESS,
    ERROR,
    EMPTY
}

@Composable
fun MembersPage(
    viewModel: CommunityMembersViewModel,
    navController: NavController
) {
//    val viewModel: CommunityMembersViewModel = viewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val membersPagingItems = viewModel.communityMembersFlow.collectAsLazyPagingItems()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val owner by viewModel.owner.collectAsState() // إضافة owner

    var searchQuery by remember { mutableStateOf("") }
    var filteredMembers by remember { mutableStateOf<List<CommunityMember>>(emptyList()) }
    var pageState by remember { mutableStateOf(MembersPageState.LOADING) }

    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = isLoading
    )

    // Determine page state based on loading state and data
    LaunchedEffect(membersPagingItems.loadState, membersPagingItems.itemCount) {
        val loadState = membersPagingItems.loadState

        pageState = when {
            loadState.refresh is LoadState.Loading -> MembersPageState.LOADING
            loadState.refresh is LoadState.Error -> MembersPageState.ERROR
            membersPagingItems.itemCount == 0 && owner == null -> MembersPageState.EMPTY
            else -> MembersPageState.SUCCESS
        }
    }

    // Filter members based on search query (including owner)
    LaunchedEffect(searchQuery, membersPagingItems.itemCount, owner) {
        val currentItems = List(membersPagingItems.itemCount) { membersPagingItems[it] }
            .filterNotNull()
            .toMutableList()

        // إضافة الـ owner للقائمة إذا كان موجود
        owner?.let { ownerData ->
            val ownerAsMember = CommunityMember(
                role = "owner",
                joinedAt = "",
                leftAt = null,
                user = ownerData
            )
            currentItems.add(0, ownerAsMember)
        }

        filteredMembers = if (searchQuery.isEmpty()) {
            currentItems
        } else {
            currentItems.filter {
                it.user.username.contains(searchQuery, ignoreCase = true) ||
                        it.user.name?.contains(searchQuery, ignoreCase = true) == true ||
                        it.user.lastName?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppBar(
            title = "Community Members",
            onBackClick = { navController.popBackStack() }
        )

        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onClearQuery = { searchQuery = "" }
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
                    MembersPageState.LOADING -> {
                        LoadingState(modifier = Modifier.align(Alignment.Center))
                    }
                    MembersPageState.SUCCESS -> {
                        if (filteredMembers.isEmpty() && searchQuery.isNotEmpty()) {
                            EmptySearchState(
                                searchQuery = searchQuery,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            MembersList(
                                membersPagingItems = membersPagingItems,
                                filteredMembers = filteredMembers,
                                isSearchActive = searchQuery.isNotEmpty(),
                                owner = owner,
                                onUserClick = { member ->
                                    coroutineScope.launch {
                                        val userPrefs = UserPreferencesManager(context)
                                        userPrefs.saveUserId(member.user.id)
                                        Log.d("user id", "user id: ${member.user.id}")
                                        navController.navigate("User Page")
                                    }
                                }
                            )
                        }
                    }
                    MembersPageState.ERROR -> {
                        ErrorState(
                            message = errorState ?: "Failed to load members",
                            onRetry = { viewModel.refresh() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    MembersPageState.EMPTY -> {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun MembersList(
    membersPagingItems: LazyPagingItems<CommunityMember>,
    filteredMembers: List<CommunityMember>,
    isSearchActive: Boolean,
    owner: UserMember?, // إضافة owner parameter
    onUserClick: (CommunityMember) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (isSearchActive) {
            // Show filtered search results (including owner if matches search)
            items(
                count = filteredMembers.size,
                key = { index -> filteredMembers[index].user.id }
            ) { index ->
                val member = filteredMembers[index]
                MemberItem(
                    member = member,
                    onClick = { onUserClick(member) }
                )
            }
        } else {
            // عرض الـ owner أولاً
            owner?.let { ownerData ->
                item(key = "owner_${ownerData.id}") {
                    val ownerAsMember = CommunityMember(
                        role = "owner",
                        joinedAt = "",
                        leftAt = null,
                        user = ownerData
                    )
                    MemberItem(
                        member = ownerAsMember,
                        onClick = { onUserClick(ownerAsMember) }
                    )
                }
            }

            // عرض باقي الأعضاء
            items(
                count = membersPagingItems.itemCount,
                key = membersPagingItems.itemKey { it.user.id }
            ) { index ->
                val member = membersPagingItems[index]
                if (member != null) {
                    MemberItem(
                        member = member,
                        onClick = { onUserClick(member) }
                    )
                }
            }

            // Handle loading states at the bottom
            membersPagingItems.apply {
                when {
                    loadState.append is LoadState.Loading -> {
                        item {
                            LoadingMoreIndicator()
                        }
                    }
                    loadState.append is LoadState.Error -> {
                        item {
                            LoadMoreErrorItem(
                                message = "Failed to load more members",
                                onRetry = { retry() }
                            )
                        }
                    }
                }
            }
        }
    }
}
//@Composable
//fun MembersPage(
//    navController: NavController
//) {
//    val viewModel: CommunityMembersViewModel = viewModel()
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//    val membersPagingItems = viewModel.communityMembersFlow.collectAsLazyPagingItems()
//    val isLoading by viewModel.isLoading.collectAsState()
//    val errorState by viewModel.errorState.collectAsState()
//
//    var searchQuery by remember { mutableStateOf("") }
//    var filteredMembers by remember { mutableStateOf<List<CommunityMember>>(emptyList()) }
//    var pageState by remember { mutableStateOf(MembersPageState.LOADING) }
//
//    val swipeRefreshState = rememberSwipeRefreshState(
//        isRefreshing = isLoading
//    )
//
//    // Determine page state based on loading state and data
//    LaunchedEffect(membersPagingItems.loadState, membersPagingItems.itemCount) {
//        val loadState = membersPagingItems.loadState
//
//        pageState = when {
//            loadState.refresh is LoadState.Loading -> MembersPageState.LOADING
//            loadState.refresh is LoadState.Error -> MembersPageState.ERROR
//            membersPagingItems.itemCount == 0 -> MembersPageState.EMPTY
//            else -> MembersPageState.SUCCESS
//        }
//    }
//
//    // Filter members based on search query
//    LaunchedEffect(searchQuery, membersPagingItems.itemCount) {
//        val currentItems = List(membersPagingItems.itemCount) { membersPagingItems[it] }
//            .filterNotNull()
//
//        filteredMembers = if (searchQuery.isEmpty()) {
//            currentItems
//        } else {
//            currentItems.filter {
//                it.user.username.contains(searchQuery, ignoreCase = true) ||
//                        it.user.name?.contains(searchQuery, ignoreCase = true) == true ||
//                        it.user.lastName?.contains(searchQuery, ignoreCase = true) == true
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//    ) {
//        AppBar(
//            title = "Community Members",
//            onBackClick = { navController.popBackStack() }
//        )
//
//        SearchBar(
//            query = searchQuery,
//            onQueryChange = { searchQuery = it },
//            onClearQuery = { searchQuery = "" }
//        )
//
//        SwipeRefresh(
//            state = swipeRefreshState,
//            onRefresh = { viewModel.refresh() },
//            indicator = { state, trigger ->
//                SwipeRefreshIndicator(
//                    state = state,
//                    refreshTriggerDistance = trigger,
//                    backgroundColor = MaterialTheme.colorScheme.surface,
//                    contentColor = MaterialTheme.colorScheme.primary
//                )
//            },
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 16.dp)
//        ) {
//            Box(
//                modifier = Modifier.fillMaxSize()
//            ) {
//                when (pageState) {
//                    MembersPageState.LOADING -> {
//                        LoadingState(modifier = Modifier.align(Alignment.Center))
//                    }
//                    MembersPageState.SUCCESS -> {
//                        if (filteredMembers.isEmpty() && searchQuery.isNotEmpty()) {
//                            EmptySearchState(
//                                searchQuery = searchQuery,
//                                modifier = Modifier.align(Alignment.Center)
//                            )
//                        } else {
//                            MembersList(
//                                membersPagingItems = membersPagingItems,
//                                filteredMembers = filteredMembers,
//                                isSearchActive = searchQuery.isNotEmpty(),
//                                onUserClick = { member ->
//                                    coroutineScope.launch {
//                                        val userPrefs = UserPreferencesManager(context)
//                                        userPrefs.saveUserId(member.user.id)
//                                        Log.d("user id", "user id: ${member.user.id}")
//                                        navController.navigate("User Page")
//                                    }
//                                }
//                            )
//                        }
//                    }
//                    MembersPageState.ERROR -> {
//                        ErrorState(
//                            message = errorState ?: "Failed to load members",
//                            onRetry = { viewModel.refresh() },
//                            modifier = Modifier.align(Alignment.Center)
//                        )
//                    }
//                    MembersPageState.EMPTY -> {
//                        EmptyState(modifier = Modifier.align(Alignment.Center))
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun MembersList(
    membersPagingItems: LazyPagingItems<CommunityMember>,
    filteredMembers: List<CommunityMember>,
    isSearchActive: Boolean,
    onUserClick: (CommunityMember) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (isSearchActive) {
            // Show filtered search results
            items(
                count = filteredMembers.size,
                key = { index -> filteredMembers[index].user.id }
            ) { index ->
                val member = filteredMembers[index]
                MemberItem(
                    member = member,
                    onClick = { onUserClick(member) }
                )
            }
        } else {
            // Show paginated results
            items(
                count = membersPagingItems.itemCount,
                key = membersPagingItems.itemKey { it.user.id }
            ) { index ->
                val member = membersPagingItems[index]
                if (member != null) {
                    MemberItem(
                        member = member,
                        onClick = { onUserClick(member) }
                    )
                }
            }

            // Handle loading states at the bottom
            membersPagingItems.apply {
                when {
                    loadState.append is LoadState.Loading -> {
                        item {
                            LoadingMoreIndicator()
                        }
                    }
                    loadState.append is LoadState.Error -> {
                        item {
                            LoadMoreErrorItem(
                                message = "Failed to load more members",
                                onRetry = { retry() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberItem(
    member: CommunityMember,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                model = member.user.profilePicture,
                contentDescription = "Profile picture of ${member.user.username}",
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
                val fullName = "${member.user.name ?: ""} ${member.user.lastName ?: ""}".trim()
                if (fullName.isNotEmpty()) {
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "@${member.user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = member.role.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    },
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
private fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
            Text(
                text = "Loading more members...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadMoreErrorItem(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retry")
            }
        }
    }
}

//@Composable
//fun MembersPage(
//    context: Context = LocalContext.current,
//    navController: NavController
//) {
//    val coroutineScope = rememberCoroutineScope()
//    var membersState by remember { mutableStateOf<List<CommunityMember>>(emptyList()) }
//    var filteredMembers by remember { mutableStateOf<List<CommunityMember>>(emptyList()) }
//    var searchQuery by remember { mutableStateOf("") }
//    var pageState by remember { mutableStateOf(MembersPageState.LOADING) }
//    var errorMessage by remember { mutableStateOf("") }
//    val userPrefs = UserPreferencesManager(context)
//
//    LaunchedEffect(Unit) {
//        coroutineScope.launch {
//            try {
//                val communityMembers = CommunityMembersApiService(context).fetchCommunityMembers()
//                if (communityMembers.success) {
//                    membersState = communityMembers.members
//                    filteredMembers = communityMembers.members
//                    pageState = if (communityMembers.members.isEmpty()) {
//                        MembersPageState.EMPTY
//                    } else {
//                        MembersPageState.SUCCESS
//                    }
//                } else {
//                    pageState = MembersPageState.ERROR
//                    errorMessage = "Failed to load members"
//                }
//            } catch (e: Exception) {
//                pageState = MembersPageState.ERROR
//                errorMessage = e.message ?: "Unknown error occurred"
//            }
//        }
//    }
//
//    LaunchedEffect(searchQuery) {
//        filteredMembers = if (searchQuery.isEmpty()) {
//            membersState
//        } else {
//            membersState.filter { it.user.username.contains(searchQuery, ignoreCase = true) }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//    ) {
//        AppBar(
//            title = "Community Members",
//            onBackClick = { navController.popBackStack() }
//        )
//
//        SearchBar(
//            query = searchQuery,
//            onQueryChange = { searchQuery = it },
//            onClearQuery = { searchQuery = "" }
//        )
//
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 16.dp)
//        ) {
//            when (pageState) {
//                MembersPageState.LOADING -> {
//                    LoadingState(modifier = Modifier.align(Alignment.Center))
//                }
//
//                MembersPageState.SUCCESS -> {
//                    if (filteredMembers.isEmpty() && searchQuery.isNotEmpty()) {
//                        EmptySearchState(
//                            searchQuery = searchQuery,
//                            modifier = Modifier.align(Alignment.Center)
//                        )
//                    } else {
//                        MembersList(
//                            members = filteredMembers,
//                            onMemberClick = { member ->
//                                coroutineScope.launch {
//                                    userPrefs.saveUserId(member.user.id)
//                                    Log.d("user id", "user id: ${member.user.id}")
//                                    navController.navigate("User Page")
//                                }
//                            }
//                        )
//                    }
//                }
//
//                MembersPageState.ERROR -> {
//                    ErrorState(
//                        message = errorMessage,
//                        onRetry = {
//                            pageState = MembersPageState.LOADING
//                            coroutineScope.launch {
//                                try {
//                                    val communityMembers =
//                                        CommunityMembersApiService(context).fetchCommunityMembers()
//                                    if (communityMembers.success) {
//                                        membersState = communityMembers.members
//                                        filteredMembers = communityMembers.members
//                                        pageState = if (communityMembers.members.isEmpty()) {
//                                            MembersPageState.EMPTY
//                                        } else {
//                                            MembersPageState.SUCCESS
//                                        }
//                                    } else {
//                                        pageState = MembersPageState.ERROR
//                                        errorMessage = "Failed to load members"
//                                    }
//                                } catch (e: Exception) {
//                                    pageState = MembersPageState.ERROR
//                                    errorMessage = e.message ?: "Unknown error occurred"
//                                }
//                            }
//                        },
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                }
//
//                MembersPageState.EMPTY -> {
//                    EmptyState(modifier = Modifier.align(Alignment.Center))
//                }
//            }
//        }
//    }
//}
@Composable
fun AppBar(
    title: String,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBackClick,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Invisible spacer to balance the back button
            Box(modifier = Modifier.size(40.dp))
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AppBar(
//    title: String,
//    onBackClick: () -> Unit
//) {
//    CenterAlignedTopAppBar(
//        title = {
//            Text(
//                text = title,
//                style = MaterialTheme.typography.titleLarge
//            )
//        },
//        navigationIcon = {
//            IconButton(onClick = onBackClick) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                    contentDescription = "Back"
//                )
//            }
//        },
//        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//            containerColor = MaterialTheme.colorScheme.primary,
//            titleContentColor = MaterialTheme.colorScheme.onPrimary,
//            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
//        )
//    )
//}

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
            Text("Search members...")
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
fun MembersList(
    members: List<CommunityMember>,
    onMemberClick: (CommunityMember) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(members) { index, member ->
            CommunityMemberItem(
                member = member,
                onMemberClick = { onMemberClick(member) }
            )

            if (index < members.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun CommunityMemberItem(
    member: CommunityMember,
    onMemberClick: () -> Unit
) {
    var isRippleEffect by remember { mutableStateOf(false) }

    LaunchedEffect(isRippleEffect) {
        if (isRippleEffect) {
            delay(300)
            isRippleEffect = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                isRippleEffect = true
                onMemberClick()
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRippleEffect)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(member.user.profilePicture ?: R.drawable.profile_default) // استخدم رابط أو صورة افتراضية
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = member.user.name.toString() + " " +member.user.lastName.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // You can add additional user info here if available
                // For example: role, join date, etc.
                if (member.user.id.isNotEmpty()) {
                    Text(
                        text = "Member Role: ${member.role}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "View Profile",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
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
            text = "Loading members...",
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
            text = "Failed to load members",
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
            contentDescription = "No Members",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Community Members",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "There are currently no members in this community",
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
            text = "No Results Found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No members matching \"$searchQuery\" were found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MembersPagePreview() {
//    MaterialTheme {
//        MembersPage(navController = rememberNavController())
//    }
//}