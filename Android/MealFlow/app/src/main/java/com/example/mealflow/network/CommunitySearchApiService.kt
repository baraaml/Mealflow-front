package com.example.mealflow.network

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.SearchResponse
import com.example.mealflow.database.token.TokenManager
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import java.net.URLEncoder

class SearchApiService(private val context: Context) {
    private val apiClientToken = ApiClientToken(context)

    suspend fun search(
        query: String,
        type: String = "all",
        limit: Int = 30,
        cursor: String? = null,
        direction: String = "before"
    ): SearchResponse {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Log.e("SearchApi", "Token is null or empty")
            return SearchResponse(false, "Token is null or empty", null)
        }

        if (query.length < 2) {
            Log.e("SearchApi", "Query must be at least 2 characters long")
            return SearchResponse(false, "Search query must be at least 2 characters long", null)
        }

        if (limit > 100) {
            Log.e("SearchApi", "Limit must be less than 100")
            return SearchResponse(false, "Limit must be less than 100", null)
        }

        val validTypes = listOf("posts", "users", "comments", "communities", "all")
        if (type !in validTypes) {
            Log.e("SearchApi", "Invalid search type")
            return SearchResponse(false, "Invalid search type. Must be one of: posts, users, comments, communities, all", null)
        }

        val urlBuilder = StringBuilder("https://mealflow.online/api/v3/search?")
            .append("q=${URLEncoder.encode(query, "UTF-8")}")
            .append("&type=$type")
            .append("&limit=$limit")
            .append("&direction=$direction")

        if (!cursor.isNullOrEmpty()) {
            urlBuilder.append("&cursor=${URLEncoder.encode(cursor, "UTF-8")}")
        }

        // Log request details
        val finalUrl = urlBuilder.toString()
        Log.d("SearchApi", "********** REQUESTING URL: $finalUrl **********")
        Log.d("SearchApi", "********** WITH query: $query **********")
        Log.d("SearchApi", "********** WITH type: $type **********")
        Log.d("SearchApi", "********** WITH limit: $limit **********")
        Log.d("SearchApi", "********** WITH cursor: $cursor **********")
        Log.d("SearchApi", "********** WITH direction: $direction **********")

        return try {
            val response: HttpResponse = apiClientToken.client.get(urlBuilder.toString()) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            Log.d("SearchApi", "API Response: ${response.bodyAsText()}")

            if (!response.status.isSuccess()) {
                Log.e("SearchApi", "Failed to search: ${response.status}")
                return SearchResponse(false, "Failed to search: ${response.status}", null)
            }

            val responseBody = response.body<SearchResponse>()
            Log.d("SearchApi", "Search results retrieved successfully: $responseBody")
            responseBody

        } catch (e: Exception) {
            Log.e("SearchApi", "Error performing search", e)
            return SearchResponse(false, "Error performing search: ${e.message}", null)
        }
    }
}