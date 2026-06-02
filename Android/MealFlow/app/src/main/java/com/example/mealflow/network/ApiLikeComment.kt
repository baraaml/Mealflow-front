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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class LikeCommentResponse(
    val success: Boolean,
    val message: String,
    val result: LikeCommentResult
)

@Serializable
data class LikeCommentResult(
    val liked: Boolean,
    val likeCount: Int
)

suspend fun toggleLikeComment(
    context: Context,
    commentId: String
): LikeCommentResponse? {
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
                apiClientToken.client.post("https://mealflow.online/api/v3/comments/$commentId/like") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

            if (!response.status.isSuccess()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to toggle comment like", Toast.LENGTH_LONG).show()
                }
                null
            } else {
                val responseBody = response.body<LikeCommentResponse>()
                Log.d("Comment", "API Response: $responseBody")
                responseBody
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Error toggling comment like", Toast.LENGTH_LONG).show()
        }
        Log.e("Comment", "Error: ${e.message}", e)
        null
    }
}
