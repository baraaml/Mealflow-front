//package com.example.mealflow.utils
//
//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import com.example.mealflow.data.model.AllCommunities
//import com.example.mealflow.network.CommunityApiService
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//class AllCommunitiesPagingSource(
//    private val apiService: CommunityApiService,
//    private val pageSize: Int = 2
//) : PagingSource<String, AllCommunities>() {
//
//    override suspend fun load(params: LoadParams<String>): LoadResult<String, AllCommunities> {
//        return try {
//            val cursor = params.key // null for first load
//
//            val response = withContext(Dispatchers.IO) {
//                apiService.fetchAllCommunities(limit = pageSize, cursor = cursor)
//            }
//
//            if (response.success) {
//                // Since AllCommunitiesResponse doesn't have explicit pagination info,
//                // we need to determine if there are more items based on the response
//                val hasMore = response.communities.size == pageSize
//                val nextCursor = if (hasMore) {
//                    // Use the last community's ID as the cursor for next page
//                    response.communities.lastOrNull()?.id
//                } else {
//                    null
//                }
//
//                LoadResult.Page(
//                    data = response.communities,
//                    prevKey = null, // No backward pagination
//                    nextKey = nextCursor
//                )
//            } else {
//                LoadResult.Error(Exception("Failed to fetch communities"))
//            }
//        } catch (e: Exception) {
//            LoadResult.Error(e)
//        }
//    }
//
//    override fun getRefreshKey(state: PagingState<String, AllCommunities>): String? {
//        return null // Restart from scratch
//    }
//}
package com.example.mealflow.utils

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mealflow.data.model.AllCommunities
import com.example.mealflow.network.CommunityApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AllCommunitiesPagingSource(
    private val apiService: CommunityApiService,
    private val pageSize: Int = 2
) : PagingSource<String, AllCommunities>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, AllCommunities> {
        return try {
            val cursor = params.key // null for first load

            val response = withContext(Dispatchers.IO) {
                apiService.fetchAllCommunities(limit = pageSize, cursor = cursor)
            }

            if (response.success) {
                // Since AllCommunitiesResponse doesn't have explicit pagination info,
                // we need to determine if there are more items based on the response
                val hasMore = response.communities.size == pageSize
                val nextCursor = if (hasMore) {
                    // Use the last community's createdAt timestamp as the cursor for next page
                    // Assuming AllCommunities has a createdAt field with ISO date string
                    response.communities.lastOrNull()?.createdAt
                } else {
                    null
                }

                LoadResult.Page(
                    data = response.communities,
                    prevKey = null, // No backward pagination
                    nextKey = nextCursor
                )
            } else {
                LoadResult.Error(Exception("Failed to fetch communities: ${response ?: "Unknown error"}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, AllCommunities>): String? {
        return null // Restart from scratch
    }
}