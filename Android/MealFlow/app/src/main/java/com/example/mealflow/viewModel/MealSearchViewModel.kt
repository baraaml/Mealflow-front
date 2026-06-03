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
import com.example.mealflow.ui.screens.search.FilterType
import com.example.mealflow.ui.screens.search.MealSearchAction
import com.example.mealflow.ui.screens.search.MealSearchEvent
import com.example.mealflow.ui.screens.search.MealSearchState
import com.example.mealflow.utils.MealSearchPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Optimized ViewModel for Meal Search using MVI and debounced metadata filtering.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MealSearchViewModel(
    application: Application,
    private val apiMeal: ApiMeal,
    private val mealRepository: MealRepository
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(MealSearchState())
    val state = _state.asStateFlow()

    private val _events = Channel<MealSearchEvent>()
    val events = _events.receiveAsFlow()

    private val refreshTrigger = MutableStateFlow(0)

    // Debounced metadata search triggers
    private val _ingredientQuery = MutableStateFlow("")
    private val _regionQuery = MutableStateFlow("")
    private val _subRegionQuery = MutableStateFlow("")
    private val _dietaryTagQuery = MutableStateFlow("")

    init {
        setupMetadataSearch()
    }

    private fun setupMetadataSearch() {
        // Debounce ingredient search
        _ingredientQuery
            .debounce(300L)
            .distinctUntilChanged()
            .filter { it.length >= 2 }
            .onEach { query ->
                mealRepository.getIngredients(filter = query).onSuccess { results ->
                    _state.update { it.copy(ingredientResults = results.map { ing -> ing.name }) }
                }
            }
            .launchIn(viewModelScope)

        // Debounce region search
        _regionQuery
            .debounce(300L)
            .distinctUntilChanged()
            .onEach { query ->
                mealRepository.getRegions(filter = query).onSuccess { results ->
                    _state.update { it.copy(regionResults = results.map { it.name }) }
                }
            }
            .launchIn(viewModelScope)

        // Debounce sub-region search
        _subRegionQuery
            .debounce(300L)
            .distinctUntilChanged()
            .onEach { query ->
                mealRepository.getSubRegions(filter = query).onSuccess { results ->
                    _state.update { it.copy(subRegionResults = results.map { it.name }) }
                }
            }
            .launchIn(viewModelScope)

        // Debounce dietary tag search
        _dietaryTagQuery
            .debounce(300L)
            .distinctUntilChanged()
            .onEach { query ->
                mealRepository.getDietaryTags(filter = query).onSuccess { results ->
                    _state.update { it.copy(dietaryTagResults = results.map { it.name }) }
                }
            }
            .launchIn(viewModelScope)
    }

    val pagedMeals: Flow<PagingData<Meal>> = refreshTrigger.flatMapLatest { _ ->
        val params = _state.value.searchParams

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
                    queryText = params.queryText ?: _state.value.query,
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

    fun onAction(action: MealSearchAction) {
        when (action) {
            is MealSearchAction.OnQueryChange -> {
                _state.update { it.copy(query = action.newQuery) }
            }
            is MealSearchAction.OnSearchClick -> refresh()
            is MealSearchAction.OnRefresh -> refresh()
            is MealSearchAction.ClearError -> _state.update { it.copy(error = null) }
            is MealSearchAction.ToggleFilters -> _state.update { it.copy(isFiltersExpanded = !it.isFiltersExpanded) }
            
            // Metadata queries
            is MealSearchAction.OnIngredientQueryChange -> {
                _ingredientQuery.value = action.query
                _state.update { it.copy(ingredientSearchQuery = action.query) }
            }
            is MealSearchAction.OnRegionQueryChange -> {
                _regionQuery.value = action.query
                _state.update { it.copy(regionSearchQuery = action.query) }
            }
            is MealSearchAction.OnSubRegionQueryChange -> {
                _subRegionQuery.value = action.query
                _state.update { it.copy(subRegionSearchQuery = action.query) }
            }
            is MealSearchAction.OnDietaryTagQueryChange -> {
                _dietaryTagQuery.value = action.query
                _state.update { it.copy(dietaryTagSearchQuery = action.query) }
            }

            // Draft Updates
            is MealSearchAction.OnIngredientModeChange -> _state.update { it.copy(draftParams = it.draftParams.copy(ingredientMode = action.mode)) }
            is MealSearchAction.OnIngredientsChange -> _state.update { it.copy(draftParams = it.draftParams.copy(ingredients = action.ingredients)) }
            is MealSearchAction.OnRegionChange -> _state.update { it.copy(draftParams = it.draftParams.copy(region = action.region)) }
            is MealSearchAction.OnSubRegionChange -> _state.update { it.copy(draftParams = it.draftParams.copy(subRegion = action.subRegion)) }
            is MealSearchAction.OnDietaryTagsChange -> _state.update { it.copy(draftParams = it.draftParams.copy(dietaryTags = action.tags)) }
            is MealSearchAction.OnMinCaloriesChange -> _state.update { it.copy(draftParams = it.draftParams.copy(minCalories = action.calories)) }
            is MealSearchAction.OnMaxCaloriesChange -> _state.update { it.copy(draftParams = it.draftParams.copy(maxCalories = action.calories)) }
            is MealSearchAction.OnMaxTimeChange -> _state.update { it.copy(draftParams = it.draftParams.copy(maxTime = action.time)) }
            is MealSearchAction.OnSearchTypeChange -> _state.update { it.copy(draftParams = it.draftParams.copy(searchType = action.type)) }
            is MealSearchAction.OnSearchParamsChange -> {
                _state.update { it.copy(searchParams = action.params, draftParams = action.params) }
                refresh()
            }

            is MealSearchAction.ApplyFilters -> {
                _state.update { it.copy(searchParams = it.draftParams, isFiltersExpanded = false) }
                refresh()
            }
            is MealSearchAction.ResetFilters -> {
                _state.update { it.copy(draftParams = SearchParams(), searchParams = SearchParams()) }
                refresh()
            }
            is MealSearchAction.OnMealClick -> {
                viewModelScope.launch { _events.send(MealSearchEvent.NavigateToDetail(action.meal.mealId)) }
            }
            is MealSearchAction.RemoveFilter -> handleRemoveFilter(action)
        }
    }

    private fun handleRemoveFilter(action: MealSearchAction.RemoveFilter) {
        val current = _state.value.searchParams
        val newParams = when (action.filterType) {
            FilterType.INGREDIENT -> current.copy(ingredients = current.ingredients?.filter { it != action.value })
            FilterType.REGION -> current.copy(region = null)
            FilterType.SUB_REGION -> current.copy(subRegion = null)
            FilterType.DIETARY_TAG -> current.copy(dietaryTags = current.dietaryTags?.filter { it != action.value })
            FilterType.MIN_CALORIES -> current.copy(minCalories = null)
            FilterType.MAX_CALORIES -> current.copy(maxCalories = null)
            FilterType.MAX_TIME -> current.copy(maxTime = null)
        }
        _state.update { it.copy(searchParams = newParams, draftParams = newParams) }
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                refreshTrigger.value = refreshTrigger.value + 1
                _state.update { it.copy(error = null) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Unknown error refreshing meals") }
                _events.send(MealSearchEvent.ShowError(_state.value.error!!))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    data class SearchParams(
        val searchType: String = "filters",
        val queryText: String? = null,
        val ingredients: List<String>? = null,
        val ingredientMode: String? = null,
        val creatorId: String? = null,
        val communityId: String? = null,
        val userId: String? = null,
        val region: String? = null,
        val subRegion: String? = null,
        val continent: String? = null,
        val dietaryTags: List<String>? = null,
        val minCalories: Int? = null,
        val maxCalories: Int? = null,
        val maxTime: Int? = null,
        val mealId: String? = null,
        val similarMealId: String? = null,
        val minSimilarity: Double? = null,
        val threshold: Double? = null,
        val timeWindow: String? = null,
        val trending: Boolean? = null,
        val cookedOnly: Boolean? = null,
        val plannedDate: String? = null
    )
}
