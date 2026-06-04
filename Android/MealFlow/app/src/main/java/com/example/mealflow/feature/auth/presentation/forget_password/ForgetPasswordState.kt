package com.example.mealflow.feature.auth.presentation.forget_password

import com.example.mealflow.core.presentation.util.UiText

data class ForgetPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null
)
