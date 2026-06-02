// File: app/src/main/java/com/example/mealflow/data/storage/FileManager.kt
package com.example.mealflow.data.storage

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.CompositeShoppingList
import com.example.mealflow.data.model.PlannedMealsData
import com.example.mealflow.data.model.ShoppingPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException

object FileManager {
    private const val PLANNED_MEALS_FILE = "planned_meals.json"
    private const val SHOPPING_LISTS_FILE = "shopping_lists.json" // For CompositeShoppingList
    private const val SHOPPING_PLANS_FILE = "shopping_plans.json"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
    }

    // PlannedMealsData Functions (Provided by user, adapted slightly for current structure)
    suspend fun savePlannedMeals(context: Context, data: PlannedMealsData) {
        context.writeJsonToFile(PLANNED_MEALS_FILE, json.encodeToString(data))
    }

    suspend fun loadPlannedMeals(context: Context): PlannedMealsData {
        val jsonString = context.readJsonFromFile(PLANNED_MEALS_FILE)
        return if (jsonString.isNullOrEmpty()) {
            PlannedMealsData()
        } else {
            try {
                json.decodeFromString<PlannedMealsData>(jsonString)
            } catch (e: Exception) {
                Log.e("FileManager", "Error decoding PlannedMealsData from $PLANNED_MEALS_FILE", e)
                PlannedMealsData() // Return default on error
            }
        }
    }

    // ShoppingPlan Functions
    suspend fun saveShoppingPlans(context: Context, data: List<ShoppingPlan>) {
        context.writeJsonToFile(SHOPPING_PLANS_FILE, json.encodeToString(data))
    }

    suspend fun loadShoppingPlans(context: Context): List<ShoppingPlan> {
        val jsonString = context.readJsonFromFile(SHOPPING_PLANS_FILE)
        return if (jsonString.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<ShoppingPlan>>(jsonString)
            } catch (e: Exception) {
                Log.e("FileManager", "Error decoding ShoppingPlans from $SHOPPING_PLANS_FILE", e)
                emptyList()
            }
        }
    }

    // CompositeShoppingList Functions
    suspend fun saveCompositeShoppingLists(context: Context, data: List<CompositeShoppingList>) {
        context.writeJsonToFile(SHOPPING_LISTS_FILE, json.encodeToString(data))
    }

    suspend fun loadCompositeShoppingLists(context: Context): List<CompositeShoppingList> {
        val jsonString = context.readJsonFromFile(SHOPPING_LISTS_FILE)
        return if (jsonString.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<CompositeShoppingList>>(jsonString)
            } catch (e: Exception) {
                Log.e("FileManager", "Error decoding CompositeShoppingLists from $SHOPPING_LISTS_FILE", e)
                emptyList()
            }
        }
    }
}

// Extension functions (can be in this file or a separate utils file)
suspend fun Context.writeJsonToFile(filename: String, jsonString: String) {
    withContext(Dispatchers.IO) {
        try {
            openFileOutput(filename, Context.MODE_PRIVATE).use { stream ->
                stream.write(jsonString.toByteArray())
            }
            Log.d("FileManager", "Successfully wrote to $filename")
        } catch (e: Exception) {
            Log.e("FileManager", "Error writing to $filename", e)
        }
    }
}

suspend fun Context.readJsonFromFile(filename: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val content = openFileInput(filename).bufferedReader().use { reader ->
                reader.readText()
            }
            Log.d("FileManager", "Successfully read from $filename")
            content
        } catch (e: FileNotFoundException) {
            Log.w("FileManager", "$filename not found")
            null
        } catch (e: Exception) {
            Log.e("FileManager", "Error reading from $filename", e)
            null
        }
    }
}