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
data class FollowingResponse(
    val success: Boolean,
    val message: String,
    val length: Int? = null,
    val data: FollowingData? = null
)

@Serializable
data class FollowingData(
    val follows: List<FollowRelationship>,
    val pagination: PaginationData? = null
)

@Serializable
data class FollowRelationship(
    val following: FollowedUser,
    val createdAt: String
)

@Serializable
data class FollowedUser(
    val id: String,
    val username: String,
    val name: String? = "",
    val lastName: String? = "",
    val profilePicture: String? = null
)

@Serializable
data class PaginationData(
    val nextCursor: String? = null,
    val prevCursor: String? = null,
    val hasMore: Boolean = false
)

class FollowingApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)
    private val tag = "FollowingApiService"

    suspend fun fetchMyFollowing(
        limit: Int = 10,
        cursor: String? = null,
        direction: String = "before",
        searchQuery: String? = null
    ): FollowingResponse {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Log.e(tag, "Token is null or empty")
            return FollowingResponse(
                success = false,
                message = "Token is null or empty",
                length = null,
                data = null
            )
        }

        val urlBuilder = StringBuilder("https://mealflow.online/api/v3/users/me/following?limit=$limit")

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
        Log.d(tag, "********** WITH limit: $limit **********")
        Log.d(tag, "********** WITH cursor: $cursor **********")
        Log.d(tag, "********** WITH direction: $direction **********")
        Log.d(tag, "********** WITH searchQuery: $searchQuery **********")

        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.get(finalUrl) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

                Log.d(tag, "API Response status: ${response.status}")

                if (!response.status.isSuccess()) {
                    Log.e(tag, "Failed to fetch following: ${response.status}")
                    return@withContext FollowingResponse(
                        success = false,
                        message = "Failed to fetch following list: ${response.status}",
                        length = null,
                        data = null
                    )
                }

                val responseText = response.bodyAsText()
                Log.d(tag, "API Response body: $responseText")

                val responseBody = response.body<FollowingResponse>()
                Log.d(tag, "Fetched ${responseBody.length ?: 0} following relationships")

                responseBody
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching following list", e)
            FollowingResponse(
                success = false,
                message = "Error fetching following list: ${e.message}",
                length = null,
                data = null
            )
        }
    }

    suspend fun fetchUserFollowing(
        userId: String? = null,
        limit: Int = 10,
        cursor: String? = null,
        direction: String = "before",
        searchQuery: String? = null
    ): FollowingResponse {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()
        val userPreferencesManager = UserPreferencesManager(context)
        val targetUserId = userId ?: userPreferencesManager.getUserIdString()

        if (token.isNullOrEmpty()) {
            Log.e(tag, "Token is null or empty")
            return FollowingResponse(
                success = false,
                message = "Token is null or empty",
                length = null,
                data = null
            )
        }

        val urlBuilder = StringBuilder("https://mealflow.online/api/v3/users/$targetUserId/following?limit=$limit")

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
        Log.d(tag, "********** WITH userId: $targetUserId **********")
        Log.d(tag, "********** WITH limit: $limit **********")
        Log.d(tag, "********** WITH cursor: $cursor **********")
        Log.d(tag, "********** WITH direction: $direction **********")
        Log.d(tag, "********** WITH searchQuery: $searchQuery **********")

        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.get(finalUrl) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

                Log.d(tag, "API Response status: ${response.status}")

                if (!response.status.isSuccess()) {
                    Log.e(tag, "Failed to fetch following: ${response.status}")
                    return@withContext FollowingResponse(
                        success = false,
                        message = "Failed to fetch following list: ${response.status}",
                        length = null,
                        data = null
                    )
                }

                val responseText = response.bodyAsText()
                Log.d(tag, "API Response body: $responseText")

                val responseBody = response.body<FollowingResponse>()
                Log.d(tag, "Fetched ${responseBody.length ?: 0} following relationships")

                responseBody
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching following list", e)
            FollowingResponse(
                success = false,
                message = "Error fetching following list: ${e.message}",
                length = null,
                data = null
            )
        }
    }
}