package com.example.mealflow.network

import android.util.Log
import com.example.mealflow.core.data.network.ApiClient
import com.example.mealflow.data.model.Meal

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class InteractionRequest(
    val userId: String,
    val mealId: String,
    val type: String,
    val liked: Boolean,
    val disliked: Boolean,
    val ignored: Boolean,
    val viewed: Boolean,
    val saved: Boolean,
    val cooked: Boolean,
    val favorited: Boolean,
    val rating: Float?,
    val reviewText: String?
)

@Serializable
data class InteractionResponse(
    val id: String,
    val userId: String,
    val mealId: String,
    val type: String,
    val liked: Boolean,
    val disliked: Boolean,
    val ignored: Boolean,
    val viewed: Boolean,
    val saved: Boolean,
    val cooked: Boolean,
    val favorited: Boolean,
    val rating: Float?,
    val reviewText: String?,
    val createdAt: String // Assuming String for simplicity, can be converted to a date type if needed
)

class ApiInteract {
    private val client = ApiClient.authClient
    private val endpoints = ApiClient.Endpoints

    suspend fun sendInteraction(interactionRequest: InteractionRequest): Result<InteractionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = client.post(endpoints.newInteractions) {
                contentType(ContentType.Application.Json)
                setBody(interactionRequest)
            }

            if (response.status.isSuccess()) {
                val interactionResponse = response.body<InteractionResponse>()
                Result.success(interactionResponse)
            } else {
                val errorBody = response.body<String>()
                Log.e("ApiInteract", "Failed to send interaction: ${response.status} - $errorBody")
                Result.failure(Exception("Failed to send interaction: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ApiInteract", "Error sending interaction: ${e.message}", e)
            Result.failure(e)
        }
    }
}