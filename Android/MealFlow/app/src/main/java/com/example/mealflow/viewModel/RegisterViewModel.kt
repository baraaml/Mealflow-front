package com.example.mealflow.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.mealflow.network.registerUser
import com.example.mealflow.utils.Validator

class RegisterViewModel : ViewModel() {
    // UI State - using Compose State like OtpViewModel
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Form Data - keeping existing LiveData structure
    private var _username = MutableLiveData("")
    val username: LiveData<String> get() = _username

    private var _email = MutableLiveData("")
    val email: LiveData<String> get() = _email

    private var _password = MutableLiveData("")
    val password: LiveData<String> get() = _password

    private var _repassword = MutableLiveData("")
    val repassword: LiveData<String> get() = _repassword

    private var _passwordVisible = MutableLiveData(false)
    val passwordVisible: LiveData<Boolean> get() = _passwordVisible

    // Reset all states
    fun resetState() {
        isLoading = false
        errorMessage = null
        _username.value = ""
        _email.value = ""
        _password.value = ""
        _repassword.value = ""
        _passwordVisible.value = false
    }

    fun updateUsername(newUsername: String) {
        _username.value = newUsername
        errorMessage = null // Clear error when user types
    }

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        errorMessage = null
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        errorMessage = null
    }

    fun updateRepassword(newRepassword: String) {
        _repassword.value = newRepassword
        errorMessage = null
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = _passwordVisible.value?.not()
    }

    fun setEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun getEmail(): String {
        return _email.value ?: ""
    }

    fun validateInputs(username: String, email: String, password: String): Boolean {
        val errors = mutableListOf<String>()

        val usernameError = Validator.validateUsername(username)
        val emailError = Validator.validateEmail(email)
        val passwordError = Validator.validatePassword(password)

        usernameError?.let { errors.add(it) }
        emailError?.let { errors.add(it) }
        passwordError?.let { errors.add(it) }

        return true // ✅ No errors, you can continue
    }

    fun register(context: Context, navController: NavController) {
        val currentUsername = _username.value ?: ""
        val currentEmail = _email.value ?: ""
        val currentPassword = _password.value ?: ""
        val currentRepassword = _repassword.value ?: ""

        // Validate form data using existing validator
        if (!validateInputs(currentUsername, currentEmail, currentPassword)) {
            return
        }

//        if (currentPassword != currentRepassword) {
//            errorMessage = "Passwords do not match"
//            return
//        }

        // Set loading state
        isLoading = true
        errorMessage = null

        try {
            // Call the API function and let it handle the loading state
            registerUser(
                context = context,
                username = currentUsername,
                email = currentEmail,
                password = currentPassword,
                navController = navController,
                viewModel = this // Pass the ViewModel
            )
            // Note: We don't set isLoading = false here because the API function will handle it
            // after it completes its operations or encounters errors
        } catch (e: Exception) {
            // Only handle exceptions that occur before the API call
            isLoading = false
            errorMessage = "Error: ${e.message}"
        }
    }

    fun clearError() {
        errorMessage = null
    }
}