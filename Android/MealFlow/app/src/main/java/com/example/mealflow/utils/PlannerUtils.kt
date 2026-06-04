package com.example.mealflow.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavBackStackEntry
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.model.MealType
import com.example.mealflow.viewModel.MealPlannerViewModel
import com.example.mealflow.viewModel.MealViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ObservePlannerResult(
    backStackEntry: NavBackStackEntry,
    mealViewModel: MealViewModel,
    mealPlannerViewModel: MealPlannerViewModel,
    currentScreenIdentifier: String
) {
    val selectedMealId by backStackEntry.savedStateHandle.getStateFlow<String?>("selectedMealId", null).collectAsState()
    val targetDate by backStackEntry.savedStateHandle.getStateFlow<String?>("targetDate", null).collectAsState()
    val targetMealTypeName by backStackEntry.savedStateHandle.getStateFlow<String?>("targetMealType", null).collectAsState()
    val resultSourceScreen by backStackEntry.savedStateHandle.getStateFlow<String?>("sourceScreenResult", null).collectAsState()

    LaunchedEffect(selectedMealId, targetDate, targetMealTypeName, resultSourceScreen) {
        if (resultSourceScreen == currentScreenIdentifier &&
            selectedMealId != null && targetDate != null && targetMealTypeName != null) {
            Log.d("ObservePlannerResult", "Result for $currentScreenIdentifier: MealId=$selectedMealId, Date=$targetDate, Type=$targetMealTypeName")
            val mealTypeForResult = runCatching { MealType.valueOf(targetMealTypeName!!) }.getOrNull()
            val dateForMeal = DateUtils.parseLocalDate(targetDate!!)

            mealViewModel.fetchMealById(selectedMealId) { fetchedMeal ->
                if (fetchedMeal != null) {
                    processMealForPlanner(fetchedMeal, mealTypeForResult, targetDate!!, dateForMeal, currentScreenIdentifier, backStackEntry, mealPlannerViewModel)
                } else {
                    Log.e("ObservePlannerResult", "Failed to fetch meal with ID: $selectedMealId")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun processMealForPlanner(
    meal: Meal,
    mealType: MealType?,
    targetDateString: String,
    dateForMeal: LocalDate?,
    currentScreenIdentifier: String,
    backStackEntry: NavBackStackEntry,
    mealPlannerViewModel: MealPlannerViewModel
) {
    if (mealType != null && dateForMeal != null) {
        when (currentScreenIdentifier) {
            "PlanConfigScreen" -> {
                Log.d("ObservePlannerResult", "Adding to new plan for PlanConfigScreen")
                mealPlannerViewModel.addMealToNewPlan(targetDateString, meal, mealType)
            }
            "PlannerPage" -> {
                Log.d("ObservePlannerResult", "Setting up for AddToPlanDialog for PlannerPage")
                backStackEntry.savedStateHandle["mealIdForDialog"] = meal.mealId
                backStackEntry.savedStateHandle["initialDateForDialog"] = targetDateString
            }
            "AllPlansScreen" -> {
                Log.d("ObservePlannerResult", "Adding to storage for $currentScreenIdentifier")
                mealPlannerViewModel.addSingleMealToStorage(meal, dateForMeal, mealType)
            }
        }
    } else {
        Log.e("ObservePlannerResult", "MealType or date was null. MealType: $mealType, Date: $dateForMeal")
    }

    backStackEntry.savedStateHandle.remove<String?>("selectedMealId")
    backStackEntry.savedStateHandle.remove<String?>("targetDate")
    backStackEntry.savedStateHandle.remove<String?>("targetMealType")
    backStackEntry.savedStateHandle.remove<String?>("sourceScreenResult")
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateShoppingDays(planStartDate: LocalDate, planDurationDays: Int, frequency: Int): List<LocalDate> {
    return when (frequency) {
        0 -> listOf(planStartDate)
        1 -> {
            val weeks = (planDurationDays / 7) + 1
            (0 until min(weeks, 3)).map { planStartDate.plusDays(it * 7L) }
        }
        2 -> {
            val biWeeks = (planDurationDays / 14) + 1
            (0 until min(biWeeks, 2)).map { planStartDate.plusDays(it * 14L) }
        }
        else -> listOf(planStartDate)
    }
}
