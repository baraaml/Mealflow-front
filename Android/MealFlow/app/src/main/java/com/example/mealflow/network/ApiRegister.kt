//package com.example.mealflow.network
//
//import android.content.Context
//import android.util.Log
//import androidx.compose.material3.SnackbarDuration
//import androidx.compose.material3.SnackbarHostState
//import androidx.navigation.NavController
//import com.example.mealflow.database.token.TokenManager
//import io.ktor.client.call.body
//import io.ktor.client.request.post
//import io.ktor.client.request.setBody
//import io.ktor.client.statement.HttpResponse
//import io.ktor.client.statement.bodyAsText
//import io.ktor.http.ContentType
//import io.ktor.http.HttpStatusCode
//import io.ktor.http.contentType
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.json.Json
//
//@Serializable
//data class RegisterRequest(val username: String, val email: String, val password: String)
//
//@Serializable
//data class RegisterResponse(val success: Boolean, val message: String,val data: Data? = null)
//
//fun registerUser(
//    context: Context,
//    username: String,
//    email: String,
//    password: String,
//    navController: NavController,
//    onError: (String) -> Unit,
//    snackbarHostState: SnackbarHostState
//) {
//    CoroutineScope(Dispatchers.IO).launch {
//        val client = ApiClient.client
//        val url = ApiClient.Endpoints.REGISTER
//        val tokenManager = TokenManager(context)
//
//        try {
//            val response: HttpResponse = client.post(url) {
//                contentType(ContentType.Application.Json)
//                setBody(RegisterRequest(username, email, password))
//            }
//
//            val responseBodyText = response.bodyAsText()
//
//            val message = try {
//                Json.decodeFromString<RegisterResponse>(responseBodyText).message
//            } catch (e: Exception) {
//                Regex("\"message\"\\s*:\\s*\"(.*?)\"").find(responseBodyText)?.groupValues?.get(1)
//                    ?: "An unknown error occurred."
//            }
//
//            Log.d("RegisterUser", "Response Status: ${response.status}")
//            Log.d("RegisterUser", "Message: $message")
//
//            withContext(Dispatchers.Main) {
//                // Display the Snackbar here after getting the message
//                snackbarHostState.showSnackbar(
//                    message = message,
//                    duration = SnackbarDuration.Short
//                )
//
//                if (response.status == HttpStatusCode.OK || message == "User account created successfully. Please check your email for the verification code.") {
//                    val responseBody = response.body<RegisterResponse>()
//                    Log.d("RegisterUser", "✅ Successful registration: Navigate to OtpPage")
//                    responseBody.data?.let {
//                        tokenManager.saveTokens(it.accessToken, it.refreshToken)
//                    }
//                    navController.navigate("Otp Page") {
//                        popUpTo("Register Page") { inclusive = true }
//                        launchSingleTop = true
//                    }
//                } else {
//                    Log.e("RegisterUser", "❌ Registration failed:$message")
//                    onError(message)
//                }
//            }
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) {
//                snackbarHostState.showSnackbar(
//                    message = "Error occurred while connecting to the server.",
//                    duration = SnackbarDuration.Short
//                )
//            }
//        }
//    }
//}
//package com.example.mealflow.network
//
//import android.content.Context
//import android.util.Log
//import android.widget.Toast
//import androidx.navigation.NavController
//import com.example.mealflow.database.token.TokenManager
//import com.example.mealflow.viewModel.RegisterViewModel
//import io.ktor.client.*
//import io.ktor.client.engine.cio.*
//import io.ktor.client.plugins.contentnegotiation.*
//import io.ktor.client.request.*
//import io.ktor.client.statement.*
//import io.ktor.http.*
//import io.ktor.serialization.kotlinx.json.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.json.Json
//
//@Serializable
//data class RegisterRequest(val username: String, val email: String, val password: String)
//
//@Serializable
//data class RegisterResponse(val success: Boolean, val message: String, val data: Data? = null)
//
//fun registerUser(
//    context: Context,
//    username: String,
//    email: String,
//    password: String,
//    navController: NavController,
//    viewModel: RegisterViewModel? = null
//) {
//    val tokenManager = TokenManager(context)
//
//    CoroutineScope(Dispatchers.IO).launch {
//        try {
//            val client = HttpClient(CIO) {
//                install(ContentNegotiation) {
//                    json(Json { ignoreUnknownKeys = true })
//                }
//            }
//
//            val url = ApiClient.Endpoints.REGISTER
//
//            Log.d("API", "🔹Send registration request: $url")
//            Log.d("API", "📩 Request data: username=$username, email=$email")
//
//            val response: HttpResponse = client.post(url) {
//                contentType(ContentType.Application.Json)
//                accept(ContentType.Application.Json)
//                setBody(RegisterRequest(username, email, password))
//            }
//
//            val responseText = response.bodyAsText()
//            val responseBody = Json.decodeFromString<RegisterResponse>(responseText)
//            Log.d("API", "🔹Server response: $responseText")
//
//            withContext(Dispatchers.Main) {
//                if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
//                    if (responseBody.success) {
//                        responseBody.data?.let {
//                            tokenManager.saveTokens(it.accessToken, it.refreshToken)
//                            Log.d("API", "📩 Tokens saved successfully")
//                        }
//                        Log.d("API", "✅ Registration successful! Go to OTP page")
//
//                        Toast.makeText(context, "Account created successfully! Check your email", Toast.LENGTH_SHORT).show()
//                        viewModel?.resetState() // Reset state after successful registration
//                        navController.navigate("Otp Page/${email}") {
//                            popUpTo("Register Page") { inclusive = true }
//                            launchSingleTop = true
//                        }
//                    } else {
//                        viewModel?.isLoading = false
//                        viewModel?.errorMessage = responseBody.message
//                        Toast.makeText(context, responseBody.message, Toast.LENGTH_LONG).show()
//                        Log.e("API", "❌ Registration error: ${responseBody.message}")
//                    }
//                } else {
//                    viewModel?.isLoading = false
//                    viewModel?.errorMessage = responseBody.message
//                    Toast.makeText(context, responseBody.message, Toast.LENGTH_LONG).show()
//                    Log.e("API", "⚠️ Unexpected response: ${response.status}")
//                }
//            }
//            client.close()
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) {
//                viewModel?.isLoading = false
//                viewModel?.errorMessage = "Registration failed: ${e.message}"
//                Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//            Log.e("API", "❌ Exception during registration: ${e.message}", e)
//        }
//    }
//}

package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.mealflow.database.token.TokenManager
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
                        navController.navigate("Otp Page/${email}") {
                            popUpTo("Register Page") { inclusive = true }
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