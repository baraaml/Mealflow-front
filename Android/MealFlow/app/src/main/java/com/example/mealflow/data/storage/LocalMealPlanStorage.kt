package com.example.mealflow.data.storage

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mealflow.data.model.PlannedMeal
import com.example.mealflow.data.model.PlannedMealsData
import com.example.mealflow.data.repository.MealRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate

class LocalMealPlanStorage(
    private val context: Context,
    private val mealRepository: MealRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }
    private val fileName = "planned_meals.json"

    private fun getStorageFile(): File = File(context.filesDir, fileName)

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun savePlannedMeals(data: PlannedMealsData): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = getStorageFile()
            val jsonString = json.encodeToString(PlannedMealsData.serializer(), data)
            file.writeText(jsonString)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("LocalStorage", "Save error", e)
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun loadPlannedMeals(): Result<PlannedMealsData> = withContext(Dispatchers.IO) {
        try {
            val file = getStorageFile()
            if (!file.exists()) {
                return@withContext Result.success(PlannedMealsData())
            }

            val jsonString = file.readText()
            val data = json.decodeFromString<PlannedMealsData>(jsonString)
            Result.success(data)
        } catch (e: Exception) {
            Log.e("LocalStorage", "Load error", e)
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addPlannedMeal(plannedMeal: PlannedMeal): Result<Unit> {
        return loadPlannedMeals().fold(
            onSuccess = { data ->
                val updatedMeals = data.plannedMeals.toMutableList().apply {
                    removeAll { it.id == plannedMeal.id ||
                            (it.date == plannedMeal.date && it.mealType == plannedMeal.mealType) }
                    add(plannedMeal)
                }
                savePlannedMeals(data.copy(
                    plannedMeals = updatedMeals,
                    lastUpdated = LocalDate.now().toString()
                ))
            },
            onFailure = { Result.failure(it) }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun markMealAsCooked(plannedMealId: String): Result<Unit> {
        return loadPlannedMeals().fold(
            onSuccess = { data ->
                val updatedMeals = data.plannedMeals.map { meal ->
                    if (meal.id == plannedMealId) meal.copy(isCooked = true) else meal
                }
                savePlannedMeals(data.copy(
                    plannedMeals = updatedMeals,
                    lastUpdated = LocalDate.now().toString()
                ))
            },
            onFailure = { Result.failure(it) }
        )
    }
}