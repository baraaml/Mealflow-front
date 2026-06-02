package com.example.mealflow.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mealflow.data.model.AllCommunities
import com.example.mealflow.network.CommunityApiService
import com.example.mealflow.utils.AllCommunitiesPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AllCommunitiesViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = CommunityApiService(application.applicationContext)

    // Refresh trigger
    private val refreshTrigger = MutableStateFlow(0)

    // Error state
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: Flow<String?> = _errorState

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading

    // Communities paging data (supports refresh)
    val communitiesPagingData: Flow<PagingData<AllCommunities>> = refreshTrigger.flatMapLatest {
        Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 5,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { AllCommunitiesPagingSource(apiService) }
        ).flow
    }.cachedIn(viewModelScope)

    /**
     * Refresh the communities list
     */
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                refreshTrigger.value = refreshTrigger.value + 1
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message ?: "Unknown error while refreshing communities"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load the initial page of communities by triggering a refresh.
     */
    fun loadInitialCommunities() {
        refresh()
    }

    /**
     * Clear any existing error
     */
    fun clearError() {
        _errorState.value = null
    }
}