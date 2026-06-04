package com.example.mealflow.feature.auth.presentation.otp

import com.example.mealflow.core.presentation.util.UiText

data class OtpState(
    val email: String = "",
    val otp: String = "",
    val timer: Int = 60,
    val canResend: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiText? = null
)
