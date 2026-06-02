//package com.example.mealflow.network
//
//import android.content.Context
//import android.util.Log
//import android.widget.Toast
//import com.example.mealflow.database.UserPreferencesManager
//import com.example.mealflow.database.token.TokenManager
//import io.ktor.client.request.delete
//import io.ktor.client.request.headers
//import io.ktor.client.statement.HttpResponse
//import io.ktor.client.statement.bodyAsText
//import io.ktor.http.HttpHeaders
//import io.ktor.http.isSuccess
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.json.Json
//
//@Serializable
//data class RemoveMemberResponse(
//    val success: Boolean,
//    val message: String,
//    val data: MemberData?
//)
//
//@Serializable
//data class MemberData(
//    val communityId: String,
//    val userId: String,
//    val role: String,
//    val joinedAt: String,
//    val leftAt: String?,
//    val isPending: Boolean,
//    val user: UserInfo
//)
//
//@Serializable
//data class UserInfo(
//    val id: String,
//    val name: String,
//    val lastName: String,
//    val username: String,
//    val profilePicture: String?
//)
//
//@Serializable
//data class ErrorResponse(
//    val success: Boolean,
//    val message: String
//)
//
//class RemoveMemberClass(private val context: Context) {
//    private val apiClientToken = ApiClientToken(context)
//
//    suspend fun removeMember(memberId: String): Boolean {
//        val tokenManager = TokenManager(context)
//        val token = tokenManager.getAccessToken()
//
//        if (token.isNullOrEmpty()) {
//            Log.e("RemoveMember", "Token is null or empty")
//            withContext(Dispatchers.Main) {
//                Toast.makeText(context, "Authentication required", Toast.LENGTH_SHORT).show()
//            }
//            return false
//        }
//
//        val communityId = UserPreferencesManager(context).getCommunityId().first()
//        if (communityId.isNullOrEmpty()) {
//            Log.e("RemoveMember", "Community ID is null or empty")
//            withContext(Dispatchers.Main) {
//                Toast.makeText(context, "Community not found", Toast.LENGTH_SHORT).show()
//            }
//            return false
//        }
//
//        val url = "https://mealflow.online/api/v3/community/$communityId/members/$memberId"
//
//        return try {
//            Log.d("RemoveMember", "Removing member $memberId from community $communityId")
//
//            val response: HttpResponse = withContext(Dispatchers.IO) {
//                apiClientToken.client.delete(url) {
//                    headers {
//                        append(HttpHeaders.Authorization, "Bearer $token")
//                    }
//                }
//            }
//
//            val responseBody = response.bodyAsText()
//            Log.d("RemoveMember", "Response: $responseBody")
//
//            when {
//                response.status.isSuccess() -> {
//                    try {
//                        val successResponse = Json.decodeFromString<RemoveMemberResponse>(responseBody)
//                        val memberName = successResponse.data?.user?.name ?: "Member"
//
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(context, "Successfully removed $memberName", Toast.LENGTH_SHORT).show()
//                        }
//                        Log.d("RemoveMember", "Member removed successfully: $memberName")
//                        true
//                    } catch (e: Exception) {
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(context, "Member removed successfully", Toast.LENGTH_SHORT).show()
//                        }
//                        Log.d("RemoveMember", "Member removed successfully (parsing error: ${e.message})")
//                        true
//                    }
//                }
//                response.status.value == 401 -> {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Authentication invalid", Toast.LENGTH_SHORT).show()
//                    }
//                    Log.e("RemoveMember", "Authentication invalid")
//                    false
//                }
//                response.status.value == 403 -> {
//                    try {
//                        val errorResponse = Json.decodeFromString<ErrorResponse>(responseBody)
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(context, errorResponse.message, Toast.LENGTH_LONG).show()
//                        }
//                        Log.e("RemoveMember", "Forbidden: ${errorResponse.message}")
//                    } catch (e: Exception) {
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
//                        }
//                        Log.e("RemoveMember", "Forbidden - Permission denied")
//                    }
//                    false
//                }
//                response.status.value == 404 -> {
//                    try {
//                        val errorResponse = Json.decodeFromString<ErrorResponse>(responseBody)
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(context, errorResponse.message, Toast.LENGTH_LONG).show()
//                        }
//                        Log.e("RemoveMember", "Not found: ${errorResponse.message}")
//                    } catch (e: Exception) {
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(context, "Member or community not found", Toast.LENGTH_SHORT).show()
//                        }
//                        Log.e("RemoveMember", "Not found - Member or community not found")
//                    }
//                    false
//                }
//                else -> {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Failed to remove member: ${response.status}", Toast.LENGTH_SHORT).show()
//                    }
//                    Log.e("RemoveMember", "Failed to remove member: ${response.status}")
//                    false
//                }
//            }
//
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) {
//                Toast.makeText(context, "Error removing member", Toast.LENGTH_SHORT).show()
//            }
//            Log.e("RemoveMember", "Error removing member", e)
//            false
//        }
//    }
//}
package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RemoveMemberResponse(
    val success: Boolean,
    val message: String,
    val data: MemberData?
)

@Serializable
data class MemberData(
    val communityId: String,
    val userId: String,
    val role: String,
    val joinedAt: String,
    val leftAt: String?,
    val isPending: Boolean,
    val user: UserInfo
)

@Serializable
data class UserInfo(
    val id: String,
    val name: String,
    val lastName: String,
    val username: String,
    val profilePicture: String?
)

@Serializable
data class ErrorResponse(
    val success: Boolean,
    val message: String
)

class RemoveMemberClass(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)

    suspend fun removeMember(memberId: String): Boolean {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Log.e("RemoveMember", "Token is null or empty")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Authentication required", Toast.LENGTH_SHORT).show()
            }
            return false
        }

        val communityId = UserPreferencesManager(context).getCommunityId().first()
        if (communityId.isNullOrEmpty()) {
            Log.e("RemoveMember", "Community ID is null or empty")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Community not found", Toast.LENGTH_SHORT).show()
            }
            return false
        }

        val url = "https://mealflow.online/api/v3/community/$communityId/members/$memberId"

        return try {
            Log.d("RemoveMember", "Removing member $memberId from community $communityId")

            val response: HttpResponse = withContext(Dispatchers.IO) {
                apiClientToken.client.delete(url) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
            }

            val responseBody = response.bodyAsText()
            Log.d("RemoveMember", "Response: $responseBody")

            when {
                response.status.isSuccess() -> {
                    try {
                        val successResponse = Json.decodeFromString<RemoveMemberResponse>(responseBody)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, successResponse.message, Toast.LENGTH_SHORT).show()
                        }
                        Log.d("RemoveMember", "Success: ${successResponse.message}")
                        true
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Member removed successfully", Toast.LENGTH_SHORT).show()
                        }
                        Log.d("RemoveMember", "Member removed successfully (parsing error: ${e.message})")
                        true
                    }
                }
                else -> {
                    try {
                        val errorResponse = Json.decodeFromString<ErrorResponse>(responseBody)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, errorResponse.message, Toast.LENGTH_LONG).show()
                        }
                        Log.e("RemoveMember", "Error: ${errorResponse.message}")
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Failed to remove member", Toast.LENGTH_SHORT).show()
                        }
                        Log.e("RemoveMember", "Failed to remove member: ${response.status} - Response: $responseBody")
                    }
                    false
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Network error occurred", Toast.LENGTH_SHORT).show()
            }
            Log.e("RemoveMember", "Error removing member", e)
            false
        }
    }
}