package com.example.mealflow.utils

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mealflow.network.PostLike
import com.example.mealflow.network.PostLikesApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostLikesPagingSource(
    private val postLikesApiService: PostLikesApiService,
    private val postId: String
) : PagingSource<String, PostLike>() {

    companion object {
        private const val TAG = "PostLikesPagingSource"
        private const val INITIAL_LOAD_SIZE = 10
        private const val PAGE_SIZE = 5
    }

    override fun getRefreshKey(state: PagingState<String, PostLike>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, PostLike> {
        val cursor = params.key
        val loadSize = if (params is LoadParams.Refresh) INITIAL_LOAD_SIZE else PAGE_SIZE

        return try {
            Log.d(TAG, "Loading post likes with cursor: $cursor, loadSize: $loadSize, postId: $postId")

            val response = withContext(Dispatchers.IO) {
                postLikesApiService.fetchPostLikes(
                    postId = postId,
                    limit = loadSize,
                    cursor = cursor
                )
            }

            if (response == null || !response.success) {
                Log.e(TAG, "Error loading post likes: ${response?.message ?: "Response is null"}")
                return LoadResult.Error(Exception("Failed to load post likes"))
            }

            val likes = response.data.likes
            val nextKey = response.data.pagination?.nextCursor

            Log.d(TAG, "Loaded ${likes.size} post likes, nextKey: $nextKey")

            LoadResult.Page(
                data = likes,
                prevKey = null,
                nextKey = if (likes.isEmpty() || nextKey.isNullOrEmpty()) null else nextKey
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading post likes for postId: $postId", e)
            LoadResult.Error(e)
        }
    }
}