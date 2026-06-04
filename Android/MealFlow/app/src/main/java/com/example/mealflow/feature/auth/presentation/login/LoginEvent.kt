package com.example.mealflow.feature.auth.presentation.login

import com.example.mealflow.core.presentation.util.UiText

sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent
    data class Error(val error: UiText) : LoginEvent
}
