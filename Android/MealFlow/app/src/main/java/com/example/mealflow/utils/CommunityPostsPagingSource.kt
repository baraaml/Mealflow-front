package com.example.mealflow.utils

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mealflow.data.model.Post
import com.example.mealflow.network.PostsCommunityApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommunityPostsPagingSource(
    private val apiService: PostsCommunityApiService,
    private val pageSize: Int = 10
) : PagingSource<String, Post>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Post> {
        return try {
            // Get current cursor from params
            val cursor = params.key

            // Make API call on IO dispatcher
            val response = withContext(Dispatchers.IO) {
                apiService.fetchPostsCommunity(limit = pageSize, cursor = cursor)
            }

            if (!response.success) {
                return LoadResult.Error(Exception(response.message))
            }

            // Extract posts and next cursor
            val posts = response.data?.posts ?: emptyList()
            val nextCursor = response.data?.pagination?.nextCursor

            LoadResult.Page(
                data = posts,
                prevKey = null, // Community posts don't support backward pagination
                nextKey = nextCursor
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Post>): String? {
        // We don't need a complex refresh key strategy since we're using cursor-based pagination
        // Just return null to refresh from the beginning
        return null
    }
}