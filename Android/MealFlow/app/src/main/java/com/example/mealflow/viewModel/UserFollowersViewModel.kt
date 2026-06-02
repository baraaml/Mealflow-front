package com.example.mealflow.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mealflow.network.FollowerRelationship
import com.example.mealflow.network.FollowersApiService
import com.example.mealflow.utils.UserFollowersPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class UserFollowersViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "UserFollowersViewModel"
        private const val PAGE_SIZE = 5
        private const val INITIAL_LOAD_SIZE = 10
    }

    private val followersApiService = FollowersApiService(application.applicationContext)

    private val refreshTrigger = MutableStateFlow(0)
    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery = _searchQuery.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val followersFlow: Flow<PagingData<FollowerRelationship>> = combine(
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
                UserFollowersPagingSource(followersApiService, query)
            }
        ).flow
    }
        .cachedIn(viewModelScope)
        .distinctUntilChanged()

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

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                refreshTrigger.value += 1
                _errorState.value = null
                Log.d(TAG, "Refreshing followers data")
            } catch (e: Exception) {
                _errorState.value = e.message ?: "An unknown error occurred while updating the follower list."
                Log.e(TAG, "Error refreshing followers", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSearch() {
        updateSearchQuery(null)
    }

    fun clearError() {
        _errorState.value = null
    }
}