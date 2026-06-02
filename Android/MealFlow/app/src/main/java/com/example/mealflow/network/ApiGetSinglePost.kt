package com.example.mealflow.network

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.Post
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class SinglePostResponse(
    val success: Boolean,
    val message: String,
    val data: Post?
)

class SinglePostApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)
    private val tokenManager = TokenManager(context)
    private val tag = "SinglePostApiService"

    suspend fun fetchSinglePost(postId: String): SinglePostResponse {
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Log.e(tag, "Token is null or empty")
            return SinglePostResponse(false, "Token is null or empty", null)
        }

        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.get(
                    "https://mealflow.online/api/v3/posts/$postId"
                ) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

                val responseText = response.bodyAsText()
                Log.d(tag, "API Response: $responseText")

                if (!response.status.isSuccess()) {
                    Log.e(tag, "Failed to fetch post: ${response.status}")
                    return@withContext SinglePostResponse(
                        false,
                        "Failed to fetch post: ${response.status}",
                        null
                    )
                }

                val responseBody = Json.decodeFromString<SinglePostResponse>(responseText)
                Log.d(tag, "Parsed SinglePostResponse: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching single post", e)
            SinglePostResponse(false, "Error fetching post: ${e.message}", null)
        }
    }
}