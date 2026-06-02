package com.example.mealflow.viewModel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.mealflow.network.updateCommunityApi
import com.example.mealflow.data.model.SingleCommunity
import com.example.mealflow.ui.screens.createCommunity.getCategories
import com.example.mealflow.utils.drawableToUri
import kotlinx.coroutines.launch

class UpdateCommunityViewModel : ViewModel() {
    // UI State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    // Community Data
    var communityId by mutableStateOf("")
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var privacy by mutableStateOf("PUBLIC") // "PUBLIC", "PRIVATE", "RESTRICTED"
    var mealCreationPermission by mutableStateOf("ANY_MEMBER") // "ANY_MEMBER", "ADMIN_ONLY"
    var categories by mutableStateOf<List<String>>(emptyList())
    var imageUri by mutableStateOf<Uri?>(null)

    // Current community data - now using UpdatedCommunity for API responses
    var updatedCommunity by mutableStateOf<SingleCommunity?>(null)
    var singleCommunity by mutableStateOf<SingleCommunity?>(null)

    // Categories - changed to use fixed categories
    var availableCategories by mutableStateOf<Map<String, List<String>>>(emptyMap())
    var selectedCategories by mutableStateOf<List<String>>(emptyList())

    // Privacy options for dropdown
    val privacyOptions = listOf("PUBLIC", "PRIVATE", "RESTRICTED")
    val mealCreationOptions = listOf("ANY_MEMBER", "ADMIN_ONLY")

    // Initialize categories from getCategories function
    fun initializeCategories() {
        availableCategories = getCategories()
    }

    // Reset all states
    fun resetState() {
        isLoading = false
        errorMessage = null
        successMessage = null

        // Reset to current community values if available
        when {
            singleCommunity != null -> {
                singleCommunity?.let { comm ->
                    communityId = comm.id
                    name = comm.name
                    description = comm.description
                    privacy = comm.privacy
                    mealCreationPermission = comm.mealCreationPermission
                    selectedCategories = comm.categories.map { it.name }
                }
            }
            updatedCommunity != null -> {
                updatedCommunity?.let { comm ->
                    communityId = comm.id
                    name = comm.name
                    description = comm.description
                    privacy = comm.privacy
                    mealCreationPermission = comm.mealCreationPermission ?: "ANY_MEMBER"
                    selectedCategories = comm.categories?.map { it.name } ?: emptyList()
                }
            }
            else -> {
                // Reset to empty
                communityId = ""
                name = ""
                description = ""
                privacy = "PUBLIC"
                mealCreationPermission = "ANY_MEMBER"
                selectedCategories = emptyList()
            }
        }

        imageUri = null
    }

    // Initialize with existing community data from UpdatedCommunity
    fun initWithUpdatedCommunity(existingCommunity: SingleCommunity) {
        updatedCommunity = existingCommunity
        singleCommunity = null // Clear single community if setting updated community
        communityId = existingCommunity.id
        name = existingCommunity.name
        description = existingCommunity.description
        privacy = existingCommunity.privacy
        mealCreationPermission = existingCommunity.mealCreationPermission ?: "ANY_MEMBER"
        selectedCategories = existingCommunity.categories?.map { it.name } ?: emptyList()
        // Note: we don't set image URI here as it would be a remote URL
    }

    // Initialize with SingleCommunity data
    fun initWithSingleCommunity(existingSingleCommunity: SingleCommunity) {
        singleCommunity = existingSingleCommunity
        updatedCommunity = null // Clear updated community if setting single community
        communityId = existingSingleCommunity.id
        name = existingSingleCommunity.name
        description = existingSingleCommunity.description
        privacy = existingSingleCommunity.privacy
        mealCreationPermission = existingSingleCommunity.mealCreationPermission
        selectedCategories = existingSingleCommunity.categories.map { it.name }
        // Note: we don't set image URI here as it would be a remote URL
    }

    // Get current community image URL
    fun getCurrentImageUrl(): String? {
        return when {
            singleCommunity != null -> singleCommunity?.image?.takeIf { it.isNotBlank() }
            updatedCommunity != null -> updatedCommunity?.image?.takeIf { it.isNotBlank() }
            else -> null
        }
    }

    // Check if user can edit this community (only for SingleCommunity)
    fun canEditCommunity(): Boolean {
        return singleCommunity?.isAdmin == true
    }

    // Get member count
    fun getMemberCount(): Int {
        return when {
            singleCommunity != null -> singleCommunity?._count?.members ?: 0
            updatedCommunity != null -> updatedCommunity?._count?.members ?: 0
            else -> 0
        }
    }

    // Get posts count (only available in SingleCommunity)
    fun getPostsCount(): Int {
        return singleCommunity?._count?.posts ?: 0
    }

    // Update selected categories from UI
    fun updateSelectedCategories(categories: List<String>) {
        // Validate that all selected categories are from available categories
        val allAvailableCategories = availableCategories.values.flatten()
        selectedCategories = categories.filter { allAvailableCategories.contains(it) }
    }

