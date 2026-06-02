////package com.example.mealflow.utils.googleSignIn
////
////import android.content.Context
////import androidx.compose.runtime.getValue
////import androidx.compose.runtime.mutableStateOf
////import androidx.compose.runtime.setValue
////import androidx.lifecycle.LiveData
////import androidx.lifecycle.MutableLiveData
////import androidx.lifecycle.ViewModel
////import androidx.lifecycle.viewModelScope
////import androidx.navigation.NavController
////import kotlinx.coroutines.launch
////
////class GoogleSignInViewModel : ViewModel() {
////
////    // Compose state for loading
////    private var _isLoading by mutableStateOf(false)
////    val isLoading: Boolean get() = _isLoading
////
////    // Compose states for error and success
////    var errorMessage by mutableStateOf<String?>(null)
////        private set
////
////    var isSuccess by mutableStateOf(false)
////        private set
////
////    // Track if user is new for navigation purposes
////    var isNewUser by mutableStateOf(false)
////        private set
////
////    // LiveData for navigation - needed for your UI screens
////    private val _navigateToHome = MutableLiveData<Boolean>()
////    val navigateToHome: LiveData<Boolean> get() = _navigateToHome
////
////    private val _navigateToSetup = MutableLiveData<Boolean>()
////    val navigateToSetup: LiveData<Boolean> get() = _navigateToSetup
////
////    fun resetState() {
////        _isLoading = false
////        errorMessage = null
////        isSuccess = false
////        isNewUser = false
////    }
////
////    fun signInWithGoogle(
////        context: Context,
////        firebaseIdToken: String,
////        navController: NavController
////    ) {
////        _isLoading = true
////        errorMessage = null
////        isSuccess = false
////
////        viewModelScope.launch {
////            firebaseGoogleLogin(
////                context = context,
////                firebaseIdToken = firebaseIdToken,
////                navController = navController,
////                viewModel = this@GoogleSignInViewModel
////            )
////        }
////    }
////
////    // Public method to set loading state - can be called from UI
////    fun setLoading(loading: Boolean) {
////        _isLoading = loading
////    }
////
////    // Internal method for error handling
////    internal fun setError(message: String?) {
////        errorMessage = message
////        _isLoading = false
////    }
////
////    // Updated internal method for success handling with user status
////    internal fun setSuccess(isNewUser: Boolean = false) {
////        isSuccess = true
////        _isLoading = false
////        errorMessage = null
////        this.isNewUser = isNewUser
////    }
////
////    // Public methods to trigger navigation from API
////    fun setNavigateToHome() {
////        _navigateToHome.postValue(true)
////    }
////
////    fun setNavigateToSetup() {
////        _navigateToSetup.postValue(true)
////    }
////
////    // Method to reset navigation state after navigation is complete
////    fun onHomeNavigationComplete() {
////        _navigateToHome.value = false
////    }
////
////    fun onSetupNavigationComplete() {
////        _navigateToSetup.value = false
////    }
////
////    // Alternative method to save token and navigate (for backwards compatibility)
////    fun saveToken(token: String) {
////        _isLoading = false
////        _navigateToHome.postValue(true)
////    }
////}
//package com.example.mealflow.utils.googleSignIn
//
//import android.content.Context
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import androidx.navigation.NavController
//import kotlinx.coroutines.launch
//
//class GoogleSignInViewModel : ViewModel() {
//
//    // Compose state for loading
//    private var _isLoading by mutableStateOf(false)
//    val isLoading: Boolean get() = _isLoading
//
//    // Compose states for error and success
//    var errorMessage by mutableStateOf<String?>(null)
//        private set
//
//    var isSuccess by mutableStateOf(false)
//        private set
//
//    // Track if user is new for navigation purposes
//    var isNewUser by mutableStateOf(false)
//        private set
//
//    fun resetState() {
//        _isLoading = false
//        errorMessage = null
//        isSuccess = false
//        isNewUser = false
//    }
//
//    fun signInWithGoogle(
//        context: Context,
//        firebaseIdToken: String,
//        navController: NavController
//    ) {
//        _isLoading = true
//        errorMessage = null
//        isSuccess = false
//
//        viewModelScope.launch {
//            firebaseGoogleLogin(
//                context = context,
//                firebaseIdToken = firebaseIdToken,
//                navController = navController,
//                viewModel = this@GoogleSignInViewModel
//            )
//        }
//    }
//
//    // Public method to set loading state - can be called from UI
//    fun setLoading(loading: Boolean) {
//        _isLoading = loading
//    }
//
//    // Internal method for error handling
//    internal fun setError(message: String?) {
//        errorMessage = message
//        _isLoading = false
//    }
//
//    // Updated internal method for success handling with user status
//    internal fun setSuccess(isNewUser: Boolean = false) {
//        isSuccess = true
//        _isLoading = false
//        errorMessage = null
//        this.isNewUser = isNewUser
//    }
//}
package com.example.mealflow.utils.googleSignIn

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch

class GoogleSignInViewModel : ViewModel() {

    // Compose state for loading
    private var _isLoading by mutableStateOf(false)
    val isLoading: Boolean get() = _isLoading

    // Compose states for error and success
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isSuccess by mutableStateOf(false)
        private set

    // Track if user is new for navigation purposes
    var isNewUser by mutableStateOf(false)
        private set

    fun resetState() {
        _isLoading = false
        errorMessage = null
        isSuccess = false
        isNewUser = false
    }

    fun signInWithGoogle(
        context: Context,
        firebaseIdToken: String,
        navController: NavController
    ) {
        _isLoading = true
        errorMessage = null
        isSuccess = false

        viewModelScope.launch {
            firebaseGoogleLogin(
                context = context,
                firebaseIdToken = firebaseIdToken,
                navController = navController,
                viewModel = this@GoogleSignInViewModel
            )
        }
    }

    // Public method to set loading state - can be called from UI
    fun setLoading(loading: Boolean) {
        _isLoading = loading
    }

    // Internal method for error handling
    internal fun setError(message: String?) {
        errorMessage = message
        _isLoading = false
    }

    // Updated internal method for success handling with user status
    internal fun setSuccess(isNewUser: Boolean = false) {
        isSuccess = true
        _isLoading = false
        errorMessage = null
        this.isNewUser = isNewUser
    }
}