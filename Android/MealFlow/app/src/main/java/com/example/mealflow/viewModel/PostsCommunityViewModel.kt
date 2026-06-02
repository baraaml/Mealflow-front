package com.example.mealflow.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mealflow.data.model.Post
import com.example.mealflow.network.PostRepository
import com.example.mealflow.network.PostsCommunityApiService
import com.example.mealflow.utils.CommunityPostsPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostsCommunityViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = PostsCommunityApiService(application)
    private val postService = PostRepository(application)


    // State for displaying error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // State for loading indicator
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Refresh trigger to handle manual refreshes
    private val refreshTrigger = MutableStateFlow(0)

    // Error handling state
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: Flow<String?> = _errorState


    // Configure paging
    private val pagingConfig = PagingConfig(
        pageSize = 10,
        prefetchDistance = 3,
        enablePlaceholders = false,
        initialLoadSize = 10,
        maxSize = 30
    )

    // Create paging data flow for posts
    val postsFlow: Flow<PagingData<Post>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = {
            CommunityPostsPagingSource(apiService)
        }
    ).flow.cachedIn(viewModelScope)

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
            } catch (e: Exception) {
                _errorState.value = e.message ?: "Unknown error refreshing posts"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun deletePostAndRefresh(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = postService.deletePost(postId)
                if (response?.success == true) {
                    refresh() // Trigger paging refresh
                } else {
                    _errorState.value = response?.message ?: "Failed to delete post"
                }
            } catch (e: Exception) {
                _errorState.value = "Error deleting post: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}