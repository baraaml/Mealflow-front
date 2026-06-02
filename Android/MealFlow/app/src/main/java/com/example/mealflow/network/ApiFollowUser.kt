package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import io.ktor.client.request.delete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class FollowUserResponse(
    val success: Boolean,
    val message: String,
    val data: FollowData?
)

@Serializable
data class FollowData(
    val id: String,
    val followerId: String,
    val followingId: String,
    val createdAt: String,
    val notifyOnPost: Boolean
)

@Serializable
data class UnfollowUserResponse(
    val success: Boolean,
    val message: String
)

class UserFollowService(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val apiClientToken = ApiClientToken(context)

    suspend fun followUser(userId: String): FollowUserResponse? {
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Token is null or empty", Toast.LENGTH_LONG).show()
            }
            return null
        }

        var responseBody: FollowUserResponse? = null

        try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse =
                    apiClientToken.client.post("https://mealflow.online/api/v3/users/$userId/follow") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $token")
                        }
                    }

                if (!response.status.isSuccess()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to follow user", Toast.LENGTH_LONG).show()
                    }
                }

                responseBody = response.body<FollowUserResponse>()
                Log.d("Follow", "API Response: $responseBody")
            }
        } catch (e: Exception) {
            Log.e("Follow", "Error following user", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error following user", Toast.LENGTH_LONG).show()
            }
        }

        return responseBody
    }

    suspend fun unfollowUser(userId: String): UnfollowUserResponse? {
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Token is null or empty", Toast.LENGTH_LONG).show()
            }
            return null
        }

        var responseBody: UnfollowUserResponse? = null

        try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse =
                    apiClientToken.client.delete("https://mealflow.online/api/v3/users/$userId/follow") {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer $token")
                        }
                    }

                if (!response.status.isSuccess()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to unfollow user", Toast.LENGTH_LONG).show()
                    }
                }

                responseBody = response.body<UnfollowUserResponse>()
                Log.d("Unfollow", "API Response: $responseBody")
            }
        } catch (e: Exception) {
            Log.e("Unfollow", "Error unfollowing user", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error unfollowing user", Toast.LENGTH_LONG).show()
            }
        }

        return responseBody
    }
}
