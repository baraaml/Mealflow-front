package com.example.mealflow.network

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.mealflow.data.model.SingleCommunity
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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

// Shared Json instance for parsing responses
private val json = Json {
    ignoreUnknownKeys = true
}

@Serializable
data class UpdateCommunityResponse(
    val success: Boolean,
    val message: String,
    val community: SingleCommunity? = null
)

@OptIn(InternalAPI::class)
fun updateCommunityApi(
    context: Context,
    communityId: String,
    name: String? = null,
    description: String? = null,
    imageUri: Uri? = null,
    privacy: String? = null,
//    mealCreationPermission: String? = null,
    categories: List<String>? = null,
    navController: NavController,
    onSuccess: ((SingleCommunity) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
) {
    val tokenManager = TokenManager(context)
    val accessToken = tokenManager.getAccessToken()
    val apiClientToken = ApiClientToken(context)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = "https://mealflow.online/api/v3/community/$communityId"

            val imageFile: File? = imageUri?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val extension = ".jpg"
                    val tempFile = File.createTempFile("community_image", extension, context.cacheDir)
                    tempFile.outputStream().use { fileOut ->
                        inputStream?.copyTo(fileOut)
                    }
                    tempFile
                } catch (e: Exception) {
                    Log.e("CommunityUpdate", "Error processing community image: ${e.localizedMessage}")
                    null
                }
            }

            val response = apiClientToken.client.patch(url) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                    append(HttpHeaders.ContentType, "application/json; charset=UTF-8")
                }

                if (imageFile != null) {
                    contentType(ContentType.MultiPart.FormData)
                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                name?.let { append("name", it) }
                                description?.let { append("description", it) }
                                privacy?.let { append("privacy", it) }
//                                mealCreationPermission?.let { append("mealCreationPermission", it) }

//                                // Use the same approach as createCommunityApi
//                                categories?.let { categoryList ->
//                                    append("categories", Json.encodeToString(categoryList))
//                                }

                                // Add community image
                                imageFile.let {
                                    append(
                                        "image",
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
                    contentType(ContentType.Application.Json)

                    val jsonBody = buildJsonObject {
                        name?.let { put("name", JsonPrimitive(it)) }
                        description?.let { put("description", JsonPrimitive(it)) }
                        privacy?.let { put("privacy", JsonPrimitive(it)) }
//                        mealCreationPermission?.let { put("mealCreationPermission", JsonPrimitive(it)) }

//                        // Add categories for JSON body (same as create)
//                        categories?.let { categoryList ->
//                            put("categories", Json.encodeToJsonElement(categoryList))
//                        }
                    }
                    setBody(jsonBody)
                }
            }

            val responseBody = response.bodyAsText()
            Log.d("CommunityUpdate", "Response: $responseBody")

            val apiResponse = try {
                json.decodeFromString<UpdateCommunityResponse>(responseBody)
            } catch (e: Exception) {
                Log.e("CommunityUpdate", "Parsing error: ${e.localizedMessage}")
                null
            }

            withContext(Dispatchers.Main) {
                when {
                    apiResponse != null && apiResponse.success && apiResponse.community != null -> {
                        Toast.makeText(context, "Community updated successfully!", Toast.LENGTH_LONG).show()
                        navController.navigate("Community Page")
                        onSuccess?.invoke(apiResponse.community)
                    }
                    apiResponse != null && !apiResponse.success -> {
                        val errorMsg = apiResponse.message
                        onError?.invoke(errorMsg)
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        val errorMsg = "Failed to update community"
                        onError?.invoke(errorMsg)
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            }

            imageFile?.delete()

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                val errorMsg = when (e) {
                    is kotlinx.serialization.SerializationException -> "Invalid data format"
                    is java.net.UnknownHostException -> "Network connection error"
                    is java.net.SocketTimeoutException -> "Request timeout"
                    else -> "Error: ${e.localizedMessage}"
                }
                onError?.invoke(errorMsg)
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
            Log.e("CommunityUpdate", "Error: ${e.localizedMessage}", e)
        }
    }
}