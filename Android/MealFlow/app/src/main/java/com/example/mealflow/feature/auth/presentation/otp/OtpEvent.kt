package com.example.mealflow.feature.auth.presentation.otp

import com.example.mealflow.core.presentation.util.UiText

sealed interface OtpEvent {
    data object VerificationSuccess : OtpEvent
    data class Error(val error: UiText) : OtpEvent
}
