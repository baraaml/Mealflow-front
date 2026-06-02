package com.example.mealflow.database.token

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking

fun HttpClientConfig<*>.installAuthInterceptor(tokenManager: TokenManager) {
    // Attach Authorization header to every request (initial and retries)
    install(DefaultRequest) {
        tokenManager.getAccessToken()?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    // Retry logic when Unauthorized
    install(HttpRequestRetry) {
        maxRetries = 1
        exponentialDelay()

        retryIf { _, response ->
            if (response.status == HttpStatusCode.Unauthorized) {
                // Try refreshing the token
                val newToken = runBlocking { refreshAccessToken(tokenManager) }
                newToken != null
            } else {
                false
            }
        }

        modifyRequest { request ->
            // Before retry, attach the new token if available
            tokenManager.getAccessToken()?.let { newToken ->
                request.headers.remove(HttpHeaders.Authorization)
                request.headers.append(HttpHeaders.Authorization, "Bearer $newToken")
            }
        }
    }
}
