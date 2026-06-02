package com.example.mealflow.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.network.DeletePostResponse
import com.example.mealflow.network.PostRepository
import kotlinx.coroutines.launch

class PostDropdownViewModel(application: Application) : AndroidViewModel(application) {
    var deleteResponse by mutableStateOf<DeletePostResponse?>(null)
    var isDeleting by mutableStateOf(false)
    var deleteError by mutableStateOf<String?>(null)

    private val postService = PostRepository(application)

    private val _dropdownStates = mutableMapOf<String, Boolean>()

    fun isExpanded(postId: String): Boolean {
        return _dropdownStates[postId] ?: false
    }

    fun toggleDropdown(postId: String) {
        _dropdownStates[postId] = !_dropdownStates.getOrDefault(postId, false)
    }

    fun deletePost(
        postId: String,
        refresh: () -> Unit
        ) {
        val context = getApplication<Application>().applicationContext
        isDeleting = true
        deleteError = null
        deleteResponse = null

        viewModelScope.launch {
            try {
                val response = postService.deletePost(postId)
                if (response != null && response.success) {
                    deleteResponse = response
                    refresh()
                } else {
                    deleteError = "Failed to delete community"
                }
            } catch (e: Exception) {
                deleteError = "Error: ${e.message}"
            } finally {
                isDeleting = false
            }
        }
    }
}
