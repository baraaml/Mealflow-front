// File: app/src/main/java/com/example/mealflow/viewModel/ShoppingListViewModelFactory.kt
package com.example.mealflow.viewModel

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mealflow.data.repository.MealPlannerRepository
import com.example.mealflow.data.repository.ShoppingRepository
import com.example.mealflow.data.storage.LocalMealPlanStorage // Needed by ShoppingRepository
import com.example.mealflow.network.ApiMeal // Needed by MealRepository -> LocalMealPlanStorage

@RequiresApi(Build.VERSION_CODES.O)
class ShoppingListViewModelFactory( // Keep the name the same for replacement
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
            val mealPlannerRepository = MealPlannerRepository(context.applicationContext)

            // Dependencies for ShoppingRepository as per your existing ShoppingListViewModelFactory
            val apiMeal = ApiMeal()
            val originalMealRepository = com.example.mealflow.data.repository.MealRepository(apiMeal) // Existing one
            val localMealPlanStorage = LocalMealPlanStorage(context.applicationContext, originalMealRepository)
            val shoppingRepository = ShoppingRepository(context.applicationContext, localMealPlanStorage)

            return ShoppingListViewModel(
                context = context.applicationContext,
                mealPlannerRepository = mealPlannerRepository,
                shoppingRepository = shoppingRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}