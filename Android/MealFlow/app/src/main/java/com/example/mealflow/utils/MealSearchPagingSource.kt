package com.example.mealflow.utils

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay
import android.util.Log
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.repository.MealRepository
import com.example.mealflow.network.ApiMeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * A PagingSource implementation for loading meal search results with offset-based pagination
 *
 * @param apiMeal API service to fetch meals using the unified search endpoint
 * @param searchType Type of search to perform (by_id, semantic, similar, ingredients, filters, collaborative)
 * @param queryText Optional text query for search
 * @param ingredients Optional list of ingredients to search for
 * @param ingredientMode Mode for ingredient search (any, all, exact)
 * @param creatorId Optional filter for meals by creator ID
 * @param communityId Optional filter for meals by community ID
 * @param userId Optional user ID for collaborative filtering
 * @param region Optional region filter
 * @param subRegion Optional sub-region filter
 * @param continent Optional continent filter
 * @param dietaryTags Optional list of dietary tags to filter by
 * @param minCalories Optional minimum calories filter
 * @param maxCalories Optional maximum calories filter
 * @param maxTime Optional maximum preparation time filter
 * @param mealId Optional specific meal ID for direct lookup
 * @param similarMealId Optional meal ID to find similar meals to
 * @param minSimilarity Optional minimum similarity threshold for similar meals
 * @param threshold Optional general threshold value
 * @param timeWindow Optional time window for trending/recent content
 * @param trending Optional flag to get trending meals
 * @param cookedOnly Optional flag to filter only cooked meals
 * @param plannedDate Optional date to filter meals planned for
 * @param limit Number of meals to fetch per page
 * @param retryDelay Delay in milliseconds before retrying a failed request
 * @param maxRetries Maximum number of retry attempts for failed requests
 */
class MealSearchPagingSource(
    private val apiMeal: ApiMeal,
    private val mealRepository: MealRepository? = null,
    // Level 1: Search Paradigm
    private val searchType: String = "filters",

    // Level 2: Text Search
    private val queryText: String? = null,

    // Level 3: Ingredient Search
    private val ingredients: List<String>? = null,
    private val ingredientMode: String? = null,

    // Level 4: Ownership Filtering
    private val creatorId: String? = null,
    private val communityId: String? = null,
    private val userId: String? = null,

    // Level 5: Metadata Filtering
    private val region: String? = null,
    private val subRegion: String? = null,
    private val continent: String? = null,
    private val dietaryTags: List<String>? = null,
    private val minCalories: Int? = null,
    private val maxCalories: Int? = null,
    private val maxTime: Int? = null,

    // Direct Lookup
    private val mealId: String? = null,
    private val similarMealId: String? = null,

    // Additional Parameters
    private val minSimilarity: Double? = null,
    private val threshold: Double? = null,
    private val timeWindow: String? = null,
    private val trending: Boolean? = null,
    private val cookedOnly: Boolean? = null,
    private val plannedDate: String? = null,

    // Pagination and retry configuration
    private val limit: Int = 10, // Default limit, params.loadSize will often override for specific loads
    private val retryDelay: Long = 3000,
    private val maxRetries: Int = 3
) : PagingSource<Int, Meal>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Meal> {
        val offset = params.key ?: 0
        return try {
            val response = withContext(Dispatchers.IO) {
                apiMeal.searchMeals(
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
                    limit = params.loadSize,
                    offset = offset
                )
            }

            if (response.isSuccess) {
                val meals = response.getOrThrow().data
                val nextKey = if (meals.isNotEmpty()) {
                    offset + meals.size
                } else {
                    null
                }

                LoadResult.Page(
                    data = meals,
                    prevKey = if (offset == 0) null else offset - params.loadSize,
                    nextKey = nextKey
                )
            } else {
                LoadResult.Error(response.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Meal>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(state.config.pageSize)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(state.config.pageSize)
        }
    }

    // Background scope for prefetching meal details without blocking the main load
    private val prefetchScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Prefetches meal details for all meals in the list to make future access faster
     * This is done in a background scope so it doesn't block the main loading process
     */
    private fun prefetchMealDetails(meals: List<Meal>) {
        if (mealRepository == null || meals.isEmpty()) return

        prefetchScope.launch {
            Log.d("MealSearchPagingSource", "Prefetching details for ${meals.size} meals")
            for (meal in meals) {
                try {
                    // This will cache the meal in the repository
                    mealRepository.getMealById(meal.mealId)
                } catch (e: Exception) {
                    // Silently ignore errors during prefetching
                    Log.d("MealSearchPagingSource", "Error prefetching meal ${meal.mealId}: ${e.message}")
                }
            }
        }
    }
}