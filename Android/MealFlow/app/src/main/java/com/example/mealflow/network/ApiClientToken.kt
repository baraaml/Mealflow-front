package com.example.mealflow.network

import android.content.Context
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.database.token.installAuthInterceptor
import com.example.mealflow.utils.JsonProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json

class ApiClientToken(context: Context) {
    companion object {
        // Base URL for all API endpoints
        private const val BASE_URL = "https://mealflow.online/api/v3"

        // API endpoint paths
        object Endpoints {
            const val COMMUNITY = "${BASE_URL}/community"
            const val CREATE_COMMUNITY = "${BASE_URL}/community"
            const val CREATE_POST = "${BASE_URL}/users/me/posts"
            const val CREATE_MEAL = "https://mealflow.online/api/v3/meals"
        }
    }

    private val tokenManager = TokenManager(context)

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(JsonProvider.json)
        }
        install(Logging) {
            level = LogLevel.ALL
        }
        installAuthInterceptor(tokenManager) // Associate tokens with each request
    }
}
