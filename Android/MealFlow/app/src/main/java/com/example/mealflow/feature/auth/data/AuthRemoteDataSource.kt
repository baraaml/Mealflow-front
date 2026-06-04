package com.example.mealflow.feature.auth.data

import com.example.mealflow.core.data.network.ApiClient
import com.example.mealflow.core.data.network.post
import com.example.mealflow.core.domain.util.DataError
import com.example.mealflow.core.domain.util.Result
import com.example.mealflow.core.domain.util.map
import com.example.mealflow.feature.auth.data.dto.*
import io.ktor.client.HttpClient

class AuthRemoteDataSource(
    private val httpClient: HttpClient
) {
    suspend fun login(email: String, password: String): Result<AuthDataDto, DataError.Network> {
        return httpClient.post<LoginRequest, AuthResponse>(
            route = ApiClient.Endpoints.LOGIN,
            body = LoginRequest(email, password)
        ).map { it.data!! }
    }

    suspend fun register(username: String, email: String, password: String): Result<AuthDataDto, DataError.Network> {
        return httpClient.post<RegisterRequest, AuthResponse>(
            route = ApiClient.Endpoints.REGISTER,
            body = RegisterRequest(username, email, password)
        ).map { it.data!! }
    }

    suspend fun quickLogin(refreshToken: String): Result<AuthDataDto, DataError.Network> {
        return httpClient.post<QuickLoginRequest, AuthResponse>(
            route = ApiClient.Endpoints.QUICK_LOGIN,
            body = QuickLoginRequest(refreshToken)
        ).map { it.data!! }
    }

    suspend fun verifyOtp(email: String, otp: String): Result<AuthResponse, DataError.Network> {
        return httpClient.post<VerifyOtpRequest, AuthResponse>(
            route = ApiClient.Endpoints.VERIFY_EMAIL,
            body = VerifyOtpRequest(email, otp)
        )
    }

    suspend fun resendOtp(email: String): Result<AuthResponse, DataError.Network> {
        return httpClient.post<ResendOtpRequest, AuthResponse>(
            route = ApiClient.Endpoints.RESET_OTP,
            body = ResendOtpRequest(email)
        )
    }

    suspend fun forgetPassword(email: String): Result<AuthResponse, DataError.Network> {
        return httpClient.post<ForgetPasswordRequest, AuthResponse>(
            route = ApiClient.Endpoints.FORGOT_PASSWORD,
            body = ForgetPasswordRequest(email)
        )
    }

    suspend fun resetPassword(token: String, newPassword: String): Result<AuthResponse, DataError.Network> {
        return httpClient.post<ResetPasswordRequest, AuthResponse>(
            route = ApiClient.Endpoints.RESET_PASSWORD,
            body = ResetPasswordRequest(token, newPassword)
        )
    }

    suspend fun logout(): Result<AuthResponse, DataError.Network> {
        return httpClient.post<Unit, AuthResponse>(
            route = ApiClient.Endpoints.LOGOUT,
            body = Unit
        )
    }
}
