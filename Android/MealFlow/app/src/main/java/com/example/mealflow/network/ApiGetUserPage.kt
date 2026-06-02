package com.example.mealflow.network

import android.content.Context
import android.util.Log
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

class UserApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)

    suspend fun fetchUser(): UserProfileResponse {
        val userPrefs = UserPreferencesManager(context)
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()
        val userid = userPrefs.getUserId().first()
        Log.d("userid", "userid : $userid")
        Log.d("Token", "Token : $token")

        if (token.isNullOrEmpty()) {
            Log.e("UserProfile", "Token is null or empty")
            return UserProfileResponse(false, "Token is null or empty", UserProfile.empty())
        }

        Log.d("UserProfile", "fetchUserProfile() called")
        Log.d("Token", "Token : $token")

        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.get("https://mealflow.online/api/v3/users/$userid") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
                val responseText = response.bodyAsText()
                Log.d("API Response", responseText)
                Log.d("response", "Response status: ${response.status}")

                if (!response.status.isSuccess()) {
                    Log.e("UserProfile", "Failed to fetch UserProfile: ${response.status}")
                    return@withContext UserProfileResponse(false, "Failed to fetch UserProfile", UserProfile.empty())
                }

                val responseBody = response.body<UserProfileResponse>()
                Log.d("UserProfile", "API Response: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e("UserProfile", "Error fetching UserProfile", e)
            UserProfileResponse(false, "Error fetching UserProfile: ${e.message}", UserProfile.empty())
        }
    }
}
