package com.example.mealflow.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.model.MealResponse
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.data.repository.MealRepository
import com.example.mealflow.network.ApiInteract
import com.example.mealflow.network.InteractionRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.mealflow.database.CommunityDatabase
import com.example.mealflow.database.interactions.PendingInteraction
import com.example.mealflow.workers.InteractionSyncWorker
import androidx.work.*
import java.util.concurrent.TimeUnit
import android.util.Log

class MealViewModel(
    application: Application,
    val repository: MealRepository
) : AndroidViewModel(application) {

    // Recommended/General meals (e.g., for initial display or a general pool)
    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _meals.asStateFlow() // This might be your old "recommended"

    // Dedicated StateFlows for HomePage sections
    private val _trendingMealsVM = MutableStateFlow<List<Meal>>(emptyList())
    val trendingMealsVM: StateFlow<List<Meal>> = _trendingMealsVM.asStateFlow()

    private val _collaborativeMealsVM = MutableStateFlow<List<Meal>>(emptyList())
    val collaborativeMealsVM: StateFlow<List<Meal>> = _collaborativeMealsVM.asStateFlow()

    // Search type for SearchPage navigation
    private val _searchType = MutableStateFlow<String>("filters")
    val searchType: StateFlow<String> = _searchType.asStateFlow()

    private val _isLoading = MutableStateFlow(false) // General loading for overall operations
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Meal>>(emptyList())
    val searchResults: StateFlow<List<Meal>> = _searchResults.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    val mealResponse: StateFlow<MealResponse?> = repository.mealResponse // From repository

    private val interactionDao by lazy {
        CommunityDatabase.getDatabase(application).pendingInteractionDao()
    }

    init {
        // Initial fetches - can be done here or triggered by HomePage
        // fetchRecommendedMeals() // If _meals is used for a general recommendation section
        fetchTrendingMeals()
        fetchCollaborativeMeals()
        loadCurrentUserId()
    }

    private fun loadCurrentUserId() {
        viewModelScope.launch {
            val userPrefs = UserPreferencesManager(getApplication())
            _currentUserId.value = userPrefs.getMyId()
        }
    }

    // Example function - replace with your actual logic
    fun fetchRecommendedMeals() {
        viewModelScope.launch {
            _isLoading.value = true
            // Example: Fetch general popular meals or based on some default filter
            repository.searchMeals(searchType = "filters", limit = 10, trending = true /* or other criteria */)
                .onSuccess { response -> _meals.value = response } // Assuming searchMeals returns List<Meal>
                .onFailure { error -> _errorMessage.value = error.message }
            _isLoading.value = false
        }
    }

    fun fetchTrendingMeals() {
        viewModelScope.launch {
            // You might want a separate isLoading flag for this if calls are independent
            // _isLoading.value = true // Or a specific isLoadingTrending
            repository.searchMeals(searchType = "filters", trending = true, limit = 10, minCalories = 0, maxCalories = 10000, maxTime = 1000000)
                .onSuccess { response -> _trendingMealsVM.value = response }
                .onFailure { error -> _errorMessage.value = "Trending: ${error.message}" }
            // _isLoading.value = false
        }
    }

    fun fetchCollaborativeMeals() {
        viewModelScope.launch {
            // _isLoading.value = true // Or a specific isLoadingCollaborative
            repository.searchMeals(searchType = "collaborative", limit = 10, minCalories = 0, maxCalories = 10000, maxTime = 1000000) // Assuming user_id is handled by repo/API
                .onSuccess { response -> _collaborativeMealsVM.value = response }
                .onFailure { error -> _errorMessage.value = "Collaborative: ${error.message}" }
            // _isLoading.value = false
        }
    }


    fun refreshMeals() { // This might refresh all sections or a specific one
        // fetchRecommendedMeals()
        fetchTrendingMeals()
        fetchCollaborativeMeals()
    }

    fun findMealById(mealId: String?): Meal? {
        if (mealId == null) return null
        return meals.value.find { it.mealId == mealId }
            ?: trendingMealsVM.value.find { it.mealId == mealId }
            ?: collaborativeMealsVM.value.find { it.mealId == mealId }
            ?: searchResults.value.find { it.mealId == mealId }
    }
    
    // New function to fetch a meal by ID asynchronously
    // Cache for recently fetched meals by ID to avoid repeated API calls
    private val mealCache = mutableMapOf<String, Meal>()
    
    fun fetchMealById(mealId: String?, onComplete: (Meal?) -> Unit) {
        if (mealId == null) {
            onComplete(null)
            return
        }
        
        // First check in-memory collections
        val cachedMeal = findMealById(mealId) ?: mealCache[mealId]
        if (cachedMeal != null) {
            onComplete(cachedMeal)
            return
        }
        
        // If not found in memory, fetch from repository
        viewModelScope.launch {
            try {
                val meal = repository.getMealById(mealId)
                if (meal != null) {
                    // Store in our local cache for future use
                    mealCache[mealId] = meal
                    // Also update other collections if needed
                    updateMealInStateFlows(meal)
                }
                onComplete(meal)
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching meal: ${e.message}"
                onComplete(null)
            }
        }
    }

    /**
     * Preload meal details for a list of meals
     * This improves performance when navigating to the detail screen
     */
    fun preloadMealDetails(mealIds: List<String>) {
        viewModelScope.launch {
            mealIds.forEach { mealId ->
                // Skip if already in cache
                if (findMealById(mealId) == null) {
                    try {
                        repository.getMealById(mealId)
                        // No need to do anything with the result, it's cached by the repository
                    } catch (e: Exception) {
                        // Silently ignore errors during preloading
                    }
                }
            }
        }
    }

    fun searchMeals(
        searchType: String = "filters",
        query: String = "",
        ingredients: List<String>? = null,
        ingredientMode: String? = null,
        region: String? = null,
        subRegion: String? = null,
        dietaryTags: List<String>? = null,
        trending: Boolean? = null,
        offset: Int = 0,
        limit: Int = 10,
        minCalories: Int?,
        maxCalories: Int?,
        maxTime: Int?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.searchMeals(
                searchType = searchType,
                queryText = if (query.isNotBlank()) query else null,
                ingredients = ingredients,
                ingredientMode = ingredientMode,
                region = region,
                subRegion = subRegion,
                dietaryTags = dietaryTags,
                trending = trending,
                offset = offset,
                limit = limit,
                minCalories = minCalories,
                maxCalories = maxCalories,
                maxTime = maxTime
            )
            result.onSuccess { meals ->
                if (offset == 0 || _searchResults.value.isEmpty()) {
                    _searchResults.value = meals
                } else {
                    _searchResults.value = _searchResults.value + meals
                }
            }.onFailure { error ->
                _errorMessage.value = "Search failed: ${error.message}"
            }
            _isLoading.value = false
        }
    }

    fun toggleFavorite(meal: Meal) {
        // Find the most up-to-date version of the meal from our state
        val currentMeal = findMealById(meal.mealId) ?: meal
        val newFavoriteState = !currentMeal.isFavorited

        val interactionRequest = InteractionRequest(
            userId = "", // Will be replaced by the ViewModel
            mealId = currentMeal.mealId,
            type = "favorite",
            liked = currentMeal.isLiked,
            disliked = currentMeal.isDisliked,
            ignored = false, // Assuming 'ignore' state is not tracked on the meal object
            viewed = true,   // A favorite action implies a view
            saved = currentMeal.isSaved,
            cooked = currentMeal.isCooked,
            favorited = newFavoriteState,
            rating = currentMeal.rating.toFloat(),
            reviewText = null // No review text when favoriting
        )
        
        // Use the unified interaction function
        sendInteraction(interactionRequest)
    }

    fun recordView(meal: Meal) {
        val interactionRequest = InteractionRequest(
            userId = "", // Will be replaced by the ViewModel
            mealId = meal.mealId,
            type = "view",
            liked = meal.isLiked,
            disliked = meal.isDisliked,
            ignored = false,
            viewed = true,
            saved = meal.isSaved,
            cooked = meal.isCooked,
            favorited = meal.isFavorited,
            rating = meal.rating.toFloat(),
            reviewText = null // No review text when viewing
        )
        sendInteraction(interactionRequest)
    }

    fun sendInteraction(interactionRequest: InteractionRequest) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId == null) {
                Log.e("MealViewModel", "User ID is null, cannot send interaction.")
                // Optionally, you could queue this interaction to be sent later when the ID is available
                return@launch
            }

            val updatedInteractionRequest = interactionRequest.copy(userId = userId)

            // Update local state immediately
            updateMealInStateFlows(updatedInteractionRequest)


            try {
                // Attempt to send to the backend
                val result = ApiInteract().sendInteraction(updatedInteractionRequest)
                if (result.isSuccess) {
                    Log.d("MealViewModel", "Interaction sent successfully for mealId: ${updatedInteractionRequest.mealId}")
                    // Optional: You might want to remove it from a local queue if it was pending
                } else {
                    Log.w("MealViewModel", "Failed to send interaction, queuing for later. Reason: ${result.exceptionOrNull()?.message}")
                    queueInteraction(updatedInteractionRequest)
                }
            } catch (e: Exception) {
                Log.e("MealViewModel", "Error sending interaction, queuing for later.", e)
                queueInteraction(updatedInteractionRequest)
            }
        }
    }

    suspend fun sendInteraction(mealId: String, interactionType: String, reviewText: String? = null): Boolean {
        val userId = _currentUserId.value
        if (userId == null) {
            Log.e("MealViewModel", "User ID is null, cannot send interaction.")
            return false
        }

        // Find the meal in one of the state flows to get its current state
        val currentMeal = findMealById(mealId)

        val interactionRequest = InteractionRequest(
            userId = userId,
            mealId = mealId,
            type = interactionType,
            reviewText = reviewText,
            // Preserve other states from the current meal object if it exists
            liked = currentMeal?.isLiked ?: false,
            disliked = currentMeal?.isDisliked ?: false,
            ignored = false, // Not tracked on Meal object
            viewed = true,   // Any interaction implies a view
            saved = currentMeal?.isSaved ?: false,
            cooked = currentMeal?.isCooked ?: (interactionType == "cooked"),
            favorited = currentMeal?.isFavorited ?: (interactionType == "favorite"),
            rating = currentMeal?.rating?.toFloat()
        )

        // Optimistically update the UI
        updateMealInStateFlows(interactionRequest)

        // Try to send to the backend, or queue if it fails
        return try {
            val result = ApiInteract().sendInteraction(interactionRequest)
            if (result.isSuccess) {
                Log.d("MealViewModel", "Interaction '$interactionType' sent successfully for meal: $mealId")
                true
            } else {
                Log.w("MealViewModel", "Failed to send interaction, queuing. Reason: ${result.exceptionOrNull()?.message}")
                queueInteraction(interactionRequest)
                false // Indicate that it was not sent immediately
            }
        } catch (e: Exception) {
            Log.e("MealViewModel", "Error sending interaction, queuing.", e)
            queueInteraction(interactionRequest)
            false // Indicate that it was not sent immediately
        }
    }

    private fun queueInteraction(interactionRequest: InteractionRequest) {
        viewModelScope.launch {
            val pendingInteraction = PendingInteraction(
                userId = interactionRequest.userId,
                mealId = interactionRequest.mealId,
                interactionType = interactionRequest.type,
                reviewText = interactionRequest.reviewText
            )
            interactionDao.insert(pendingInteraction)
            Log.d("MealViewModel", "Interaction queued for meal ${interactionRequest.mealId}")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<InteractionSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            WorkManager.getInstance(getApplication()).enqueue(workRequest)
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Set search type for navigation to search page
    fun setSearchType(type: String) {
        _searchType.value = type
        // Log for debugging
        println("Setting search type to: $type")
    }

    // Retry mechanism for failed meals
    fun retryFailedMeal(mealId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.searchMeals(
                    searchType = "by_id",
                    mealId = mealId,
                    limit = 1
                )

                result.onSuccess { meals ->
                    if (meals.isNotEmpty()) {
                        val updatedMeal = meals.first()
                        // Update the meal in all relevant StateFlows
                        updateMealInStateFlows(updatedMeal)
                    }
                }.onFailure { error ->
                    _errorMessage.value = "Failed to retry meal: ${error.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Retry failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper method to update a meal across all StateFlows
    private fun updateMealInStateFlows(updatedMeal: Meal) {
        val updater: (Meal) -> Meal = { meal ->
            if (meal.mealId == updatedMeal.mealId) updatedMeal else meal
        }

        _meals.update { it.map(updater) }
        _trendingMealsVM.update { it.map(updater) }
        _collaborativeMealsVM.update { it.map(updater) }
        _searchResults.update { it.map(updater) }
    }

    // Refresh meals with loading errors
    fun refreshFailedMeals() {
        viewModelScope.launch {
            val failedMealIds = mutableSetOf<String>()

            // Collect IDs of meals with loading errors from all StateFlows
            _meals.value.filter { it.hasLoadingError }.forEach { failedMealIds.add(it.mealId) }
            _trendingMealsVM.value.filter { it.hasLoadingError }.forEach { failedMealIds.add(it.mealId) }
            _collaborativeMealsVM.value.filter { it.hasLoadingError }.forEach { failedMealIds.add(it.mealId) }
            _searchResults.value.filter { it.hasLoadingError }.forEach { failedMealIds.add(it.mealId) }

            // Retry each failed meal
            failedMealIds.forEach { mealId ->
                retryFailedMeal(mealId)
            }
        }
    }

    // Get count of meals with data quality issues
    fun getDataQualityStats(): Triple<Int, Int, Int> {
        val allMeals = (_meals.value + _trendingMealsVM.value + _collaborativeMealsVM.value + _searchResults.value).distinctBy { it.mealId }
        val errorCount = allMeals.count { it.hasLoadingError }
        val partialCount = allMeals.count { it.isPartialData }
        val totalCount = allMeals.size
        return Triple(errorCount, partialCount, totalCount)
    }

    fun ignoreMeal(meal: Meal) {
        val interactionRequest = InteractionRequest(
            userId = "", // Will be replaced by the ViewModel
            mealId = meal.mealId,
            type = "ignore",
            ignored = true,
            liked = meal.isLiked,
            disliked = meal.isDisliked,
            viewed = true, // Ignoring implies viewing
            saved = meal.isSaved,
            cooked = meal.isCooked,
            favorited = meal.isFavorited,
            rating = meal.rating.toFloat(),
            reviewText = ""
        )
        sendInteraction(interactionRequest)
        removeMealFromStateFlows(meal.mealId)
    }

    fun likeMeal(meal: Meal) {
        val interactionRequest = InteractionRequest(
            userId = "", // Will be replaced by the ViewModel
            mealId = meal.mealId,
            type = "like",
            liked = true, // Set the like flag
            disliked = false, // Cannot like and dislike at the same time
            ignored = false,
            viewed = true, // Liking implies viewing
            saved = meal.isSaved,
            cooked = meal.isCooked,
            favorited = meal.isFavorited,
            rating = meal.rating.toFloat(),
            reviewText = ""
        )
        sendInteraction(interactionRequest)
        // Remove the meal from the list for immediate feedback - REMOVED
        // removeMealFromStateFlows(meal.mealId)
    }

    private fun removeMealFromStateFlows(mealId: String) {
        _trendingMealsVM.update { it.filterNot { meal -> meal.mealId == mealId } }
        _collaborativeMealsVM.update { it.filterNot { meal -> meal.mealId == mealId } }
        _searchResults.update { it.filterNot { meal -> meal.mealId == mealId } }
        _meals.update { it.filterNot { meal -> meal.mealId == mealId } }
    }

    private fun updateMealInStateFlows(interaction: InteractionRequest) {
        val mealIdToUpdate = interaction.mealId

        val updater: (List<Meal>) -> List<Meal> = { mealList ->
            mealList.map { meal ->
                if (meal.mealId == mealIdToUpdate) {
                    meal.copy(
                        isFavorited = interaction.favorited,
                        isLiked = interaction.liked,
                        isDisliked = interaction.disliked,
                        isSaved = interaction.saved,
                        isCooked = interaction.cooked,
                        rating = interaction.rating?.toDouble() ?: meal.rating
                        // reviewText is not part of the Meal model, so we can't update it here.
                        // The UI will get the review text from the interaction response if needed.
                    )
                } else {
                    meal
                }
            }
        }

        _meals.update(updater)
        _trendingMealsVM.update(updater)
        _collaborativeMealsVM.update(updater)
        _searchResults.update(updater)
    }
}
