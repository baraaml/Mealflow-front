package com.example.mealflow.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.data.model.AllCommunities
import com.example.mealflow.network.CommunityApiService
import kotlinx.coroutines.launch

class GetAllCommunitiesViewModel(application: Application) : AndroidViewModel(application) {
    private val communitiesApiService = CommunityApiService(application.applicationContext)

    var communities by mutableStateOf<List<AllCommunities>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var hasMoreCommunities by mutableStateOf(true) // Assume there are more until proven otherwise
    var totalCount by mutableIntStateOf(0)

    private var nextCursor: String? = null
    private val defaultLimit = 2

    /**
     * Load all communities from the API
     * @param reset If true, clears existing list and starts from the beginning
     * @param limit Number of communities to load per request
     * @return Boolean indicating if there are more communities to load
     */
    suspend fun loadAllCommunities(reset: Boolean = false, limit: Int = defaultLimit): Boolean {
        if (reset) {
            communities = emptyList()
            nextCursor = null
        }

        // Don't proceed if already loading or if we've reached the end and not resetting
        if (isLoading || (!reset && !hasMoreCommunities && communities.isNotEmpty())) {
            return false
        }

        isLoading = true
        errorMessage = null

        return try {
            val cursor = if (reset) null else nextCursor
            val response = communitiesApiService.fetchAllCommunities(limit, cursor)

            if (response.success) {
                communities = if (reset) {
                    response.communities
                } else {
                    communities + response.communities
                }

                totalCount = response.count

                // Determine if there are more communities to load
                // Either there's an explicit next cursor or we can calculate based on count
                hasMoreCommunities = communities.size < totalCount

                // Update nextCursor based on your API pagination logic
                // This may need adjustment based on your actual API implementation
                // For example, if your API returns the next cursor explicitly:
                // nextCursor = response.pagination?.nextCursor

                // If your API doesn't provide a next cursor, you might need to create one
                // This is a simple example where we use the last community's ID as cursor
                nextCursor = if (hasMoreCommunities && communities.isNotEmpty()) {
                    communities.last().id
                } else {
                    null
                }

                hasMoreCommunities
            } else {
                errorMessage = "Failed to load communities"
                false
            }
        } catch (e: Exception) {
            errorMessage = "Error loading communities: ${e.message}"
            false
        } finally {
            isLoading = false
        }
    }

    /**
     * Load initial communities - convenience method for UI components
     */
    fun loadInitialCommunities() {
        viewModelScope.launch {
            loadAllCommunities(reset = true)
        }
    }

    /**
     * Load more communities when user scrolls to the end of the list
     */
    fun loadMoreCommunitiesIfNeeded() {
        if (!isLoading && hasMoreCommunities) {
            viewModelScope.launch {
                loadAllCommunities(reset = false)
            }
        }
    }

    /**
     * Refresh the communities list
     */
    fun refresh() {
        viewModelScope.launch {
            loadAllCommunities(reset = true)
        }
    }
}


//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------