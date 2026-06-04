package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.navigation.Destination
import com.example.mealflow.viewModel.RegisterViewModel
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
data class RegisterRequest(val username: String, val email: String, val password: String)

@Serializable
data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val data: Data? = null,
    val stackTrace: String? = null // Add this field to handle server stackTrace
)

fun registerUser(
    context: Context,
    username: String,
    email: String,
    password: String,
    navController: NavController,
    viewModel: RegisterViewModel? = null
) {
    val tokenManager = TokenManager(context)

    CoroutineScope(Dispatchers.IO).launch {
        var client: HttpClient? = null
        try {
            client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = false
                    }
                    )
                }
            }

            val url = ApiClient.Endpoints.REGISTER

            Log.d("API", "🔹Send registration request: $url")
            Log.d("API", "📩 Request data: username=$username, email=$email")

            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(RegisterRequest(username, email, password))
            }

            val responseText = response.bodyAsText()
            Log.d("API", "🔹Server response: $responseText")

            // Use the same JSON config for decoding
            val responseBody = Json.decodeFromString<RegisterResponse>(responseText)

            withContext(Dispatchers.Main) {
                if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                    if (responseBody.success) {
                        responseBody.data?.let {
                            tokenManager.saveTokens(it.accessToken, it.refreshToken)
                            Log.d("API", "📩 Tokens saved successfully")
                        }
                        Log.d("API", "✅ Registration successful! Go to OTP page")

                        Toast.makeText(context, "Account created successfully! Check your email", Toast.LENGTH_SHORT).show()
                        viewModel?.resetState() // Reset state after successful registration
                        navController.navigate(Destination.Otp(email)) {
                            popUpTo(Destination.Register) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        viewModel?.isLoading = false
                        viewModel?.errorMessage = responseBody.message
                        Toast.makeText(context, responseBody.message, Toast.LENGTH_LONG).show()
                        Log.e("API", "❌ Registration error: ${responseBody.message}")
                    }
                } else {
                    viewModel?.isLoading = false
                    viewModel?.errorMessage = responseBody.message
                    Toast.makeText(context, responseBody.message, Toast.LENGTH_LONG).show()
                    Log.e("API", "⚠️ Unexpected response: ${response.status}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                viewModel?.isLoading = false
                viewModel?.errorMessage = "Registration failed: ${e.message}"
                Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
            Log.e("API", "❌ Exception during registration: ${e.message}", e)
        } finally {
            client?.close()
        }
    }
}