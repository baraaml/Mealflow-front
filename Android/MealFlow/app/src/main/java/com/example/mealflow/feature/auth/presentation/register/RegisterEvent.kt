package com.example.mealflow.feature.auth.presentation.register

import com.example.mealflow.core.presentation.util.UiText

sealed interface RegisterEvent {
    data class RegisterSuccess(val email: String) : RegisterEvent
    data class Error(val error: UiText) : RegisterEvent
}
