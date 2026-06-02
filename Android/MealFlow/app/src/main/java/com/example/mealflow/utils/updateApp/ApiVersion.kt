package com.example.mealflow.utils.updateApp

import android.content.Context
import android.util.Log
import com.example.mealflow.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import io.ktor.client.request.*

// 1. First, create the data classes and repository
@Serializable
data class VersionData(
    val appName: String,
    val version: String,
    val minSupportedVersion: String,
    val latestVersion: String,
    val forceUpdate: Boolean,
    val updateMessage: String,
    val whatsNew: List<String>
)

@Serializable
data class VersionResponse(
    val success: Boolean,
    val message: String,
    val data: VersionData
)

class VersionRepository(private val context: Context) {
    private val apiClient = ApiClient
    val url = "https://mealflow.ddns.net/api/download/version"

    companion object {
        private const val TAG = "VersionRepository"
    }

    suspend fun checkVersion(): Result<VersionData> {
        Log.d(TAG, "Starting version check...")
        Log.d(TAG, "API URL: $url")

        return try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Making API request to: $url")
                val response: HttpResponse = apiClient.client.get(url)

                Log.d(TAG, "Response status: ${response.status}")
                Log.d(TAG, "Response headers: ${response.headers}")

                if (response.status.isSuccess()) {
                    Log.d(TAG, "Response is successful, parsing body...")
                    val responseBody = response.body<String>()
                    Log.d(TAG, "Raw response body: $responseBody")

                    val versionResponse = response.body<VersionResponse>()
                    Log.d(TAG, "Parsed response - success: ${versionResponse.success}, message: ${versionResponse.message}")

                    if (versionResponse.success) {
                        Log.d(TAG, "Version data received successfully")
                        Log.d(TAG, "App Name: ${versionResponse.data.appName}")
                        Log.d(TAG, "Current Version: ${versionResponse.data.version}")
                        Log.d(TAG, "Latest Version: ${versionResponse.data.latestVersion}")
                        Log.d(TAG, "Force Update: ${versionResponse.data.forceUpdate}")
                        Log.d(TAG, "Update Message: ${versionResponse.data.updateMessage}")
                        Log.d(TAG, "What's New: ${versionResponse.data.whatsNew}")

                        Result.success(versionResponse.data)
                    } else {
                        Log.w(TAG, "API returned success=false with message: ${versionResponse.message}")
                        Result.failure(Exception(versionResponse.message))
                    }
                } else {
                    Log.e(TAG, "HTTP request failed with status: ${response.status}")
                    Log.e(TAG, "Response body: ${response.body<String>()}")
                    Result.failure(Exception("Failed to fetch version info - Status: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception occurred while checking version", e)
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}