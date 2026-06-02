package com.example.mealflow.network

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.AllCommunitiesResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommunityApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)
    private val tag = "CommunityApiService"

    suspend fun fetchAllCommunities(limit: Int = 2, cursor: String? = null): AllCommunitiesResponse {
        val urlBuilder = StringBuilder("https://mealflow.online/api/v3/community?limit=$limit")
        if (!cursor.isNullOrEmpty()) {
            urlBuilder.append("&cursor=$cursor")
        }

        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.get(urlBuilder.toString())
                Log.d(tag, "Raw All Communities API Response: ${response.bodyAsText()}")

                if (!response.status.isSuccess()) {
                    Log.e(tag, "Failed to fetch all communities: ${response.status}")
                    return@withContext AllCommunitiesResponse(
                        success = false,
                        count = 0,
                        communities = emptyList()
                    )
                }

                val responseBody = response.body<AllCommunitiesResponse>()
                Log.d(tag, "Parsed All Communities API Response: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching all communities", e)
            AllCommunitiesResponse(
                success = false,
                count = 0,
                communities = emptyList()
            )
        }
    }
}
