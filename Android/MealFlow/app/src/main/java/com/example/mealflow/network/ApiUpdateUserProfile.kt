
package com.example.mealflow.network

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.mealflow.R
import com.example.mealflow.navigation.Destination
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.io.File

// Shared Json instance for parsing responses
private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class UpdateProfileResponse(
    val success: Boolean,
    val message: String,
    val data: UserProfile
)

@OptIn(InternalAPI::class)
fun updateUserProfileApi(
    context: Context,
    name: String?,
    lastName: String?,
    birthDate: String?, // format: "2000-01-01T00:00:00.000Z"
    height: Int?,
    weight: Int?,
    gender: String?,
    bio: String?,
    profilePictureUri: Uri?,
    coverPictureUri: Uri?,
    navController: NavController,
    onSuccess: ((UserProfile) -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
    isProfile: Boolean = false
): UpdateProfileResponse? {
    val tokenManager = TokenManager(context)
    val accessToken = tokenManager.getAccessToken()
    val apiClientToken = ApiClientToken(context)
    val userPrefs = UserPreferencesManager(context)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = "https://mealflow.online/api/v3/users/me"

            // Process profile picture if provided
            val profilePictureFile: File? = profilePictureUri?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val extension = ".jpg"
                    val tempFile = File.createTempFile("profile_picture", extension, context.cacheDir)
                    tempFile.outputStream().use { fileOut ->
                        inputStream?.copyTo(fileOut)
                    }
                    tempFile
                } catch (e: Exception) {
                    Log.e("ProfileUpdate", "Error processing profile picture: ${e.localizedMessage}")
                    null
                }
            }

            // Process cover picture if provided
            val coverPictureFile: File? = coverPictureUri?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val extension = ".jpg"
                    val tempFile = File.createTempFile("cover_picture", extension, context.cacheDir)
                    tempFile.outputStream().use { fileOut ->
                        inputStream?.copyTo(fileOut)
                    }
                    tempFile
                } catch (e: Exception) {
                    Log.e("ProfileUpdate", "Error processing cover picture: ${e.localizedMessage}")
                    null
                }
            }

            val response = apiClientToken.client.patch(url) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                }

                // Use MultiPartFormData if there are files to upload
                if (profilePictureFile != null || coverPictureFile != null) {
                    contentType(ContentType.MultiPart.FormData)
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                name?.let { append("name", it) }
                                lastName?.let { append("lastName", it) }
                                birthDate?.let { append("birthDate", it) }
                                height?.let { append("height", it.toString()) }
                                weight?.let { append("weight", it.toString()) }
                                gender?.let { append("gender", it) }
                                bio?.let { append("bio", it) }

                                // Add profile picture if available
                                profilePictureFile?.let {
                                    append(
                                        "profilePicture",
                                        it.readBytes(),
                                        Headers.build {
                                            append(HttpHeaders.ContentType, "image/jpeg")
                                            append(HttpHeaders.ContentDisposition, "filename=${it.name}")
                                        }
                                    )
                                }

                                // Add cover picture if available
                                coverPictureFile?.let {
                                    append(
                                        "coverPicture",
                                        it.readBytes(),
                                        Headers.build {
                                            append(HttpHeaders.ContentType, "image/jpeg")
                                            append(HttpHeaders.ContentDisposition, "filename=${it.name}")
                                        }
                                    )
                                }
                            }
                        )
                    )
                } else {
                    // If no files, use JSON body
                    contentType(ContentType.Application.Json)

                    val jsonBody = buildJsonObject {
                        name?.let { put("name", JsonPrimitive(it)) }
                        lastName?.let { put("lastName", JsonPrimitive(it)) }
                        birthDate?.let { put("birthDate", JsonPrimitive(it)) }
                        height?.let { put("height", JsonPrimitive(it)) }
                        weight?.let { put("weight", JsonPrimitive(it)) }
                        gender?.let { put("gender", JsonPrimitive(it)) }
                        bio?.let { put("bio", JsonPrimitive(it)) }
                    }
                    setBody(jsonBody)
                }
            }

            val responseBody = response.bodyAsText()
            Log.d("ProfileUpdate", "Response: $responseBody")

            val apiResponse = try {
                json.decodeFromString<UpdateProfileResponse>(responseBody)
            } catch (e: Exception) {
                Log.e("ProfileUpdate", "Parsing error: ${e.localizedMessage}")
                null
            }

            withContext(Dispatchers.Main) {
                if (apiResponse != null && apiResponse.success) {
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_LONG).show()
                    onSuccess?.invoke(apiResponse.data)
                    userPrefs.saveMyImageProfile(apiResponse.data.profilePicture?:"https://photobasement.com/wp-content/uploads/2017/04/this-is-a-photo.jpg")
                    userPrefs.saveFirstName(apiResponse.data.name?:"unknown")
                    if(isProfile)
                    {
                        navController.navigate(Destination.Profile)
                    }
                } else {
                    val errorMsg = apiResponse?.message ?: "Failed to update profile"
                    onError?.invoke(errorMsg)
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                val errorMsg = "Error: ${e.localizedMessage}"
                onError?.invoke(errorMsg)
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
            Log.e("ProfileUpdate", "Error: ${e.localizedMessage}", e)
        }
    }

    return null
}