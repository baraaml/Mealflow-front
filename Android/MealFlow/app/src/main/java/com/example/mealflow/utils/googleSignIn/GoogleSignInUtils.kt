package com.example.mealflow.utils.googleSignIn

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.mealflow.R
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.network.ApiClient
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class FirebaseGoogleResponse(
    val success: Boolean,
    val message: String,
    val data: FirebaseUserData? = null
)

@Serializable
data class FirebaseUserData(
    val accessToken: String,
    val refreshToken: String,
    val user: User,
    val isNewUser: Boolean
)

@Serializable
data class User(
    val id: String,
    val email: String,
    val username: String,
    val name: String,
    val lastName: String,
    val profilePicture: String,
    val isVerified: Boolean
)

fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("278656087482-57432k5s896ih5kfni1fim5t8cr8830j.apps.googleusercontent.com")
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(context, gso)
}

// Alternative method using web client ID from resources
fun getGoogleSignInClientFromResources(context: Context): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(context, gso)
}

fun handleGoogleSignInResult(
    context: Context,
    data: Intent?,
    viewModel: GoogleSignInViewModel,
    navController: NavController
) {
    try {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.getResult(ApiException::class.java)

        Log.d("GoogleSignIn", "✅ Google sign-in success: ${account.email}")
        logAccountDetails(account)

        // Proceed with Firebase authentication
        firebaseAuthWithGoogle(context, account.idToken!!, viewModel, navController)

    } catch (e: ApiException) {
        viewModel.setError("Google sign-in failed: ${e.message}")
        Toast.makeText(context, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
        Log.e("GoogleSignIn", "❌ Google sign-in failed with code: ${e.status}", e)
    }
}

private fun firebaseAuthWithGoogle(
    context: Context,
    idToken: String,
    viewModel: GoogleSignInViewModel,
    navController: NavController
) {
    val auth = FirebaseAuth.getInstance()
    val credential = GoogleAuthProvider.getCredential(idToken, null)

    viewModel.setLoading(true)

    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                Log.d("GoogleSignIn", if (isNewUser) "🆕 New User Registered" else "✅ Existing User Logged In")

                // Get fresh Firebase ID token and send to backend
                user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                    if (tokenTask.isSuccessful) {
                        val firebaseToken = tokenTask.result?.token
                        if (firebaseToken != null) {
                            // Send token to your backend API
                            viewModel.signInWithGoogle(context, firebaseToken, navController)
                        } else {
                            viewModel.setError("Failed to get Firebase token")
                        }
                    } else {
                        viewModel.setError("Failed to get Firebase ID Token: ${tokenTask.exception?.message}")
                        Log.e("GoogleSignIn", "❌ Failed to get Firebase ID Token", tokenTask.exception)
                    }
                }
            } else {
                viewModel.setError("Firebase authentication failed: ${task.exception?.message}")
                Log.e("GoogleSignIn", "❌ Firebase authentication failed", task.exception)
            }
        }
}

