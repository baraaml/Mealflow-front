package com.example.mealflow.feature.auth.data

import com.example.mealflow.core.domain.util.Result
import com.example.mealflow.core.domain.util.onSuccess
import com.example.mealflow.core.domain.util.asEmptyDataResult
import com.example.mealflow.core.domain.util.DataError
import com.example.mealflow.feature.auth.domain.AuthRepository
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.database.UserPreferencesManager

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val tokenManager: TokenManager,
    private val userPreferencesManager: UserPreferencesManager
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit, DataError.Network> {
        return remoteDataSource.login(email, password).onSuccess { data ->
            tokenManager.saveTokens(data.accessToken, data.refreshToken)
            userPreferencesManager.saveMyId(data.user.id)
            userPreferencesManager.saveFirstName(data.user.name ?: "")
            userPreferencesManager.saveMyImageProfile(data.user.profilePicture ?: "")
        }.asEmptyDataResult()
    }

    override suspend fun register(username: String, email: String, password: String): Result<Unit, DataError.Network> {
        return remoteDataSource.register(username, email, password).onSuccess { data ->
            tokenManager.saveTokens(data.accessToken, data.refreshToken)
        }.asEmptyDataResult()
    }

    override suspend fun quickLogin(refreshToken: String): Result<Unit, DataError.Network> {
        return remoteDataSource.quickLogin(refreshToken).onSuccess { data ->
            tokenManager.saveTokens(data.accessToken, data.refreshToken)
        }.asEmptyDataResult()
    }

    override suspend fun verifyOtp(email: String, otp: String): Result<Unit, DataError.Network> {
        return remoteDataSource.verifyOtp(email, otp).asEmptyDataResult()
    }

    override suspend fun resendOtp(email: String): Result<Unit, DataError.Network> {
        return remoteDataSource.resendOtp(email).asEmptyDataResult()
    }

    override suspend fun forgetPassword(email: String): Result<Unit, DataError.Network> {
        return remoteDataSource.forgetPassword(email).asEmptyDataResult()
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit, DataError.Network> {
        return remoteDataSource.resetPassword(token, newPassword).asEmptyDataResult()
    }

    override suspend fun logout(): Result<Unit, DataError.Network> {
        return remoteDataSource.logout().onSuccess {
            tokenManager.clearTokens()
        }.asEmptyDataResult()
    }
}
