// PlannedMealModels.kt
package com.example.mealflow.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class PlannedMeal(
    val id: String,
    val meal: Meal,
    val date: String, // ISO format date: YYYY-MM-DD
    val mealType: String, // Values: BREAKFAST, LUNCH, DINNER
    val isCooked: Boolean = false,
    val recurringPattern: String? = null,
    val color: String? = null
) {
    fun getMealTypeEnum(): MealType {
        return try {
            MealType.valueOf(mealType)
        } catch (e: IllegalArgumentException) {
            MealType.OTHER
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLocalDate(): LocalDate? {
        return try {
            // Use the DateUtils formatter for consistency
            com.example.mealflow.utils.DateUtils.parseLocalDate(date)
        } catch (e: Exception) {
            null
        }
    }
}

@Serializable
data class PlannedMealsData(
    val plannedMeals: List<PlannedMeal> = emptyList(),
    val lastUpdated: String = "" // ISO format date-time
)
