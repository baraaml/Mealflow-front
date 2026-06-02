package com.example.mealflow.utils

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mealflow.data.model.CommunityRole
import com.example.mealflow.network.UserCommunitiesApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommunityPagingSource(
    private val communitiesApiService: UserCommunitiesApiService
) : PagingSource<String, CommunityRole>() {

    companion object {
        private const val TAG = "CommunityPagingSource"
    }

    override fun getRefreshKey(state: PagingState<String, CommunityRole>): String? {
        // Use the pointer closest to the current loading position
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, CommunityRole> {
        val cursor = params.key
        val loadSize = params.loadSize

        return try {
            Log.d(TAG, "Loading communities with cursor: $cursor, loadSize: $loadSize")

            val response = withContext(Dispatchers.IO) {
                communitiesApiService.fetchUserCommunities(
                    limit = loadSize,
                    cursor = cursor
                )
            }

            if (!response.success) {
                Log.e(TAG, "Error loading communities: ${response.message}")
                return LoadResult.Error(Exception(response.message))
            }

            val communities = response.data?.communities ?: emptyList()
            val nextKey = response.data?.pagination?.nextCursor

            Log.d(TAG, "Loaded ${communities.size} communities, nextKey: $nextKey")

            LoadResult.Page(
                data = communities,
                prevKey = null, // In this application, we only move forward in loading.
                nextKey = if (communities.isEmpty() || nextKey == null) null else nextKey
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading communities", e)
            LoadResult.Error(e)
        }
    }
}