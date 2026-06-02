package com.example.mealflow.viewModel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mealflow.data.repository.MealPlannerRepository
// Import the general MealRepository and its dependency ApiMeal for dummy meal fetching
import com.example.mealflow.data.repository.MealRepository as GeneralMealRepository
import com.example.mealflow.network.ApiMeal

private const val TAG = "MealPlannerViewModelFactory"

/**
 * Factory for creating MealPlannerViewModel instances with proper dependencies.
 */
@RequiresApi(Build.VERSION_CODES.O)
class MealPlannerViewModelFactory(
    private val context: Context,
    private val shoppingListViewModel: ShoppingListViewModel? = null,
    private val mealViewModel: MealViewModel
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealPlannerViewModel::class.java)) {
            try {
                // Use application context to prevent memory leaks
                val appContext = context.applicationContext

                // Create repository instances
                val mealPlannerRepository = MealPlannerRepository(appContext)

                // Create ApiMeal instance
                val apiMeal = ApiMeal()

                // Create GeneralMealRepository for dummy meal fetching
                val generalMealRepository = GeneralMealRepository(apiMeal)

                // Create and return the ViewModel
                return MealPlannerViewModel(
                    repository = mealPlannerRepository,
                    context = appContext,
                    generalMealRepository = generalMealRepository,
                    shoppingListViewModel = shoppingListViewModel,
                    mealViewModel = mealViewModel
                ) as T
            } catch (e: Exception) {
                Log.e(TAG, "Error creating MealPlannerViewModel", e)
                throw e
            }
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}