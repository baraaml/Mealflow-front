package com.example.mealflow.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mealflow.data.model.AggregatedIngredient
import com.example.mealflow.data.model.GeneratedShoppingList
import com.example.mealflow.data.model.PlannedMeal
import com.example.mealflow.data.model.ShoppingSettings
import com.example.mealflow.data.storage.LocalMealPlanStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class ShoppingRepository(
    private val context: Context,
    private val localMealPlanStorage: LocalMealPlanStorage
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; isLenient = true }
    private val shoppingListsDir = File(context.filesDir, "shopping_lists")
    private val shoppingSettingsFile = File(context.filesDir, "shopping_settings.json")

    init {
        if (!shoppingListsDir.exists()) {
            shoppingListsDir.mkdirs()
        }
    }

    suspend fun getShoppingSettings(): ShoppingSettings = withContext(Dispatchers.IO) {
        if (!shoppingSettingsFile.exists()) {
            return@withContext ShoppingSettings()
        }
        try {
            val jsonString = shoppingSettingsFile.readText()
            if (jsonString.isBlank()) ShoppingSettings() else json.decodeFromString<ShoppingSettings>(jsonString)
        } catch (e: Exception) {
            Log.e("ShoppingRepository", "Error loading shopping settings: ${e.message}", e)
            ShoppingSettings() // Return default on error
        }
    }

    suspend fun saveShoppingSettings(settings: ShoppingSettings): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            shoppingSettingsFile.writeText(json.encodeToString(settings))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ShoppingRepository", "Error saving shopping settings: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getPlannedMealsInRange(startDate: LocalDate, endDate: LocalDate): List<PlannedMeal> {
        val loadResult = localMealPlanStorage.loadPlannedMeals() // This is already suspend and on Dispatchers.IO
        return if (loadResult.isSuccess) {
            loadResult.getOrThrow().plannedMeals.filter { plannedMeal ->
                try {
                    // Use the getLocalDate() method from your PlannedMeal model
                    val mealDate = plannedMeal.getLocalDate()
                    mealDate != null && !mealDate.isBefore(startDate) && !mealDate.isAfter(endDate)
                } catch (e: Exception) {
                    Log.e("ShoppingRepository", "Error processing planned meal date: ${plannedMeal.date}", e)
                    false
                }
            }
        } else {
            Log.e("ShoppingRepository", "Failed to load planned meals for range", loadResult.exceptionOrNull())
            emptyList()
        }
    }

    suspend fun aggregateIngredientsFromPlannedMeals(
        plannedMeals: List<PlannedMeal>
    ): List<AggregatedIngredient> = withContext(Dispatchers.Default) {
        val ingredientMap = mutableMapOf<String, AggregatedIngredient>()

        plannedMeals.forEach { plannedMeal ->
            plannedMeal.meal.ingredients.forEach { ingredient ->
                // Key by name and unit for aggregation
                val key = "${ingredient.phrase?.trim()?.lowercase()}_${ingredient.unit?.trim()?.lowercase()}"
                val quantityDouble = ingredient.quantity

                if (quantityDouble != null && quantityDouble > 0) {
                    val existing = ingredientMap[key]
                    if (existing != null) {
                        existing.totalQuantity += quantityDouble
                    } else {
                        ingredientMap[key] = AggregatedIngredient(
                            ingredientId = ingredient.id,
                            name = ingredient.phrase.toString(),
                            unit = ingredient.unit.toString(),
                            totalQuantity = quantityDouble
                        )
                    }
                } else if (quantityDouble == null) {
                    Log.w("ShoppingRepository", "Could not parse quantity for ingredient: ${ingredient.phrase}, value: '${ingredient.quantity}'")
                }
                // Implicitly, quantityDouble <= 0 is ignored
            }
        }
        ingredientMap.values.toList().sortedBy { it.name } // Sort for consistent display
    }


    suspend fun saveGeneratedShoppingList(list: GeneratedShoppingList): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(shoppingListsDir, "list_${list.id}.json")
            file.writeText(json.encodeToString(list))
            cleanupOldLists() // Clean up after saving a new list
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ShoppingRepository", "Error saving generated shopping list: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getLatestShoppingLists(count: Int = 3): List<GeneratedShoppingList> = withContext(Dispatchers.IO) {
        try {
            shoppingListsDir.listFiles { _, name -> name.startsWith("list_") && name.endsWith(".json") }
                ?.mapNotNull { file ->
                    try {
                        json.decodeFromString<GeneratedShoppingList>(file.readText())
                    } catch (e: Exception) {
                        Log.e("ShoppingRepository", "Error decoding shopping list file: ${file.name}", e)
                        null
                    }
                }
                ?.sortedByDescending {
                    try { LocalDate.parse(it.generationDate, DateTimeFormatter.ISO_LOCAL_DATE) }
                    catch (e: Exception) { LocalDate.MIN } // Fallback for sorting if date is invalid
                }
                ?.take(count) ?: emptyList()
        } catch (e: Exception) {
            Log.e("ShoppingRepository", "Error listing shopping list files", e)
            emptyList()
        }
    }

    private suspend fun cleanupOldLists() = withContext(Dispatchers.IO) {
        try {
            val allFiles = shoppingListsDir.listFiles { _, name -> name.startsWith("list_") && name.endsWith(".json") }
            if (allFiles != null && allFiles.size > 3) {
                val allParsedLists = allFiles.mapNotNull { file ->
                    try {
                        json.decodeFromString<GeneratedShoppingList>(file.readText()) to file
                    } catch (e: Exception) { null }
                }.sortedByDescending { (listData, _) ->
                    try { LocalDate.parse(listData.generationDate, DateTimeFormatter.ISO_LOCAL_DATE) }
                    catch (e:Exception) { LocalDate.MIN }
                }

                if (allParsedLists.size > 3) {
                    allParsedLists.drop(3).forEach { (_, fileToDrop) ->
                        if (fileToDrop.delete()) {
                            Log.d("ShoppingRepository", "Deleted old shopping list file: ${fileToDrop.name}")
                        } else {
                            Log.w("ShoppingRepository", "Failed to delete old shopping list file: ${fileToDrop.name}")
                        }
                    }
                } else {

                }
            } else {

            }
        } catch (e: Exception) {
            Log.e("ShoppingRepository", "Error during cleanupOldLists: ${e.message}", e)
        }
    }

    fun generateNewListId(): String = UUID.randomUUID().toString()
}