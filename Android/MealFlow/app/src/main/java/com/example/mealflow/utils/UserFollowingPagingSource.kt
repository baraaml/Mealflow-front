package com.example.mealflow.utils

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mealflow.network.FollowRelationship
import com.example.mealflow.network.FollowingApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserFollowingPagingSource(
    private val followingApiService: FollowingApiService,
    private val searchQuery: String? = null
) : PagingSource<String, FollowRelationship>() {

    companion object {
        private const val TAG = "UserFollowingPagingSource"
        private const val INITIAL_LOAD_SIZE = 10
        private const val PAGE_SIZE = 5
    }

    override fun getRefreshKey(state: PagingState<String, FollowRelationship>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, FollowRelationship> {
        return withContext(Dispatchers.IO) {
            try {
                val cursor = params.key
                val loadSize = if (params is LoadParams.Refresh) INITIAL_LOAD_SIZE else PAGE_SIZE

                Log.d(TAG, "Loading following for user with cursor: $cursor, loadSize: $loadSize, searchQuery: $searchQuery")
                Log.d(TAG, "Running on thread: ${Thread.currentThread().name}")

                val response = followingApiService.fetchUserFollowing(
                    limit = loadSize,
                    cursor = cursor,
                    direction = "after",
                    searchQuery = searchQuery
                )

                if (!response.success) {
                    return@withContext LoadResult.Error(Exception(response.message))
                }

                val follows = response.data?.follows ?: emptyList()
                val nextKey = response.data?.pagination?.nextCursor

                LoadResult.Page(
                    data = follows,
                    prevKey = null,
                    nextKey = if (follows.isEmpty() || nextKey.isNullOrEmpty()) null else nextKey
                )
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }
    }
}