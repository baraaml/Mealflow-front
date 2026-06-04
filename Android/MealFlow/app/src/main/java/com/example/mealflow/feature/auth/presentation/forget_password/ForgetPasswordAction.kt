package com.example.mealflow.feature.auth.presentation.forget_password

sealed interface ForgetPasswordAction {
    data class OnEmailChange(val email: String) : ForgetPasswordAction
    data object OnSubmitClick : ForgetPasswordAction
}
