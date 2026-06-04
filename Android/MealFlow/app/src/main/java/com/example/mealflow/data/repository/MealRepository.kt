package com.example.mealflow.data.repository

import android.util.Log
import com.example.mealflow.data.model.DietaryTag
import com.example.mealflow.data.model.IngredientsResponse
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.model.MealResponse
import com.example.mealflow.data.model.Region
import com.example.mealflow.data.model.SearchIngredient
import com.example.mealflow.data.model.SubRegion
import com.example.mealflow.network.ApiMeal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

/**
 * Repository for managing meal data from both network and local cache
 */
class MealRepository(private val apiMeal: ApiMeal) {
    // Cache for frequently accessed meal lists
    private var cachedRecommendedMeals: List<Meal> = emptyList()
    private var cachedTrendingMeals: List<Meal> = emptyList()
    private var cachedCollaborativeMeals: List<Meal> = emptyList()
    private var cachedPlannedMeals: Map<LocalDate, List<Meal>> = emptyMap()

    // StateFlow for API response
    private val _mealResponse = MutableStateFlow<MealResponse?>(null)
    val mealResponse: StateFlow<MealResponse?> = _mealResponse

    /**
     * Fetches ingredients
     */
    suspend fun getIngredients(limit: Int = 50, offset: Int = 0, filter: String? = null): Result<List<SearchIngredient>> {
        Log.d("MealRepository", "Fetching ingredients with limit=$limit, offset=$offset, filter=$filter")
        return apiMeal.getIngredients(limit, offset, filter).also { result -> // Pass filter
            result.exceptionOrNull()?.let { e ->
                Log.e("MealRepository", "Error fetching ingredients: ${e.message}", e)
            }
        }
    }

    /**
     * Fetches regions
     */
    suspend fun getRegions(limit: Int = 50, offset: Int = 0, filter: String): Result<List<Region>> {
        Log.d("MealRepository", "Fetching regions with limit=$limit, offset=$offset, filter=$filter") // Log filter
        return apiMeal.getRegions(limit, offset, filter).also { result -> // Pass filter
            result.exceptionOrNull()?.let { e ->
                Log.e("MealRepository", "Error fetching regions: ${e.message}", e)
            }
        }
    }

    /**
     * Fetches sub-regions
     */
    suspend fun getSubRegions(limit: Int = 50, offset: Int = 0, filter: String): Result<List<SubRegion>> {
        Log.d("MealRepository", "Fetching sub-regions with limit=$limit, offset=$offset, filter=$filter") // Log filter
        return apiMeal.getSubRegions(limit, offset, filter).also { result -> // Pass filter
            result.exceptionOrNull()?.let { e ->
                Log.e("MealRepository", "Error fetching sub-regions: ${e.message}", e)
            }
        }
    }

    /**
     * Fetches dietary tags
     */
    suspend fun getDietaryTags(limit: Int = 50, offset: Int = 0, filter: String): Result<List<DietaryTag>> {
        Log.d("MealRepository", "Fetching dietary tags with limit=$limit, offset=$offset, filter=$filter") // Log filter
        return apiMeal.getDietaryTags(limit, offset, filter).also { result -> // Pass filter
            result.exceptionOrNull()?.let { e ->
                Log.e("MealRepository", "Error fetching dietary tags: ${e.message}", e)
            }
        }
    }

    /**
     * Fetches continents
     */
    suspend fun getContinents(limit: Int = 50, offset: Int = 0): Result<List<String>> {
        Log.d("MealRepository", "Fetching continents with limit=$limit, offset=$offset")
        // Assuming apiMeal.getContinents also takes an optional filter if needed, add it here.
        // For now, it doesn't seem to take a filter in ApiMeal.kt
        return apiMeal.getContinents(limit, offset).also { result ->
            result.exceptionOrNull()?.let { e ->
                Log.e("MealRepository", "Error fetching continents: ${e.message}", e)
            }
        }
    }

