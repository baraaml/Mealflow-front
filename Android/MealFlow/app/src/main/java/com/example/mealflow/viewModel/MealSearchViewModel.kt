package com.example.mealflow.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.repository.MealRepository
import com.example.mealflow.network.ApiMeal
import com.example.mealflow.utils.MealSearchPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for handling meal searches with pagination
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MealSearchViewModel(application: Application) : AndroidViewModel(application) {
    // Assuming ApiMeal has a singleton instance or different constructor
    private val apiMeal = ApiMeal()
    // Assuming MealRepository takes appropriate parameters
    private val mealRepository = MealRepository(apiMeal)

    // Refresh trigger to handle manual refreshes
    private val refreshTrigger = MutableStateFlow(0)

    // Search parameters - can be updated to change search results
    private val _searchParams = MutableStateFlow(SearchParams())

    // Error handling state
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: Flow<String?> = _errorState

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading

    // Meals flow with search and refresh support
    val pagedMeals: Flow<PagingData<Meal>> = refreshTrigger.flatMapLatest { _ ->
        // Get current search parameters
        val params = _searchParams.value

        Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 5,
                initialLoadSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                MealSearchPagingSource(
                    apiMeal = apiMeal,
                    searchType = params.searchType,
                    queryText = params.queryText,
                    ingredients = params.ingredients,
                    ingredientMode = params.ingredientMode,
                    creatorId = params.creatorId,
                    communityId = params.communityId,
                    userId = params.userId,
                    region = params.region,
                    subRegion = params.subRegion,
                    continent = params.continent,
                    dietaryTags = params.dietaryTags,
                    minCalories = params.minCalories,
                    maxCalories = params.maxCalories,
                    maxTime = params.maxTime,
                    mealId = params.mealId,
                    similarMealId = params.similarMealId,
                    minSimilarity = params.minSimilarity,
                    threshold = params.threshold,
                    timeWindow = params.timeWindow,
                    trending = params.trending,
                    cookedOnly = params.cookedOnly,
                    plannedDate = params.plannedDate
                )
            }
        ).flow
    }.cachedIn(viewModelScope)

    /**
     * Update search parameters and refresh results
     */
    fun search(params: SearchParams) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _searchParams.value = params
                // Increment trigger to cause recomposition
                refreshTrigger.value = refreshTrigger.value + 1
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message ?: "Unknown error updating search parameters"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Force refresh with current search parameters
     */
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Increment trigger to cause recomposition
                refreshTrigger.value = refreshTrigger.value + 1
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message ?: "Unknown error refreshing meals"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update a single search parameter and refresh results
     */
    fun updateSearchParam(update: (SearchParams) -> SearchParams) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _searchParams.value = update(_searchParams.value)
                // Increment trigger to cause recomposition
                refreshTrigger.value = refreshTrigger.value + 1
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message ?: "Unknown error updating search parameter"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear any error state
     */
    fun clearError() {
        _errorState.value = null
    }

    /**
     * Data class to encapsulate all search parameters
     */
    data class SearchParams(
        // Level 1: Search Paradigm
        val searchType: String = "filters",

        // Level 2: Text Search
        val queryText: String? = null,

        // Level 3: Ingredient Search
        val ingredients: List<String>? = null,
        val ingredientMode: String? = null,

        // Level 4: Ownership Filtering
        val creatorId: String? = null,
        val communityId: String? = null,
        val userId: String? = null,

        // Level 5: Metadata Filtering
        val region: String? = null,
        val subRegion: String? = null,
        val continent: String? = null,
        val dietaryTags: List<String>? = null,
        val minCalories: Int? = null,
        val maxCalories: Int? = null,
        val maxTime: Int? = null,

        // Direct Lookup
        val mealId: String? = null,
        val similarMealId: String? = null,

        // Additional Parameters
        val minSimilarity: Double? = null,
        val threshold: Double? = null,
        val timeWindow: String? = null,
        val trending: Boolean? = null,
        val cookedOnly: Boolean? = null,
        val plannedDate: String? = null
    )
}