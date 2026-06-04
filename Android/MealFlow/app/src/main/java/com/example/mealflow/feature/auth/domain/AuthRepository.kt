package com.example.mealflow.feature.auth.domain

import com.example.mealflow.core.domain.util.Result
import com.example.mealflow.core.domain.util.DataError

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit, DataError.Network>
    suspend fun quickLogin(refreshToken: String): Result<Unit, DataError.Network>
    suspend fun register(username: String, email: String, password: String): Result<Unit, DataError.Network>
    suspend fun verifyOtp(email: String, otp: String): Result<Unit, DataError.Network>
    suspend fun resendOtp(email: String): Result<Unit, DataError.Network>
    suspend fun forgetPassword(email: String): Result<Unit, DataError.Network>
    suspend fun resetPassword(token: String, newPassword: String): Result<Unit, DataError.Network>
    suspend fun logout(): Result<Unit, DataError.Network>
}
