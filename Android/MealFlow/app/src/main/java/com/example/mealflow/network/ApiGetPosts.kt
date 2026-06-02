package com.example.mealflow.network

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.PostsResponse
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json


class PostApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)
    private val tokenManager = TokenManager(context)
    private val userPreferencesManager = UserPreferencesManager(context)
    private val tag = "PostApiService"

    suspend fun fetchUserPosts(limit: Int? = 2, cursor: String? = null): PostsResponse {
        val token = tokenManager.getAccessToken()
        val userId = userPreferencesManager.getUserIdString()
        Log.d(tag, "UserId: $userId")

        if (token.isNullOrEmpty()) {
            Log.e(tag, "Token is null or empty")
            return PostsResponse(false, "Token is null or empty", null, null)
        }

        val urlBuilder = StringBuilder("https://mealflow.online/api/v3/users/$userId/posts?limit=$limit")
        if (!cursor.isNullOrEmpty()) {
            urlBuilder.append("&cursor=$cursor")
        }
        val finalUrl = urlBuilder.toString()

        Log.d(tag, "********** REQUESTING URL: $finalUrl **********")
        Log.d(tag, "********** WITH userId: $userId **********")
        Log.d(tag, "********** WITH limit: $limit **********")
        Log.d(tag, "********** WITH cursor: $cursor **********")

        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.get(finalUrl) {
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

                // هنا تأكد من استخدام مكتبة التحويل المناسبة (kotlinx.serialization، gson...)
                val responseBody = Json.decodeFromString<PostsResponse>(responseText)
                Log.d(tag, "Parsed PostsResponse: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching posts", e)
            PostsResponse(false, "Error fetching posts: ${e.message}", null, null)
        }
    }
}
