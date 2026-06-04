package com.example.mealflow.feature.auth.presentation.reset_password

import com.example.mealflow.core.presentation.util.UiText

sealed interface ResetPasswordEvent {
    data object Success : ResetPasswordEvent
    data class Error(val error: UiText) : ResetPasswordEvent
}
