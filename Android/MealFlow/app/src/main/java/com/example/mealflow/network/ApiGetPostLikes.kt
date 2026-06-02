package com.example.mealflow.network

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.Pagination
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class PostLikesResponse(
    val success: Boolean,
    val message: String,
    val length: Int,
    val data: LikesData
)

@Serializable
data class LikesData(
    val likes: List<PostLike>,
    val pagination: Pagination? = null
)

@Serializable
data class PostLike(
    val id: String,
    val createdAt: String,
    val user: LikeUser
)

@Serializable
data class LikeUser(
    val id: String,
    val username: String,
    val name: String?,
    val lastName: String?,
    val profilePicture: String?
)

class PostLikesApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)

    suspend fun fetchPostLikes(
        postId: String,
        limit: Int = 10,
        cursor: String? = null
    ): PostLikesResponse? {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()
        val url = "https://mealflow.online/api/v3/posts/$postId/likes"

        Log.d("Token", "Token : $token")
        Log.d("URL", "URL : $url")

        if (token.isNullOrEmpty()) {
            Log.e("PostLikes", "Token is null or empty")
            return null
        }

        Log.d("PostLikes", "fetchPostLikes() called with postId: $postId, limit: $limit, cursor: $cursor")

        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.get(url) {
                    parameter("limit", limit)
                    cursor?.let { parameter("cursor", it) }
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

                Log.d("API Response", response.bodyAsText())
                Log.d("Response", "Response status: ${response.status}")

                if (!response.status.isSuccess()) {
                    Log.e("PostLikes", "Failed to fetch post likes : ${response.status}")
                    return@withContext null
                }

                val responseBody = response.body<PostLikesResponse>()
                Log.d("PostLikes", "API Response: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e("PostLikes", "Error fetching post likes", e)
            null
        }
    }
}
