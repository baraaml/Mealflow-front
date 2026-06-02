package com.example.mealflow.network

import android.content.Context
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.database.token.installAuthInterceptor
import com.example.mealflow.utils.JsonProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Singleton object that provides a shared HTTP client for all API requests.
 * This ensures consistent configuration and efficient resource usage.
 */
object ApiClient {
    private lateinit var tokenManager: TokenManager

    lateinit var authClient: HttpClient

    fun init(context: Context) {
        tokenManager = TokenManager(context)
        authClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(JsonProvider.json)
            }
            installAuthInterceptor(tokenManager)
        }
    }

    // Base URL for all API endpoints
    const val BASE_URL = "https://mealflow.online/api/v3"
    const val InteractionsURL = "https://mealflow.online/api/v3"
    // Shared HTTP client instance
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(JsonProvider.json)
        }

        // You can add additional client configurations here as needed:
        // - Timeout settings
        // - Logging
        // - Error handling
        // - Authentication interceptors
    }

    // API endpoint paths
    object Endpoints {
        const val LOGIN = "$BASE_URL/users/login"
        const val REGISTER = "$BASE_URL/users/register"
        const val VERIFY_EMAIL = "$BASE_URL/users/verify-email"
        const val FORGOT_PASSWORD = "$BASE_URL/users/forgot-password"
        const val RESET_PASSWORD = "$BASE_URL/users/reset-password"
        const val RESET_OTP = "$BASE_URL/users/resend-verification"
        const val LOGOUT = "$BASE_URL/users/logout"
        const val QUICK_LOGIN = "$BASE_URL/users/quick-login"
        const val FIREBASE_GOOGLE_LOGIN = "$BASE_URL/users/firebase/google"


        // Meal endpoints
        const val MEALS = "$BASE_URL/meals"
        const val SEARCH = "$InteractionsURL/meals/search"
        const val newInteractions = "$InteractionsURL/interactions/interact"
        const val METADATA = "$InteractionsURL/metadata"
    }
}