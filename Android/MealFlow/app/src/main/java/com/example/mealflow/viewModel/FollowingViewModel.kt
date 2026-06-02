package com.example.mealflow.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mealflow.network.FollowRelationship
import com.example.mealflow.network.FollowingApiService
import com.example.mealflow.utils.MyFollowingPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MyFollowingViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "FollowingViewModel"
        private const val PAGE_SIZE = 5
        private const val INITIAL_LOAD_SIZE = 10
    }

    private val followingApiService = FollowingApiService(application.applicationContext)

    // Refresh trigger to handle manual refreshes
    private val refreshTrigger = MutableStateFlow(0)
    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery = _searchQuery.asStateFlow()

    // Error handling state
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Following flow with refresh and search support
    val followingFlow: Flow<PagingData<FollowRelationship>> = combine(
        refreshTrigger,
        _searchQuery
    ) { _, query ->
        query
    }.flatMapLatest { query ->
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 2,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                MyFollowingPagingSource(followingApiService, query)
            }
        ).flow
    }
        .cachedIn(viewModelScope)
        .distinctUntilChanged()

    /**
     * Update search query and trigger refresh
     */
    fun updateSearchQuery(query: String?) {
        viewModelScope.launch {
            val trimmedQuery = query?.trim()?.takeIf { it.isNotEmpty() }
            if (_searchQuery.value != trimmedQuery) {
                _searchQuery.value = trimmedQuery
                _errorState.value = null
                Log.d(TAG, "Search query updated to: $trimmedQuery")
            }
        }
    }

    /**
     * Force refresh the following data
     */
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Increment trigger to cause recomposition
                refreshTrigger.value = refreshTrigger.value + 1
                _errorState.value = null
                Log.d(TAG, "Refreshing following data")
            } catch (e: Exception) {
                _errorState.value = e.message ?: "An unknown error occurred while updating your following list."
                Log.e(TAG, "Error refreshing following", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear search query
     */
    fun clearSearch() {
        updateSearchQuery(null)
    }

    /**
     * Clear any error state
     */
    fun clearError() {
        _errorState.value = null
    }
}