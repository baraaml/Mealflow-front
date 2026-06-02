package com.example.mealflow.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class CommentAuthor(
    val id: String,
    val name: String? = null,
    val username: String,
    val profilePicture: String? = null
)

@Serializable
data class CommentData(
    val id: String,
    val content: String,
    val postId: String,
    val authorId: String,
    val parentId: String? = null,
    val isDeleted: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val author: CommentAuthor? = null
)

@Serializable
data class CommentResponse(
    val success: Boolean,
    val message: String,
    val data: CommentData
)

@Serializable
data class CommentItem(
    val id: String,
    val content: String,
    val postId: String,
    val authorId: String,
    val parentId: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Boolean,
    val likeCount: Int,
    val hasLiked: Boolean = false,
    val author: CommentAuthor
)

@Serializable
data class CommentPagination(
    val nextCursor: String?,
    val prevCursor: String?,
    val hasMore: Boolean
)

@Serializable
data class GetCommentData(
    val comments: List<CommentItem>,
    val pagination: CommentPagination
)

@Serializable
data class GetCommentsResponse(
    val success: Boolean,
    val message: String,
    val length: Int,
    val data: GetCommentData
)

@Serializable
data class UpdatedComment(
    val id: String,
    val content: String,
    val postId: String,
    val authorId: String,
    val parentId: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val author: CommentAuthor
)

@Serializable
data class UpdateCommentResponse(
    val success: Boolean,
    val message: String,
    val data: UpdatedComment
)

class CommentApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)
    val tokenManager = TokenManager(context)
    val token = tokenManager.getAccessToken()

    suspend fun createComment(
        postId: String,
        content: String,
        parentId: String? = null
    ): CommentResponse? = withContext(Dispatchers.IO) {
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Token is null or empty", Toast.LENGTH_LONG).show()
            return@withContext null
        }

        return@withContext try {
            val response: HttpResponse = apiClientToken.client.post("https://mealflow.online/api/v3/posts/$postId/comments") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(
                    mapOf(
                        "content" to content,
                        "parentId" to parentId
                    )
                )
            }

            if (!response.status.isSuccess()) {
                Toast.makeText(context, "Failed to create comment", Toast.LENGTH_LONG).show()
                null
            } else {
                val responseBody = response.body<CommentResponse>()
                Log.d("Comment", "API Response: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e("Comment", "Error creating comment (${e.javaClass.simpleName})", e)
            Toast.makeText(context, "Error creating comment", Toast.LENGTH_LONG).show()
            null
        }
    }

    suspend fun getPostComments(
        postId: String,
        limit: Int = 5,
        cursor: String? = null,
        direction: String = "before"
    ): GetCommentsResponse? = withContext(Dispatchers.IO) {
        val token = tokenManager.getAccessToken()

        return@withContext try {
            val response: HttpResponse = apiClientToken.client.get("https://mealflow.online/api/v3/posts/$postId/comments") {
                headers {
                    if (!token.isNullOrEmpty()) {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
                url {
                    parameters.append("limit", limit.toString())
                    if (cursor != null) parameters.append("cursor", cursor)
                    parameters.append("direction", direction)
                }
            }

            if (!response.status.isSuccess()) {
                Toast.makeText(context, "Failed to fetch comments", Toast.LENGTH_LONG).show()
                null
            } else {
                val responseBody = response.body<GetCommentsResponse>()
                Log.d("Comments", "API Response: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e("Comments", "Error fetching comments", e)
            Toast.makeText(context, "Error fetching comments", Toast.LENGTH_LONG).show()
            null
        }
    }

    suspend fun updateComment(commentId: String, newContent: String): UpdateCommentResponse? = withContext(Dispatchers.IO) {
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Authentication required", Toast.LENGTH_LONG).show()
            return@withContext null
        }

        return@withContext try {
            val response: HttpResponse = apiClientToken.client.patch("https://mealflow.online/api/v3/comments/$commentId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(mapOf("content" to newContent))
            }

            if (!response.status.isSuccess()) {
                Toast.makeText(context, "Failed to update comment", Toast.LENGTH_LONG).show()
                null
            } else {
                val responseBody = response.body<UpdateCommentResponse>()
                Log.d("UpdateComment", "API Response: $responseBody")
                responseBody
            }
        } catch (e: Exception) {
            Log.e("UpdateComment", "Error updating comment", e)
            Toast.makeText(context, "Error updating comment", Toast.LENGTH_LONG).show()
            null
        }
    }

    suspend fun deleteComment(commentId: String): Boolean = withContext(Dispatchers.IO) {
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Authentication required", Toast.LENGTH_LONG).show()
            return@withContext false
        }

        return@withContext try {
            val response: HttpResponse = apiClientToken.client.delete("https://mealflow.online/api/v3/comments/$commentId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            if (response.status.isSuccess()) {
                Toast.makeText(context, "Comment deleted successfully", Toast.LENGTH_SHORT).show()
                true
            } else {
                Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_LONG).show()
                false
            }
        } catch (e: Exception) {
            Log.e("DeleteComment", "Error deleting comment", e)
            Toast.makeText(context, "Error deleting comment", Toast.LENGTH_LONG).show()
            false
        }
    }
}