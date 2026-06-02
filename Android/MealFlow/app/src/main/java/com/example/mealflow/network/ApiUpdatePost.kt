package com.example.mealflow.network

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.utils.JsonProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.io.File

@Serializable
data class UpdatePostResponse(
    val success: Boolean,
    val message: String,
    val data: PostDto? = null
)

@Serializable
data class PostDto(
    val id: String,
    val title: String,
    val content: String,
    val targetType: String,
    val mediaType: String? = null,
    val mediaUrl: String? = null,
    val isPinned: Boolean,
    val isEdited: Boolean,
    val lastEditedAt: String,
    val isHidden: Boolean,
    val hiddenReason: String? = null,
    val shareCount: Int,
    val authorId: String,
    val communityId: String? = null,
    val flairId: String,
    val viewCount: Int,
    val isLocked: Boolean,
    val allowComments: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val author: AuthorDto,
    val flair: FlairDto,
    val community: CommunityDto? = null
)

@Serializable
data class AuthorDto(
    val id: String,
    val name: String? = null,
    val username: String
)

@Serializable
data class FlairDto(
    val id: String,
    val name: String
)

@Serializable
data class CommunityDto(
    val id: String,
    val name: String
)

@OptIn(InternalAPI::class)
suspend fun updatePostProfileApi(
    context: Context,
    postId: String,
    title: String,
    content: String,
    flair: String,
    mediaUri: Uri?,
    mediaType: String?,
    isHidden: Boolean?,
    allowComments: Boolean?
): UpdatePostResponse? = withContext(Dispatchers.IO) {
    val tokenManager = TokenManager(context)
    val accessToken = tokenManager.getAccessToken()
    val apiClientToken = ApiClientToken(context)

    try {
        val now = getCurrentIsoTime()
        val url = "https://mealflow.online/api/v3/users/me/posts/$postId"

        val mediaFile: File? = mediaUri?.let { uri ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val extension = if (mediaType == "VIDEO") ".mp4" else ".jpg"
                val tempFile = File.createTempFile("post_media", extension, context.cacheDir)
                tempFile.outputStream().use { fileOut ->
                    inputStream?.copyTo(fileOut)
                }
                tempFile
            } catch (e: Exception) {
                Log.e("MediaUpload", "Error processing media: ${e.localizedMessage}")
                null
            }
        }

        val response = apiClientToken.client.patch(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }

            if (mediaFile != null) {
                contentType(ContentType.MultiPart.FormData)
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("title", title)
                            append("content", content)
                            append("targetType", "PROFILE")
                            append("mediaType", mediaType ?: "NONE")
                            append("flair", flair)
                            append("isEdited", "true")
                            append("updatedAt", now)
                            append("isHidden", isHidden == true)
                            append("allowComments", allowComments != false)
                            if (mediaType != null) {
                                append(
                                    "mediaUrl",
                                    mediaFile.readBytes(),
                                    Headers.build {
                                        append(HttpHeaders.ContentType, if (mediaType == "VIDEO") "video/mp4" else "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=${mediaFile.name}")
                                    }
                                )
                            }
                        }
                    )
                )
            } else {
                contentType(ContentType.Application.Json)
                val jsonBody = buildJsonObject {
                    put("title", JsonPrimitive(title))
                    put("content", JsonPrimitive(content))
                    put("targetType", JsonPrimitive("PROFILE"))
                    put("mediaType", JsonPrimitive(mediaType ?: "NONE"))
                    put("flair", JsonPrimitive(flair))
                    put("isEdited", JsonPrimitive(true))
                    put("updatedAt", JsonPrimitive(now))
                    put("isHidden", JsonPrimitive(isHidden == true))
                    put("allowComments", JsonPrimitive(allowComments != false))
                }
                setBody(jsonBody)
            }
        }

        val responseBody = response.bodyAsText()
        Log.d("PostUpdate", "Response: $responseBody")

        try {
            JsonProvider.json.decodeFromString<UpdatePostResponse>(responseBody)
        } catch (e: Exception) {
            Log.e("PostUpdate", "Parsing error: ${e.localizedMessage}")
            UpdatePostResponse(
                success = false,
                message = "Failed to parse response: ${e.localizedMessage}",
                data = null
            )
        }
    } catch (e: Exception) {
        Log.e("PostUpdate", "Error: ${e.localizedMessage}", e)
        UpdatePostResponse(
            success = false,
            message = "Error: ${e.localizedMessage}",
            data = null
        )
    }
}

