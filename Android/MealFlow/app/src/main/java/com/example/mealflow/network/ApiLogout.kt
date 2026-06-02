//package com.example.mealflow.network
//
//import android.content.Context
//import android.util.Log
//import android.widget.Toast
//import androidx.navigation.NavController
//import com.example.mealflow.database.UserPreferencesManager
//import com.example.mealflow.database.token.TokenManager
//import io.ktor.client.call.body
//import io.ktor.client.request.post
//import io.ktor.client.request.setBody
//import io.ktor.client.statement.HttpResponse
//import io.ktor.http.ContentType
//import io.ktor.http.contentType
//import io.ktor.http.isSuccess
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.Serializable
//
//// ----------------------- LogoutRequest ---------------------------
//@Serializable
//data class LogoutRequest(val refreshToken: String)
//
//// ----------------------- LogoutResponse ---------------------------
//@Serializable
//data class LogoutResponse(val success: Boolean, val message: String,val data: Data? = null)
//
//
//fun logoutApi(
//    context: Context,
//    navController: NavController
//) {
//    CoroutineScope(Dispatchers.IO).launch {
//        val client = ApiClient.client
//        val url = ApiClient.Endpoints.LOGOUT
//        val tokenManager = TokenManager(context)
//        val userPrefs = UserPreferencesManager(context)
//        val refreshToken = tokenManager.getRefreshToken()
//        try {
//            Log.d("API", "📩Send request: refreshToken = $refreshToken")
//            val response: HttpResponse = client.post(url) {
//                contentType(ContentType.Application.Json)
//                setBody(LogoutRequest(refreshToken.toString()))
//            }
//
//            val responseBody = response.body<LogoutResponse>()
//
//            withContext(Dispatchers.Main) {
//                // Display the message independently of the transition
//                CoroutineScope(Dispatchers.Main).launch {
//                    Toast.makeText(context, responseBody.message, Toast.LENGTH_SHORT).show()
//                    delay(1000)
//                }
//                // Go directly without waiting
//                if (response.status.isSuccess() && responseBody.success) {
//                    Log.d("Logout API", "✅ Successful logout, go to the next page")
//                    Log.d("accessToken", "accessToken: ${tokenManager.getAccessToken()}")
//                    Log.d("refreshToken", "refreshToken: ${tokenManager.getRefreshToken()}")
//                    tokenManager.clearAccessToken()
//                    userPrefs.clearPreferences()
//                    navController.navigate("Login Page")
//                } else {
//                    Log.e("Logout API", "❌ Logout failed: ${responseBody.message}")
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("Logout API", "❌ Exception during order execution:${e.localizedMessage}")
//            withContext(Dispatchers.Main) {
//                Toast.makeText(context, "Error occurred while connecting to the server.", Toast.LENGTH_SHORT).show()
//                delay(1000)
//            }
//        }
//    }
//}
package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.utils.googleSignIn.getGoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

// ----------------------- LogoutRequest ---------------------------
@Serializable
data class LogoutRequest(val refreshToken: String)

// ----------------------- LogoutResponse ---------------------------
@Serializable
data class LogoutResponse(val success: Boolean, val message: String,val data: Data? = null)


fun logoutApi(
    context: Context,
    navController: NavController
) {
    CoroutineScope(Dispatchers.IO).launch {
        val client = ApiClient.client
        val url = ApiClient.Endpoints.LOGOUT
        val tokenManager = TokenManager(context)
        val userPrefs = UserPreferencesManager(context)
        val refreshToken = tokenManager.getRefreshToken()

        try {
            Log.d("API", "📩Send request: refreshToken = $refreshToken")
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(LogoutRequest(refreshToken.toString()))
            }

            val responseBody = response.body<LogoutResponse>()

            withContext(Dispatchers.Main) {
                // Display the message independently of the transition
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, responseBody.message, Toast.LENGTH_SHORT).show()
                    delay(1000)
                }

                // Go directly without waiting
                if (response.status.isSuccess() && responseBody.success) {
                    Log.d("Logout API", "✅ Successful logout, go to the next page")
                    Log.d("accessToken", "accessToken: ${tokenManager.getAccessToken()}")
                    Log.d("refreshToken", "refreshToken: ${tokenManager.getRefreshToken()}")

                    // Clear local tokens and preferences
                    tokenManager.clearAccessToken()
                    userPrefs.clearPreferences()

                    // Sign out from Google and Firebase
                    signOutFromGoogleAndFirebase(context) {
                        // Navigate to login page after Google sign out is complete
                        navController.navigate("Login Page") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }

                } else {
                    Log.e("Logout API", "❌ Logout failed: ${responseBody.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("Logout API", "❌ Exception during order execution:${e.localizedMessage}")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error occurred while connecting to the server.", Toast.LENGTH_SHORT).show()
                delay(1000)
            }
        }
    }
}

// Helper function to sign out from Google and Firebase
private fun signOutFromGoogleAndFirebase(context: Context, onComplete: () -> Unit) {
    try {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()
        Log.d("Logout", "✅ Firebase sign out successful")

        // Sign out from Google
        val googleSignInClient = getGoogleSignInClient(context)
        googleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Logout", "✅ Google sign out successful")
            } else {
                Log.e("Logout", "❌ Google sign out failed: ${task.exception?.message}")
            }
            // Call onComplete regardless of success/failure
            onComplete()
        }

    } catch (e: Exception) {
        Log.e("Logout", "❌ Exception during Google/Firebase sign out: ${e.message}")
        // Still call onComplete to ensure navigation happens
        onComplete()
    }
}