    /**
     * Unified search method that supports all search paradigms
     */
    suspend fun searchMeals(
        // Level 1: Search Paradigm
        searchType: String = "filters",

        // Level 2: Text Search
        queryText: String? = null,

        // Level 3: Ingredient Search
        ingredients: List<String>? = null,
        ingredientMode: String? = null, // any, all, exact

        // Level 4: Ownership Filtering
        creatorId: String? = null,
        communityId: String? = null,
        userId: String? = null,

        // Level 5: Metadata Filtering
        region: String? = null,
        subRegion: String? = null,
        continent: String? = null,
        dietaryTags: List<String>? = null,
        minCalories: Int? = null,
        maxCalories: Int? = null,
        maxTime: Int? = null,

        // Direct lookup
        mealId: String? = null,
        similarMealId: String? = null,

        // Additional parameters
        minSimilarity: Double? = null,
        threshold: Double? = null,
        timeWindow: String? = null,
        trending: Boolean? = null,
        cookedOnly: Boolean? = null,
        plannedDate: String? = null,

        // Pagination
        limit: Int = 10,
        offset: Int = 0
    ): Result<List<Meal>> {
        Log.d("MealRepository", "Searching with type=$searchType, queryText=$queryText, region=$region, minCal=$minCalories, maxCal=$maxCalories, maxTime=$maxTime")

        try {
            val response = apiMeal.searchMeals(
                searchType = searchType,
                queryText = queryText,
                ingredients = ingredients,
                ingredientMode = ingredientMode,
                creatorId = creatorId,
                communityId = communityId,
                userId = userId,
                region = region,
                subRegion = subRegion,
                continent = continent,
                dietaryTags = dietaryTags,
                minCalories = minCalories,
                maxCalories = maxCalories,
                maxTime = maxTime,
                mealId = mealId,
                similarMealId = similarMealId,
                minSimilarity = minSimilarity,
                threshold = threshold,
                timeWindow = timeWindow,
                trending = trending,
                cookedOnly = cookedOnly,
                plannedDate = plannedDate,
                limit = limit,
                offset = offset
            )

            if (response.isSuccess) {
                val mealResponse = response.getOrNull()
                _mealResponse.value = mealResponse
                val meals = mealResponse?.data ?: emptyList()

                // Cache meals depending on search type
                when (searchType) {
                    "collaborative" -> cachedCollaborativeMeals = meals
                    "similar" -> { /* Don't cache similar results */ }
                    "filters" -> {
                        if (trending == true) {
                            cachedTrendingMeals = meals
                        } else if (response.getOrNull()?.searchInfo?.search_type == "recommendations") {
                            cachedRecommendedMeals = meals
                        }
                        // Potentially cache other filter-based searches if needed,
                        // or clear general cache if filters are too specific.
                    }
                }

                return Result.success(meals)
            } else {
                val errorMessage = "Failed to search meals: ${response.exceptionOrNull()?.message}"
                Log.e("MealRepository", errorMessage)
                return Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("MealRepository", "Error searching meals: ${e.message}", e)
            return Result.failure(e)
        }
    }

    /**
     * Fetches recommended meals based on content similarity
     */
    suspend fun getRecommendedMeals(): Result<List<Meal>> {
        Log.d("MealRepository", "Fetching recommended meals (content-based)")
        // Recommendations might not use calories/time filters by default,
        // or they might be based on user profile if available.
        // The current searchMeals call for recommendations is very basic.
        return searchMeals(searchType = "filters", limit = 10) // This is a generic "filters" search
        // Backend "recommendations" logic is based on search_info.search_type == "recommendations"
        // which is determined by the backend.
        // Consider a specific searchType="recommendations" if backend supports it.
    }

    /**
     * Clears all cached data
     */
    fun clearCache() {
        cachedRecommendedMeals = emptyList()
        cachedTrendingMeals = emptyList()
        cachedCollaborativeMeals = emptyList()
        cachedPlannedMeals = emptyMap()
    }

    /**
     * Fetches a meal by its ID with improved caching
     */
    suspend fun getMealById(mealId: String): Meal? {
        // Fast path: Check all caches first
        val cachedMeal = findMealInCaches(mealId)
        if (cachedMeal != null) {
            return cachedMeal
        }
        
        // Slow path: Fetch from API
        return try {
            val result = searchMeals(mealId = mealId, limit = 1)
            if (result.isSuccess) {
                val meals = result.getOrNull() ?: emptyList()
                if (meals.isNotEmpty()) {
                    val meal = meals.first()
                    // Cache the meal for future use
                    cacheMeal(meal)
                    meal
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Helper method to find a meal in all caches
     */
    private fun findMealInCaches(mealId: String): Meal? {
        // Check all in-memory caches
        cachedRecommendedMeals.find { it.mealId == mealId }?.let { return it }
        cachedTrendingMeals.find { it.mealId == mealId }?.let { return it }
        cachedCollaborativeMeals.find { it.mealId == mealId }?.let { return it }
        
        // Check planned meals cache
        for ((_, meals) in cachedPlannedMeals) {
            meals.find { it.mealId == mealId }?.let { return it }
        }
        
        return null
    }
    
    /**
     * Helper method to cache a meal
     */
    private fun cacheMeal(meal: Meal) {
        // Add to recommended meals cache if it's small
        if (cachedRecommendedMeals.size < 100) {
            cachedRecommendedMeals = cachedRecommendedMeals + meal
        }
    }
}
