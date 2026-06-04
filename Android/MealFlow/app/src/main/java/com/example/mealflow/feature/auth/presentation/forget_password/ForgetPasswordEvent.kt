package com.example.mealflow.feature.auth.presentation.forget_password

import com.example.mealflow.core.presentation.util.UiText

sealed interface ForgetPasswordEvent {
    data class Success(val email: String) : ForgetPasswordEvent
    data class Error(val error: UiText) : ForgetPasswordEvent
}
