package com.example.mealflow.viewModel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.data.model.Post
import com.example.mealflow.network.updatePostProfileApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdatePostViewModel : ViewModel() {

    // UI State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Post Data
    var title by mutableStateOf("")
    var content by mutableStateOf("")
    var flair by mutableStateOf("")
    var mediaUri by mutableStateOf<Uri?>(null)
    var mediaType by mutableStateOf<String?>(null) // "IMAGE", "VIDEO", "NONE" or null
    var isHidden by mutableStateOf(false)
    var allowComments by mutableStateOf(true)

    var navToPage by mutableStateOf("")

    // Current post data flow
    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post.asStateFlow()

    // Load post data from API or database
    fun loadPost(postId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                // Here you would call your API or repository function to get the post
                // _post.value = yourRepository.getPostById(postId)

                // For now it's a placeholder
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load post: ${e.message}"
                isLoading = false
            }
        }
    }

    // Initialize the form from post data
    fun initializeFromPost(post: Post) {
        title = post.title
        content = post.content
        flair = post.flair?.name ?: ""
        mediaType = post.mediaType
        isHidden = post.isHidden
        allowComments = post.allowComments
        // mediaUri remains null as we don't want to re-upload the existing media
    }

    // Reset all states
    fun resetState() {
        isLoading = false
        errorMessage = null
        title = ""
        content = ""
        flair = ""
        mediaUri = null
        mediaType = null
        isHidden = false
        allowComments = true
    }

    fun updatePost(
        context: Context,
        postId: String,
        refresh: () -> Unit
    ) {
        // Validate common required fields
        if (title.isBlank() || content.isBlank() || flair.isBlank()) {
            errorMessage = "Please fill in all required fields including title, content, and flair"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val response = updatePostProfileApi(
                    context = context,
                    postId = postId,
                    title = title,
                    content = content,
                    flair = flair,
                    mediaUri = mediaUri,
                    mediaType = mediaType,
                    isHidden = isHidden,
                    allowComments = allowComments
                )
                isLoading = false
                if (response?.success == true) {
                    Toast.makeText(context, "Post updated successfully!", Toast.LENGTH_LONG).show()
                    refresh()
                } else {
                    errorMessage = response?.message
                    Toast.makeText(context, response?.message ?: "Unknown error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error: ${e.message}"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

//        try {
//            updatePostProfileApi(
//                context = context,
//                postId = postId,
//                title = title,
//                content = content,
//                flair = flair,
//                mediaUri = mediaUri,
//                mediaType = mediaType,
//                isHidden = isHidden,
//                allowComments = allowComments,
//                navToPage = navToPage,
//                navController = navController,
//                viewModel = this
//            )
//        } catch (e: Exception) {
//            isLoading = false
//            errorMessage = "Error: ${e.message}"
//        }
    }
}