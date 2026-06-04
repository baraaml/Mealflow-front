package com.example.mealflow.feature.auth.presentation.reset_password

sealed interface ResetPasswordAction {
    data class OnPasswordChange(val password: String) : ResetPasswordAction
    data class OnConfirmPasswordChange(val confirmPassword: String) : ResetPasswordAction
    data object OnTogglePasswordVisibility : ResetPasswordAction
    data object OnSubmitClick : ResetPasswordAction
}
