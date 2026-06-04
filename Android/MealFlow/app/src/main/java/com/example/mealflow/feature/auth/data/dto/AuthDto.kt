package com.example.mealflow.feature.auth.data.dto

import com.example.mealflow.core.data.network.dto.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(val username: String, val email: String, val password: String)

@Serializable
data class QuickLoginRequest(val refreshToken: String)

@Serializable
data class VerifyOtpRequest(val email: String, val otp: String)

@Serializable
data class ResendOtpRequest(val email: String)

@Serializable
data class ForgetPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(val token: String, val newPassword: String)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthDataDto? = null
)

@Serializable
data class AuthDataDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)
