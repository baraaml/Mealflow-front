package com.example.mealflow.network

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.PostsResponse
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json


class MyPostsApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)
    private val tokenManager = TokenManager(context)
    private val tag = "MyPostsApiService"

    suspend fun fetchMyPosts(limit: Int = 3, cursor: String? = null): PostsResponse {
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Log.e(tag, "Token is null or empty")
            return PostsResponse(false, "Token is null or empty", null, null)
        }

        val urlBuilder = StringBuilder("https://mealflow.online/api/v3/users/me/posts?limit=$limit")
        if (!cursor.isNullOrEmpty()) {
            urlBuilder.append("&cursor=$cursor")
        }

        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.get(urlBuilder.toString()) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

                val responseText = response.bodyAsText()
                Log.d(tag, "API Response: $responseText")

                if (!response.status.isSuccess()) {
                    Log.e(tag, "Failed to fetch posts: ${response.status}")
                    return@withContext PostsResponse(false, "Failed to fetch posts", null, null)
                }

                val responseBody = Json.decodeFromString<PostsResponse>(responseText)
                Log.d(tag, "Parsed PostsResponse: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching posts", e)
            PostsResponse(false, "Error fetching posts: ${e.message}", null, null)
        }
    }

    suspend fun deletePost(postId: String): DeletePostResponse? {
        val token = tokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            Log.e(tag, "Token is null or empty")
            return null
        }

        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.delete(
                    "https://mealflow.online/api/v3/users/me/posts/$postId"
                ) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
                val responseText = response.bodyAsText()
                Log.d(tag, "Delete post response: $responseText")
                Json.decodeFromString<DeletePostResponse>(responseText)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error deleting post", e)
            null
        }
    }
}
