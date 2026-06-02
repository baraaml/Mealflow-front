package com.example.mealflow.network

import android.util.Log
import com.example.mealflow.data.model.ContinentsResponse
import com.example.mealflow.data.model.DietaryTag
import com.example.mealflow.data.model.DietaryTagsResponse
import com.example.mealflow.data.model.IngredientsResponse
import com.example.mealflow.data.model.MealResponse
import com.example.mealflow.data.model.Region
import com.example.mealflow.data.model.RegionsResponse
import com.example.mealflow.data.model.SearchIngredient
import com.example.mealflow.data.model.SubRegion
import com.example.mealflow.data.model.SubRegionsResponse
import com.example.mealflow.network.ApiClient.Endpoints.METADATA
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiMeal {
    private val client = ApiClient.client
    private val endpoints = ApiClient.Endpoints

    // Get regions with server-side filtering
    suspend fun getRegions(
        limit: Int = 50,
        offset: Int = 0,
        filter: String? = null
    ): Result<List<Region>> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$METADATA/regions") {
                parameter("limit", limit)
                parameter("offset", offset)
                if (!filter.isNullOrBlank()) {
                    parameter("queryText", filter)
                }
                headers { append(HttpHeaders.Accept, "application/json") }
            }

            if (response.status.isSuccess()) {
                val regionsResponse = response.body<RegionsResponse>()
                val regions = regionsResponse.regions.map { Region(name = it) }
                Result.success(regions)
            } else {
                Result.failure(Exception("Failed to get regions: ${response.status}"))
            }
        } catch (e: Exception) {
            if (e is java.util.concurrent.CancellationException) {
                throw e
            }
            Log.e("ApiMeal", "Error getting regions: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Get sub-regions with server-side filtering
    suspend fun getSubRegions(
        limit: Int = 50,
        offset: Int = 0,
        filter: String? = null
    ): Result<List<SubRegion>> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$METADATA/sub-regions") {
                parameter("limit", limit)
                parameter("offset", offset)
                if (!filter.isNullOrBlank()) {
                    parameter("queryText", filter)
                }
                headers { append(HttpHeaders.Accept, "application/json") }
            }

            if (response.status.isSuccess()) {
                val subRegionsResponse = response.body<SubRegionsResponse>()
                val subregions = subRegionsResponse.subRegions.map { SubRegion(name = it) }
                Result.success(subregions)
            } else {
                Result.failure(Exception("Failed to get sub-regions: ${response.status}"))
            }
        } catch (e: Exception) {
            if (e is java.util.concurrent.CancellationException) {
                throw e
            }
            Log.e("ApiMeal", "Error getting sub-regions: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Get ingredients with server-side filtering
    suspend fun getIngredients(
        limit: Int = 50,
        offset: Int = 0,
        filter: String? = null
    ): Result<List<SearchIngredient>> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$METADATA/ingredients") {
                parameter("limit", limit)
                parameter("offset", offset)
                if (!filter.isNullOrBlank()) {
                    parameter("queryText", filter)
                }
                headers { append(HttpHeaders.Accept, "application/json") }
            }

            if (response.status.isSuccess()) {
                val ingredientsResponse = response.body<IngredientsResponse>()
                Result.success(ingredientsResponse.ingredients)
            } else {
                Result.failure(Exception("Failed to get ingredients: ${response.status}"))
            }
        } catch (e: Exception) {
            if (e is java.util.concurrent.CancellationException) {
                throw e
            }
            Log.e("ApiMeal", "Error getting ingredients: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Get dietary tags with server-side filtering
    suspend fun getDietaryTags(
        limit: Int = 50,
        offset: Int = 0,
        filter: String? = null
    ): Result<List<DietaryTag>> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$METADATA/dietary-tags") {
                parameter("limit", limit)
                parameter("offset", offset)
                if (!filter.isNullOrBlank()) {
                    parameter("queryText", filter)
                }
                headers { append(HttpHeaders.Accept, "application/json") }
            }

            if (response.status.isSuccess()) {
                val dietaryTagsResponse = response.body<DietaryTagsResponse>()
                val dietaryTags = dietaryTagsResponse.dietaryTags.map { DietaryTag(name = it.toString()) }
                Result.success(dietaryTags)
            } else {
                Result.failure(Exception("Failed to get dietary tags: ${response.status}"))
            }
        } catch (e: Exception) {
            if (e is java.util.concurrent.CancellationException) {
                throw e
            }
            Log.e("ApiMeal", "Error getting dietary tags: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Get continents with server-side filtering
    suspend fun getContinents(
        limit: Int = 50,
        offset: Int = 0,
        filter: String? = null
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$METADATA/continents") {
                parameter("limit", limit)
                parameter("offset", offset)
                if (!filter.isNullOrBlank()) {
                    parameter("queryText", filter)
                }
                headers { append(HttpHeaders.Accept, "application/json") }
            }

            if (response.status.isSuccess()) {
                val continentsResponse = response.body<ContinentsResponse>()
                Result.success(continentsResponse.continents)
            } else {
                Result.failure(Exception("Failed to get continents: ${response.status}"))
            }
        } catch (e: Exception) {
            if (e is java.util.concurrent.CancellationException) {
                throw e
            }
            Log.e("ApiMeal", "Error getting continents: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Unified search endpoint with all search paradigms
    suspend fun searchMeals(
        // Level 1: Search Paradigm
        searchType: String = "filters", // by_id, semantic, similar, ingredients, filters, collaborative

        // Level 2: Text Search
        queryText: String? = null,

        // Level 3: Ingredient Search
        ingredients: List<String>? = null,
        ingredientMode: String? = null, // any, all, exact

        // Level 4: Ownership Filtering
        creatorId: String? = null,
        communityId: String? = null,
        userId: String? = null, // For collaborative filtering

        // Level 5: Metadata Filtering
        region: String? = null,
        subRegion: String? = null,
        continent: String? = null,
        dietaryTags: List<String>? = null,
        minCalories: Int? = null,
        maxCalories: Int? = null,
        maxTime: Int? = null,

        // Direct Lookup
        mealId: String? = null,
        similarMealId: String? = null,

        // Additional Parameters
        minSimilarity: Double? = null,
        threshold: Double? = null,
        timeWindow: String? = null,
        trending: Boolean? = null,
        cookedOnly: Boolean? = null,
        plannedDate: String? = null,

        // Pagination
        limit: Int = 10,
        offset: Int = 0
    ): Result<MealResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("ApiMeal", "Searching with type: $searchType")
            val response = client.get("${endpoints.SEARCH}") {
                // Level 1: Search Paradigm
                parameter("searchType", searchType)

                // Level 2: Text Search
                queryText?.let { parameter("queryText", it) }

                // Level 3: Ingredient Search
                ingredients?.let { parameter("ingredients", it.joinToString(",")) }
                ingredientMode?.let { parameter("ingredientMode", it) }

                // Level 4: Ownership Filtering
                creatorId?.let { parameter("creatorId", it) }
                communityId?.let { parameter("commId", it) }
                userId?.let { parameter("userId", it) }
                Log.d("user id for meals","user id : $userId")

                // Level 5: Metadata Filtering
                region?.let { parameter("region", it) }
                subRegion?.let { parameter("subRegion", it) }
                continent?.let { parameter("continent", it) }
                dietaryTags?.let { parameter("dietaryTags", it.joinToString(",") { tag -> tag ?: "" }) }
                minCalories?.let { parameter("minCalories", it) }
                maxCalories?.let { parameter("maxCalories", it) }
                maxTime?.let { parameter("maxTime", it) }

                // Direct Lookup
                mealId?.let { parameter("mealId", it) }
                similarMealId?.let { parameter("similarMealId", it) }

                // Additional Parameters
                minSimilarity?.let { parameter("minSimilarity", it) }
                threshold?.let { parameter("threshold", it) }
                timeWindow?.let { parameter("timeWindow", it) }
                trending?.let { parameter("trending", true) }
                cookedOnly?.let { parameter("cookedOnly", true) }
                plannedDate?.let { parameter("plannedDate", it) }

                // Pagination
                parameter("limit", limit)
                parameter("offset", offset)

                headers { append(HttpHeaders.Accept, "application/json") }
            }
            Log.d("response","response : $response")
            val responseBody = response.body<MealResponse>()
            Log.d("responseBody","responseBody : $responseBody")

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to search meals: ${response.status}"))
            }
        } catch (e: Exception) {
            if (e is java.util.concurrent.CancellationException) {
                throw e
            }
            Log.e("ApiMeal", "Error searching meals: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Create interaction (like, view, rating, favorite, plan, cooked)
    suspend fun createInteraction(
        mealId: String,
        type: String,
        rating: Double? = null,
        reviewText: String? = null
    ): Result<Boolean> {
        return try {
            val body = mutableMapOf(
                "mealId" to mealId,
                "type" to type
            )

            rating?.let { body["rating"] = it.toString() }
            reviewText?.let { body["reviewText"] = it }

            val response = client.post(endpoints.newInteractions) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            if (e is java.util.concurrent.CancellationException) {
                throw e
            }
            Log.e("ApiMeal", "Error creating interaction: ${e.message}", e)
            Result.failure(e)
        }
    }
}