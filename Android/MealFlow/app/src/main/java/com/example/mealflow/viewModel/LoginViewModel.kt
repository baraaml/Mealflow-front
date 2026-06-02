package com.example.mealflow.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.mealflow.network.loginApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private var _email = MutableLiveData("")
    val email: LiveData<String> get() = _email

    private var _password = MutableLiveData("")
    val password: LiveData<String> get() = _password

    private var _passwordVisible = MutableLiveData(false)
    val passwordVisible: LiveData<Boolean> get() = _passwordVisible

    // Add a new LiveData to track navigation to home screen
    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean> get() = _navigateToHome

    // Loading state as mutableStateOf for Composable observation
    var isLoading by mutableStateOf(false)
        private set

    // This will be set to true when login is successful to trigger data fetching
    private val _loginSuccessful = MutableLiveData<Boolean>()
    val loginSuccessful: LiveData<Boolean> get() = _loginSuccessful

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun loginUser(context: Context, email: String, password: String, navController: NavController, snackbarHostState: SnackbarHostState) {
        // Set loading state to true at the beginning of the login process
        isLoading = true

        viewModelScope.launch {
            try {
                // Call the login API without passing ViewModel
                val loginResult = loginApi(
                    context = context,
                    email = email,
                    password = password,
                    snackbarHostState = snackbarHostState
                )

                // Handle the result based on success/failure
                if (loginResult.isSuccess) {
                    // Mark login as successful to trigger data fetching
                    _loginSuccessful.postValue(true)

                    // Navigate to home screen on success
                    delay(150) // Small delay to ensure smooth transition
                    _navigateToHome.postValue(true)
                }

            } catch (e: Exception) {
                // Handle error - loading will be set to false in finally block
                Log.e("LoginViewModel", "Login failed: ${e.localizedMessage}")
            } finally {
                // Always set loading to false, whether success or failure
                isLoading = false
            }
        }
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = _passwordVisible.value?.not()
    }

    // Add a function to reset navigation flag after navigation completes
    fun onHomeNavigationComplete() {
        _navigateToHome.value = false
    }

    // Reset the login successful flag (useful for logout or when data refresh is complete)
    fun resetLoginState() {
        _loginSuccessful.value = false
    }
}