package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AdminRequest(
    val memberId: String
)

@Serializable
data class SetAdminsResponse(
    val success: Boolean,
    val message: String,
    val admins: List<CommunityAdmins>
)

@Serializable
data class CommunityAdmins(
    val count: Int
)

class SetAdminsClass(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)

    suspend fun setAdmins(memberId: String): Boolean {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Log.e("Community", "Token is null or empty")
            return false
        }

        val communityId = UserPreferencesManager(context).getCommunityId().first()
        val url = "https://mealflow.online/api/v3/community/$communityId/admins"

        return try {
            val requestBody = AdminRequest(memberId)
            Log.d("Community", "RequestBody: $requestBody")

            val response: HttpResponse = withContext(Dispatchers.IO) {
                apiClientToken.client.patch(url) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                    setBody(Json.encodeToString(requestBody))
                }
            }

            val responseBody = response.bodyAsText()
            Log.d("Community", "Response: $responseBody")

            if (!response.status.isSuccess()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to update admins: ${response.status}", Toast.LENGTH_SHORT).show()
                }
                Log.e("Community", "Failed to update admins: ${response.status}")
                false
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Admins updated successfully", Toast.LENGTH_SHORT).show()
                }
                Log.d("Community", "Admins updated successfully")
                true
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error updating admins", Toast.LENGTH_SHORT).show()
            }
            Log.e("Community", "Error updating admins", e)
            false
        }
    }
}
