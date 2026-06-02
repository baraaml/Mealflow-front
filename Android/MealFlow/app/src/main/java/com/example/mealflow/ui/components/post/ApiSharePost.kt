package com.example.mealflow.ui.components.post

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.network.ApiClientToken
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

// ----------------------- SharePostResponse ---------------------------
@Serializable
data class SharePostResponse(
    val success: Boolean,
    val message: String,
    val data: SharePostData? = null
)

@Serializable
data class SharePostData(
    val deepLink: String,
    val shareLinks: ShareLinks
)

@Serializable
data class ShareLinks(
    val whatsapp: String,
    val twitter: String,
    val facebook: String,
    val telegram: String,
    val email: String,
    val discord: String
)

// ----------------------- Share Post API Function ---------------------------
fun sharePostApi(
    postId: String,
    context: Context,
    onSuccess: (SharePostData) -> Unit,
    onError: (String) -> Unit = { message ->
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
) {
    val apiClientToken = ApiClientToken(context)

    CoroutineScope(Dispatchers.IO).launch {
        val url = "https://mealflow.online/api/v3/posts/$postId/shareLinks"
        Log.d("SharePost", "📩Send request: URL = $url")
        val tokenManager = TokenManager(context)
        val accessToken = tokenManager.getAccessToken()

        try {
            Log.d("SharePost", "📩Send request: Token = $accessToken, postId=$postId")

            val response: HttpResponse = apiClientToken.client.get(url) {
                header("Authorization", "Bearer $accessToken")
                header("Content-Type", ContentType.Application.Json.toString())
            }

            val responseBody = response.body<SharePostResponse>()

            withContext(Dispatchers.Main) {
                Log.d("SharePost", "response : ${responseBody.message}")
                Log.d("SharePost", "response : $responseBody")

                if (response.status.isSuccess() && responseBody.success) {
                    Log.d("SharePost", "✅ Successfully generated share links")
                    responseBody.data?.let { shareData ->
                        onSuccess(shareData)
                    } ?: run {
                        Log.e("SharePost", "❌ Share data is null")
                        onError("Failed to generate share links")
                    }
                } else {
                    Log.e("SharePost", "❌ Share post failed: ${responseBody.message}")
                    onError(responseBody.message)
                }
            }
        } catch (e: Exception) {
            Log.e("SharePost", "❌ Exception during share post execution: ${e.localizedMessage}")
            withContext(Dispatchers.Main) {
                onError("An error occurred while connecting to the server.")
            }
        }
    }
}

// ----------------------- Share Post API Function with Fallback ---------------------------
fun sharePostApiWithFallback(
    postId: String,
    context: Context,
    onSuccess: (SharePostData) -> Unit,
    onError: (String) -> Unit = { message ->
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
) {
    sharePostApi(
        postId = postId,
        context = context,
        onSuccess = onSuccess,
        onError = { errorMessage ->
            // If API fails, create fallback share links
            Log.w("SharePost", "API failed, creating fallback links: $errorMessage")
            val fallbackData = createFallbackShareLinks(postId)
            onSuccess(fallbackData)
        }
    )
}

// ----------------------- Fallback Share Links Generator ---------------------------
private fun createFallbackShareLinks(postId: String): SharePostData {
    val deepLink = "mealflow://posts/$postId"
    val encodedLink = java.net.URLEncoder.encode(deepLink, "UTF-8")
    val encodedText = java.net.URLEncoder.encode("Check out this post!", "UTF-8")

    return SharePostData(
        deepLink = deepLink,
        shareLinks = ShareLinks(
            whatsapp = "https://api.whatsapp.com/send?text=$encodedText%20$encodedLink",
            twitter = "https://twitter.com/intent/tweet?url=$encodedLink&text=$encodedText",
            facebook = "https://www.facebook.com/sharer/sharer.php?u=$encodedLink",
            telegram = "https://t.me/share/url?url=$encodedLink&text=$encodedText",
            email = "mailto:?subject=$encodedText&body=I%20thought%20you%20might%20be%20interested%20in%20this%20post%3A%20$encodedLink",
            discord = "https://discord.com/channels/@me?content=$encodedText%20$encodedLink"
        )
    )
}