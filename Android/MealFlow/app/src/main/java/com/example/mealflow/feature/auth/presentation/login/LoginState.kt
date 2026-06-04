package com.example.mealflow.feature.auth.presentation.login

import com.example.mealflow.core.presentation.util.UiText

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiText? = null
)
