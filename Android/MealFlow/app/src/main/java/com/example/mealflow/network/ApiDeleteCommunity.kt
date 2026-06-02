package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

//@Serializable
//data class DeleteCommunityResponse(
//    val success: Boolean,
//    val count: Int,
//    val members: List<CommunityMember>
//)

@Serializable
data class DeleteCommunityResponse(
    val success: Boolean,
    val message: String
)


suspend fun deleteCommunity(context: Context): DeleteCommunityResponse? {
    val tokenManager = TokenManager(context)
    val communityId = UserPreferencesManager(context).getCommunityId().first()
    val token = tokenManager.getAccessToken()
    val apiClientToken = ApiClientToken(context)

    if (token.isNullOrEmpty()) {
        Toast.makeText(context, "Token is null or empty", Toast.LENGTH_LONG).show()
        return null
    }

    var responseBody: DeleteCommunityResponse? = null

    try {
        // نحط طلب الشبكة داخل withContext(Dispatchers.IO)
        withContext(Dispatchers.IO) {
            val response: HttpResponse =
                apiClientToken.client.delete("https://mealflow.online/api/v3/community/$communityId") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
            if (!response.status.isSuccess()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to delete community", Toast.LENGTH_LONG).show()
                }
            }
            responseBody = response.body<DeleteCommunityResponse>()
            Log.d("Community", "API Response: $responseBody")
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Error deleting community : $e", Toast.LENGTH_LONG).show()
        }
    }
    return responseBody
}