//@OptIn(InternalAPI::class)
//fun updatePostProfileApi(
//    context: Context,
//    postId: String,
//    title: String,
//    content: String,
//    flair: String,
//    mediaUri: Uri?,
//    mediaType: String?,
//    isHidden: Boolean?,
//    allowComments: Boolean?,
//    viewModel: UpdatePostViewModel? = null
//) {
//    val tokenManager = TokenManager(context)
//    val accessToken = tokenManager.getAccessToken()
//    val apiClientToken = ApiClientToken(context)
//
//    CoroutineScope(Dispatchers.IO).launch {
//        try {
//            val now = getCurrentIsoTime()
//            val url = "https://mealflow.online/api/v3/users/me/posts/$postId"
//
//            val mediaFile: File? = mediaUri?.let { uri ->
//                try {
//                    val inputStream = context.contentResolver.openInputStream(uri)
//                    val extension = if (mediaType == "VIDEO") ".mp4" else ".jpg"
//                    val tempFile = File.createTempFile("post_media", extension, context.cacheDir)
//                    tempFile.outputStream().use { fileOut ->
//                        inputStream?.copyTo(fileOut)
//                    }
//                    tempFile
//                } catch (e: Exception) {
//                    Log.e("MediaUpload", "Error processing media: ${e.localizedMessage}")
//                    null
//                }
//            }
//
//            val response = apiClientToken.client.patch(url) {
//                headers {
//                    append(HttpHeaders.Authorization, "Bearer $accessToken")
//                }
//
//                if (mediaFile != null) {
//                    contentType(ContentType.MultiPart.FormData)
//                    setBody(
//                        MultiPartFormDataContent(
//                            formData {
//                                append("title", title)
//                                append("content", content)
//                                append("targetType", "PROFILE")
//                                append("mediaType", mediaType ?: "NONE")
//                                append("flair", flair)
//                                append("isEdited", "true") // Using string "true" instead of boolean
//                                append("updatedAt", now)
//                                append("isHidden", isHidden ?:false)
//                                append("allowComments", allowComments ?: true)
//                                if (mediaFile != null && mediaType != null) {
//                                    append(
//                                        "mediaUrl",
//                                        mediaFile.readBytes(),
//                                        Headers.build {
//                                            append(HttpHeaders.ContentType, if (mediaType == "VIDEO") "video/mp4" else "image/jpeg")
//                                            append(HttpHeaders.ContentDisposition, "filename=${mediaFile.name}")
//                                        }
//                                    )
//                                }
//                            }
//                        )
//                    )
//                } else {
//                    contentType(ContentType.Application.Json)
//                    // Use buildJsonObject to ensure proper serialization
//                    val jsonBody = buildJsonObject {
//                        put("title", JsonPrimitive(title))
//                        put("content", JsonPrimitive(content))
//                        put("targetType", JsonPrimitive("PROFILE"))
//                        put("mediaType", JsonPrimitive(mediaType ?: "NONE"))
//                        put("flair", JsonPrimitive(flair))
//                        put("isEdited", JsonPrimitive(true))
//                        put("updatedAt", JsonPrimitive(now))
//                        put("isHidden", JsonPrimitive(isHidden))
//                        put("allowComments", JsonPrimitive(allowComments))
//                    }
//                    setBody(jsonBody)
//                }
//            }
//
//            val responseBody = response.bodyAsText()
//            Log.d("PostUpdate", "Response: $responseBody")
//
//            val apiResponse = try {
//                Json { ignoreUnknownKeys = true }.decodeFromString< UpdatePostResponse>(responseBody)
//            } catch (e: Exception) {
//                Log.e("PostUpdate", "Parsing error: ${e.localizedMessage}")
//                null
//            }
//
//            withContext(Dispatchers.Main) {
//                if (apiResponse != null) {
//                    Toast.makeText(context, "Post updated successfully!", Toast.LENGTH_LONG).show()
//                    viewModel?.resetState()
//                } else {
//                    viewModel?.errorMessage = "Failed to update post"
//                    viewModel?.isLoading = false
//                    Toast.makeText(context, "Failed to update post", Toast.LENGTH_LONG).show()
//                }
//            }
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) {
//                viewModel?.isLoading = false
//                viewModel?.errorMessage = "Error: ${e.localizedMessage}"
//                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
//            }
//            Log.e("PostUpdate", "Error: ${e.localizedMessage}", e)
//        }
//    }
//}
