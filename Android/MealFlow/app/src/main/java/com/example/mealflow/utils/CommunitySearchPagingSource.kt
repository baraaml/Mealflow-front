package com.example.mealflow.utils

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mealflow.network.SearchApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * A PagingSource implementation for search results with cursor-based pagination
 *
 * @param apiService Service to perform search from the API
 * @param query The search query string
 * @param type The type of content to search for
 * @param limit Number of results to fetch per page (default: 30)
 * @param direction Direction of pagination (default: "before")
 * @param retryDelay Delay in milliseconds before retrying a failed request (default: 3000ms)
 * @param maxRetries Maximum number of retry attempts for failed requests (default: 1)
 */
class SearchPagingSource<T : Any>(
    private val apiService: SearchApiService,
    private val query: String,
    private val type: String,
    private val limit: Int = 30,
    private val direction: String = "before",
    private val retryDelay: Long = 3000,
    private val maxRetries: Int = 1,
    private val dataExtractor: (com.example.mealflow.data.model.SearchData?) -> List<T>
) : PagingSource<String, T>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, T> {
        var attempts = 0
        var lastException: Exception? = null

        while (attempts <= maxRetries) {
            try {
                return withContext(Dispatchers.IO) {
                    val cursor = params.key // null for initial load
                    val loadSize = params.loadSize.coerceAtMost(limit * 3)

                    Log.d("SearchPagingSource", "Searching with query: $query, type: $type, cursor: $cursor, loadSize: $loadSize")
                    Log.d("SearchPagingSource", "Running on thread: ${Thread.currentThread().name}")

                    val response = apiService.search(
                        query = query,
                        type = type,
                        limit = loadSize,
                        cursor = cursor,
                        direction = direction
                    )

                    if (response.success && response.data != null) {
                        val items = dataExtractor(response.data)

                        // Extract nextKey based on the search type
                        val nextKey = extractNextCursor(response.data.pagination, type)

                        LoadResult.Page(
                            data = items,
                            prevKey = null,
                            nextKey = if (response.data.pagination.hasMore) nextKey else null
                        )
                    } else {
                        LoadResult.Error(Throwable(response.message))
                    }
                }
            } catch (e: Exception) {
                lastException = e
                attempts++
                if (attempts <= maxRetries) {
                    delay(retryDelay)
                }
            }
        }
        return LoadResult.Error(lastException ?: Exception("Unknown error performing search"))
    }

    /**
     * Extract the appropriate next cursor based on the search type
     */
    private fun extractNextCursor(
        pagination: com.example.mealflow.data.model.SearchPagination,
        searchType: String
    ): String? {
        val cursorObject = pagination.getNextCursorObject()
        return when (searchType) {
            "posts" -> cursorObject?.posts ?: cursorObject?.cursor ?: pagination.getNextCursorAsString()
            "users" -> cursorObject?.users ?: cursorObject?.cursor ?: pagination.getNextCursorAsString()
            "comments" -> cursorObject?.comments ?: cursorObject?.cursor ?: pagination.getNextCursorAsString()
            "communities" -> cursorObject?.communities ?: cursorObject?.cursor ?: pagination.getNextCursorAsString()
            "all" -> {
                // For "all" type, we prioritize based on which type has data
                cursorObject?.let { cursors ->
                    cursors.posts ?: cursors.users ?: cursors.comments ?: cursors.communities ?: cursors.cursor
                } ?: pagination.getNextCursorAsString()
            }
            else -> cursorObject?.cursor ?: pagination.getNextCursorAsString()
        }
    }

    override fun getRefreshKey(state: PagingState<String, T>): String? {
        // For search, we typically want to start fresh from the beginning
        return null
    }
}