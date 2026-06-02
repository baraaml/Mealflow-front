package com.example.mealflow.network

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.utils.JsonProvider
import com.example.mealflow.viewModel.CreatePostViewModel
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class CreateCommunityPostResponse(
    val post: PostCreateCommunity
)

@Serializable
data class PostCreateCommunity(
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val communityId: String? = null,
    val flairId: String? = null,
    val mediaType: String? = null,
    val mediaUrl: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val author: Author? = null,
    val flair: Flair? = null,
    val community:CommunityPost? = null
)

@Serializable
data class CommunityPost(
    val id: String,
    val name: String,
    val description: String,
    val ownerId: String,
    val image: String? = null,
    val privacy: String,
    val recipeCreationPermission: String,
    val createdAt: String,
    val updatedAt: String
)

@OptIn(InternalAPI::class)
fun createPostCommunityApi(
    context: Context,
    title: String,
    content: String,
    communityId: String,
    flair: String,
    mediaUri: Uri?,
    mediaType: String?,
    navController: NavController,
    viewModel: CreatePostViewModel? = null
) {
    val tokenManager = TokenManager(context)
    val accessToken = tokenManager.getAccessToken()
    val userPrefs = UserPreferencesManager(context)
    val apiClientToken = ApiClientToken(context)

    // Let the ViewModel handle all validation logic
    // No validation or loading state changes here

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val authorId = userPrefs.getUserId().first()

            val url = "https://mealflow.online/api/v3/community/$communityId/posts"

            val now = getCurrentIsoTime()

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

            val response = apiClientToken.client.post(url) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                contentType(ContentType.MultiPart.FormData)

                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("title", title)
                            append("content", content)
                            append("targetType", "COMMUNITY")
                            append("mediaType",
                                (if (mediaFile != null) mediaType else "NONE").toString()
                            )
                            append("authorId", authorId)
                            append("flair", flair)
                            append("communityId", communityId)
                            append("isPinned", "false")
                            append("isEdited", "false")
                            append("createdAt", now)
                            append("updatedAt", now)
                            mediaFile?.let { file ->
                                append(
                                    "mediaUrl",
                                    file.readBytes(),
                                    Headers.build {
                                        append(HttpHeaders.ContentType, if (mediaType == "VIDEO") "video/mp4" else "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                                    }
                                )
                            }
                        }
                    )
                )
            }

            val responseBody = response.bodyAsText()
            Log.d("PostCreation", "Response: $responseBody")

            val apiResponse = try {
                JsonProvider.json.decodeFromString<CreatePostResponse>(responseBody)
            } catch (e: Exception) {
                Log.e("PostCreation", "Parsing error: ${e.localizedMessage}")
                null
            }

            withContext(Dispatchers.Main) {
                if (apiResponse != null) {
                    Toast.makeText(context, "Post created successfully!", Toast.LENGTH_LONG).show()
                    viewModel?.resetState() // Reset state after successful post
                    navController.navigate("Community Page")
                } else {
                    viewModel?.errorMessage = "Failed to create post"
                    viewModel?.isLoading = false // Only set loading to false on error
                    Toast.makeText(context, "Failed to create post", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                viewModel?.isLoading = false // Reset loading only on error
                viewModel?.errorMessage = "Error: ${e.localizedMessage}"
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
            Log.e("PostCreation", "Error: ${e.localizedMessage}", e)
        }
    }
}