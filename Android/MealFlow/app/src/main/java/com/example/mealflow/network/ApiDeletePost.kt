package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class DeletePostResponse(
    val success: Boolean,
    val message: String,
)

class PostRepository(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val apiClientToken = ApiClientToken(context)

    suspend fun deletePost(postId: String): DeletePostResponse? {
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Token is null or empty", Toast.LENGTH_LONG).show()
            }
            return null
        }

        var responseBody: DeletePostResponse? = null

        try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse =
                    apiClientToken.client.delete("https://mealflow.online/api/v3/users/me/posts/$postId") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $token")
                        }
                    }

                if (!response.status.isSuccess()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to delete Post", Toast.LENGTH_LONG).show()
                    }
                }

                responseBody = response.body<DeletePostResponse>()
                Log.d("Post", "API Response: $responseBody")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error deleting Post : $e", Toast.LENGTH_LONG).show()
            }
        }

        return responseBody
    }
}
