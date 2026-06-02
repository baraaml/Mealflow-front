package com.example.mealflow.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.network.UserApiService
import com.example.mealflow.network.UserProfile
import kotlinx.coroutines.launch

class UserPageViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = UserApiService(application.applicationContext)

    // Data status
    var userProfile by mutableStateOf<UserProfile?>(null)
        private set

    // Loading status
    var isLoading by mutableStateOf(false)
        private set

    // Error message
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Download user data from API
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val response = apiService.fetchUser()

                if (response.success && response.data != null) {
                    userProfile = response.data
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "An error occurred while loading the profile:${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Reload user data
     */
    fun refreshProfile() {
        userProfile = null
        loadUserProfile()
    }
}