package com.example.mealflow.network

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.CommunitiesResponse
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


class MyCommunitiesApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)

    suspend fun fetchMyCommunities(limit: Int = 3, cursor: String? = null): CommunitiesResponse {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Log.e("UserPosts", "Token is null or empty")
            return CommunitiesResponse(false, "Token is null or empty", null)
        }

        val urlBuilder = StringBuilder("https://mealflow.online/api/v3/users/me/communities?limit=$limit")
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

                val responseBodyText = response.bodyAsText()
                Log.d("API Response", responseBodyText)

                if (!response.status.isSuccess()) {
                    Log.e("User Communities", "Failed to fetch My Communities: ${response.status}")
                    return@withContext CommunitiesResponse(false, "Failed to fetch My Communities", null)
                }

                val responseBody = Json.decodeFromString<CommunitiesResponse>(responseBodyText)
                Log.d("UserPosts", "API Response: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e("User Communities", "Error fetching My Communities", e)
            CommunitiesResponse(false, "Error fetching My Communities: ${e.message}", null)
        }
    }
}
