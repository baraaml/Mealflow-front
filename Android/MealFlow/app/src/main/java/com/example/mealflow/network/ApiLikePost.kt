package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class LikePostResponse(
    val success: Boolean,
    val message: String,
    val result: LikeResult
)

@Serializable
data class LikeResult(
    val liked: Boolean,
    val likeCount: Int
)

suspend fun toggleLikePost(
    context: Context,
    postId: String
): LikePostResponse? {
    val tokenManager = TokenManager(context)
    val token = tokenManager.getAccessToken()
    val apiClientToken = ApiClientToken(context)

    if (token.isNullOrEmpty()) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Token is null or empty", Toast.LENGTH_LONG).show()
        }
        return null
    }

    return try {
        withContext(Dispatchers.IO) {
            val response: HttpResponse =
                apiClientToken.client.post("https://mealflow.online/api/v3/posts/$postId/like") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

            if (!response.status.isSuccess()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to toggle like", Toast.LENGTH_LONG).show()
                }
                null
            } else {
                val responseBody = response.body<LikePostResponse>()
                Log.d("Post", "API Response: $responseBody")
                responseBody
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Error toggling like: ${e.message}", Toast.LENGTH_LONG).show()
        }
        Log.e("Post", "Error toggling like", e)
        null
    }
}

