package com.example.mealflow.viewModel

// ViewModel implementation
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mealflow.data.model.Post
import com.example.mealflow.network.MyPostsApiService
import com.example.mealflow.network.PostRepository
import com.example.mealflow.utils.PostsPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModel(application: Application) : AndroidViewModel(application) {
    private val postApiService = MyPostsApiService(application.applicationContext)
    private val postService = PostRepository(application)
    // Refresh trigger to handle manual refreshes
    private val refreshTrigger = MutableStateFlow(0)

    // Error handling state
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: Flow<String?> = _errorState

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading

    // Posts flow with refresh support
    val pagedPosts: Flow<PagingData<Post>> = refreshTrigger.flatMapLatest { _ ->
        Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 5,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                PostsPagingSource(postApiService)
            }
        ).flow
    }.cachedIn(viewModelScope)

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

    /**
     * Clear any error state
     */
    fun clearError() {
        _errorState.value = null
    }
}