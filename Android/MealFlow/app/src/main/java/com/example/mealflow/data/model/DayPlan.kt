// File: app/src/main/java/com/example/mealflow/data/model/DayPlan.kt
// This was inferred from MealPlannerViewModel and AllPlansScreen
package com.example.mealflow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DayPlan(
    val date: String, // ISO format date
    val breakfastMeals: List<PlannedMeal>, // Using existing PlannedMeal
    val lunchMeals: List<PlannedMeal>,    // Using existing PlannedMeal
    val dinnerMeals: List<PlannedMeal>,   // Using existing PlannedMeal
    val snackMeals: List<PlannedMeal> = emptyList(), // Added for SNACK type
    val otherMeals: List<PlannedMeal> = emptyList()  // Added for OTHER type
)