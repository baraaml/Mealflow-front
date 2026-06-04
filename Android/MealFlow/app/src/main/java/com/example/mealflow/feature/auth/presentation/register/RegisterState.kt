package com.example.mealflow.feature.auth.presentation.register

import com.example.mealflow.core.presentation.util.UiText

data class RegisterState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiText? = null
)
