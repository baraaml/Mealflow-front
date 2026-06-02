package com.example.mealflow.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mealflow.network.PostLike
import com.example.mealflow.network.PostLikesApiService
import com.example.mealflow.utils.PostLikesPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PostLikesViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PostLikesViewModel"
        private const val PAGE_SIZE = 5
        private const val INITIAL_LOAD_SIZE = 10
    }

    private val postLikesApiService = PostLikesApiService(application.applicationContext)

    private val refreshTrigger = MutableStateFlow(0)

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _postId = MutableStateFlow<String?>(null)

    fun setPostId(postId: String) {
        Log.d(TAG, "Setting postId: $postId")
        _postId.value = postId
        // Clear any previous errors when setting new postId
        _errorState.value = null
    }

    val postLikesFlow: Flow<PagingData<PostLike>> = combine(
        _postId,
        refreshTrigger
    ) { postId, _ ->
        postId
    }.flatMapLatest { postId ->
        if (postId.isNullOrEmpty()) {
            Log.d(TAG, "PostId is null or empty, returning empty PagingData")
            kotlinx.coroutines.flow.flowOf(PagingData.empty())
        } else {
            Log.d(TAG, "Creating Pager for postId: $postId")
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    prefetchDistance = 2,
                    initialLoadSize = INITIAL_LOAD_SIZE,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = {
                    Log.d(TAG, "Creating PostLikesPagingSource for postId: $postId")
                    PostLikesPagingSource(postLikesApiService, postId)
                }
            ).flow
        }
    }
        .cachedIn(viewModelScope)
        .distinctUntilChanged()

    fun refresh() {
        viewModelScope.launch {
            val currentPostId = _postId.value
            if (currentPostId.isNullOrEmpty()) {
                Log.w(TAG, "Cannot refresh: postId is null or empty")
                _errorState.value = "Post ID is not set"
                return@launch
            }

            _isLoading.value = true
            try {
                refreshTrigger.value += 1
                _errorState.value = null
                Log.d(TAG, "Refreshing post likes data for postId: $currentPostId")
            } catch (e: Exception) {
                _errorState.value = e.message ?: "An unknown error occurred while updating the post likes list."
                Log.e(TAG, "Error refreshing post likes", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorState.value = null
    }
}