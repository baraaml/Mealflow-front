package com.example.mealflow.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mealflow.data.model.*
import com.example.mealflow.network.SearchApiService
import com.example.mealflow.utils.SearchPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for handling search functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val searchApiService = SearchApiService(application.applicationContext)

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Search type state
    private val _searchType = MutableStateFlow("all")
    val searchType: StateFlow<String> = _searchType.asStateFlow()

    // Error handling state
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Search trigger to handle new searches
    private val searchTrigger = MutableStateFlow(SearchParams("", "all"))

    // Search results flows for different types
    val searchPosts: Flow<PagingData<Post>> = searchTrigger
        .filter { it.query.length >= 2 && (it.type == "posts" || it.type == "all") }
        .flatMapLatest { params ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    initialLoadSize = 30,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    SearchPagingSource(
                        apiService = searchApiService,
                        query = params.query,
                        type = "posts", // Always use specific type for API call
                        dataExtractor = { data -> data?.posts ?: emptyList() }
                    )
                }
            ).flow
        }.cachedIn(viewModelScope)

    val searchUsers: Flow<PagingData<SearchUser>> = searchTrigger
        .filter { it.query.length >= 2 && (it.type == "users" || it.type == "all") }
        .flatMapLatest { params ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    initialLoadSize = 30,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    SearchPagingSource(
                        apiService = searchApiService,
                        query = params.query,
                        type = "users", // Always use specific type for API call
                        dataExtractor = { data -> data?.users ?: emptyList() }
                    )
                }
            ).flow
        }.cachedIn(viewModelScope)

    val searchComments: Flow<PagingData<SearchComment>> = searchTrigger
        .filter { it.query.length >= 2 && (it.type == "comments" || it.type == "all") }
        .flatMapLatest { params ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    initialLoadSize = 30,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    SearchPagingSource(
                        apiService = searchApiService,
                        query = params.query,
                        type = "comments", // Always use specific type for API call
                        dataExtractor = { data -> data?.comments ?: emptyList() }
                    )
                }
            ).flow
        }.cachedIn(viewModelScope)

    val searchCommunities: Flow<PagingData<SearchCommunity>> = searchTrigger
        .filter { it.query.length >= 2 && (it.type == "communities" || it.type == "all") }
        .flatMapLatest { params ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    initialLoadSize = 30,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    SearchPagingSource(
                        apiService = searchApiService,
                        query = params.query,
                        type = "communities", // Always use specific type for API call
                        dataExtractor = { data -> data?.communities ?: emptyList() }
                    )
                }
            ).flow
        }.cachedIn(viewModelScope)

    // For "all" type searches, create a separate flow
    val searchAll: Flow<PagingData<SearchResult>> = searchTrigger
        .filter { it.query.length >= 2 && it.type == "all" }
        .flatMapLatest { params ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 5,
                    initialLoadSize = 30,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    SearchPagingSource(
                        apiService = searchApiService,
                        query = params.query,
                        type = "all",
                        dataExtractor = { data ->
                            // Combine all results into a unified list
                            val allResults = mutableListOf<SearchResult>()
                            data?.posts?.forEach { allResults.add(SearchResult.PostResult(it)) }
                            data?.users?.forEach { allResults.add(SearchResult.UserResult(it)) }
                            data?.comments?.forEach { allResults.add(SearchResult.CommentResult(it)) }
                            data?.communities?.forEach { allResults.add(SearchResult.CommunityResult(it)) }
                            allResults
                        }
                    )
                }
            ).flow
        }.cachedIn(viewModelScope)

    /**
     * Perform a search with the given query and type
     */
    fun search(query: String, type: String = "all") {
        if (query.length < 2) {
            _errorState.value = "Search query must be at least 2 characters long"
            return
        }

        val validTypes = listOf("posts", "users", "comments", "communities", "all")
        if (type !in validTypes) {
            _errorState.value = "Invalid search type"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _searchQuery.value = query
                _searchType.value = type
                searchTrigger.value = SearchParams(query, type)
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message ?: "Unknown error performing search"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update search query
     */
    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Update search type
     */
    fun updateType(type: String) {
        _searchType.value = type
    }

    /**
     * Clear search results and query
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchType.value = "all"
        searchTrigger.value = SearchParams("", "all")
        _errorState.value = null
    }

    /**
     * Clear any error state
     */
    fun clearError() {
        _errorState.value = null
    }

    /**
     * Data class to hold search parameters
     */
    private data class SearchParams(
        val query: String,
        val type: String
    )
}

/**
 * Sealed class to represent different types of search results
 */
sealed class SearchResult {
    data class PostResult(val post: Post) : SearchResult()
    data class UserResult(val user: SearchUser) : SearchResult()
    data class CommentResult(val comment: SearchComment) : SearchResult()
    data class CommunityResult(val community: SearchCommunity) : SearchResult()
}