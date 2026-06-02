package com.example.mealflow.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.repository.MealRepository

class MealDetailsViewModelFactory(
    private val mealRepository: MealRepository,
    private val mealId: String,
    private val initialMeal: Meal? = null
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealDetailsViewModel(mealRepository, mealId, initialMeal) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 