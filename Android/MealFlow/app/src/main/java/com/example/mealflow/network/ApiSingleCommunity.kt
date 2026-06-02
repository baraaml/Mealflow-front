package com.example.mealflow.network

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.SingleCommunityResponse
import com.example.mealflow.database.UserPreferencesManager
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json


class SingleCommunityApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)
    private val userPrefs = UserPreferencesManager(context)

    suspend fun fetchSingleCommunity(): SingleCommunityResponse {
        Log.d("Community", "fetchSingleCommunity() called")

        return try {
            // Use firstOrNull() or timeout to avoid indefinite waiting
            val communityId = withTimeoutOrNull(3000) { userPrefs.getCommunityId().first() }

            if (communityId.isNullOrEmpty()) {
                Log.e("Community", "Community ID is null or empty")
                return SingleCommunityResponse(false,"",null)
            }

            val response: HttpResponse = withContext(Dispatchers.IO) {
                apiClientToken.client.get("https://mealflow.online/api/v3/community/$communityId")
            }

            val responseText = response.bodyAsText()
            Log.d("API Response", responseText)
            Log.d("response", "Response status: ${response.status}")

            if (!response.status.isSuccess()) {
                Log.e("Community", "Failed to fetch community: ${response.status}")
                return SingleCommunityResponse(false,"",null)
            }

            val responseBody = Json.decodeFromString<SingleCommunityResponse>(responseText)
            Log.d("Community", "API Response: $responseBody")
            responseBody

        } catch (e: Exception) {
            Log.e("Community", "Error fetching community", e)
            SingleCommunityResponse(false,"",null)
        }
    }
}
