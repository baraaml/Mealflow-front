package com.example.mealflow.database.token

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class RefreshTokenResponse(val success: Boolean, val message: String, val data: TokenData?)

@Serializable
data class TokenData(val accessToken: String)

suspend fun refreshAccessToken(tokenManager: TokenManager): String? {
    val refreshToken = tokenManager.getRefreshToken() ?: run {
        Log.d("RefreshToken", "No refresh token available")
        return null
    }

    val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    Log.d("RefreshToken", "Sending refresh token request...")

    val response: HttpResponse = client.get("https://mealflow.online/api/v3/users/refresh-token") {
        contentType(ContentType.Application.Json)
        setBody(RefreshTokenRequest(refreshToken))
    }

    Log.d("RefreshToken", "Received response with status: ${response.status}")

    return if (response.status == HttpStatusCode.OK) {
        val jsonResponse = response.body<RefreshTokenResponse>()
        val newAccessToken = jsonResponse.data?.accessToken

        Log.d("RefreshToken", "New access token: $newAccessToken")

        if (newAccessToken != null) {
            tokenManager.saveTokens(newAccessToken, refreshToken)
            Log.d("RefreshToken", "Tokens saved successfully")
        } else {
            tokenManager.clearTokens()
            Log.e("RefreshToken", "Failed to get new access token, tokens cleared")
        }
        newAccessToken
    } else {
        tokenManager.clearTokens()
        Log.e("RefreshToken", "Refresh token invalid, tokens cleared")
        null
    }
}
