package com.example.mealflow.network

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(val email: String, val password: String)

// ----------------------- LoginResponse ---------------------------
@Serializable
data class LoginResponse(val success: Boolean, val message: String, val data: Data? = null)

@Serializable
data class Data(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

// Result class to return success/failure status
data class LoginResult(
    val isSuccess: Boolean,
    val message: String,
    val data: Data? = null
)

suspend fun loginApi(
    context: Context,
    email: String,
    password: String,
    snackbarHostState: SnackbarHostState
): LoginResult {
    return try {
        val client = ApiClient.client
        val url = ApiClient.Endpoints.LOGIN
        val tokenManager = TokenManager(context)
        val userPrefs = UserPreferencesManager(context)

        Log.d("API", "📩 Send Request: email=$email, password=$password")

        val response: HttpResponse = withContext(Dispatchers.IO) {
            client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
        }

        val responseBody = response.body<LoginResponse>()

        // Display the message independently of the navigation
        withContext(Dispatchers.Main) {
            snackbarHostState.showSnackbar(
                message = responseBody.message,
                duration = SnackbarDuration.Short
            )
        }

        if (response.status.isSuccess() && responseBody.success) {
            Log.d("API", "✅ Successful login")

            responseBody.data?.let { data ->
                tokenManager.saveTokens(data.accessToken, data.refreshToken)
                userPrefs.saveMyId(data.user.id)
                userPrefs.saveFirstName(data.user.name ?: "")
                userPrefs.saveMyImageProfile(data.user.profilePicture ?: "")

                Log.d("user.id", "📩 User ID: ${data.user.id}")
                Log.d("API", "📩 Response: $responseBody")
                Log.d("accessToken", "accessToken: ${tokenManager.getAccessToken()}")
                Log.d("refreshToken", "refreshToken: ${tokenManager.getRefreshToken()}")
            }

            LoginResult(
                isSuccess = true,
                message = responseBody.message,
                data = responseBody.data
            )
        } else {
            Log.e("API", "❌ Login failed: ${responseBody.message}")
            LoginResult(
                isSuccess = false,
                message = responseBody.message
            )
        }

    } catch (e: Exception) {
        Log.e("API", "❌ Exception during login: ${e.localizedMessage}")

        withContext(Dispatchers.Main) {
            snackbarHostState.showSnackbar(
                message = "Error occurred while connecting to the server.",
                duration = SnackbarDuration.Short
            )
        }

        LoginResult(
            isSuccess = false,
            message = "Connection error: ${e.localizedMessage}"
        )
    }
}