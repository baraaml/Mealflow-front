//package com.example.mealflow.network
//
//import android.content.Context
//import android.util.Log
//import com.example.mealflow.data.model.Pagination
//import com.example.mealflow.database.UserPreferencesManager
//import com.example.mealflow.database.token.TokenManager
//import io.ktor.client.call.body
//import io.ktor.client.request.get
//import io.ktor.client.statement.HttpResponse
//import io.ktor.client.statement.bodyAsText
//import io.ktor.http.isSuccess
//import kotlinx.coroutines.flow.first
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class CommunityMembersResponse(
//    val success: Boolean,
//    val count: Int,
//    val members: List<CommunityMember>,
//    val pagination: Pagination? = null
//)
//
//@Serializable
//data class CommunityMember(
//    val role: String,
//    val joinedAt: String,
//    val leftAt: String?,
//    val user: UserMember
//)
//
//@Serializable
//data class UserMember(
//    val id: String,
//    val name: String?,
//    val lastName: String?,
//    val profilePicture: String?,
//    val coverPicture: String?,
//    val username: String,
//    val email: String
//)
//
//class CommunityMembers(private val context: Context) {
//    private val apiClientToken = ApiClientToken(context)
//    suspend fun fetchCommunityMembers(): CommunityMembersResponse {
//        val tokenManager = TokenManager(context)
//        val token = tokenManager.getAccessToken()
//        val communityIdFlow = UserPreferencesManager(context).getCommunityId()
//        val communityId = communityIdFlow.first()
//        val url = "https://mealflow.online/api/v3/community/$communityId/members"
//        Log.d("Token", "Token : $token")
//        Log.d("URL", "URL : $url")
//        if (token.isNullOrEmpty()) {
//            Log.e("Community", "Token is null or empty")
//            return CommunityMembersResponse(false, 0, emptyList())
//        }
//        Log.d("Community", "fetchCommunities() called")
//        Log.d("Token", "Token : $token")
//        return try {
//            val response: HttpResponse = apiClientToken.client.get(url)
//
//            Log.d("API Response", response.bodyAsText())
//            Log.d("response", "Response status: ${response.status}")
//
//            if (!response.status.isSuccess()) {
//                Log.e("Community", "Failed to fetch community members : ${response.status}")
//                return CommunityMembersResponse(false, 0, emptyList())
//            }
//            val responseBody = response.body<CommunityMembersResponse>()
//            Log.d("Community", "API Response: $responseBody")
//            responseBody
//
//        } catch (e: Exception) {
//            Log.e("Community", "Error fetching communities members", e)
//            CommunityMembersResponse(false, 0, emptyList())
//        }
//    }
//}
package com.example.mealflow.network

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.Pagination
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class CommunityMembersResponse(
    val success: Boolean,
    val message: String,
    val count: Int,
    val data: CommunityMembersData
)

@Serializable
data class CommunityMembersData(
    val members: List<CommunityMember>,
    val owner: UserMember,
    val pagination: Pagination
)

@Serializable
data class CommunityMember(
    val role: String,
    val joinedAt: String,
    val leftAt: String?,
    val user: UserMember
)

@Serializable
data class UserMember(
    val id: String,
    val name: String?,
    val lastName: String?,
    val profilePicture: String?,
    val coverPicture: String? = null,
    val username: String,
    val email: String? = null // تعديل هنا
)

class CommunityMembersApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)
    companion object {
        fun emptyResponse() = CommunityMembersResponse(
            success = false,
            message = "",
            count = 0,
            data = CommunityMembersData(
                members = emptyList(),
                owner = UserMember(
                    id = "",
                    name = null,
                    lastName = null,
                    profilePicture = null,
                    coverPicture = null,
                    username = "",
                    email = ""
                ),
                pagination = Pagination(
                    nextCursor = "",
                    prevCursor = "",
                    hasMore = false
                )
            )
        )
    }

    suspend fun fetchCommunityMembers(
        limit: Int = 10,
        cursor: String? = null
    ): CommunityMembersResponse {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()
        val communityIdFlow = UserPreferencesManager(context).getCommunityId()
        val communityId = communityIdFlow.first()
        val url = "https://mealflow.online/api/v3/community/$communityId/members"

        Log.d("Token", "Token : $token")
        Log.d("URL", "URL : $url")

        if (token.isNullOrEmpty()) {
            Log.e("Community", "Token is null or empty")
            return emptyResponse()
        }

        Log.d("Community", "fetchCommunityMembers() called with limit: $limit, cursor: $cursor")

        return try {
            withContext(Dispatchers.IO) {
                val response: HttpResponse = apiClientToken.client.get(url) {
                    parameter("limit", limit)
                    cursor?.let { parameter("cursor", it) }
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

                Log.d("API Response", response.bodyAsText())
                Log.d("response", "Response status: ${response.status}")

                if (!response.status.isSuccess()) {
                    Log.e("Community", "Failed to fetch community members : ${response.status}")
                    return@withContext emptyResponse()
                }

                val responseBody = response.body<CommunityMembersResponse>()
                Log.d("Community", "API Response: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e("Community", "Error fetching community members", e)
            emptyResponse()
        }
    }
}
