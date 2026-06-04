package com.example.mealflow.feature.auth.presentation.quick_login

import com.example.mealflow.core.presentation.util.UiText

sealed interface QuickLoginEvent {
    data object QuickLoginSuccess : QuickLoginEvent
    data class Error(val error: UiText) : QuickLoginEvent
}
