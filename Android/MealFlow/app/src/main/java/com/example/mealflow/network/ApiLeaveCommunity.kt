package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

// ----------------------- LeaveCommunityResponse ---------------------------
@Serializable
data class LeaveCommunityResponse(
    val success: Boolean,
    val message: String,
    val data: LeaveCommunityData? = null
)

@Serializable
data class LeaveCommunityData(
    val userId: String? = null,
    val communityId: String,
    val role: String? = null,
    val name: String? = null, // For community deletion scenario
    val newAdmin: NewAdmin? = null // For admin promotion scenario
)

@Serializable
data class NewAdmin(
    val id: String,
    val name: String,
    val username: String,
    val lastName: String,
    val profilePicture: String?
)

/**
 * Leave Community API function
 * @param idCommunity The ID of the community to leave
 * @param context Android context for UI operations
 * @param onSuccess Callback for successful leave operation
 * @param onError Callback for error handling
 */
fun leaveCommunityApi(
    idCommunity: String,
    context: Context,
    onSuccess: ((LeaveCommunityResponse) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
) {
    val apiClientToken = ApiClientToken(context)

    CoroutineScope(Dispatchers.IO).launch {
        val url = "https://mealflow.online/api/v3/community/$idCommunity/leave"
        Log.d("URL", "📩Send request: URL = $url")
        val tokenManager = TokenManager(context)
        val accessToken = tokenManager.getAccessToken()

        try {
            Log.d("API", "📩Send request: Token = $accessToken, idCommunity=$idCommunity")

            val response: HttpResponse = apiClientToken.client.delete(url) {
                header("Authorization", "Bearer $accessToken")
                header("Content-Type", ContentType.Application.Json.toString())
            }

            val responseBody = response.body<LeaveCommunityResponse>()

            withContext(Dispatchers.Main) {
                if (response.status.isSuccess() && responseBody.success) {
                    Log.d("Leave Community", "✅ Successfully left community")
                    Log.d("Leave Community", "Response: $responseBody")

                    // Show appropriate message based on scenario
                    Toast.makeText(context, responseBody.message, Toast.LENGTH_SHORT).show()

                    // Handle different scenarios
                    handleLeaveCommunityScenario(responseBody, context)

                    // Call success callback if provided
                    onSuccess?.invoke(responseBody)

                } else {
                    Log.e("Leave Community", "❌ Leave community failed: ${responseBody.message}")
                    Toast.makeText(context, responseBody.message, Toast.LENGTH_SHORT).show()
                    onError?.invoke(responseBody.message)
                }
            }
        } catch (e: Exception) {
            Log.e("Leave Community", "❌ Exception during leave community: ${e.localizedMessage}")
            withContext(Dispatchers.Main) {
                val errorMessage = "An error occurred while leaving the community."
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                onError?.invoke(errorMessage)
            }
        }
    }
}

/**
 * Handle different leave community scenarios
 */
private fun handleLeaveCommunityScenario(response: LeaveCommunityResponse, context: Context) {
    response.data?.let { data ->
        when {
            // Community was deleted (last member left)
            data.name != null -> {
                Log.d("Leave Community", "🗑️ Community '${data.name}' was deleted")
                // You might want to navigate back to communities list or main screen
            }

            // New admin was promoted
            data.newAdmin != null -> {
                Log.d("Leave Community", "👑 New admin promoted: ${data.newAdmin.name}")
                // You might want to show additional information about the new admin
            }

            // Regular member or admin left (community continues)
            data.role != null -> {
                Log.d("Leave Community", "👋 ${data.role} left the community")
                // Navigate back or refresh community list
            }
        }
    }
}

/**
 * Leave Community API with confirmation dialog
 * @param idCommunity The ID of the community to leave
 * @param context Android context
 * @param onConfirm Callback when user confirms leaving
 * @param onCancel Callback when user cancels
 */
// later
/**
 * Extension function to check if the leave operation resulted in community deletion
 */
fun LeaveCommunityResponse.isCommunityDeleted(): Boolean {
    return data?.name != null
}

/**
 * Extension function to check if a new admin was promoted
 */
fun LeaveCommunityResponse.hasNewAdmin(): Boolean {
    return data?.newAdmin != null
}

/**
 * Extension function to get the user's previous role
 */
fun LeaveCommunityResponse.getUserRole(): String? {
    return data?.role
}