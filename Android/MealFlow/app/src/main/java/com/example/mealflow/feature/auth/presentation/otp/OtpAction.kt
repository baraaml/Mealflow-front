package com.example.mealflow.feature.auth.presentation.otp

sealed interface OtpAction {
    data class OnOtpChange(val otp: String) : OtpAction
    data object OnVerifyClick : OtpAction
    data object OnResendClick : OtpAction
}
