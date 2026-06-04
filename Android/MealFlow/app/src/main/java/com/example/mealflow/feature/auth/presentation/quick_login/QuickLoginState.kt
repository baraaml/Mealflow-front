package com.example.mealflow.feature.auth.presentation.quick_login

import com.example.mealflow.core.presentation.util.UiText

data class QuickLoginState(
    val isLoading: Boolean = false,
    val error: UiText? = null
)
