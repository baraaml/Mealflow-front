package com.example.mealflow.utils

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mealflow.network.FollowerRelationship
import com.example.mealflow.network.FollowersApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FollowersPagingSource(
    private val followersApiService: FollowersApiService,
    private val searchQuery: String? = null
) : PagingSource<String, FollowerRelationship>() {

    companion object {
        private const val TAG = "FollowersPagingSource"
        private const val INITIAL_LOAD_SIZE = 10
        private const val PAGE_SIZE = 5
    }

    override fun getRefreshKey(state: PagingState<String, FollowerRelationship>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, FollowerRelationship> {
        return withContext(Dispatchers.IO) {
            try {
                val cursor = params.key
                val loadSize = if (params is LoadParams.Refresh) INITIAL_LOAD_SIZE else PAGE_SIZE

                Log.d(TAG, "Loading followers with cursor: $cursor, loadSize: $loadSize, searchQuery: $searchQuery")
                Log.d(TAG, "Running on thread: ${Thread.currentThread().name}")

                val response = followersApiService.fetchMyFollowers(
                    limit = loadSize,
                    cursor = cursor,
                    direction = "after",
                    searchQuery = searchQuery
                )

                if (!response.success) {
                    return@withContext LoadResult.Error(Exception(response.message))
                }

                val followers = response.data?.follows ?: emptyList()
                val nextKey = response.data?.pagination?.nextCursor

                LoadResult.Page(
                    data = followers,
                    prevKey = null,
                    nextKey = if (followers.isEmpty() || nextKey.isNullOrEmpty()) null else nextKey
                )
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }
    }
}

//    override suspend fun load(params: LoadParams<String>): LoadResult<String, FollowerRelationship> {
//        val cursor = params.key
//        val loadSize = if (params is LoadParams.Refresh) INITIAL_LOAD_SIZE else PAGE_SIZE
//
//        return try {
//            Log.d(TAG, "Loading followers with cursor: $cursor, loadSize: $loadSize")
//
//            // 🚀 نفذ الـ API call على IO dispatcher عشان ما يبطّئش الـ UI
//            val response = withContext(Dispatchers.IO) {
//                followersApiService.fetchMyFollowers(
//                    limit = loadSize,
//                    cursor = cursor,
//                    direction = "after"
//                )
//            }
//
////            val response = followersApiService.fetchMyFollowers(
////                limit = loadSize,
////                cursor = cursor,
////                direction = "after"
////            )
//
//            if (!response.success) {
//                Log.e(TAG, "Error loading followers: ${response.message}")
//                return LoadResult.Error(Exception(response.message))
//            }
//
//            val followers = response.data?.follows ?: emptyList()
//            val nextKey = response.data?.pagination?.nextCursor
//
//            Log.d(TAG, "Loaded ${followers.size} followers, nextKey: $nextKey")
//
//            LoadResult.Page(
//                data = followers,
//                prevKey = null,
//                nextKey = if (followers.isEmpty() || nextKey.isNullOrEmpty()) null else nextKey
//            )
//        } catch (e: Exception) {
//            Log.e(TAG, "Error loading followers", e)
//            LoadResult.Error(e)
//        }
//    }
