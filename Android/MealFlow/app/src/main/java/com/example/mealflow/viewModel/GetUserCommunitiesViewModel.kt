package com.example.mealflow.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mealflow.data.model.CommunityRole
import com.example.mealflow.network.UserCommunitiesApiService
import com.example.mealflow.utils.CommunityPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GetUserCommunitiesViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CommunityViewModel"
        private const val PAGE_SIZE = 5
        private const val INITIAL_LOAD_SIZE = 10
    }

    private val communitiesApiService = UserCommunitiesApiService(application.applicationContext)

    // Refresh trigger to handle manual refreshes
    private val refreshTrigger = MutableStateFlow(0)

    // Error handling state
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Communities flow with refresh support
    val communitiesFlow: Flow<PagingData<CommunityRole>> = refreshTrigger.flatMapLatest { _ ->
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 2,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                CommunityPagingSource(communitiesApiService)
            }
        ).flow
    }
        .cachedIn(viewModelScope)
        .distinctUntilChanged()

    /**
     * Force refresh the communities data
     */
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Increment trigger to cause recomposition
                refreshTrigger.value = refreshTrigger.value + 1
                _errorState.value = null
                Log.d(TAG, "Refreshing communities data")
            } catch (e: Exception) {
                _errorState.value = e.message ?: "An unknown error occurred while updating communities."
                Log.e(TAG, "Error refreshing communities", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear any error state
     */
    fun clearError() {
        _errorState.value = null
    }
}
