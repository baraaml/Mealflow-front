//package com.example.mealflow.viewModel
//
//import android.net.Uri
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//
//class CreateCommunityViewModel : ViewModel() {
//    private var _communityName = MutableLiveData("")
//    val communityName: LiveData<String> get() = _communityName
//
//    fun updateCommunityName(newName: String) {
//        _communityName.value = newName
//    }
//
//    private var _communityDescription = MutableLiveData("")
//    val communityDescription: LiveData<String> get() = _communityDescription
//
//    fun updateCommunityDescription(newDescription: String) {
//        _communityDescription.value = newDescription
//    }
//
//    private var _recipeCreationPermission = MutableLiveData("")
//    val recipeCreationPermission: LiveData<String> get() = _recipeCreationPermission
//
//    fun updateRecipeCreationPermission(permission: String) {
//        _recipeCreationPermission.value = permission
//    }
//
//    private var _categories = MutableLiveData<List<String>>(emptyList())
//    val categories: LiveData<List<String>> get() = _categories
//
//    fun updateCategories(newCategories: List<String>) {
//        _categories.value = newCategories
//    }
//
//    var selectedImageUri by mutableStateOf<Uri?>(null)
//        private set
//
//    fun setImageUri(uri: Uri?) {
//        selectedImageUri = uri
//    }
//}
//
//package com.example.mealflow.viewModel
//
//import android.content.Context
//import android.net.Uri
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.navigation.NavController
//import com.example.mealflow.network.createCommunityApi
//
//class CreateCommunityViewModel : ViewModel() {
//    // UI State - Loading and Error handling (similar to CreatePostViewModel)
//    var isLoading by mutableStateOf(false)
//    var errorMessage by mutableStateOf<String?>(null)
//
//    private var _communityName = MutableLiveData("")
//    val communityName: LiveData<String> get() = _communityName
//
//    fun updateCommunityName(newName: String) {
//        _communityName.value = newName
//    }
//
//    private var _communityDescription = MutableLiveData("")
//    val communityDescription: LiveData<String> get() = _communityDescription
//
//    fun updateCommunityDescription(newDescription: String) {
//        _communityDescription.value = newDescription
//    }
//
//    private var _recipeCreationPermission = MutableLiveData("")
//    val recipeCreationPermission: LiveData<String> get() = _recipeCreationPermission
//
//    fun updateRecipeCreationPermission(permission: String) {
//        _recipeCreationPermission.value = permission
//    }
//
//    private var _categories = MutableLiveData<List<String>>(emptyList())
//    val categories: LiveData<List<String>> get() = _categories
//
//    fun updateCategories(newCategories: List<String>) {
//        _categories.value = newCategories
//    }
//
//    var selectedImageUri by mutableStateOf<Uri?>(null)
//        private set
//
//    fun setImageUri(uri: Uri?) {
//        selectedImageUri = uri
//    }
//
//    // Reset all states
//    fun resetState() {
//        isLoading = false
//        errorMessage = null
//        _communityName.value = ""
//        _communityDescription.value = ""
//        _recipeCreationPermission.value = ""
//        _categories.value = emptyList()
//        selectedImageUri = null
//    }
//
//    // Function to create community
//    fun createCommunity(
//        context: Context,
//        accessToken: String,
//        navController: NavController
//    ) {
//        // Validate required fields
//        val name = _communityName.value.orEmpty()
//        val description = _communityDescription.value.orEmpty()
//        val permission = _recipeCreationPermission.value.orEmpty()
//        val categoriesList = _categories.value.orEmpty()
//
//        if (name.isBlank() || description.isBlank() || permission.isBlank()) {
//            errorMessage = "Please fill in all required fields"
//            return
//        }
//
//        // Set loading state
//        isLoading = true
//        errorMessage = null
//
//        try {
//            createCommunityApi(
//                context = context,
//                name = name,
//                description = description,
//                recipeCreationPermission = permission,
//                accessToken = accessToken,
//                categories = categoriesList,
//                imageUri = selectedImageUri,
//                navController = navController
//            )
//            // Note: isLoading will be set to false in the API function after completion
//        } catch (e: Exception) {
//            // Only handle exceptions that occur before the API call
//            isLoading = false
//            errorMessage = "Error: ${e.message}"
//        }
//    }
//
//    // Function to clear selected image
//    fun clearImage() {
//        selectedImageUri = null
//    }
//}
package com.example.mealflow.viewModel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.mealflow.network.createCommunityApi

class CreateCommunityViewModel : ViewModel() {
    // UI State - Loading and Error handling (similar to CreatePostViewModel)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private var _communityName = MutableLiveData("")
    val communityName: LiveData<String> get() = _communityName

    fun updateCommunityName(newName: String) {
        _communityName.value = newName
    }

    private var _communityDescription = MutableLiveData("")
    val communityDescription: LiveData<String> get() = _communityDescription

    fun updateCommunityDescription(newDescription: String) {
        _communityDescription.value = newDescription
    }

//    private var _recipeCreationPermission = MutableLiveData("")
//    val recipeCreationPermission: LiveData<String> get() = _recipeCreationPermission

//    fun updateRecipeCreationPermission(permission: String) {
//        _recipeCreationPermission.value = permission
//    }

    private var _categories = MutableLiveData<List<String>>(emptyList())
    val categories: LiveData<List<String>> get() = _categories

    fun updateCategories(newCategories: List<String>) {
        _categories.value = newCategories
    }

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    fun setImageUri(uri: Uri?) {
        selectedImageUri = uri
    }

    // Reset all states
    fun resetState() {
        isLoading = false
        errorMessage = null
        _communityName.value = ""
        _communityDescription.value = ""
//        _recipeCreationPermission.value = ""
        _categories.value = emptyList()
        selectedImageUri = null
    }

    // Function to create community
    fun createCommunity(
        context: Context,
        accessToken: String,
        navController: NavController
    ) {
        // Validate required fields
        val name = _communityName.value.orEmpty()
        val description = _communityDescription.value.orEmpty()
//        val permission = _recipeCreationPermission.value.orEmpty()
        val categoriesList = _categories.value.orEmpty()

//        if (name.isBlank() || description.isBlank() || permission.isBlank()) {
//            errorMessage = "Please fill in all required fields"
//            return
//        }
        if (name.isBlank() || description.isBlank() ) {
            errorMessage = "Please fill in all required fields"
            return
        }

        // Set loading state
        isLoading = true
        errorMessage = null

        try {
            createCommunityApi(
                context = context,
                name = name,
                description = description,
//                recipeCreationPermission = permission,
                accessToken = accessToken,
                categories = categoriesList,
                imageUri = selectedImageUri,
                navController = navController,
                viewModel = this // ✅ Pass the viewModel instance
            )
            // Note: isLoading will be set to false in the API function after completion
        } catch (e: Exception) {
            // Only handle exceptions that occur before the API call
            isLoading = false
            errorMessage = "Error: ${e.message}"
        }
    }

    // Function to clear selected image
    fun clearImage() {
        selectedImageUri = null
    }
}