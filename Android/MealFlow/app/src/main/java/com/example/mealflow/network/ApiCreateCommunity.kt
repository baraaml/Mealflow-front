package com.example.mealflow.network

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mealflow.navigation.Destination
import com.example.mealflow.utils.JsonProvider
import com.example.mealflow.viewModel.CreateCommunityViewModel
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

// ----------------------- ApiCreateCommunityResponse ---------------------------
@Serializable
data class CreateCommunityResponse(
    val success: Boolean,
    val message: String,
    val community: Community ? = null
)
@Entity(tableName = "community_table")
@Serializable
data class Community(
    @PrimaryKey val id: String,  // ✅ @PrimaryKey has been added
    val name: String,
    val description: String,
    val ownerId: String,
    val image: String?,
    val privacy: String,
    val mealCreationPermission: String,
    val createdAt: String,
    val updatedAt: String,
    val categories: List<Category>,
    val members: List<Member>,
    val owner: OwnerCommunity,
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    val parentId: String? = null
)

@Serializable
data class Member(
    val role: String,
    val joinedAt: String,
    val isPending: Boolean? = null,
    val user: UserCreateCommunity
)

@Serializable
data class UserCreateCommunity(
    val id: String,
    val name: String?,
    val username: String
)

@Serializable
data class OwnerCommunity(
    val id: String,
    val name: String?,
    val username: String
)

fun createCommunityApi(
    context: Context,
    name: String,
    description: String,
//    recipeCreationPermission: String,
    accessToken: String,
    categories: List<String>,
    imageUri: Uri?,
    navController: NavController,
    viewModel: CreateCommunityViewModel? = null
) {
    val apiClientToken = ApiClientToken(context)
    // Validate inputs
    if (name.isBlank() || description.isBlank() || accessToken.isBlank()) {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel?.let {
                it.isLoading = false
                it.errorMessage = "Please fill in all required fields"
            }
            Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_LONG).show()
        }
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = ApiClientToken.Companion.Endpoints.CREATE_COMMUNITY

            // Prepare image for upload
            val imageFile: File? = imageUri?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val tempFile = File.createTempFile("community_image", ".jpg", context.cacheDir)
                    tempFile.outputStream().use { fileOut ->
                        inputStream?.copyTo(fileOut)
                    }
                    tempFile
                } catch (e: Exception) {
                    Log.e("ImageUpload", "Error processing image: ${e.localizedMessage}")
                    null
                }
            }

            // Prepare multipart form data
            val response = apiClientToken.client.post(url) {
                headers {
                    append("Authorization", "Bearer $accessToken")
                }
                contentType(ContentType.MultiPart.FormData)

                setBody(MultiPartFormDataContent(
                    formData {
                        append("name", name)
                        append("description", description)
//                        append("recipeCreationPermission", recipeCreationPermission)
                        append("categories", Json.encodeToString(categories))

                        imageFile?.let { file ->
                            append("image", file.readBytes(),
                                Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                                }
                            )
                        }
                    }
                ))
            }

            // Process response
            val responseBody = response.bodyAsText()
            Log.d("CommunityCreation", "Response: $responseBody")

            val apiResponse = try {
                JsonProvider.json.decodeFromString<CreateCommunityResponse>(responseBody)
            } catch (e: Exception) {
                Log.e("CommunityCreation", "Parsing error: ${e.localizedMessage}")
                null
            }

            // Handle response on Main thread
            withContext(Dispatchers.Main) {
                // ALWAYS set loading to false first
                viewModel?.let {
                    it.isLoading = false
                }

                if (apiResponse?.success == true) {
                    // Only reset state on SUCCESS
                    viewModel?.let {
                        it.errorMessage = null
                        it.resetState() // Reset only after successful creation
                    }
                    Toast.makeText(context, "Community created successfully!", Toast.LENGTH_LONG).show()
                    // Safe navigation
                    navController.navigate(Destination.CommunityHome) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                } else {
                    // On failure, just update error message without resetting
                    val errorMsg = apiResponse?.message ?: "Failed to create community"
                    viewModel?.errorMessage = errorMsg
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                // Set loading to false and update error message
                viewModel?.let {
                    it.isLoading = false
                    it.errorMessage = "Error: ${e.localizedMessage}"
                }
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
            Log.e("CommunityCreation", "Error: ${e.localizedMessage}", e)
        }
    }
}