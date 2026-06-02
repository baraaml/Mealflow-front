package com.example.mealflow.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mealflow.data.model.Post
import com.example.mealflow.network.PostApiService
import com.example.mealflow.utils.UserPostPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class UserPostViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PostViewModel"
        private const val PAGE_SIZE = 5
        private const val INITIAL_LOAD_SIZE = 10
    }

    private val postApiService = PostApiService(application.applicationContext)

    // Refresh trigger to handle manual refreshes
    private val refreshTrigger = MutableStateFlow(0)

    // Error handling state
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Posts flow with refresh support
    val postsFlow: Flow<PagingData<Post>> = refreshTrigger.flatMapLatest { _ ->
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 2,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                UserPostPagingSource(postApiService)
            }
        ).flow
    }
        .cachedIn(viewModelScope)
        .distinctUntilChanged()

    /**
     * Force refresh the posts data
     */
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Increment trigger to cause recomposition
                refreshTrigger.value = refreshTrigger.value + 1
                _errorState.value = null
                Log.d(TAG, "Refreshing posts data")
            } catch (e: Exception) {
                _errorState.value = e.message ?: "An unknown error occurred while updating posts."
                Log.e(TAG, "Error refreshing posts", e)
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