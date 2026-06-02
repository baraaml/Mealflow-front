package com.example.mealflow.network

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiClientMockTest {

    @Test
    fun testMockBackendSearch() = runBlocking {
        // ApiClient is a singleton, and USE_MOCK_BACKEND is set to true in its definition
        val client = ApiClient.client
        val response = client.get(ApiClient.Endpoints.SEARCH)
        val body = response.bodyAsText()
        
        println("Response Body: $body")
        
        assertTrue("Response should contain success: true", body.contains("\"success\": true"))
        assertTrue("Response should contain mock meals", body.contains("Mock Meal"))
    }

    @Test
    fun testMockBackendLogin() = runBlocking {
        val client = ApiClient.client
        val response = client.get(ApiClient.Endpoints.LOGIN)
        val body = response.bodyAsText()
        
        println("Login Response Body: $body")
        
        assertTrue("Response should contain mock user", body.contains("user123"))
        assertTrue("Response should contain fake-jwt-token", body.contains("fake-jwt-token"))
    }
}