    // Function to update the community
    fun updateCommunity(
        context: Context,
        navController: NavController,
        onUpdateComplete: ((SingleCommunity) -> Unit)? = null // Added callback for external handling
    ) {
        // Check if user has permission to edit (for SingleCommunity)
        if (singleCommunity != null && !canEditCommunity()) {
            errorMessage = "You don't have permission to edit this community"
            return
        }

        // Data validation
        if (name.isBlank()) {
            errorMessage = "Community name is required"
            return
        }

        if (name.length < 3 || name.length > 50) {
            errorMessage = "Community name must be between 3 and 50 characters"
            return
        }

        if (communityId.isBlank()) {
            errorMessage = "Community ID is required"
            return
        }

        // Use the selected categories list directly
        val categoryList = selectedCategories.filter { it.isNotBlank() }

        // Set loading state
        isLoading = true
        errorMessage = null

        try {
            updateCommunityApi(
                context = context,
                communityId = communityId,
                name = name.takeIf { it.isNotBlank() },
                description = description.takeIf { it.isNotBlank() },
                imageUri = imageUri,
                privacy = privacy,
//                mealCreationPermission = mealCreationPermission,
                categories = categoryList.takeIf { it.isNotEmpty() },
                navController = navController,
                onSuccess = { returnedUpdatedCommunity ->
                    isLoading = false
                    successMessage = "Community updated successfully"

                    // Update the internal state with the returned community
                    updatedCommunity = returnedUpdatedCommunity
                    initWithUpdatedCommunity(returnedUpdatedCommunity)

                    imageUri = null // Clear the selected image after successful update

                    // Call external callback if provided
                    onUpdateComplete?.invoke(returnedUpdatedCommunity)
                },
                onError = { error ->
                    isLoading = false
                    errorMessage = error
                }
            )
        } catch (e: Exception) {
            // Only handle exceptions that occur before the API call
            isLoading = false
            errorMessage = "Error: ${e.message}"
        }
    }

    // Function to update community image
    fun setCommunityImage(uri: Uri?) {
        imageUri = uri
    }

    // Clear community image
    fun clearCommunityImage() {
        imageUri = null
    }

    // Method to set community image from drawable
    fun setCommunityImageFromDrawable(context: Context, drawableResId: Int) {
        viewModelScope.launch {
            val uri = drawableToUri(context, drawableResId)
            if (uri != null) {
                imageUri = uri
                successMessage = "Community image selected"
            } else {
                errorMessage = "Failed to set community image"
            }
        }
    }

    // Add a category - only if it exists in available categories
    fun addCategory(category: String) {
        val allAvailableCategories = availableCategories.values.flatten()
        if (allAvailableCategories.contains(category) && !selectedCategories.contains(category)) {
            selectedCategories = selectedCategories + category
        }
    }

    // Remove a category
    fun removeCategory(category: String) {
        selectedCategories = selectedCategories.filter { it != category }
    }

    // Clear all categories
    fun clearCategories() {
        selectedCategories = emptyList()
    }

    // Get available categories for a specific group
    fun getCategoriesForGroup(groupName: String): List<String> {
        return availableCategories[groupName] ?: emptyList()
    }

    // Get all available categories as a flat list
    fun getAllAvailableCategories(): List<String> {
        return availableCategories.values.flatten()
    }

    // Check if a category is available
    fun isCategoryAvailable(category: String): Boolean {
        return getAllAvailableCategories().contains(category)
    }

    // Get selected categories count
    fun getSelectedCategoriesCount(): Int {
        return selectedCategories.size
    }

    // Update privacy setting
    fun updatePrivacy(newPrivacy: String) {
        if (privacyOptions.contains(newPrivacy)) {
            privacy = newPrivacy
        }
    }

    // Update meal creation permission
    fun updateMealCreationPermission(newPermission: String) {
        if (mealCreationOptions.contains(newPermission)) {
            mealCreationPermission = newPermission
        }
    }

    // Clear error message
    fun clearErrorMessage() {
        errorMessage = null
    }

    // Clear success message
    fun clearSuccessMessage() {
        successMessage = null
    }

    // Validate form data
    fun isFormValid(): Boolean {
        return name.isNotBlank() &&
                name.length >= 3 &&
                name.length <= 50 &&
                communityId.isNotBlank() &&
                privacyOptions.contains(privacy) &&
                mealCreationOptions.contains(mealCreationPermission)
    }

    // Get display text for privacy setting
    fun getPrivacyDisplayText(): String {
        return when (privacy) {
            "PUBLIC" -> "Public - Anyone can join"
            "PRIVATE" -> "Private - Invite only"
            "RESTRICTED" -> "Restricted - Admin approval required"
            else -> privacy
        }
    }

    // Get display text for meal creation permission
    fun getMealCreationDisplayText(): String {
        return when (mealCreationPermission) {
            "ANY_MEMBER" -> "Any Member"
            "ADMIN_ONLY" -> "Admin Only"
            else -> mealCreationPermission
        }
    }
}