package com.example.mealflow.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.network.MyProfileApiService
import com.example.mealflow.network.UserProfile
import kotlinx.coroutines.launch


class ProfilePageViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = MyProfileApiService(application.applicationContext)

    // State variables
    var userProfile by mutableStateOf<UserProfile?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    /**
     * Load user profile from the API
     * @return Boolean indicating success or failure
     */
    suspend fun loadUserProfile(): Boolean {
        isLoading = true
        errorMessage = null

        return try {
            val response = apiService.fetchMyProfile()

            if (response.success) {
                if (response.data != null) {
                    userProfile = response.data
                    true
                } else {
                    errorMessage = "User data was not received from the server."
                    false
                }
            } else {
                errorMessage = response.message
                false
            }
        } catch (e: Exception) {
            errorMessage = "An error occurred while loading the profile:${e.message}"
            false
        } finally {
            isLoading = false
        }
    }

    /**
     * Convenience method to load user profile in viewModelScope
     */
    fun loadInitialProfile() {
        viewModelScope.launch {
            loadUserProfile()
        }
    }

    /**
     * Refresh user profile
     */
    fun refreshProfile() {
        userProfile = null
        loadInitialProfile()
    }
}