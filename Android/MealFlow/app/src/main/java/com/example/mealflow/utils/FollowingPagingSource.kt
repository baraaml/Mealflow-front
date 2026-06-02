package com.example.mealflow.utils

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mealflow.network.FollowRelationship
import com.example.mealflow.network.FollowingApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyFollowingPagingSource(
    private val followingApiService: FollowingApiService,
    private val searchQuery: String? = null
) : PagingSource<String, FollowRelationship>() {

    companion object {
        private const val TAG = "FollowingPagingSource"
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
        val cursor = params.key
        val loadSize = if (params is LoadParams.Refresh) INITIAL_LOAD_SIZE else PAGE_SIZE

        return try {
            Log.d(TAG, "Loading following with cursor: $cursor, loadSize: $loadSize, searchQuery: $searchQuery")

            val response = withContext(Dispatchers.IO) {
                followingApiService.fetchMyFollowing(
                    limit = loadSize,
                    cursor = cursor,
                    direction = "after",
                    searchQuery = searchQuery
                )
            }

            if (!response.success) {
                Log.e(TAG, "Error loading following: ${response.message}")
                return LoadResult.Error(Exception(response.message))
            }

            val follows = response.data?.follows ?: emptyList()
            val nextKey = response.data?.pagination?.nextCursor

            Log.d(TAG, "Loaded ${follows.size} follows, nextKey: $nextKey")

            LoadResult.Page(
                data = follows,
                prevKey = null, // only forward paging
                nextKey = if (follows.isEmpty() || nextKey.isNullOrEmpty()) null else nextKey
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading following", e)
            LoadResult.Error(e)
        }
    }
}