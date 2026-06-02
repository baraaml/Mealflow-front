package com.example.mealflow.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

object MockEngineFactory {

    fun create(): MockEngine {
        return MockEngine { request ->
            val url = request.url.toString()
            
            when {
                url.contains(ApiClient.Endpoints.LOGIN) -> {
                    respond(
                        content = MockDataProvider.getLoginResponse(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                url.contains(ApiClient.Endpoints.SEARCH) -> {
                    respond(
                        content = MockDataProvider.getMealListResponse(10),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                url.contains("/api/v3/communities") -> {
                    respond(
                        content = MockDataProvider.getCommunityListResponse(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                url.contains("/api/v3/posts") -> {
                    respond(
                        content = MockDataProvider.getPostListResponse(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                else -> {
                    // Default response for unhandled endpoints
                    respond(
                        content = "{\"success\": true}",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
        }
    }
}
