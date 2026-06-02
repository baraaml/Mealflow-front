package com.example.mealflow.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.data.model.SingleCommunity
import com.example.mealflow.network.SingleCommunityApiService
import kotlinx.coroutines.launch

class SingleCommunityViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = SingleCommunityApiService(application.applicationContext)

    var community by mutableStateOf<SingleCommunity?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    /**
     * Load single community data
     */
    fun loadSingleCommunity() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val response = apiService.fetchSingleCommunity()

                if (response.success && response.community != null) {
                    community = response.community
                } else {
                    errorMessage = "Failed to load community data"
                }
            } catch (e: Exception) {
                errorMessage = "An error occurred while loading the community:${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
