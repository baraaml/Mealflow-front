package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

// ----------------------- JoinCommunityResponse ---------------------------
@Serializable
data class JoinCommunityResponse(
    val success: Boolean,
    val message: String,
    val community: JoinCommunity? = null
)

@Serializable
data class JoinCommunity(
    val communityId: String,
    val userId: String,
    val role: String,
    val joinedAt: String,
    val leftAt: String?,
    val isPending: Boolean
)


fun joinCommunityApi(
    idCommunity: String,
    context: Context,
) {
    val apiClientToken = ApiClientToken(context)

    CoroutineScope(Dispatchers.IO).launch {
        val url = "https://mealflow.online/api/v3/community/$idCommunity/join"
        Log.d("URL", "📩Send request: URL = $url")
        val tokenManager = TokenManager(context)
        val accessToken = tokenManager.getAccessToken()

        try {
            Log.d("API", "📩Send request: Token = $accessToken, idCommunity=$idCommunity")

            val response: HttpResponse = apiClientToken.client.post(url) {
                header("Authorization", "Bearer $accessToken")
                header("Content-Type", ContentType.Application.Json.toString())
            }

            val responseBody = response.body<JoinCommunityResponse>()

            withContext(Dispatchers.Main) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, responseBody.message, Toast.LENGTH_SHORT).show()
                }
                Log.d("join Community", "response : ${responseBody.message}")
                Log.d("join Community", "response : $responseBody")
                if (response.status.isSuccess() && responseBody.success) {
                    Log.d("join Community", "✅ Successful join Community, go to the next page")
                } else {
                    Log.e("join Community", "❌ join Community failed:${responseBody.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, responseBody.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("join Community", "❌ Exception during order execution:${e.localizedMessage}")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "An error occurred while connecting to the server.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


