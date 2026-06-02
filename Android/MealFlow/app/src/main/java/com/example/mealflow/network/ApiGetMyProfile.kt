package com.example.mealflow.network


import android.content.Context
import android.util.Log
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


@Serializable
data class UserProfileResponse(
    val success: Boolean,
    val message: String,
    val data: UserProfile
)

@Serializable
data class UserProfile(
    val id: String,
    val name: String? = "",
    val lastName: String? = "",
    val username: String,
    val email: String,
    val isVerified: Boolean? = null,
    val verifiedAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val birthDate: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val gender: String,
    val bio: String? = null,
    val profilePicture: String? = null,
    val coverPicture: String? = null,
    val location: String? = null,
    val isFollowing: Boolean? = null,
    val communitiesOwned: List<CommunityProfileData> = emptyList(),
    val _count: Count? = null,
) {
    companion object {
        fun empty() = UserProfile(
            id = "",
            name = "",
            lastName = "",
            username = "",
            email = "",
            isVerified = false,
            verifiedAt = null,
            createdAt = "",
            updatedAt = "",
            birthDate = null,
            height = null,
            weight = null,
            gender = "",
            bio = null,
            profilePicture = null,
            coverPicture = null,
            location = null,
            communitiesOwned = emptyList(),
        )
    }
}

@Serializable
data class CommunityProfileData(
    val id: String,
    val name: String,
    val description: String,
    val ownerId: String,
    val image: String?,
    val privacy: String,
    val mealCreationPermission: String,
    val createdAt: String,
    val updatedAt: String,
    val categories: List<String>? = null,  // Nullable list
    val members: List<String>? = null,    // Nullable list
    val owner: String? = null              // Nullable string
)

@Serializable
data class Count(
    val posts: Int,
    val followers: Int,
    val following: Int,
)

class MyProfileApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)

    suspend fun fetchMyProfile(): UserProfileResponse {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()
        val userPrefs = UserPreferencesManager(context)

        Log.d("Token", "Token : $token")

        if (token.isNullOrEmpty()) {
            Log.e("UserProfile", "Token is null or empty")
            return UserProfileResponse(false, "Token is null or empty", UserProfile.empty())
        }

        Log.d("UserProfile", "fetchUserProfile() called")
        Log.d("Token", "Token : $token")

        return try {
            // هنا نلف الاستدعاء داخل withContext
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.get("https://mealflow.online/api/v3/users/me") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

                Log.d("API Response", response.bodyAsText())
                Log.d("response", "Response status: ${response.status}")

                if (!response.status.isSuccess()) {
                    Log.e("UserProfile", "Failed to fetch UserProfile: ${response.status}")
                    return@withContext UserProfileResponse(false, "Failed to fetch UserProfile", UserProfile.empty())
                }

                val responseBody = response.body<UserProfileResponse>()
                Log.d("UserProfile", "API Response: $responseBody")
                userPrefs.saveFirstName(responseBody.data.name.toString())
                userPrefs.saveMyImageProfile(responseBody.data.profilePicture ?: "")
                responseBody
            }
        } catch (e: Exception) {
            Log.e("UserProfile", "Error fetching UserProfile", e)
            UserProfileResponse(false, "Error fetching UserProfile: ${e.message}", UserProfile.empty())
        }
    }
}
