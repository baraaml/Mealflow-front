package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.navigation.Destination
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.viewModel.OtpViewModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class OtpRequest(val otp: String, val email: String)

@Serializable
data class OtpResponse(val success: Boolean, val message: String, val data: AuthData? = null)

@Serializable
data class AuthData(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

@Serializable
data class User(
    val id: String,
    val username: String? = "",
    val name: String? = "",
    val profilePicture: String? = "",
    val email: String,
    val isVerified: Boolean
)

fun verifyEmailApi(
    context: Context,
    otp: String,
    email: String,
    navController: NavController,
    viewModel: OtpViewModel? = null
) {
    val tokenManager = TokenManager(context)
    val userPrefs = UserPreferencesManager(context)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

            val url = ApiClient.Endpoints.VERIFY_EMAIL

            Log.d("API", "🔹Send verification request: $url")
            Log.d("API", "📩 Request data: otp=$otp, email=$email")

            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(OtpRequest(otp, email))
            }

            val responseText = response.bodyAsText()
            val responseBody = Json.decodeFromString<OtpResponse>(responseText)
            Log.d("API", "🔹Server response: $responseText")

            withContext(Dispatchers.Main) {
                if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Accepted) {
                    if (responseBody.success) {
                        // Save data first
                        responseBody.data?.let {
                            tokenManager.saveTokens(it.accessToken, it.refreshToken)
                            userPrefs.saveMyId(it.user.id)
                            Log.d("user.id", "📩 Send Request: responseBody=${it.user.id}")
                            Log.d("API", "📩 Send Request: responseBody=$responseBody")
                        }

                        Log.d("API", "✅ Verification successful! Go to the next page")
                        Toast.makeText(context, "Verified successfully!", Toast.LENGTH_SHORT).show()
                        viewModel?.resetState() // Reset state after successful verification

                        // Use navigation correctly without going back
                        navController.navigate(Destination.SetupWelcome) {
                            // Remove all previous pages from the back stack
                            popUpTo(0) { inclusive = true }
                            // Prevent opening the same page multiple times
                            launchSingleTop = true
                            // Prevent page state recovery
                            restoreState = false
                        }
                    } else {
                        viewModel?.isLoading = false
                        viewModel?.errorMessage = responseBody.message
                        Toast.makeText(context, responseBody.message, Toast.LENGTH_LONG).show()
                        Log.e("API", "❌ Validation error: ${responseBody.message}")
                    }
                } else {
                    viewModel?.isLoading = false
                    viewModel?.errorMessage = responseBody.message
                    Toast.makeText(context, responseBody.message, Toast.LENGTH_LONG).show()
                    Log.e("API", "⚠️ Unexpected response: ${response.status}")
                }
            }

            client.close()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                viewModel?.isLoading = false
                viewModel?.errorMessage = "Verification failed: ${e.message}"
                Toast.makeText(context, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
            Log.e("API", "❌ Exception during verification: ${e.message}", e)
        }
    }
}
