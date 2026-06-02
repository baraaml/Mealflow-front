package com.example.mealflow.utils

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mealflow.data.model.Post
import com.example.mealflow.network.MyPostsApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * A PagingSource implementation for loading user posts with cursor-based pagination
 *
 * @param apiService Service to fetch posts from the API
 * @param limit Number of posts to fetch per page (default: 10)
 * @param retryDelay Delay in milliseconds before retrying a failed request (default: 3000ms)
 * @param maxRetries Maximum number of retry attempts for failed requests (default: 3)
 */
class PostsPagingSource(
    private val apiService: MyPostsApiService,
    private val limit: Int = 3,
    private val retryDelay: Long = 3000,
    private val maxRetries: Int = 1
) : PagingSource<String, Post>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Post> {
        var attempts = 0
        var lastException: Exception? = null

        while (attempts <= maxRetries) {
            try {
                val cursor = params.key // null for initial load

                val response = withContext(Dispatchers.IO) {
                    apiService.fetchMyPosts(
                        limit = params.loadSize.coerceAtMost(limit * 3), // Allow for larger initial loads
                        cursor = cursor
                    )
                }

                if (response.success && response.data != null) {
                    val posts = response.data.posts
                    val nextKey = if (response.data.pagination.hasMore) {
                        response.data.pagination.nextCursor
                    } else {
                        null
                    }

                    return LoadResult.Page(
                        data = posts,
                        prevKey = null,
                        nextKey = nextKey
                    )
                } else {
                    return LoadResult.Error(
                        Throwable(response.message)
                    )
                }
            } catch (e: Exception) {
                lastException = e
                attempts++

                if (attempts <= maxRetries) {
                    delay(retryDelay)
                }
            }
        }

        return LoadResult.Error(lastException ?: Exception("Unknown error loading posts"))
    }

    override fun getRefreshKey(state: PagingState<String, Post>): String? {
        // Try to find the closest item to current position for a smoother refresh experience
        return state.anchorPosition?.let { anchorPosition ->
            state.closestItemToPosition(anchorPosition)?.id
        }
    }
}