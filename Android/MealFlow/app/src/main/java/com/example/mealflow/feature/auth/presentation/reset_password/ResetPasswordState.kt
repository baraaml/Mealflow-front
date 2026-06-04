package com.example.mealflow.feature.auth.presentation.reset_password

import com.example.mealflow.core.presentation.util.UiText

data class ResetPasswordState(
    val token: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiText? = null
)
