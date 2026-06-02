package com.example.mealflow.viewModel

import androidx.lifecycle.ViewModel
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.repository.MealRepository
import com.example.mealflow.network.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MealDetailsViewModel(
    private val mealRepository: MealRepository,
    private val mealId: String,
    initialMeal: Meal? = null
) : ViewModel() {

    private val _mealDetails = MutableStateFlow<Resource<Meal>>(
        if (initialMeal != null) Resource.Success(initialMeal) 
        else Resource.Loading()
    )
    val mealDetails: StateFlow<Resource<Meal>> = _mealDetails

    init {
        if (initialMeal == null) {
            fetchMealDetails()
        }
    }

    private fun fetchMealDetails() {
        viewModelScope.launch {
            _mealDetails.value = Resource.Loading()
            try {
                val meal = mealRepository.getMealById(mealId)
                if (meal != null) {
                    _mealDetails.value = Resource.Success(meal)
                } else {
                    _mealDetails.value = Resource.Error("Meal not found")
                }
            } catch (e: Exception) {
                _mealDetails.value = Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
    
    // Function to refresh meal details if needed
    fun refreshMealDetails() {
        fetchMealDetails()
    }
} 