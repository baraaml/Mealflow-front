package com.example.mealflow.ui.screens.setup

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import androidx.navigation.NavController
import com.example.mealflow.network.MyProfileApiService
import com.example.mealflow.network.updateUserProfileApi

class SetupProfileViewModel : ViewModel() {
    // Profile data states
    var name by mutableStateOf("")
    var lastName by mutableStateOf("")
    var birthDate by mutableStateOf("")
    var gender by mutableStateOf("")
    var height by mutableIntStateOf(0)
    var weight by mutableIntStateOf(0)
    var bio by mutableStateOf("")
    var profilePictureUri by mutableStateOf<Uri?>(null)
    var coverPictureUri by mutableStateOf<Uri?>(null)

    // UI states
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun setProfilePicture(uri: Uri?) {
        profilePictureUri = uri
    }

    fun setCoverPicture(uri: Uri?) {
        coverPictureUri = uri
    }

    fun clearProfilePicture() {
        profilePictureUri = null
    }

    fun clearCoverPicture() {
        coverPictureUri = null
    }

    fun setHeight(value: String) {
        height = value.toIntOrNull() ?: 0
    }

    fun setWeight(value: String) {
        weight = value.toIntOrNull() ?: 0
    }

    fun skipSetup(navController: NavController) {
        // Navigate to main app or home screen
        navController.navigate("setup_basic_info")
    }
    fun skipSetupBasicInfoScreen(navController: NavController) {
        // Navigate to main app or home screen
        navController.navigate("setup_physical_info")
    }
    fun skipSetupPhysicalInfoScreen(navController: NavController) {
        // Navigate to main app or home screen
        navController.navigate("setup_photos")
    }
    fun skipSetupPhotosScreen(navController: NavController) {
        // Navigate to main app or home screen
        navController.navigate("Questions Page")
    }

    fun completeSetup(context: Context, navController: NavController) {
        viewModelScope.launch {
            isLoading = true
            try {
                // Call the same API as update profile
                val result = updateUserProfileApi(
                    context = context,
                    name = name,
                    lastName = lastName,
                    birthDate = birthDate,
                    gender = gender,
                    height = if (height > 0) height else null,
                    weight = if (weight > 0) weight else null,
                    bio = bio,
                    profilePictureUri = profilePictureUri,
                    coverPictureUri = coverPictureUri,
                    navController = navController
                )

                if (result?.success?:true) {
                    successMessage = "Profile setup completed successfully!"
                    // Navigate to main app
                    navController.navigate("Questions Page")
                } else {
                    errorMessage = result?.message ?: "Failed to setup profile"
                }
            } catch (e: Exception) {
                errorMessage = "Error setting up profile: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    fun basicInfoSetup(context: Context, navController: NavController) {
        viewModelScope.launch {
            isLoading = true
            try {
                // Call the same API as update profile
                val result = updateUserProfileApi(
                    context = context,
                    name = name,
                    lastName = lastName,
                    birthDate = birthDate,
                    gender = gender,
                    height = null,
                    weight = null,
                    bio = null,
                    profilePictureUri = null,
                    coverPictureUri = null,
                    navController = navController
                )

                if (result?.success ?: true) {
                    successMessage = "Profile setup completed successfully!"
                    // Navigate to main app
                    navController.navigate("Questions Page")
                } else {
                    errorMessage = result?.message ?: "Failed to setup profile"
                }
            } catch (e: Exception) {
                errorMessage = "Error setting up profile: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    fun physicalInfoSetup(context: Context, navController: NavController) {
        viewModelScope.launch {
            isLoading = true
            try {
                // Call the same API as update profile
                val result = updateUserProfileApi(
                    context = context,
                    name = null,
                    lastName = null,
                    birthDate = null,
                    gender = null,
                    height = if (height > 0) height else null,
                    weight = if (weight > 0) weight else null,
                    bio = null,
                    profilePictureUri = null,
                    coverPictureUri = null,
                    navController = navController
                )

                if (result?.success ?: true) {
                    successMessage = "Profile setup completed successfully!"
                    // Navigate to main app
                    navController.navigate("Questions Page")
                } else {
                    errorMessage = result?.message ?: "Failed to setup profile"
                }
            } catch (e: Exception) {
                errorMessage = "Error setting up profile: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    fun photoInfoSetup(context: Context, navController: NavController) {
        viewModelScope.launch {
            isLoading = true
            try {
                // Call the same API as update profile
                val result = updateUserProfileApi(
                    context = context,
                    name = null,
                    lastName = null,
                    birthDate = null,
                    gender = null,
                    height = null,
                    weight = null,
                    bio = bio,
                    profilePictureUri = profilePictureUri,
                    coverPictureUri = coverPictureUri,
                    navController = navController
                )

                if (result?.success ?: true) {
                    successMessage = "Profile setup completed successfully!"
                    // Navigate to main app
                    navController.navigate("Questions Page")
                } else {
                    errorMessage = result?.message ?: "Failed to setup profile"
                }
            } catch (e: Exception) {
                errorMessage = "Error setting up profile: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
