package com.example.mealflow.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.data.model.Post
import com.example.mealflow.network.toggleLikePost
import kotlinx.coroutines.launch

class InteractionsViewModel(application: Application) : AndroidViewModel(application) {
    // Map to store like state for each post based on postId
    private val _likeState = mutableStateOf<Map<String, Boolean>>(emptyMap())
    val likeState: Map<String, Boolean> get() = _likeState.value

    var likeError by mutableStateOf<String?>(null)

    // Function to initialize like states from a list of posts
    fun initializeLikeStates(posts: List<Post>) {
        val initialStates = posts.associate { it.id to it.hasLiked }
        _likeState.value = _likeState.value + initialStates
    }

    fun toggleLike(postId: String) {
        val context = getApplication<Application>().applicationContext
        likeError = null

        // 1. Optimistic update for UI
        val currentState = _likeState.value
        val newLikeState = !currentState.getOrDefault(postId, false)
        _likeState.value = currentState + (postId to newLikeState)

        // 2. Execute request in background
        viewModelScope.launch {
            try {
                val response = toggleLikePost(context, postId)
                if (response == null || !response.success) {
                    // If request fails, revert to previous state
                    _likeState.value = currentState
                    likeError = "Failed to update like"
                }
            } catch (e: Exception) {
                // If exception occurs, revert to previous state
                _likeState.value = currentState
                likeError = "Error occurred: ${e.message}"
            }
        }
    }
}