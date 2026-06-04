package com.example.mealflow.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mealflow.navigation.Destination
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val membersPagingItems = viewModel.communityMembersFlow.collectAsLazyPagingItems()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val owner by viewModel.owner.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filteredMembers by remember { mutableStateOf<List<CommunityMember>>(emptyList()) }
    var pageState by remember { mutableStateOf(MembersPageState.LOADING) }

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    LaunchedEffect(membersPagingItems.loadState, membersPagingItems.itemCount) {
        val loadState = membersPagingItems.loadState
        pageState = when {
            loadState.refresh is LoadState.Loading -> MembersPageState.LOADING
            loadState.refresh is LoadState.Error -> MembersPageState.ERROR
            membersPagingItems.itemCount == 0 && owner == null -> MembersPageState.EMPTY
            else -> MembersPageState.SUCCESS
        }
    }

    LaunchedEffect(searchQuery, membersPagingItems.itemCount, owner) {
        val currentItems = List(membersPagingItems.itemCount) { membersPagingItems[it] }
            .filterNotNull()
            .toMutableList()

        owner?.let { ownerData ->
            val ownerAsMember = CommunityMember(role = "owner", joinedAt = "", leftAt = null, user = ownerData)
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
        MembersAppBar(
            title = "Community Members",
            onBackClick = { navController.popBackStack() }
        )

        MembersSearchBar(
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
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (pageState) {
                    MembersPageState.LOADING -> MembersLoadingState(modifier = Modifier.align(Alignment.Center))
                    MembersPageState.SUCCESS -> {
                        if (filteredMembers.isEmpty() && searchQuery.isNotEmpty()) {
                            MembersEmptySearchState(searchQuery = searchQuery, modifier = Modifier.align(Alignment.Center))
                        } else {
                            MembersListContent(
                                membersPagingItems = membersPagingItems,
                                filteredMembers = filteredMembers,
                                isSearchActive = searchQuery.isNotEmpty(),
                                owner = owner,
                                onUserClick = { member ->
                                    coroutineScope.launch {
                                        val userPrefs = UserPreferencesManager(context)
                                        userPrefs.saveUserId(member.user.id)
                                        navController.navigate(Destination.User)
                                    }
                                }
                            )
                        }
                    }
                    MembersPageState.ERROR -> MembersErrorState(message = errorState ?: "Failed to load members", onRetry = { viewModel.refresh() }, modifier = Modifier.align(Alignment.Center))
                    MembersPageState.EMPTY -> MembersEmptyState(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun MembersListContent(
    membersPagingItems: LazyPagingItems<CommunityMember>,
    filteredMembers: List<CommunityMember>,
    isSearchActive: Boolean,
    owner: UserMember?,
    onUserClick: (CommunityMember) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (isSearchActive) {
            items(count = filteredMembers.size, key = { index -> filteredMembers[index].user.id }) { index ->
                val member = filteredMembers[index]
                CommunityMemberItem(member = member, onMemberClick = { onUserClick(member) })
            }
        } else {
            owner?.let { ownerData ->
                item(key = "owner_${ownerData.id}") {
                    val ownerAsMember = CommunityMember(role = "owner", joinedAt = "", leftAt = null, user = ownerData)
                    CommunityMemberItem(member = ownerAsMember, onMemberClick = { onUserClick(ownerAsMember) })
                }
            }

            items(count = membersPagingItems.itemCount, key = membersPagingItems.itemKey { it.user.id }) { index ->
                val member = membersPagingItems[index]
                if (member != null) {
                    CommunityMemberItem(member = member, onMemberClick = { onUserClick(member) })
                }
            }

            membersPagingItems.apply {
                when {
                    loadState.append is LoadState.Loading -> item { MembersLoadingMoreIndicator() }
                    loadState.append is LoadState.Error -> item { MembersLoadMoreErrorItem(message = "Failed to load more members", onRetry = { retry() }) }
                }
            }
        }
    }
}

@Composable
fun MembersAppBar(title: String, onBackClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary, shadowElevation = 4.dp) {
        Row(modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onBackClick, shape = CircleShape, color = Color.Transparent, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                }
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp), color = MaterialTheme.colorScheme.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun MembersSearchBar(query: String, onQueryChange: (String) -> Unit, onClearQuery: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(16.dp).focusRequester(focusRequester).shadow(4.dp, RoundedCornerShape(8.dp)),
        placeholder = { Text("Search members...") },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = { if (query.isNotEmpty()) { IconButton(onClick = onClearQuery) { Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear") } } },
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

@Composable
fun CommunityMemberItem(member: CommunityMember, onMemberClick: () -> Unit) {
    Card(onClick = onMemberClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)).border(width = 2.dp, brush = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)), shape = CircleShape)) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(member.user.profilePicture ?: R.drawable.profile_default).crossfade(true).build(), contentDescription = "Profile Picture", modifier = Modifier.size(50.dp).clip(CircleShape).align(Alignment.Center), contentScale = ContentScale.Crop)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${member.user.name.toString()} ${member.user.lastName.toString()}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Member Role: ${member.role}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(imageVector = Icons.Default.Person, contentDescription = "View Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun MembersLoadingState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Loading members...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MembersErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.Default.Person, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Failed to load members", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Retry") }
    }
}

@Composable
fun MembersEmptyState(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.Default.Person, contentDescription = "No Members", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "No Community Members", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "There are currently no members in this community", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
    }
}

@Composable
fun MembersEmptySearchState(searchQuery: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.Default.Search, contentDescription = "No Results", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "No Results Found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "No members matching \"$searchQuery\" were found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
    }
}

@Composable
private fun MembersLoadingMoreIndicator() {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            Text(text = "Loading more members...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MembersLoadMoreErrorItem(message: String, onRetry: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }
            }
        }
    }
}
