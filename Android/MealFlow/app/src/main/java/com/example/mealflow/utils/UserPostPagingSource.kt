package com.example.mealflow.utils

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mealflow.data.model.Post
import com.example.mealflow.network.PostApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserPostPagingSource(
    private val postApiService: PostApiService
) : PagingSource<String, Post>() {

    companion object {
        private const val TAG = "PostPagingSource"
        private const val INITIAL_LOAD_SIZE = 10
        private const val PAGE_SIZE = 5
    }

    override fun getRefreshKey(state: PagingState<String, Post>): String? {
        // Use the pointer closest to the current loading position
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Post> {
        val cursor = params.key
        val loadSize = if (params is LoadParams.Refresh) INITIAL_LOAD_SIZE else PAGE_SIZE

        return try {
            Log.d(TAG, "Loading posts with cursor: $cursor, loadSize: $loadSize")

            val response = withContext(Dispatchers.IO) {
                postApiService.fetchUserPosts(
                    limit = loadSize,
                    cursor = cursor
                )
            }

            if (!response.success) {
                Log.e(TAG, "Error loading posts: ${response.message}")
                return LoadResult.Error(Exception(response.message))
            }

            val posts = response.data?.posts ?: emptyList()
            val nextKey = response.data?.pagination?.nextCursor

            Log.d(TAG, "Loaded ${posts.size} posts, nextKey: $nextKey")

            LoadResult.Page(
                data = posts,
                prevKey = null, // In this application, we only move forward in loading.
                nextKey = if (posts.isEmpty() || nextKey.isNullOrEmpty()) null else nextKey
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading posts", e)
            LoadResult.Error(e)
        }
    }
}