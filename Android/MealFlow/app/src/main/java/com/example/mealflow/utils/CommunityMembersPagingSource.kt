package com.example.mealflow.utils

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mealflow.network.CommunityMember
import com.example.mealflow.network.CommunityMembersApiService
import com.example.mealflow.network.UserMember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//class CommunityMembersPagingSource(
//    private val communityMembersApiService: CommunityMembersApiService
//) : PagingSource<String, CommunityMember>() {
//
//    companion object {
//        private const val TAG = "CommunityMembersPagingSource"
//        private const val INITIAL_LOAD_SIZE = 10
//        private const val PAGE_SIZE = 5
//    }
//
//    override fun getRefreshKey(state: PagingState<String, CommunityMember>): String? {
//        return state.anchorPosition?.let { anchorPosition ->
//            state.closestPageToPosition(anchorPosition)?.prevKey
//                ?: state.closestPageToPosition(anchorPosition)?.nextKey
//        }
//    }
//
//    override suspend fun load(params: LoadParams<String>): LoadResult<String, CommunityMember> {
//        val cursor = params.key
//        val loadSize = if (params is LoadParams.Refresh) INITIAL_LOAD_SIZE else PAGE_SIZE
//
//        return try {
//            Log.d(TAG, "Loading community members with cursor: $cursor, loadSize: $loadSize")
//
//            val response = withContext(Dispatchers.IO) {
//                communityMembersApiService.fetchCommunityMembers(
//                    limit = loadSize,
//                    cursor = cursor
//                )
//            }
//
//            if (!response.success) {
//                Log.e(TAG, "Error loading community members: ${response.count}")
//                return LoadResult.Error(Exception("Failed to load community members"))
//            }
//
//            val members = response.data.members
//            val nextKey = response.data.pagination?.nextCursor
//
//            Log.d(TAG, "Loaded ${members.size} community members, nextKey: $nextKey")
//
//            LoadResult.Page(
//                data = members,
//                prevKey = null,
//                nextKey = if (members.isEmpty() || nextKey.isNullOrEmpty()) null else nextKey
//            )
//        } catch (e: Exception) {
//            Log.e(TAG, "Error loading community members", e)
//            LoadResult.Error(e)
//        }
//    }
//}
class CommunityMembersPagingSource(
    private val communityMembersApiService: CommunityMembersApiService,
    private val onOwnerLoaded: (UserMember) -> Unit = {}
) : PagingSource<String, CommunityMember>() {

    companion object {
        private const val TAG = "CommunityMembersPagingSource"
        private const val INITIAL_LOAD_SIZE = 10
        private const val PAGE_SIZE = 5
    }

    override fun getRefreshKey(state: PagingState<String, CommunityMember>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, CommunityMember> {
        val cursor = params.key
        val loadSize = if (params is LoadParams.Refresh) INITIAL_LOAD_SIZE else PAGE_SIZE

        return try {
            Log.d(TAG, "Loading community members with cursor: $cursor, loadSize: $loadSize")

            val response = withContext(Dispatchers.IO) {
                communityMembersApiService.fetchCommunityMembers(
                    limit = loadSize,
                    cursor = cursor
                )
            }

            if (!response.success) {
                Log.e(TAG, "Error loading community members: ${response.message}")
                return LoadResult.Error(Exception("Failed to load community members: ${response.message}"))
            }

            val members = response.data.members
            val nextKey = response.data.pagination?.nextCursor

            // إرسال الـ owner عند أول تحميل
            if (params is LoadParams.Refresh) {
                onOwnerLoaded(response.data.owner)
            }

            Log.d(TAG, "Loaded ${members.size} community members, nextKey: $nextKey")

            LoadResult.Page(
                data = members,
                prevKey = null,
                nextKey = if (members.isEmpty() || nextKey.isNullOrEmpty()) null else nextKey
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading community members", e)
            LoadResult.Error(e)
        }
    }
}