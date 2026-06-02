package com.example.mealflow.viewModel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.mealflow.network.createPostCommunityApi
import com.example.mealflow.network.createPostProfileApi

class CreatePostViewModel : ViewModel() {
    // UI State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Post Data
    var title by mutableStateOf("")
    var content by mutableStateOf("")
    var flair by mutableStateOf("")
    var mediaUri by mutableStateOf<Uri?>(null)
    var mediaType by mutableStateOf<String?>(null) // "IMAGE", "VIDEO", or null

    // Reset all states
    fun resetState() {
        isLoading = false
        errorMessage = null
        title = ""
        content = ""
        flair = ""
        mediaUri = null
        mediaType = null
    }

    // Function to create post based on whether it's a community post or profile post
    fun createPost(
        context: Context,
        communityId: String?, // If null, it's a profile post
        navController: NavController
    ) {
        // Validate common required fields
        if (title.isBlank() || content.isBlank() || flair.isBlank()) {
            errorMessage = "Please fill in all required fields including title, content, and flair"
            return
        }

        // Set loading state
        isLoading = true
        errorMessage = null

        try {
            if (communityId.isNullOrBlank()) {
                // Create a profile post
                createPostProfileApi(
                    context = context,
                    title = title,
                    content = content,
                    flair = flair,
                    mediaUri = mediaUri,
                    mediaType = mediaType,
                    navController = navController,
                    viewModel = this // Pass the ViewModel
                )
            } else {
                // Create a community post
                createPostCommunityApi(
                    context = context,
                    title = title,
                    content = content,
                    communityId = communityId,
                    flair = flair,
                    mediaUri = mediaUri,
                    mediaType = mediaType,
                    navController = navController,
                    viewModel = this // Pass the ViewModel
                )
            }
            // Note: We don't set isLoading = false here because the API functions will handle it
            // after they complete their operations or encounter errors
        } catch (e: Exception) {
            // Only handle exceptions that occur before the API call
            isLoading = false
            errorMessage = "Error: ${e.message}"
        }
    }

    // Function to update media data when user selects media
    fun setMedia(uri: Uri?, type: String?) {
        mediaUri = uri
        mediaType = type
    }

    // Function to clear selected media
    fun clearMedia() {
        mediaUri = null
        mediaType = null
    }
}