fun firebaseGoogleLogin(
    context: Context,
    firebaseIdToken: String,
    navController: NavController,
    viewModel: GoogleSignInViewModel
) {
    val tokenManager = TokenManager(context)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

            val url = ApiClient.Endpoints.FIREBASE_GOOGLE_LOGIN

            Log.d("API", "🔹Sending Google Login request to: $url")
            Log.d("API", "📩 Firebase Token: $firebaseIdToken")

            val response: HttpResponse = client.post(url) {
                header("Authorization", "Bearer $firebaseIdToken")
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
            }

            val responseText = response.bodyAsText()
            Log.d("API", "🔹Server response status: ${response.status}")
            Log.d("API", "🔹Server response body: $responseText")

            withContext(Dispatchers.Main) {
                when (response.status) {
                    HttpStatusCode.OK -> {
                        try {
                            val responseBody = Json.decodeFromString<FirebaseGoogleResponse>(responseText)
                            handleSuccessfulResponse(context, responseBody, tokenManager, viewModel, navController)
                        } catch (e: Exception) {
                            Log.e("API", "❌ Failed to parse response: $responseText", e)
                            viewModel.setError("Invalid response format from server")
                        }
                    }
                    HttpStatusCode.Unauthorized -> {
                        viewModel.setError("Authentication failed. Please try again.")
                        Toast.makeText(context, "Authentication failed", Toast.LENGTH_LONG).show()
                    }
                    HttpStatusCode.InternalServerError -> {
                        viewModel.setError("Server error. Please try again later.")
                        Toast.makeText(context, "Server error occurred", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        viewModel.setError("Login failed with status: ${response.status}")
                        Toast.makeText(context, "Login failed", Toast.LENGTH_LONG).show()
                    }
                }
            }

            client.close()

        } catch (e: Exception) {
            Log.e("API", "❌ Exception during login", e)
            withContext(Dispatchers.Main) {
                val errorMsg = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> "Connection timeout. Please check your internet."
                    e.message?.contains("connection", ignoreCase = true) == true -> "Connection failed. Please try again."
                    else -> "Login failed: ${e.message}"
                }
                viewModel.setError(errorMsg)
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }
}

private fun handleSuccessfulResponse(
    context: Context,
    responseBody: FirebaseGoogleResponse,
    tokenManager: TokenManager,
    viewModel: GoogleSignInViewModel,
    navController: NavController
) {
    if (responseBody.success && responseBody.data != null) {
        val userData = responseBody.data

        // Save tokens
        tokenManager.saveTokens(userData.accessToken, userData.refreshToken)
        Log.d("API", "✅ Tokens saved successfully")

        // Show welcome message
        val welcomeMessage = if (userData.isNewUser) {
            "Welcome to MealFlow, ${userData.user.name}!"
        } else {
            "Welcome back, ${userData.user.name}!"
        }

        Toast.makeText(context, welcomeMessage, Toast.LENGTH_SHORT).show()

        // Update ViewModel state
        viewModel.setSuccess(userData.isNewUser)
        val userPrefs = UserPreferencesManager(context)

        // Navigate based on user status - API handles all navigation
        try {
            CoroutineScope(Dispatchers.IO).launch {
                userPrefs.saveMyId(userData.user.id)
                userPrefs.saveFirstName(userData.user.name)
                userPrefs.saveMyImageProfile(userData.user.profilePicture)
            }
            if (userData.isNewUser) {
                Log.d("GoogleSignIn", "✅ New user - navigating to setup_welcome")
                navController.navigate("setup_welcome") {
                    // Remove all previous pages from the back stack
                    popUpTo(0) { inclusive = true }
                    // Prevent opening the same page multiple times
                    launchSingleTop = true
                    // Prevent page state recovery
                    restoreState = false
                }
            } else {
                Log.d("GoogleSignIn", "✅ Existing user - navigating to Home Page")
                navController.navigate("Home Page") {
                    popUpTo("Login Page") { inclusive = true }
                    launchSingleTop = true
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "❌ Navigation error: ${e.message}", e)
            // Set error state if navigation fails
            viewModel.setError("Navigation failed. Please try again.")
            Toast.makeText(context, "Navigation failed. Please try again.", Toast.LENGTH_LONG).show()
        }

    } else {
        viewModel.setError(responseBody.message)
        Toast.makeText(context, responseBody.message, Toast.LENGTH_LONG).show()
        Log.e("API", "❌ Login error: ${responseBody.message}")
    }
}

private fun logAccountDetails(account: GoogleSignInAccount) {
    Log.d("GoogleSignIn", """
        Account Details:
        DisplayName: ${account.displayName}
        Email: ${account.email}
        Id: ${account.id}
        IdToken: ${account.idToken?.take(50)}...
        PhotoUrl: ${account.photoUrl}
        GivenName: ${account.givenName}
        FamilyName: ${account.familyName}
    """.trimIndent())
}