package com.example.mealflow.network

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.navigation.Destination
import com.example.mealflow.utils.JsonProvider
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

@Serializable
data class Ingredient(
    val name: String,
    val quantity: Double,
    val unit: String,
    val state: String? = null,
    val phrase: String? = null
)

@Serializable
data class RecipeRequest(
    val title: String,
    val region: String,
    val subRegion: String,
    val imageUrl: String? = null,
    val cookTime: Int,
    val prepTime: Int,
    val totalTime: Int,
    val servings: Int,
    val calories: Int,
    val dietaryTags: List<String>,
    val commId: String? = null,
    val ingredients: List<Ingredient>,
    val instructions: List<String>
)

@Serializable
data class RecipeResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val id: String? = null,
    val title: String? = null,
    val region: String? = null,
    val subRegion: String? = null,
    val continent: String? = null,
    val source: String? = null,
    val imageUrl: String? = null,
    val cookTime: Int? = null,
    val prepTime: Int? = null,
    val totalTime: Int? = null,
    val servings: Int? = null,
    val url: String? = null,
    val calories: Int? = null,
    val dietaryTags: List<String> = emptyList(),
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<String> = emptyList(),
    val commId: String? = null,
    val createdAt: String? = null
)

fun createRecipeApi(
    context: Context,
    title: String,
    region: String,
    subRegion: String,
    imageUri: Uri?,
    cookTime: Int,
    prepTime: Int,
    servings: Int,
    calories: Int,
    dietaryTags: List<String>,
    communityId: String? = null,
    ingredients: List<Ingredient>,
    instructions: List<String>,
    navController: NavController
) {
    val tokenManager = TokenManager(context)
    val accessToken = tokenManager.getAccessToken()
    val apiClientToken = ApiClientToken(context)

    if (title.isBlank() || ingredients.isEmpty() || instructions.isEmpty()) {
        Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_LONG).show()
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = ApiClientToken.Companion.Endpoints.CREATE_MEAL
            val totalTime = cookTime + prepTime

            val mediaFile: File? = imageUri?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val tempFile = File.createTempFile("imageUrl", ".jpg", context.cacheDir)
                    tempFile.outputStream().use { fileOut ->
                        inputStream?.copyTo(fileOut)
                    }
                    tempFile
                } catch (e: Exception) {
                    Log.e("MediaUpload", "Error processing media: ${e.localizedMessage}")
                    null
                }
            }

            val response = if (mediaFile != null) {
                apiClientToken.client.post(url) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $accessToken")
                    }
                    contentType(ContentType.MultiPart.FormData)

                    setBody(
                        MultiPartFormDataContent(
                            formData {
                                append("title", title)
                                append("region", region)
                                append("subRegion", subRegion)
                                append("cookTime", cookTime.toString())
                                append("prepTime", prepTime.toString())
                                append("totalTime", totalTime.toString())
                                append("servings", servings.toString())
                                append("calories", calories.toString())
                                append("dietaryTags", Json.encodeToString(dietaryTags))
                                communityId?.let {
                                    append("commId", it)
                                }
                                append("ingredients", Json.encodeToString(ingredients))
                                append("instructions", Json.encodeToString(instructions))
                                append(
                                    "imageUrl",
                                    mediaFile.readBytes(),
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=${mediaFile.name}")
                                    }
                                )
                            }
                        )
                    )
                }
            } else {
                val recipeRequest = RecipeRequest(
                    title = title,
                    region = region,
                    subRegion = subRegion,
                    imageUrl = null,
                    cookTime = cookTime,
                    prepTime = prepTime,
                    totalTime = totalTime,
                    servings = servings,
                    calories = calories,
                    dietaryTags = dietaryTags,
                    commId = communityId,
                    ingredients = ingredients,
                    instructions = instructions
                )

                apiClientToken.client.post(url) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $accessToken")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(recipeRequest))
                }
            }

            val responseBody = response.bodyAsText()
            Log.d("RecipeCreation", "Response: $responseBody")

            val apiResponse = try {
                JsonProvider.json.decodeFromString<RecipeResponse>(responseBody)
            } catch (e: Exception) {
                Log.e("RecipeCreation", "Parsing error: ${e.localizedMessage}")
                null
            }

            withContext(Dispatchers.Main) {
                if (apiResponse?.success == true) {
                    Toast.makeText(context, "Recipe created successfully!", Toast.LENGTH_LONG).show()
                    val destination = if (communityId == null) Destination.Profile else Destination.CommunityPage
                    navController.navigate(destination) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                } else {
                    Toast.makeText(context, "Recipe created successfully!", Toast.LENGTH_LONG).show()
                    val destination = if (communityId == null) Destination.Profile else Destination.CommunityPage
                    navController.navigate(destination) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
            Log.e("RecipeCreation", "Error: ${e.localizedMessage}", e)
        }
    }
}
