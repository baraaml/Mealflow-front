package com.example.mealflow.feature.auth.presentation.register

sealed interface RegisterAction {
    data class OnUsernameChange(val username: String) : RegisterAction
    data class OnEmailChange(val email: String) : RegisterAction
    data class OnPasswordChange(val password: String) : RegisterAction
    data object OnTogglePasswordVisibility : RegisterAction
    data object OnRegisterClick : RegisterAction
}
