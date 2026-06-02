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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class FollowersResponse(
    val success: Boolean,
    val message: String,
    val length: Int? = null,
    val data: FollowersData? = null
)

@Serializable
data class FollowersData(
    val follows: List<FollowerRelationship>,
    val pagination: PaginationData? = null
)

@Serializable
data class FollowerRelationship(
    val follower: FollowerUser,
    val createdAt: String
)

@Serializable
data class FollowerUser(
    val id: String,
    val username: String,
    val name: String,
    val lastName: String? = null,
    val profilePicture: String? = null
)

class FollowersApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)
    private val tag = "FollowersApiService"

    suspend fun fetchMyFollowers(
        limit: Int = 10,
        cursor: String? = null,
        direction: String = "before",
        searchQuery: String? = null
    ): FollowersResponse {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Log.e(tag, "Token is null or empty")
            return FollowersResponse(
                success = false,
                message = "Token is null or empty",
                length = null,
                data = null
            )
        }

        val urlBuilder = StringBuilder("https://mealflow.online/api/v3/users/me/followers?limit=$limit")

        if (!cursor.isNullOrEmpty()) {
            urlBuilder.append("&cursor=$cursor")
        }

        if (direction.isNotEmpty()) {
            urlBuilder.append("&direction=$direction")
        }

        if (!searchQuery.isNullOrEmpty()) {
            urlBuilder.append("&q=$searchQuery")
        }

        val finalUrl = urlBuilder.toString()
        Log.d(tag, "********** REQUESTING URL: $finalUrl **********")

        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.get(finalUrl) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

                Log.d(tag, "API Response status: ${response.status}")

                if (!response.status.isSuccess()) {
                    Log.e(tag, "Failed to fetch followers: ${response.status}")
                    return@withContext FollowersResponse(
                        success = false,
                        message = "Failed to fetch followers list: ${response.status}",
                        length = null,
                        data = null
                    )
                }

                val responseText = response.bodyAsText()
                Log.d(tag, "API Response body: $responseText")

                val responseBody = response.body<FollowersResponse>()
                Log.d(tag, "Fetched ${responseBody.length ?: 0} follower relationships")

                responseBody
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching followers list", e)
            FollowersResponse(
                success = false,
                message = "Error fetching followers list: ${e.message}",
                length = null,
                data = null
            )
        }
    }

    suspend fun fetchUserFollowers(
        userId: String? = null,
        limit: Int = 10,
        cursor: String? = null,
        direction: String = "before",
        searchQuery: String? = null
    ): FollowersResponse {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()
        val userPreferencesManager = UserPreferencesManager(context)
        val targetUserId = userId ?: userPreferencesManager.getUserIdString()

        if (token.isNullOrEmpty()) {
            Log.e(tag, "Token is null or empty")
            return FollowersResponse(
                success = false,
                message = "Token is null or empty",
                length = null,
                data = null
            )
        }

        val urlBuilder = StringBuilder("https://mealflow.online/api/v3/users/$targetUserId/followers?limit=$limit")

        if (!cursor.isNullOrEmpty()) {
            urlBuilder.append("&cursor=$cursor")
        }

        if (direction.isNotEmpty()) {
            urlBuilder.append("&direction=$direction")
        }

        if (!searchQuery.isNullOrEmpty()) {
            urlBuilder.append("&q=$searchQuery")
        }

        val finalUrl = urlBuilder.toString()
        Log.d(tag, "********** REQUESTING URL: $finalUrl **********")

        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.get(finalUrl) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

                Log.d(tag, "API Response status: ${response.status}")

                if (!response.status.isSuccess()) {
                    Log.e(tag, "Failed to fetch followers: ${response.status}")
                    return@withContext FollowersResponse(
                        success = false,
                        message = "Failed to fetch followers list: ${response.status}",
                        length = null,
                        data = null
                    )
                }

                val responseText = response.bodyAsText()
                Log.d(tag, "API Response body: $responseText")

                val responseBody = response.body<FollowersResponse>()
                Log.d(tag, "Fetched ${responseBody.length ?: 0} follower relationships")

                responseBody
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching followers list", e)
            FollowersResponse(
                success = false,
                message = "Error fetching followers list: ${e.message}",
                length = null,
                data = null
            )
        }
    }
}