package com.example.mealflow.network

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.CommunitiesResponse
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

class UserCommunitiesApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)

    suspend fun fetchUserCommunities(limit: Int = 3, cursor: String? = null): CommunitiesResponse {
        val userPreferencesManager = UserPreferencesManager(context)
        val userId = userPreferencesManager.getUserIdString()
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Log.e("UserPosts", "Token is null or empty")
            return CommunitiesResponse(false, "Token is null or empty", null)
        }

        val urlBuilder = StringBuilder("https://mealflow.online/api/v3/users/$userId/communities?limit=$limit")
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
                    Log.e("User Communities", "Failed to fetch Communities: ${response.status}")
                    return@withContext CommunitiesResponse(false, "Failed to fetch Communities", null)
                }

                val responseBody = response.body<CommunitiesResponse>()
                Log.d("User Communities", "API Response: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e("User Communities", "Error fetching Communities", e)
            CommunitiesResponse(false, "Error fetching Communities: ${e.message}", null)
        }
    }
}
