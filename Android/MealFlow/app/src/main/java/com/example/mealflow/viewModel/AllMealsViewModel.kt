// File: com/example/mealflow/viewModel/AllMealsViewModel.kt
package com.example.mealflow.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mealflow.data.model.Meal
import com.example.mealflow.network.ApiMeal
import com.example.mealflow.utils.MealSearchPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class AllMealsViewModel(
    application: Application,
    val sectionTitle: String // Made public to be accessible if needed elsewhere, or keep private
) : AndroidViewModel(application) {

    private val apiMeal = ApiMeal() // Consider dependency injection for ApiMeal
    // Create repository for caching and prefetching
    private val mealRepository = com.example.mealflow.data.repository.MealRepository(apiMeal)


    // Refresh trigger to allow manual refreshes
    private val refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    // Store the current search parameters. Initially derived from sectionTitle.
    private val _currentSearchParams = MutableStateFlow(
        deriveInitialSearchParams(sectionTitle)
    )
    val currentSearchParams: Flow<MealSearchViewModel.SearchParams> = _currentSearchParams

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedMeals: Flow<PagingData<Meal>> = refreshTrigger.flatMapLatest { _ ->
        val params = _currentSearchParams.value
        Pager(
            config = PagingConfig(
                pageSize = 10, // How many items to load per page
                prefetchDistance = 5, // When to start loading next page
                initialLoadSize = 10, // How many items to load on initial an T                       d refresh
                enablePlaceholders = false // No need for placeholders if UI handles loading states
            ),
            pagingSourceFactory = {
                MealSearchPagingSource(
                    apiMeal = apiMeal,
                    mealRepository = mealRepository, // Pass repository for prefetching
                    searchType = params.searchType,
                    queryText = params.queryText,
                    ingredients = params.ingredients,
                    ingredientMode = params.ingredientMode,
                    creatorId = params.creatorId,
                    communityId = params.communityId,
                    userId = params.userId, // Important for "For You" if it's collaborative
                    region = params.region,
                    subRegion = params.subRegion,
                    continent = params.continent,
                    dietaryTags = params.dietaryTags,
                    minCalories = params.minCalories,
                    maxCalories = params.maxCalories,
                    maxTime = params.maxTime,
                    trending = params.trending,
                    cookedOnly = params.cookedOnly,
                    plannedDate = params.plannedDate
                    // Ensure all params from MealSearchViewModel.SearchParams are passed
                )
            }
        ).flow.cachedIn(viewModelScope) // Cache in ViewModelScope
    }

    private fun deriveInitialSearchParams(title: String): MealSearchViewModel.SearchParams {
        return when (title.lowercase()) {
            "community's popular" -> MealSearchViewModel.SearchParams(searchType = "filters", trending = true)
            "for you" -> MealSearchViewModel.SearchParams(searchType = "collaborative") // Or "recommendations" or "filters" with specific user-based logic if backend supports
            // Example: If "For You" is based on user's saved/liked meals' similarity
            // searchType = "similar", similarMealId = "some_user_derived_meal_id_or_profile_vector"
            else -> MealSearchViewModel.SearchParams(searchType = "filters") // Default for unknown titles
        }
    }

    fun refresh() {
        refreshTrigger.value = System.currentTimeMillis()
    }

    // Call this if AllMealsPage implements its own filtering that requires a new backend query
    fun updateSearchQuery(newQueryText: String?) {
        viewModelScope.launch {
            _currentSearchParams.value = _currentSearchParams.value.copy(
                queryText = newQueryText,
                // If newQueryText is not blank, you might want to change searchType to "semantic" or "filters"
                // depending on how your backend handles text search for these sections.
                // For now, it keeps the original searchType derived from sectionTitle.
                searchType = if (!newQueryText.isNullOrBlank()) "filters" else deriveInitialSearchParams(sectionTitle).searchType
            )
            refresh()
        }
    }


    class Factory(
        private val application: Application,
        private val sectionTitle: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AllMealsViewModel::class.java)) {
                return AllMealsViewModel(application, sectionTitle) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

}
private fun deriveInitialSearchParams(title: String): MealSearchViewModel.SearchParams {
    return when (title.lowercase()) {
        "community's popular" -> MealSearchViewModel.SearchParams(searchType = "filters", trending = true)
        "for you", "you may like" -> MealSearchViewModel.SearchParams(
            searchType = "collaborative"
            // Potentially pass userId here if your backend needs it for collaborative filtering
            // userId = your_user_id_source (e.g., from a TokenManager or UserPreferences)
        )
        "recommended" -> MealSearchViewModel.SearchParams(
            searchType = "filters" // This might be a general "filters" call, or
            // searchType = "recommendations" // if your backend has a specific endpoint for this
        )
        "planned" -> MealSearchViewModel.SearchParams(
            searchType = "filters", // Assuming "planned" meals are also fetched via API
            plannedDate = "all" // Example: or a specific date range. This depends on your API.
            // If "Planned" meals are from local storage, this ViewModel/PagingSource is not suitable.
        )
        else -> MealSearchViewModel.SearchParams(searchType = "filters") // Default
    }
}