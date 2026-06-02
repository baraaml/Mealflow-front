package com.example.mealflow.network

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class ResetOtpRequest(val email: String)

@Serializable
data class ResetOtpResponse(val success: Boolean, val message: String)

suspend fun resetOtpApi(
    email: String,
    snackbarHostState: SnackbarHostState
) {
    val client = ApiClient.client
    val url = ApiClient.Endpoints.RESET_OTP

    try {
        Log.d("API", "📩 Sending request: email=$email")

        val response: HttpResponse = withContext(Dispatchers.IO) {
            client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(ResetOtpRequest(email))
            }
        }

        if (response.status.isSuccess()) {
            val responseBody = response.body<ResetOtpResponse>()
            Log.d("OTP API", "✅ OTP resent successfully.")

            withContext(Dispatchers.Main) {
                snackbarHostState.showSnackbar(
                    message = responseBody.message,
                    duration = SnackbarDuration.Short
                )
            }

            if (!responseBody.success) {
                Log.e("OTP API", "❌ OTP resend failed: ${responseBody.message}")
            }

        } else {
            Log.e("OTP API", "❌ Server error: ${response.status.description}")
            withContext(Dispatchers.Main) {
                snackbarHostState.showSnackbar(
                    message = "Failed to resend OTP. Please try again.",
                    duration = SnackbarDuration.Short
                )
            }
        }

    } catch (e: Exception) {
        Log.e("OTP API", "❌ Exception during request: ${e.localizedMessage}")
        withContext(Dispatchers.Main) {
            snackbarHostState.showSnackbar(
                message = "Error connecting to the server.",
                duration = SnackbarDuration.Short
            )
        }
    }
}
