package com.example.mealflow.data.repository

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.PlannedMealsData
import com.example.mealflow.data.model.ShoppingPlan
import com.example.mealflow.data.storage.OptimizedFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "MealPlannerRepository"

/**
 * Repository for handling meal planning related data operations.
 * Primarily interacts with FileManager for storing and retrieving PlannedMeals and ShoppingPlans.
 */
class MealPlannerRepository(private val context: Context) {

    private val dataMutex = Mutex()

    /**
     * Gets all planned meals from storage.
     * @return PlannedMealsData containing all planned meals
     */
    suspend fun getPlannedMeals(): PlannedMealsData = withContext(Dispatchers.IO) {
        dataMutex.withLock {
            try {
                OptimizedFileManager.loadPlannedMeals(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading planned meals", e)
                PlannedMealsData() // Return empty data on error
            }
        }
    }

    /**
     * Saves planned meals to storage.
     * @param data PlannedMealsData object containing all meals to save
     */
    suspend fun savePlannedMeals(data: PlannedMealsData) = withContext(Dispatchers.IO) {
        dataMutex.withLock {
            try {
                OptimizedFileManager.savePlannedMeals(context, data)
                Log.d(TAG, "Successfully saved ${data.plannedMeals.size} planned meals")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving planned meals", e)
                throw e // Rethrow to let ViewModel handle the error
            }
        }
    }

    /**
     * Gets all shopping plans from storage.
     * @return List of ShoppingPlan objects
     */
    suspend fun getAllShoppingPlans(): List<ShoppingPlan> = withContext(Dispatchers.IO) {
        try {
            OptimizedFileManager.loadShoppingPlans(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading shopping plans", e)
            emptyList() // Return empty list on error
        }
    }

    /**
     * Gets a specific shopping plan by ID
     * @param id The unique identifier of the shopping plan
     * @return The ShoppingPlan if found, null otherwise
     */
    suspend fun getShoppingPlanById(id: String): ShoppingPlan? = withContext(Dispatchers.IO) {
        try {
            val plans = OptimizedFileManager.loadShoppingPlans(context)
            plans.find { it.id == id }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding shopping plan with id: $id", e)
            null
        }
    }

    /**
     * Saves multiple shopping plans to storage.
     * @param plans List of ShoppingPlan objects to save
     */
    suspend fun saveShoppingPlans(plans: List<ShoppingPlan>) = withContext(Dispatchers.IO) {
        dataMutex.withLock {
            try {
                OptimizedFileManager.saveShoppingPlans(context, plans)
                Log.d(TAG, "Successfully saved ${plans.size} shopping plans")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving shopping plans", e)
                throw e // Rethrow to let ViewModel handle the error
            }
        }
    }

    /**
     * Adds or updates a single shopping plan.
     * @param plan ShoppingPlan to add or update
     */
    suspend fun addShoppingPlan(plan: ShoppingPlan) = withContext(Dispatchers.IO) {
        try {
            val currentPlans = getAllShoppingPlans().toMutableList()
            val existingIndex = currentPlans.indexOfFirst { it.id == plan.id }

            if (existingIndex >= 0) {
                // Update existing plan
                currentPlans[existingIndex] = plan
                Log.d(TAG, "Updated existing shopping plan with id: ${plan.id}")
            } else {
                // Add new plan
                currentPlans.add(plan)
                Log.d(TAG, "Added new shopping plan with id: ${plan.id}")
            }

            saveShoppingPlans(currentPlans)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding/updating shopping plan", e)
            throw e // Rethrow to let ViewModel handle the error
        }
    }

    /**
     * Deletes a shopping plan by ID.
     * @param planId ID of the shopping plan to delete
     * @return true if deletion was successful, false otherwise
     */
    suspend fun deleteShoppingPlan(planId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentPlans = getAllShoppingPlans().toMutableList()
            val initialSize = currentPlans.size

            currentPlans.removeAll { it.id == planId }

            if (currentPlans.size < initialSize) {
                saveShoppingPlans(currentPlans)
                Log.d(TAG, "Deleted shopping plan with id: $planId")
                true
            } else {
                Log.d(TAG, "No shopping plan found with id: $planId")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting shopping plan with id: $planId", e)
            false
        }
    }

    /**
     * Deletes a planned meal by its ID from the stored data.
     * @param plannedMealId The unique identifier of the planned meal to delete.
     */
    suspend fun deletePlannedMeal(plannedMealId: String) = withContext(Dispatchers.IO) {
        dataMutex.withLock {
            try {
                // Load the current data
                val currentData = OptimizedFileManager.loadPlannedMeals(context)
                
                // Filter out the meal to be deleted
                val updatedMeals = currentData.plannedMeals.filterNot { it.id == plannedMealId }

                // Save the updated data, if a change was made
                if (updatedMeals.size < currentData.plannedMeals.size) {
                    val updatedData = currentData.copy(plannedMeals = updatedMeals)
                    OptimizedFileManager.savePlannedMeals(context, updatedData)
                    Log.d(TAG, "Successfully deleted planned meal with id: $plannedMealId")
                } else {
                    Log.w(TAG, "Attempted to delete a non-existent planned meal with id: $plannedMealId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting planned meal with id: $plannedMealId", e)
                throw e // Rethrow to let the ViewModel handle the error state
            }
        }
    }
}