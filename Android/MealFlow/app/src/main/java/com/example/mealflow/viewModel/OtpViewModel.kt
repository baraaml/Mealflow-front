package com.example.mealflow.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.mealflow.network.resetOtpApi
import com.example.mealflow.network.verifyEmailApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OtpViewModel : ViewModel() {
    // UI State - using Compose State like CreatePostViewModel
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // OTP Data
    var otp by mutableStateOf("")

    // Timer State
    var timer by mutableIntStateOf(60)
    var canResend by mutableStateOf(false)

    init {
        startTimer()
    }

    // Reset all states
    fun resetState() {
        isLoading = false
        errorMessage = null
        otp = ""
        timer = 60
        canResend = false
    }

    fun updateOtp(newOtp: String) {
        otp = newOtp
        errorMessage = null // Clear error when user types
    }

    fun verifyOtp(context: Context, email: String, navController: NavController) {
        // Validate OTP
        if (otp.isBlank() || otp.length != 6) {
            errorMessage = "Please enter the full OTP code.ً"
            return
        }

        // Set loading state
        isLoading = true
        errorMessage = null

        try {
            // Call the API function and let it handle the loading state
            verifyEmailApi(
                context = context,
                otp = otp,
                email = email,
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

    fun resendOtp(email: String, context: Context) {
        timer = 60
        canResend = false
        otp = "" // Clear current OTP
        errorMessage = null
        startTimer()

        // Call resend API if needed
        // resetOtpApi can be called here
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (timer > 0) {
                delay(1000L)
                timer--
            }
            canResend = true
        }
    }

    fun clearError() {
        errorMessage = null
    }
}