package com.example.mealflow.viewModel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.data.model.DayPlan
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.model.MealType
import com.example.mealflow.data.model.PlannedMeal
import com.example.mealflow.data.model.PlannedMealsData
import com.example.mealflow.data.model.ShoppingPlan
import com.example.mealflow.data.repository.MealPlannerRepository
import com.example.mealflow.data.repository.MealRepository as GeneralMealRepository // For dummy meals
import com.example.mealflow.utils.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID
import com.example.mealflow.database.interactions.PendingInteraction
import com.example.mealflow.workers.InteractionSyncWorker
import androidx.work.*
import com.example.mealflow.database.CommunityDatabase
import java.util.concurrent.TimeUnit

private const val TAG = "MealPlannerViewModel"

@RequiresApi(Build.VERSION_CODES.O)
class MealPlannerViewModel(
    private val repository: MealPlannerRepository,
    private val context: Context,
    private val generalMealRepository: GeneralMealRepository, // For dummy meal simulation
    private val shoppingListViewModel: ShoppingListViewModel?,
    private val mealViewModel: MealViewModel
) : ViewModel() {

    private val _currentDate = MutableStateFlow(LocalDate.now()) // For DateNavigator on PlannerPage
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    private val _currentDayMeals = MutableStateFlow<List<PlannedMeal>>(emptyList())
    val currentDayMeals: StateFlow<List<PlannedMeal>> = _currentDayMeals.asStateFlow()

    private val _allPlannedMealsData = MutableStateFlow(PlannedMealsData())
    val allPlannedMealsData: StateFlow<PlannedMealsData> = _allPlannedMealsData.asStateFlow()

    // For creating a new plan in PlanConfigScreen
    private val _newPlanMeals = MutableStateFlow<MutableList<PlannedMeal>>(mutableListOf())
    val newPlanMeals: StateFlow<List<PlannedMeal>> = _newPlanMeals.asStateFlow()

    // Add loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Add error state
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    // Add celebration state tracker
    private val _hasShownCelebrationFor = MutableStateFlow<String?>(null)
    val hasShownCelebrationFor: StateFlow<String?> = _hasShownCelebrationFor.asStateFlow()

    // For AllPlansScreen calendar view
    private val _currentDisplayMonth = MutableStateFlow(LocalDate.now())
    val currentDisplayMonthString: StateFlow<String> = _currentDisplayMonth.map { date ->
        DateUtils.formatMonth(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DateUtils.formatMonth(_currentDisplayMonth.value)
    )

    private val _selectedWeek = MutableStateFlow(1) // 1-5
    val selectedWeek: StateFlow<Int> = _selectedWeek.asStateFlow()

    private val _weeklyPlans = MutableStateFlow<List<DayPlan>>(emptyList())
    val weeklyPlans: StateFlow<List<DayPlan>> = _weeklyPlans.asStateFlow()

    // Track number of weeks in the current month
    private val _numberOfWeeksInMonth = MutableStateFlow(0)
    val numberOfWeeksInMonth: StateFlow<Int> = _numberOfWeeksInMonth.asStateFlow()

    // For Shopping Plan creation meta-data
    private val _activeShoppingPlan = MutableStateFlow<ShoppingPlan?>(null)
    val activeShoppingPlan: StateFlow<ShoppingPlan?> = _activeShoppingPlan.asStateFlow()

    // Temporary state for meal addition flow (simplified simulation)
    private var targetDateForMealAddition: String? = null
    private var targetMealTypeForMealAddition: MealType? = null

    // For caching data
    private var lastRefreshTime = 0L
    private val REFRESH_INTERVAL = 5 * 60 * 1000L // 5 minutes

    // Add pending meal addition state
    private val _pendingMealAddition = MutableStateFlow<PendingMealAddition?>(null)
    val pendingMealAddition: StateFlow<PendingMealAddition?> = _pendingMealAddition.asStateFlow()

    init {
        Log.d(TAG, "Initializing.")
        loadAllPlannedMeals()
        updateNumberOfWeeksInMonth() // Initialize week count
        
        // Auto-select current week if we're viewing current month
        val today = LocalDate.now()
        val currentMonth = _currentDisplayMonth.value
        if (today.month == currentMonth.month && today.year == currentMonth.year) {
            selectCurrentWeek()
        }
    }

    private fun shouldRefreshData(): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime - lastRefreshTime > REFRESH_INTERVAL || _allPlannedMealsData.value.plannedMeals.isEmpty()
    }

    private fun loadAllPlannedMeals() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading all planned meals from repository.")
                val loadedData = repository.getPlannedMeals()
                _allPlannedMealsData.value = loadedData
                lastRefreshTime = System.currentTimeMillis()
                Log.d(TAG, "Loaded ${loadedData.plannedMeals.size} planned meals in total.")

                // Set initial displays after data is loaded
                setCurrentDate(_currentDate.value)
                updateWeeklyPlans()
            } catch (e: Exception) {
                handleError(e, "Error loading planned meals")
                // Initialize with empty data on error
                _allPlannedMealsData.value = PlannedMealsData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Improve error handling
    private fun handleError(e: Exception, message: String) {
        Log.e(TAG, message, e)
        _errorState.value = "$message: ${e.message}"
        // Clear after some time
        viewModelScope.launch {
            delay(3000)
            _errorState.value = null
        }
    }

    // Data class to track pending meal additions
    data class PendingMealAddition(
        val targetDate: String,
        val mealType: MealType,
        val sourceScreen: String
    )

    fun setPendingMealAddition(date: String, mealType: MealType, sourceScreen: String) {
        _pendingMealAddition.value = PendingMealAddition(date, mealType, sourceScreen)
        Log.d(TAG, "Setting pending meal addition: date=$date, mealType=$mealType, source=$sourceScreen")
    }

    fun clearPendingMealAddition() {
        _pendingMealAddition.value = null
    }

    // Called by DateNavigator on PlannerPage or when a specific date's meals are needed
    fun setCurrentDate(date: LocalDate) {
        _currentDate.value = date // Update the state for DateNavigator
        val dateString = DateUtils.formatToISO(date)
        Log.d(TAG, "Updating current day meals for date: $dateString")

        // Update current day meals from all planned meals
        _currentDayMeals.value = _allPlannedMealsData.value.plannedMeals
            .filter { it.date == dateString }
            .sortedBy { it.getMealTypeEnum().ordinal } // Sort by meal type enum ordinal for proper ordering

        Log.d(TAG, "Found ${_currentDayMeals.value.size} meals for $dateString.")
    }

    fun navigateToPreviousDay() {
        val newDate = _currentDate.value.minusDays(1)
        setCurrentDate(newDate) // This will update _currentDate and trigger _currentDayMeals update
    }

    fun navigateToNextDay() {
        val newDate = _currentDate.value.plusDays(1)
        setCurrentDate(newDate)
    }

    fun isCurrentDateToday(): Boolean {
        return _currentDate.value == LocalDate.now()
    }

    // Function to refresh current day meals - used for pull-to-refresh
    fun refreshCurrentDayMeals() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val refreshedData = repository.getPlannedMeals()
                _allPlannedMealsData.value = refreshedData
                lastRefreshTime = System.currentTimeMillis()

                // Update current day meals
                setCurrentDate(_currentDate.value)
                updateWeeklyPlans()

                Log.d(TAG, "Refreshed current day meals. Found ${_currentDayMeals.value.size} meals for ${_currentDate.value}")
            } catch (e: Exception) {
                handleError(e, "Error refreshing meals")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function to check if celebration should be shown and manage its state
    fun shouldShowCelebration(): Boolean {
        val dateString = DateUtils.formatToISO(_currentDate.value)
        val cookedMealsCount = currentDayMeals.value.count { it.isCooked }
        val totalMealsCount = currentDayMeals.value.size

        val shouldCelebrate = cookedMealsCount > 0 && cookedMealsCount == totalMealsCount &&
                _hasShownCelebrationFor.value != dateString &&
                totalMealsCount > 0

        if (shouldCelebrate) {
            _hasShownCelebrationFor.value = dateString
        }

        return shouldCelebrate
    }

    // Function to handle swipe to mark as cooked
    fun handleMealSwipeAction(plannedMealId: String) {
        toggleCookedStatus(plannedMealId)
        // Additional swipe-specific logic could be added here if needed
    }

    // --- Deletion of a saved planned meal ---
    fun deletePlannedMeal(plannedMealId: String) {
        viewModelScope.launch {
            // Optimistically update the UI
            val currentData = _allPlannedMealsData.value
            val updatedMeals = currentData.plannedMeals.filterNot { it.id == plannedMealId }
            _allPlannedMealsData.value = currentData.copy(plannedMeals = updatedMeals)

            // Update the displays that depend on the data
            updateWeeklyPlans()
            setCurrentDate(_currentDate.value)

            // Call the repository to delete from the backend
            try {
                repository.deletePlannedMeal(plannedMealId)
                Log.d(TAG, "Successfully deleted planned meal with id: $plannedMealId")
            } catch (e: Exception) {
                handleError(e, "Failed to delete planned meal")
                // If the deletion fails, roll back the UI change
                _allPlannedMealsData.value = currentData
                updateWeeklyPlans()
                setCurrentDate(_currentDate.value)
            }
        }
    }

    // --- Plan Creation Flow (PlanConfigScreen) ---
    fun addMealToNewPlan(date: String, meal: Meal, mealType: MealType) {
        val plannedMeal = PlannedMeal(
            id = UUID.randomUUID().toString(), // Unique ID for this planning instance
            meal = meal,
            date = date,
            mealType = mealType.name,
            isCooked = false
        )

        _newPlanMeals.update { currentList ->
            val newList = currentList.toMutableList()
            // Replace if a meal of the same type already exists for this date in the new plan
            val indexToReplace = newList.indexOfFirst {
                it.date == date && it.getMealTypeEnum() == mealType
            }

            if (indexToReplace >= 0) {
                newList[indexToReplace] = plannedMeal
            } else {
                newList.add(plannedMeal)
            }
            newList
        }

        Log.d(TAG, "Added to new plan: ${meal.name} on $date as $mealType. Total in new plan: ${_newPlanMeals.value.size}")
    }

    fun handleSelectedMeal(meal: Meal) {
        val pending = _pendingMealAddition.value ?: return

        viewModelScope.launch {
            try {
                when (pending.sourceScreen) {
                    "PlanConfigScreen" -> addMealToNewPlan(pending.targetDate, meal, pending.mealType)
                    "AllPlansScreen" -> addSingleMealToStorage(meal, DateUtils.parseLocalDate(pending.targetDate)!!, pending.mealType)
                    "PlannerPage" -> {
                        val targetDate = DateUtils.parseLocalDate(pending.targetDate)
                        if (targetDate != null) {
                            addSingleMealToStorage(meal, targetDate, pending.mealType)
                        } else {
                            throw IllegalArgumentException("Invalid date format for: ${pending.targetDate}")
                        }
                    }
                    else -> Log.w(TAG, "Unknown source screen: ${pending.sourceScreen}")
                }
                // Clear pending meal addition after successful processing
                _pendingMealAddition.value = null
            } catch (e: Exception) {
                handleError(e, "Error adding selected meal")
            }
        }
    }

    // SIMULATED: Called when "Add Meal" is clicked in PlanConfigScreen or AllPlansScreen
    fun prepareForMealAddition(date: String, mealType: MealType, sourceScreen: String = "AllPlansScreen") {
        setPendingMealAddition(date, mealType, sourceScreen)
        Log.d(TAG, "Preparing to add meal for $date as $mealType from $sourceScreen. User should navigate to search.")
    }

    // SIMULATED: This would be called by SearchPage upon selecting a meal.
    suspend fun getADummyMeal(): Meal? {
        return try {
            val result = generalMealRepository.searchMeals(limit = 1) // Fetch any one meal for simulation
            result.getOrNull()?.firstOrNull()
        } catch (e: Exception) {
            handleError(e, "Error getting dummy meal")
            null
        }
    }

    fun getMealsForDateInNewPlan(date: String): List<PlannedMeal> {
        return _newPlanMeals.value.filter { it.date == date }
    }

    fun removeMealFromNewPlan(plannedMealId: String) {
        _newPlanMeals.update { currentList ->
            currentList.filterNot { it.id == plannedMealId }.toMutableList()
        }
    }

    fun clearNewPlan() {
        _newPlanMeals.value = mutableListOf()
        Log.d(TAG, "Cleared new plan builder.")
    }

    // --- AllPlansScreen Logic ---
    fun previousMonth() {
        _currentDisplayMonth.update { it.minusMonths(1) }
        updateNumberOfWeeksInMonth() // Update week count for the new month
        
        // Try to maintain relative week position or select current week if available
        val today = LocalDate.now()
        val newMonth = _currentDisplayMonth.value
        if (today.month == newMonth.month && today.year == newMonth.year) {
            selectCurrentWeek()
        } else {
            selectWeek(1) // Reset to first week
        }
        
        Log.d(TAG, "Navigated to previous month: ${currentDisplayMonthString.value}")
    }

    fun nextMonth() {
        _currentDisplayMonth.update { it.plusMonths(1) }
        updateNumberOfWeeksInMonth() // Update week count for the new month
        
        // Try to maintain relative week position or select current week if available
        val today = LocalDate.now()
        val newMonth = _currentDisplayMonth.value
        if (today.month == newMonth.month && today.year == newMonth.year) {
            selectCurrentWeek()
        } else {
            selectWeek(1) // Reset to first week
        }
        
        Log.d(TAG, "Navigated to next month: ${currentDisplayMonthString.value}")
    }

    // Calculate weeks based on days of month (1-7, 8-14, 15-21, 22-28, 29-31)
    private fun updateNumberOfWeeksInMonth() {
        val current = _currentDisplayMonth.value
        val yearMonth = YearMonth.of(current.year, current.month)
        val daysInMonth = yearMonth.lengthOfMonth()
        
        // Calculate number of weeks based on days in month
        // Week 1: days 1-7, Week 2: days 8-14, etc.
        val weekCount = Math.ceil(daysInMonth / 7.0).toInt()
        
        _numberOfWeeksInMonth.value = weekCount
        
        Log.d(TAG, "Month: ${current.month} ${current.year}")
        Log.d(TAG, "Days in month: $daysInMonth")
        Log.d(TAG, "Calculated $weekCount weeks for display")
    }

    fun selectWeek(week: Int) {
        val maxWeeks = _numberOfWeeksInMonth.value
        val newWeek = week.coerceIn(1, maxWeeks)
        
        // Only update if the week actually changed
        if (_selectedWeek.value != newWeek) {
            _selectedWeek.value = newWeek
            updateWeeklyPlans()
            Log.d(TAG, "Selected week: $newWeek/$maxWeeks for ${currentDisplayMonthString.value}")
        }
    }

    // Updated function to get the week number for a date based on days of month
    fun getWeekNumberForDate(date: LocalDate): Int {
        val current = _currentDisplayMonth.value
        if (date.month != current.month || date.year != current.year) {
            return 1 // Default to first week if date is outside current month
        }

        // Calculate week number based on day of month
        // Week 1: days 1-7, Week 2: days 8-14, etc.
        val dayOfMonth = date.dayOfMonth
        val weekNumber = ((dayOfMonth - 1) / 7) + 1
        
        return weekNumber
    }

    // Function to automatically select the week containing today's date
    fun selectCurrentWeek() {
        val today = LocalDate.now()
        val current = _currentDisplayMonth.value
        
        if (today.month == current.month && today.year == current.year) {
            val weekNumber = getWeekNumberForDate(today)
            selectWeek(weekNumber)
            Log.d(TAG, "Auto-selected current week: $weekNumber")
        } else {
            selectWeek(1) // Default to first week if today is not in current month
        }
    }

    // Updated weekly plans calculation to use days of month based weeks
    private fun updateWeeklyPlans() {
        val current = _currentDisplayMonth.value
        val yearMonth = YearMonth.of(current.year, current.month)
        val daysInMonth = yearMonth.lengthOfMonth()
        
        // Calculate the start day of the selected week
        val weekIndex = _selectedWeek.value - 1 // 0-based index
        val firstDayOfWeek = (weekIndex * 7) + 1
        
        // Calculate the end day of the selected week
        val lastDayOfWeek = Math.min(firstDayOfWeek + 6, daysInMonth)
        
        // Create a list of dates for the selected week
        val weekDates = (firstDayOfWeek..lastDayOfWeek).map { day ->
            LocalDate.of(current.year, current.month, day)
        }
        
        Log.d(TAG, "Week ${_selectedWeek.value} calculation:")
        Log.d(TAG, "  First day of week: $firstDayOfWeek")
        Log.d(TAG, "  Last day of week: $lastDayOfWeek")
        Log.d(TAG, "  Selected week: ${weekDates.first()} to ${weekDates.last()}")
        Log.d(TAG, "  Week dates: ${weekDates.joinToString(", ")}")

        // Transform these dates into DayPlan objects
        val newWeeklyPlans = weekDates.map { date ->
            val dateString = DateUtils.formatToISO(date)
            val mealsForDate = _allPlannedMealsData.value.plannedMeals.filter { it.date == dateString }

            DayPlan(
                date = dateString,
                breakfastMeals = mealsForDate.filter { it.getMealTypeEnum() == MealType.BREAKFAST },
                lunchMeals = mealsForDate.filter { it.getMealTypeEnum() == MealType.LUNCH },
                dinnerMeals = mealsForDate.filter { it.getMealTypeEnum() == MealType.DINNER },
                snackMeals = mealsForDate.filter { it.getMealTypeEnum() == MealType.SNACK },
                otherMeals = mealsForDate.filter { it.getMealTypeEnum() == MealType.OTHER }
            )
        }

        _weeklyPlans.value = newWeeklyPlans

        val totalMealsInView = newWeeklyPlans.sumOf {
            it.breakfastMeals.size + it.lunchMeals.size + it.dinnerMeals.size + 
            it.snackMeals.size + it.otherMeals.size
        }

        Log.d(TAG, "Updated weekly plans: $totalMealsInView meals across ${newWeeklyPlans.size} days")
    }

    // Function to search through planned meals
    fun searchPlannedMeals(query: String): List<PlannedMeal> {
        if (query.isEmpty()) return emptyList()

        return _allPlannedMealsData.value.plannedMeals.filter {
            it.meal.name.contains(query, ignoreCase = true) ||
                    it.meal.description?.contains(query, ignoreCase = true) == true
        }
    }

    // Get week range as a formatted string for display
    fun getSelectedWeekRangeString(): String {
        val current = _currentDisplayMonth.value
        return DateUtils.getWeekRangeString(current.year, current.monthValue, _selectedWeek.value)
    }

    // Check if the selected week contains today
    fun selectedWeekContainsToday(): Boolean {
        val current = _currentDisplayMonth.value
        return DateUtils.weekContainsToday(current.year, current.monthValue, _selectedWeek.value)
    }

    // Navigate to the month containing today and select current week
    fun goToCurrentMonth() {
        val today = LocalDate.now()
        _currentDisplayMonth.value = today.withDayOfMonth(1)
        updateNumberOfWeeksInMonth()
        selectCurrentWeek()
        Log.d(TAG, "Navigated to current month and selected current week")
    }

    // Get statistics for the current week
    fun getCurrentWeekStats(): WeekStats {
        val totalMeals = _weeklyPlans.value.sumOf { 
            it.breakfastMeals.size + it.lunchMeals.size + it.dinnerMeals.size + 
            it.snackMeals.size + it.otherMeals.size
        }
        val daysWithMeals = _weeklyPlans.value.count { 
            it.breakfastMeals.isNotEmpty() || it.lunchMeals.isNotEmpty() || 
            it.dinnerMeals.isNotEmpty() || it.snackMeals.isNotEmpty() || 
            it.otherMeals.isNotEmpty()
        }
        val totalDays = _weeklyPlans.value.size
        val emptyDays = totalDays - daysWithMeals

        return WeekStats(
            totalMeals = totalMeals,
            daysWithMeals = daysWithMeals,
            emptyDays = emptyDays,
            totalDays = totalDays
        )
    }

    // Data class for week statistics
    data class WeekStats(
        val totalMeals: Int,
        val daysWithMeals: Int,
        val emptyDays: Int,
        val totalDays: Int
    )

    // --- Shopping Plan Meta-Data Management ---
    fun createShoppingPlan(planStartDate: String, planEndDate: String, frequency: Int, shoppingDays: List<String>) {
        val newShoppingPlan = ShoppingPlan(
            id = UUID.randomUUID().toString(), // New ID for each shopping configuration
            planStartDate = planStartDate,
            planEndDate = planEndDate,
            frequency = frequency,
            shoppingDays = shoppingDays
        )

        _activeShoppingPlan.value = newShoppingPlan // Set as the currently active shopping setup

        viewModelScope.launch {
            try {
                // Save this ShoppingPlan meta-data
                repository.addShoppingPlan(newShoppingPlan) // Use the more specific method
                Log.d(TAG, "Created and stored shopping plan meta-data: $newShoppingPlan")
            } catch (e: Exception) {
                handleError(e, "Error saving shopping plan")
            }
        }
    }

    // Helper to get a meal by its ID (e.g., for MealDetailScreen)
    fun getMealById(mealId: String): Meal? {
        return _allPlannedMealsData.value.plannedMeals.find { it.meal.mealId == mealId }?.meal
    }

    fun saveNewPlanToStorage() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                if (_newPlanMeals.value.isEmpty()) {
                    Log.d(TAG, "New plan is empty. Nothing to save.")
                    return@launch
                }

                Log.d(TAG, "Saving new plan with ${_newPlanMeals.value.size} meals.")
                val currentOverallData = repository.getPlannedMeals()
                val updatedOverallPlannedMeals = currentOverallData.plannedMeals.toMutableList()
                
                // Create a map of existing meals by date and type for quick lookup
                val existingMealsByDateAndType = updatedOverallPlannedMeals.groupBy {
                    "${it.date}_${it.mealType}"
                }
                
                // Create a new list that will contain all meals except those being replaced
                val mealsToKeep = updatedOverallPlannedMeals.toMutableList()
                
                // Process each new meal
                _newPlanMeals.value.forEach { newMeal ->
                    val key = "${newMeal.date}_${newMeal.mealType}"
                    
                    // Remove any existing meal for this exact date and type
                    mealsToKeep.removeAll { it.date == newMeal.date && it.mealType == newMeal.mealType }
                    
                    // Add the new meal
                    mealsToKeep.add(newMeal)
                }
                
                // Use the updated list
                val finalMealsList = mealsToKeep

                val updatedData = PlannedMealsData(
                    plannedMeals = finalMealsList.distinctBy { it.id }.sortedBy { it.date },
                    lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )

                repository.savePlannedMeals(updatedData)
                _allPlannedMealsData.value = updatedData

                // Update UI views
                setCurrentDate(_currentDate.value)
                updateWeeklyPlans()
                
                // Update shopping list for each new meal if there's an active shopping plan
                if (shoppingListViewModel != null) {
                    Log.d(TAG, "Updating shopping list with new meals")
                    _newPlanMeals.value.forEach { newMeal ->
                        shoppingListViewModel.addSingleMealToShoppingList(newMeal)
                    }
                } else {
                    Log.d(TAG, "No shopping list view model available, skipping shopping list update")
                }

                Log.d(TAG, "New plan saved. It remains in _newPlanMeals for shopping config. " +
                        "Total meals in storage now: ${updatedData.plannedMeals.size}")
            } catch (e: Exception) {
                handleError(e, "Error saving new plan")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun finalizeAndClearNewPlanBuilder() {
        clearNewPlan()
        Log.d(TAG, "_newPlanMeals cleared after shopping list flow or cancellation.")
    }

    fun addSingleMealToStorage(meal: Meal, date: LocalDate, mealType: MealType) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dateString = DateUtils.formatToISO(date)
                val newPlannedMeal = PlannedMeal(
                    id = UUID.randomUUID().toString(),
                    meal = meal,
                    date = dateString,
                    mealType = mealType.name,
                    isCooked = false
                )

                val currentData = repository.getPlannedMeals()
                val updatedPlannedMeals = currentData.plannedMeals.toMutableList()

                // Log existing meals for debugging
                Log.d(TAG, "Current total meals before adding: ${updatedPlannedMeals.size}")
                Log.d(TAG, "Adding meal for date: $dateString, type: $mealType")
                
                // Find and remove any existing meal for this exact date and type
                val mealsToRemove = updatedPlannedMeals.filter { 
                    it.date == dateString && it.getMealTypeEnum() == mealType 
                }
                
                if (mealsToRemove.isNotEmpty()) {
                    Log.d(TAG, "Replacing ${mealsToRemove.size} existing meals for $dateString as $mealType")
                    updatedPlannedMeals.removeAll(mealsToRemove)
                }
                
                // Add the new meal
                updatedPlannedMeals.add(newPlannedMeal)
                
                // Verify the meal was added correctly
                val mealAdded = updatedPlannedMeals.any { it.id == newPlannedMeal.id }
                Log.d(TAG, "Meal added successfully: $mealAdded. New total: ${updatedPlannedMeals.size}")

                val updatedData = PlannedMealsData(
                    plannedMeals = updatedPlannedMeals.distinctBy { it.id }.sortedBy { it.date },
                    lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )

                // Update storage first
                repository.savePlannedMeals(updatedData)
                
                // Update all state in a specific order to ensure UI consistency
                _allPlannedMealsData.value = updatedData // Update local cache of all plans
                
                // Force a refresh of the current date view
                val currentDate = _currentDate.value
                _currentDate.value = LocalDate.MIN // Reset to force state change
                setCurrentDate(currentDate) // Set back to actual date
                
                // Update weekly plans view
                updateWeeklyPlans()

                // Add the meal to the shopping list if there's an active shopping plan
                shoppingListViewModel?.addSingleMealToShoppingList(newPlannedMeal)

                Log.d(TAG, "Added single meal '${meal.name}' to $dateString as $mealType.")
            } catch (e: Exception) {
                handleError(e, "Error adding single meal")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Improved function to toggle a planned meal's cooked status
    fun toggleCookedStatus(plannedMealId: String) {
        viewModelScope.launch {
            val currentData = _allPlannedMealsData.value
            val mealToToggle = currentData.plannedMeals.find { it.id == plannedMealId } ?: return@launch
            val newCookedStatus = !mealToToggle.isCooked

            // Optimistically update the local state for immediate UI feedback.
            val updatedMeals = currentData.plannedMeals.map { meal ->
                if (meal.id == plannedMealId) meal.copy(isCooked = newCookedStatus) else meal
            }
            val updatedData = currentData.copy(plannedMeals = updatedMeals)
            
            // Persist the change to the local repository.
            repository.savePlannedMeals(updatedData)
            _allPlannedMealsData.value = updatedData
            setCurrentDate(_currentDate.value)
            updateWeeklyPlans()

            // Sync this change with the backend.
            val mealId = mealToToggle.meal.mealId
            val interactionType = if (newCookedStatus) "cooked" else "uncooked"
            
            val success = mealViewModel.sendInteraction(mealId, interactionType)
            if (success) {
                Log.d(TAG, "Successfully synced '$interactionType' status for meal: $plannedMealId")
            } else {
                Log.w(TAG, "Failed to sync '$interactionType' status for meal: $plannedMealId. Queuing for background sync.")
                // queueCookedInteractionForSync(mealId) // You may want a similar queue for 'uncooked'
            }
        }
    }

    private fun queueCookedInteractionForSync(mealId: String) {
        viewModelScope.launch {
            val userId = mealViewModel.currentUserId.value
            if (userId == null) {
                Log.e(TAG, "Cannot queue interaction sync: User ID is null.")
                return@launch
            }

            // 1. Create PendingInteraction
            val pendingInteraction = PendingInteraction(
                userId = userId,
                mealId = mealId,
                interactionType = "cooked"
            )

            // 2. Save to local DB
            val interactionDao = CommunityDatabase.getDatabase(context).pendingInteractionDao()
            interactionDao.insert(pendingInteraction)

            // 3. Enqueue WorkManager job
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<InteractionSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "interaction-sync",
                ExistingWorkPolicy.KEEP,
                syncRequest
            )
            Log.d(TAG, "Enqueued 'cooked' interaction for background sync for meal: $mealId")
        }
    }

    private fun updateCurrentDayAndWeeklyPlans(updatedData: PlannedMealsData) {
        val currentDataString = DateUtils.formatToISO(_currentDate.value)
        _currentDayMeals.value = updatedData.plannedMeals.filter { it.date == currentDataString }
        updateWeeklyPlans()
    }
}