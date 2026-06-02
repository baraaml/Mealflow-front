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
import kotlinx.coroutines.flow.first


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostsCommunityApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)
    suspend fun fetchPostsCommunity(limit: Int = 3, cursor: String? = null): PostsResponse {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()
        val communityId = UserPreferencesManager(context).getCommunityId().first()

        if (token.isNullOrEmpty()) {
            Log.e("UserPosts", "Token is null or empty")
            return PostsResponse(false, "Token is null or empty", null, null)
        }

        val urlBuilder = StringBuilder("https://mealflow.online/api/v3/community/$communityId/posts?limit=$limit")
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
                Log.d("API Response", responseText)

                if (!response.status.isSuccess()) {
                    Log.e("UserPosts", "Failed to fetch posts: ${response.status}")
                    return@withContext PostsResponse(false, "Failed to fetch posts", null, null)
                }

                val responseBody = response.body<PostsResponse>()
                Log.d("UserPosts", "API Response: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e("UserPosts", "Error fetching posts", e)
            PostsResponse(false, "Error fetching posts: ${e.message}", null, null)
        }
    }
}
