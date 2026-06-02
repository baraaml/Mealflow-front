package com.example.mealflow.viewModel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.mealflow.network.UserProfile
import com.example.mealflow.network.updateUserProfileApi
import com.example.mealflow.utils.drawableToUri
import kotlinx.coroutines.launch

class UpdateProfileViewModel : ViewModel() {
    // UI State
    var isLoading by mutableStateOf(false)
    var isProfile by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    // Profile Data
    var name by mutableStateOf("")
    var lastName by mutableStateOf("")
    var birthDate by mutableStateOf("") // Format: "2000-01-01T00:00:00.000Z"
    var height by mutableStateOf<Int?>(null)
    var weight by mutableStateOf<Int?>(null)
    var gender by mutableStateOf("")
    var bio by mutableStateOf("")
    var profilePictureUri by mutableStateOf<Uri?>(null)
    var coverPictureUri by mutableStateOf<Uri?>(null)

    // Current user profile data
    var userProfile by mutableStateOf<UserProfile?>(null)

    // Reset all states
    fun resetState() {
        isLoading = false
        isProfile = false
        errorMessage = null
        successMessage = null

        // Reset to current profile values if available
        userProfile?.let { profile ->
            name = profile.name ?: ""
            lastName = profile.lastName ?: ""
            birthDate = profile.birthDate ?: ""
            height = profile.height
            weight = profile.weight
            gender = profile.gender
            bio = profile.bio ?: ""
        } ?: run {
            // If no profile available, reset to empty
            name = ""
            lastName = ""
            birthDate = ""
            height = null
            weight = null
            gender = ""
            bio = ""
        }

        profilePictureUri = null
        coverPictureUri = null
    }

    // Initialize with existing profile data
    fun initWithProfile(profile: UserProfile) {
        userProfile = profile
        name = profile.name ?: ""
        lastName = profile.lastName ?: ""
        birthDate = profile.birthDate ?: ""
        height = profile.height
        weight = profile.weight
        gender = profile.gender
        bio = profile.bio ?: ""
        // Note: we don't set profile/cover picture URIs here as they would be remote URLs
    }

    // Function to update the user profile
    fun updateProfile(
        context: Context,
        navController: NavController
    ) {
        // Data validation
        val heightInt = height
        val weightInt = weight

        // Height validation (optional)
        if (heightInt != null && (heightInt <= 0 || heightInt > 300)) {
            errorMessage = "Please enter a valid height"
            return
        }

        // Weight validation (optional)
        if (weightInt != null && (weightInt <= 0 || weightInt > 500)) {
            errorMessage = "Please enter a valid weight"
            return
        }

        // Set loading state
        isLoading = true
        errorMessage = null

        try {
            updateUserProfileApi(
                context = context,
                name = name.takeIf { it.isNotBlank() },
                lastName = lastName.takeIf { it.isNotBlank() },
                birthDate = birthDate.takeIf { it.isNotBlank() },
                height = heightInt,
                weight = weightInt,
                gender = gender.takeIf { it.isNotBlank() },
                bio = bio.takeIf { it.isNotBlank() },
                profilePictureUri = profilePictureUri,
                coverPictureUri = coverPictureUri,
                navController = navController,
                onSuccess = { updatedProfile ->
                    isLoading = false
                    successMessage = "Profile updated successfully"
                    userProfile = updatedProfile
                },
                onError = { error ->
                    isLoading = false
                    errorMessage = error
                },
                isProfile = isProfile
            )
        } catch (e: Exception) {
            // Only handle exceptions that occur before the API call
            isLoading = false
            errorMessage = "Error: ${e.message}"
        }
    }

    // Function to update profile picture
    fun setProfilePicture(uri: Uri?) {
        profilePictureUri = uri
    }

    // Function to update cover picture
    fun setCoverPicture(uri: Uri?) {
        coverPictureUri = uri
    }

    // Helper function to update numeric fields
    fun setHeight(value: String) {
        height = value.toIntOrNull()
    }

    fun setWeight(value: String) {
        weight = value.toIntOrNull()
    }

    // Clear pictures
    fun clearProfilePicture() {
        profilePictureUri = null
    }

    fun clearCoverPicture() {
        coverPictureUri = null
    }

    // Method to set profile picture from drawable
    fun setProfilePictureFromDrawable(context: Context, drawableResId: Int) {
        viewModelScope.launch {
            val uri = drawableToUri(context, drawableResId)
            if (uri != null) {
                profilePictureUri = uri
                successMessage = "Avatar selected"
            } else {
                errorMessage = "Failed to set avatar"
            }
        }
    }
}