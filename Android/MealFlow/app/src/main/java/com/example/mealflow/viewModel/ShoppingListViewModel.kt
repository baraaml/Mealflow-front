// File: app/src/main/java/com/example/mealflow/viewModel/ShoppingListViewModel.kt
package com.example.mealflow.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.data.model.AggregatedIngredient
import com.example.mealflow.data.model.CompositeShoppingList
import com.example.mealflow.data.model.EnhancedShoppingListItem
import com.example.mealflow.data.model.MealIngredient
import com.example.mealflow.data.model.PlannedMeal
import com.example.mealflow.data.model.ShoppingListItem
import com.example.mealflow.data.model.ShoppingPlan
import com.example.mealflow.data.repository.MealPlannerRepository
import com.example.mealflow.data.repository.ShoppingRepository
import com.example.mealflow.data.storage.OptimizedFileManager
import com.example.mealflow.utils.DateUtils
import com.example.mealflow.utils.IngredientParser
import com.example.mealflow.utils.ShoppingReminderManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class NewShoppingListUiState(
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null,
    val activeShoppingPlan: ShoppingPlan? = null, // From MealPlannerViewModel or loaded here
    val compositeShoppingList: CompositeShoppingList? = null,
    val customItemToEdit: EnhancedShoppingListItem? = null, // For AddCustomItemScreen
    val showReminderDialog: Boolean = false,
    val reminderDialogDayIndex: Int = 0,
    val reminderDialogNotes: String = "",
    val needsExactAlarmPermission: Boolean = false
)

@RequiresApi(Build.VERSION_CODES.O)
class ShoppingListViewModel(
    private val context: Context,
    private val mealPlannerRepository: MealPlannerRepository,
    private val shoppingRepository: ShoppingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewShoppingListUiState())
    val uiState: StateFlow<NewShoppingListUiState> = _uiState.asStateFlow()
    private var currentShoppingPlan: ShoppingPlan? = null
    private var lastDeletedList: CompositeShoppingList? = null
    
    // Create the reminder manager
    private val reminderManager = ShoppingReminderManager(context)

    init {
        loadLatestShoppingList()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun loadLatestShoppingList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                Log.d("ShoppingListVM", "Loading latest shopping list...")
            val lists = OptimizedFileManager.loadCompositeShoppingLists(context)
                Log.d("ShoppingListVM", "Found ${lists.size} shopping lists")
                
                if (lists.isNotEmpty()) {
                    // Sort by ID in descending order to get the most recent list
                    val latestList = lists.sortedByDescending { it.id }.first()
                    Log.d("ShoppingListVM", "Latest list ID: ${latestList.id}, items: ${latestList.items.size}")
                    
                    val plans = OptimizedFileManager.loadShoppingPlans(context)
                    Log.d("ShoppingListVM", "Found ${plans.size} shopping plans")
                    
                    val plan = plans.find { it.id == latestList.shoppingPlanId }
                    if (plan == null) {
                        Log.w("ShoppingListVM", "No shopping plan found for list ${latestList.id}")
                    }
                    
                    _uiState.update { it.copy(
                        isLoading = false,
                        compositeShoppingList = latestList,
                        activeShoppingPlan = plan
                    ) }
                currentShoppingPlan = plan
            } else {
                    Log.d("ShoppingListVM", "No shopping lists found")
                _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e("ShoppingListVM", "Error loading shopping list", e)
                handleError(e, "Error loading shopping list")
            }
        }
    }

    /**
     * Creates a new shopping plan and generates a shopping list from the meal plan
     */
    fun createNewShoppingList(
        planStartDate: LocalDate,
        planEndDate: LocalDate,
        shoppingFrequency: Int, // 0=once, 1=weekly, 2=bi-weekly
        shoppingDays: List<LocalDate>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Fetch meals for the date range
                val mealsInDateRange = mealPlannerRepository.getMealsInDateRange(
                    DateUtils.formatToISO(planStartDate),
                    DateUtils.formatToISO(planEndDate)
                )

                val newShoppingPlan = ShoppingPlan(
                    id = UUID.randomUUID().toString(),
                    planStartDate = DateUtils.formatToISO(planStartDate),
                    planEndDate = DateUtils.formatToISO(planEndDate),
                    frequency = shoppingFrequency,
                    shoppingDays = shoppingDays.map { DateUtils.formatToISO(it) }
                )
                currentShoppingPlan = newShoppingPlan
                mealPlannerRepository.addShoppingPlan(newShoppingPlan)

                // Process ingredients with the new IngredientParser
                val processedItems = mutableListOf<EnhancedShoppingListItem>()
                
                mealsInDateRange.sortedBy { it.date }.forEach { plannedMeal ->
                    val mealDate = DateUtils.parseLocalDate(plannedMeal.date)!!
                    val shoppingDayIndex = assignToShoppingDay(mealDate, shoppingDays)

                    plannedMeal.meal.ingredients.forEach { ingredient ->
                        if ((ingredient.quantity ?: 0.0) > 0) {
                            // Process each ingredient using the parser
                            val shoppingItem = IngredientParser.processIngredientForShoppingList(
                                ingredient = ingredient,
                                shoppingDayIndex = shoppingDayIndex
                            ).copy(
                                // Store the meal date with the ingredient for sorting by urgency
                                mealDate = plannedMeal.date
                            )
                            processedItems.add(shoppingItem)
                        }
                    }
                }
                
                // Consolidate similar ingredients while preserving the earliest meal date
                val consolidatedItems = IngredientParser.consolidateShoppingListWithDates(processedItems)

                // Create and save the shopping list
                val shoppingList = CompositeShoppingList(
                    id = UUID.randomUUID().toString(),
                    shoppingPlanId = newShoppingPlan.id,
                    items = consolidatedItems,
                    inHouseItems = emptyList()
                )

                // Update UI state
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeShoppingPlan = newShoppingPlan,
                        compositeShoppingList = shoppingList
                    )
                }

                // Save the new list
                saveCompositeShoppingList(shoppingList)

            } catch (e: Exception) {
                handleError(e, "Error creating shopping list")
            }
        }
    }

    private fun calculateShoppingDays(planStartDate: LocalDate, planDurationDays: Int, frequency: Int): List<LocalDate> {
        if (frequency <= 0) return listOf(planStartDate)
        if (frequency == 1) return listOf(planStartDate)

        val days = mutableListOf<LocalDate>()
        val interval = ((planDurationDays -1 ).toDouble() / (frequency -1).coerceAtLeast(1)).coerceAtLeast(1.0)

        // Limit to maximum 3 shopping days
        val effectiveFrequency = minOf(frequency, 3)

        for (i in 0 until effectiveFrequency) {
            val dayOffset = if (effectiveFrequency == 1) 0 else (i * interval).toInt()
            var shoppingDay = planStartDate.plusDays(dayOffset.toLong())
            // Ensure the last shopping day is not after the planEndDate
            if (i == effectiveFrequency -1) { // Last trip
                shoppingDay = planStartDate.plusDays(planDurationDays -1L) // Make it the last day of the plan
            }

            if (days.isNotEmpty() && shoppingDay.isBefore(days.last().plusDays(1))) {
                // Avoid adding a day too close or before the previous one if interval is small or 0
                // This can happen if duration is very short relative to frequency
                // For this simple model, we might just accept it or adjust interval logic
            }


            if (!shoppingDay.isAfter(planStartDate.plusDays(planDurationDays - 1L))) {
                days.add(shoppingDay)
            } else if (i == 0) { // If first calculated day is already after end (e.g. duration 1, freq 2)
                days.add(planStartDate.plusDays(planDurationDays-1L)) // Add the last day of plan
            }
        }
        if (days.isEmpty() && planDurationDays > 0) { // Failsafe
            days.add(planStartDate)
        }
        return days.distinct().sorted().take(3) // Final safety check to limit to 3 days
    }

    private fun assignToShoppingDay(mealDate: LocalDate, shoppingDays: List<LocalDate>): Int {
        // If there are no shopping days, default to index 0
        if (shoppingDays.isEmpty()) return 0
        
        // Find the shopping day that's closest to but not after the meal date
        // This ensures ingredients are bought on the shopping trip immediately before they're needed
        var closestBeforeIndex = -1
        var minDaysAfter = Int.MAX_VALUE
        
        // First try to find a shopping day before or on the meal date
        for (i in shoppingDays.indices) {
            val shoppingDay = shoppingDays[i]
            
            if (!shoppingDay.isAfter(mealDate)) {
                // This shopping day is before or on the meal date
                closestBeforeIndex = i
            }
        }
        
        // If we found a shopping day before the meal, use it
        if (closestBeforeIndex >= 0) {
            return closestBeforeIndex
        }
        
        // If all shopping days are after the meal date, find the closest one
        for (i in shoppingDays.indices) {
            val shoppingDay = shoppingDays[i]
            val daysAfter = java.time.temporal.ChronoUnit.DAYS.between(mealDate, shoppingDay).toInt()
            
            if (daysAfter < minDaysAfter) {
                minDaysAfter = daysAfter
                closestBeforeIndex = i
            }
        }
        
        // Default to the first shopping day if all else fails
        return closestBeforeIndex.coerceAtLeast(0)
    }


    private fun saveCompositeShoppingList(list: CompositeShoppingList) {
        viewModelScope.launch {
            try {
                val currentLists = OptimizedFileManager.loadCompositeShoppingLists(context).toMutableList()
                // Remove any existing list with the same ID
                currentLists.removeAll { it.id == list.id }
                // Add the new list
                currentLists.add(list)
                // Save all lists
                OptimizedFileManager.saveCompositeShoppingLists(context, currentLists)
                
                // Ensure UI state is updated with the exact same list we saved
                _uiState.update { it.copy(compositeShoppingList = list) }
            } catch (e: Exception) {
                handleError(e, "Error saving shopping list")
            }
        }
    }

    fun toggleItemPurchasedStatus(item: EnhancedShoppingListItem, purchased: Boolean) {
        _uiState.value.compositeShoppingList?.let { currentList ->
            try {
                // Create a new list with the updated item
                val updatedItems = currentList.items.map {
                    if (it.name == item.name && it.unit == item.unit && it.shoppingDayIndex == item.shoppingDayIndex) {
                        it.copy(isPurchased = purchased)
                    } else it
                }
                
                // Create a new list object to trigger UI updates
                val newList = currentList.copy(items = updatedItems)
                
                // First update UI state for immediate feedback
                _uiState.update { it.copy(compositeShoppingList = newList) }
                
                // Then save to storage
                saveCompositeShoppingList(newList)
            } catch (e: Exception) {
                handleError(e, "Error toggling item status")
            }
        }
    }

    fun moveItemToInventory(itemToMove: EnhancedShoppingListItem) {
        _uiState.value.compositeShoppingList?.let { currentList ->
            val updatedListItems = currentList.items.filterNot {
                it.name.equals(itemToMove.name, ignoreCase = true) &&
                        it.unit.equals(itemToMove.unit, ignoreCase = true) &&
                        it.shoppingDayIndex == itemToMove.shoppingDayIndex
            }
            val existingInHouse = currentList.inHouseItems.find {
                it.name.equals(itemToMove.name, ignoreCase = true) &&
                        it.unit.equals(itemToMove.unit, ignoreCase = true)
            }
            val updatedInHouseItems = currentList.inHouseItems.toMutableList()

            if (existingInHouse != null) {
                updatedInHouseItems.remove(existingInHouse)
                updatedInHouseItems.add(existingInHouse.copy(quantity = existingInHouse.quantity + itemToMove.quantity))
            } else {
                updatedInHouseItems.add(
                    ShoppingListItem(
                        ingredientId = itemToMove.ingredientId,
                        name = itemToMove.name,
                        quantity = itemToMove.quantity,
                        unit = itemToMove.unit,
                        isInInventory = true,
                        isPurchased = true
                    )
                )
            }
            val newList = currentList.copy(items = updatedListItems, inHouseItems = updatedInHouseItems.sortedBy { it.name })
            _uiState.update { it.copy(compositeShoppingList = newList) }
            saveCompositeShoppingList(newList)
        }
    }

    fun moveItemFromInventoryToNeeded(itemToMoveBack: ShoppingListItem) {
        _uiState.value.compositeShoppingList?.let { currentList ->
            val updatedInHouseItems = currentList.inHouseItems.filterNot {
                it.name.equals(itemToMoveBack.name, ignoreCase = true) &&
                        it.unit.equals(itemToMoveBack.unit, ignoreCase = true)
            }
            val updatedListItems = currentList.items.toMutableList()

            val existingNeeded = updatedListItems.find {
                it.name.equals(itemToMoveBack.name, ignoreCase = true) &&
                        it.unit.equals(itemToMoveBack.unit, ignoreCase = true) &&
                        it.shoppingDayIndex == 0 // Assuming it goes to the first shopping day by default
            }
            if(existingNeeded != null) {
                updatedListItems.remove(existingNeeded)
                updatedListItems.add(existingNeeded.copy(quantity = existingNeeded.quantity + itemToMoveBack.quantity, isPurchased = false))
            } else {
                updatedListItems.add(
                    EnhancedShoppingListItem(
                        ingredientId = itemToMoveBack.ingredientId,
                        name = itemToMoveBack.name,
                        quantity = itemToMoveBack.quantity,
                        unit = itemToMoveBack.unit,
                        isPurchased = false,
                        shoppingDayIndex = 0
                    )
                )
            }

            val newList = currentList.copy(items = updatedListItems.sortedWith(compareBy({ it.shoppingDayIndex }, { it.name })), inHouseItems = updatedInHouseItems)
            _uiState.update { it.copy(compositeShoppingList = newList) }
            saveCompositeShoppingList(newList)
        }
    }


    fun prepareToAddCustomItem() {
        _uiState.update { it.copy(customItemToEdit = null) }
    }

    fun prepareToEditCustomItem(item: EnhancedShoppingListItem) {
        _uiState.update { it.copy(customItemToEdit = item) }
    }

    // --- REVISED addOrUpdateCustomItem ---
    fun addOrUpdateCustomItem(name: String, quantity: Double, unit: String, isPurchased: Boolean, isInInventory: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
        val currentList = _uiState.value.compositeShoppingList ?: CompositeShoppingList(
            id = UUID.randomUUID().toString(), // Create new list if none exists
            shoppingPlanId = currentShoppingPlan?.id ?: "default_plan", // Use active plan or a default
            items = emptyList(),
            inHouseItems = emptyList()
        )

        var tempItems = currentList.items.toMutableList()
        var tempInHouse = currentList.inHouseItems.toMutableList()

        val itemBeingEdited = _uiState.value.customItemToEdit

        // If editing, remove the old instance first from wherever it was
        if (itemBeingEdited != null) {
            tempItems.removeAll {
                it.name.equals(itemBeingEdited.name, ignoreCase = true) &&
                        it.unit.equals(itemBeingEdited.unit, ignoreCase = true) &&
                        it.shoppingDayIndex == itemBeingEdited.shoppingDayIndex
            }
            tempInHouse.removeAll {
                it.name.equals(itemBeingEdited.name, ignoreCase = true) &&
                        it.unit.equals(itemBeingEdited.unit, ignoreCase = true)
            }
        }

        if (isInInventory) { // Item should be in "In the House"
            val existingInHouse = tempInHouse.find { it.name.equals(name, ignoreCase = true) && it.unit.equals(unit, ignoreCase = true) }
            if (existingInHouse != null) { // Update quantity if already in house
                tempInHouse.remove(existingInHouse)
                tempInHouse.add(existingInHouse.copy(quantity = existingInHouse.quantity + quantity))
            } else { // Add new to in house
                tempInHouse.add(
                    ShoppingListItem(
                        ingredientId = itemBeingEdited?.ingredientId, // Preserve ID if editing
                        name = name,
                        quantity = quantity,
                        unit = unit,
                        isInInventory = true,
                        isPurchased = true // If in house, it's considered purchased
                    )
                )
            }
        } else { // Item should be in "Needed" list (items)
            val shoppingDayIndexForItem = itemBeingEdited?.shoppingDayIndex ?: 0 // Default to first day or keep original
            val existingNeeded = tempItems.find {
                it.name.equals(name, ignoreCase = true) &&
                        it.unit.equals(unit, ignoreCase = true) &&
                        it.shoppingDayIndex == shoppingDayIndexForItem
            }
            if (existingNeeded != null) { // Update quantity if already in needed list for that day
                tempItems.remove(existingNeeded)
                tempItems.add(existingNeeded.copy(quantity = existingNeeded.quantity + quantity, isPurchased = isPurchased))
            } else { // Add new to needed list
                tempItems.add(
                    EnhancedShoppingListItem(
                        ingredientId = itemBeingEdited?.ingredientId, // Preserve ID if editing
                        name = name,
                        quantity = quantity,
                        unit = unit,
                        isPurchased = isPurchased, // User specified if it's already purchased but still needed (rare)
                        isInInventory = false,     // Explicitly not in inventory if it's needed
                        shoppingDayIndex = shoppingDayIndexForItem
                    )
                )
            }
        }

        val newList = currentList.copy(
            items = tempItems.sortedWith(compareBy({ it.shoppingDayIndex }, { it.name })),
            inHouseItems = tempInHouse.sortedBy { it.name }
        )

                // Save the updated list first
        saveCompositeShoppingList(newList)
                
                // Then update the UI state
                _uiState.update { it.copy(
                    compositeShoppingList = newList,
                    customItemToEdit = null,
                    isLoading = false,
                    snackbarMessage = if (itemBeingEdited != null) "Item Updated" else "Item Added"
                ) }
            } catch (e: Exception) {
                handleError(e, "Error adding/updating custom item")
            }
        }
    }

    fun markShoppingDayComplete() {
        _uiState.value.compositeShoppingList?.let { currentList ->
            _uiState.update { it.copy(isLoading = true) }
            val firstShoppingDayIndexWithUnpurchased = currentList.items
                .filterNot { it.isPurchased }
                .minOfOrNull { it.shoppingDayIndex }

            if (firstShoppingDayIndexWithUnpurchased != null) {
                val itemsToMoveToInventory = mutableListOf<ShoppingListItem>()
                val remainingNeededItems = currentList.items.toMutableList()
                val itemsForCompletedDay = remainingNeededItems.filter { it.shoppingDayIndex == firstShoppingDayIndexWithUnpurchased && !it.isPurchased }

                itemsForCompletedDay.forEach { itemToComplete ->
                    itemsToMoveToInventory.add(
                        ShoppingListItem(
                            ingredientId = itemToComplete.ingredientId,
                            name = itemToComplete.name,
                            quantity = itemToComplete.quantity,
                            unit = itemToComplete.unit,
                            isInInventory = true,
                            isPurchased = true
                        )
                    )
                }
                // Remove completed items from the 'needed' list for that day
                remainingNeededItems.removeAll { it.shoppingDayIndex == firstShoppingDayIndexWithUnpurchased && !it.isPurchased }


                // Merge with existing in-house items
                val currentInHouse = currentList.inHouseItems.toMutableList()
                itemsToMoveToInventory.forEach { newItem ->
                    val existing = currentInHouse.find { it.name.equals(newItem.name, true) && it.unit.equals(newItem.unit, true) }
                    if (existing != null) {
                        currentInHouse.remove(existing)
                        currentInHouse.add(existing.copy(quantity = existing.quantity + newItem.quantity))
                    } else {
                        currentInHouse.add(newItem)
                    }
                }

                val newList = currentList.copy(items = remainingNeededItems, inHouseItems = currentInHouse.sortedBy { it.name })
                _uiState.update { it.copy(compositeShoppingList = newList, isLoading = false, snackbarMessage = "Shopping Day ${firstShoppingDayIndexWithUnpurchased + 1} items moved to inventory.") }
                saveCompositeShoppingList(newList)

                // Optional: Update ShoppingPlan meta-data (e.g., last shopped date)
                currentShoppingPlan?.let { plan ->
                    val completedShoppingDayDate = plan.shoppingDays.getOrNull(firstShoppingDayIndexWithUnpurchased)
                    if (completedShoppingDayDate != null) {
                        // Logic to update plan's last shopped date or similar meta-data
                        // mealPlannerRepository.updateShoppingPlan(plan.copy(...))
                    }
                }

            } else {
                _uiState.update { it.copy(isLoading = false, snackbarMessage = "All items already marked as purchased or moved.") }
            }
        }
    }

    private fun handleError(exception: Exception, message: String) {
        Log.e("ShoppingListVM", "$message: ${exception.message}", exception)
        _uiState.update { it.copy(
            isLoading = false,
            errorMessage = "$message: ${exception.localizedMessage ?: "Unknown error"}"
        )}

        // Auto-clear error after a delay
        viewModelScope.launch {
            delay(5000)
            _uiState.update { it.copy(errorMessage = null) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun clearShoppingList() {
        viewModelScope.launch {
            try {
                // Store the current list for potential restoration
                lastDeletedList = _uiState.value.compositeShoppingList
                
                // Get the current shopping list ID to delete
                val listToDeleteId = _uiState.value.compositeShoppingList?.id
                
                // Update UI state immediately for responsiveness
                _uiState.update { currentState ->
                    currentState.copy(
                        compositeShoppingList = null,
                        activeShoppingPlan = null,
                        snackbarMessage = "Shopping list deleted"
                    )
                }
                
                // Actually delete from storage
                if (listToDeleteId != null) {
                    // Load all lists
                    val currentLists = OptimizedFileManager.loadCompositeShoppingLists(context).toMutableList()
                    
                    // Remove the list with matching ID
                    val initialSize = currentLists.size
                    currentLists.removeAll { it.id == listToDeleteId }
                    val removedCount = initialSize - currentLists.size
                    
                    // Save the updated list collection (without the deleted list)
                    OptimizedFileManager.saveCompositeShoppingLists(context, currentLists)
                    
                    Log.d("ShoppingListVM", "Deleted shopping list $listToDeleteId from storage. Removed $removedCount lists.")
                    
                    // Clear the memory cache to ensure fresh data on next load
                    OptimizedFileManager.invalidateCache("shopping_lists")
                } else {
                    Log.d("ShoppingListVM", "No shopping list to delete")
                }
            } catch (e: Exception) {
                Log.e("ShoppingListVM", "Error deleting shopping list", e)
                handleError(e, "Error deleting shopping list")
            }
        }
    }

    fun restoreShoppingList() {
        viewModelScope.launch {
            try {
                lastDeletedList?.let { list ->
                    // Update UI state immediately for responsiveness
                    _uiState.update { currentState ->
                        currentState.copy(
                            compositeShoppingList = list,
                            snackbarMessage = "Shopping list restored"
                        )
                    }
                    
                    // Restore the shopping plan if needed
                    val shoppingPlanId = list.shoppingPlanId
                    if (shoppingPlanId.isNotEmpty()) {
                        val plans = OptimizedFileManager.loadShoppingPlans(context)
                        val plan = plans.find { it.id == shoppingPlanId }
                        if (plan != null) {
                            _uiState.update { it.copy(activeShoppingPlan = plan) }
                            currentShoppingPlan = plan
                            Log.d("ShoppingListVM", "Restored associated shopping plan: $shoppingPlanId")
                        }
                    }
                    
                    // Actually restore to storage
                    val currentLists = OptimizedFileManager.loadCompositeShoppingLists(context).toMutableList()
                    
                    // Remove any existing list with the same ID
                    currentLists.removeAll { it.id == list.id }
                    
                    // Add the restored list
                    currentLists.add(list)
                    
                    // Save to storage
                    OptimizedFileManager.saveCompositeShoppingLists(context, currentLists)
                    
                    Log.d("ShoppingListVM", "Restored shopping list ${list.id} with ${list.items.size} items to storage")
                    
                    // Clear the memory cache to ensure fresh data on next load
                    OptimizedFileManager.invalidateCache("shopping_lists")
                    
                    // Clear the deleted list reference
                    lastDeletedList = null
                }
            } catch (e: Exception) {
                Log.e("ShoppingListVM", "Error restoring shopping list", e)
                handleError(e, "Error restoring shopping list")
            }
        }
    }

    fun refreshShoppingList() {
        loadLatestShoppingList()
    }

    // Add ingredients from a single meal to the existing shopping list
    fun addSingleMealToShoppingList(plannedMeal: PlannedMeal) {
        viewModelScope.launch {
            try {
                Log.d("ShoppingListVM", "Starting to add meal ${plannedMeal.meal.name} (${plannedMeal.id}) to shopping list")
                
                // Get the current shopping list
                val currentList = _uiState.value.compositeShoppingList
                if (currentList == null) {
                    Log.e("ShoppingListVM", "No active shopping list found")
                    return@launch
                }
                
                val activeShoppingPlan = _uiState.value.activeShoppingPlan
                if (activeShoppingPlan == null) {
                    Log.e("ShoppingListVM", "No active shopping plan found")
                    return@launch
                }
                
                // Parse shopping days from the active plan
                val shoppingDays = activeShoppingPlan.shoppingDays.mapNotNull { DateUtils.parseLocalDate(it) }
                if (shoppingDays.isEmpty()) {
                    Log.e("ShoppingListVM", "No valid shopping days in active plan")
                    return@launch
                }
                
                // Parse the meal date
                val mealDate = DateUtils.parseLocalDate(plannedMeal.date)
                if (mealDate == null) {
                    Log.e("ShoppingListVM", "Invalid meal date: ${plannedMeal.date}")
                    return@launch
                }
                
                // Check if the meal date is within the shopping plan date range
                val planStartDate = DateUtils.parseLocalDate(activeShoppingPlan.planStartDate)
                if (planStartDate == null) {
                    Log.e("ShoppingListVM", "Invalid plan start date: ${activeShoppingPlan.planStartDate}")
                    return@launch
                }
                
                val planEndDate = DateUtils.parseLocalDate(activeShoppingPlan.planEndDate)
                if (planEndDate == null) {
                    Log.e("ShoppingListVM", "Invalid plan end date: ${activeShoppingPlan.planEndDate}")
                    return@launch
                }
                
                if (mealDate.isBefore(planStartDate) || mealDate.isAfter(planEndDate)) {
                    // Meal is outside the current shopping plan date range
                    Log.d("ShoppingListVM", "Meal date $mealDate is outside shopping plan range $planStartDate to $planEndDate")
                    return@launch
                }
                
                // Determine which shopping day this meal belongs to
                val shoppingDayIndex = assignToShoppingDay(mealDate, shoppingDays)
                Log.d("ShoppingListVM", "Meal assigned to shopping day index: $shoppingDayIndex")
                
                // Process ingredients from the meal using the IngredientParser
                val newItems = mutableListOf<EnhancedShoppingListItem>()
                
                plannedMeal.meal.ingredients.forEach { ingredient ->
                    if ((ingredient.quantity ?: 0.0) > 0) {
                        // Process ingredient with the parser
                        val shoppingItem = IngredientParser.processIngredientForShoppingList(
                            ingredient = ingredient,
                            shoppingDayIndex = shoppingDayIndex
                        )
                        newItems.add(shoppingItem)
                    }
                }
                
                Log.d("ShoppingListVM", "Processed ${newItems.size} ingredients from meal")
                
                // Create a temporary list with both current and new items for consolidation
                val tempItems = mutableListOf<EnhancedShoppingListItem>()
                tempItems.addAll(currentList.items)
                Log.d("ShoppingListVM", "Current shopping list has ${currentList.items.size} items")
                tempItems.addAll(newItems)
                
                // Consolidate similar ingredients
                val consolidatedItems = IngredientParser.consolidateShoppingList(tempItems)
                Log.d("ShoppingListVM", "Consolidated list now has ${consolidatedItems.size} items")
                
                // Create updated shopping list
                val updatedList = currentList.copy(items = consolidatedItems)
                
                // Update UI state first for immediate feedback
                _uiState.update { it.copy(
                    compositeShoppingList = updatedList, 
                    snackbarMessage = "Shopping list updated with new meal ingredients!"
                )}
                
                // Then save to storage
                saveCompositeShoppingList(updatedList)
                
                Log.d("ShoppingListVM", "Successfully added ${newItems.size} new ingredients to shopping list")
            } catch (e: Exception) {
                Log.e("ShoppingListVM", "Error adding meal to shopping list", e)
                handleError(e, "Error updating shopping list with new meal")
            }
        }
    }

    /**
     * Gets a repository implementation to fetch meals in date range
     */
    private suspend fun MealPlannerRepository.getMealsInDateRange(startDate: String, endDate: String): List<PlannedMeal> {
        val allPlannedMealsData = getPlannedMeals()
        return allPlannedMealsData.plannedMeals.filter {
            val mealDate = DateUtils.parseLocalDate(it.date)
            val start = DateUtils.parseLocalDate(startDate)
            val end = DateUtils.parseLocalDate(endDate)
            mealDate != null && start != null && end != null && 
            !mealDate.isBefore(start) && !mealDate.isAfter(end)
        }
    }
    
    /**
     * Format a shopping list item quantity for display
     */
    fun formatItemQuantity(item: EnhancedShoppingListItem): String {
        // Use the IngredientParser to format the quantity
        val formattedQuantity = IngredientParser.formatQuantity(item.quantity)
        
        // Check if we can suggest a better unit
        val betterUnit = IngredientParser.suggestBetterUnit(item.quantity, item.unit)
        
        return if (betterUnit != null) {
            // Format with the better unit
            "${IngredientParser.formatQuantity(betterUnit.first)} ${betterUnit.second}"
        } else {
            // Format with the original unit
            "$formattedQuantity ${item.unit}"
        }
    }

    /**
     * Shows the reminder dialog for a specific shopping day
     */
    fun showReminderDialog(shoppingDayIndex: Int) {
        // Get existing reminder notes if available
        val reminderData = reminderManager.getReminderMetadata(shoppingDayIndex)
        val notes = reminderData?.notes ?: ""
        
        _uiState.update { it.copy(
            showReminderDialog = true,
            reminderDialogDayIndex = shoppingDayIndex,
            reminderDialogNotes = notes
        ) }
    }
    
    /**
     * Dismisses the reminder dialog
     */
    fun dismissReminderDialog() {
        _uiState.update { it.copy(
            showReminderDialog = false
        ) }
    }
    
    /**
     * Sets a reminder for a shopping day
     */
    fun setReminder(shoppingDayIndex: Int, reminderTimeIndex: Int, notes: String) {
        viewModelScope.launch {
            try {
                val shoppingPlan = _uiState.value.activeShoppingPlan ?: return@launch
                
                // Get the shopping date for this day index
                val shoppingDateStr = shoppingPlan.shoppingDays.getOrNull(shoppingDayIndex) ?: return@launch
                val shoppingDate = DateUtils.parseLocalDate(shoppingDateStr) ?: return@launch
                
                // Check for permission first
                if (!reminderManager.canScheduleExactAlarms()) {
                    _uiState.update { it.copy(
                        needsExactAlarmPermission = true,
                        snackbarMessage = "Permission needed to set exact alarms",
                        showReminderDialog = false
                    ) }
                    return@launch
                }
                
                // Cancel any existing reminder
                if (reminderManager.hasReminder(shoppingDayIndex)) {
                    reminderManager.cancelReminder(shoppingDayIndex)
                }
                
                // Schedule the new reminder
                val reminderId = reminderManager.scheduleReminder(
                    shoppingDate = shoppingDate,
                    shoppingDayIndex = shoppingDayIndex,
                    reminderTimeIndex = reminderTimeIndex,
                    notes = notes
                )
                
                if (reminderId == null) {
                    _uiState.update { it.copy(
                        snackbarMessage = "Failed to set reminder. Check app permissions.",
                        showReminderDialog = false
                    ) }
                    return@launch
                }
                
                // Show success message
                _uiState.update { it.copy(
                    snackbarMessage = "Reminder set for ${DateUtils.formatShortDate(shoppingDateStr)}",
                    showReminderDialog = false
                ) }
                
                Log.d("ShoppingListVM", "Set reminder $reminderId for day $shoppingDayIndex")
            } catch (e: Exception) {
                handleError(e, "Failed to set reminder")
            }
        }
    }
    
    /**
     * Sets a reminder with custom time for a shopping day
     */
    fun setReminderWithCustomTime(shoppingDayIndex: Int, reminderTimeIndex: Int, notes: String, customTime: LocalTime) {
        viewModelScope.launch {
            try {
                val shoppingPlan = _uiState.value.activeShoppingPlan ?: return@launch
                
                // Get the shopping date for this day index
                val shoppingDateStr = shoppingPlan.shoppingDays.getOrNull(shoppingDayIndex) ?: return@launch
                val shoppingDate = DateUtils.parseLocalDate(shoppingDateStr) ?: return@launch
                
                // Check for permission first
                if (!reminderManager.canScheduleExactAlarms()) {
                    _uiState.update { it.copy(
                        needsExactAlarmPermission = true,
                        snackbarMessage = "Permission needed to set exact alarms",
                        showReminderDialog = false
                    ) }
                    return@launch
                }
                
                // Cancel any existing reminder
                if (reminderManager.hasReminder(shoppingDayIndex)) {
                    reminderManager.cancelReminder(shoppingDayIndex)
                }
                
                // Schedule the new reminder with custom time
                val reminderId = reminderManager.scheduleReminder(
                    shoppingDate = shoppingDate,
                    shoppingDayIndex = shoppingDayIndex,
                    reminderTimeIndex = reminderTimeIndex,
                    notes = notes,
                    customTime = customTime
                )
                
                if (reminderId == null) {
                    _uiState.update { it.copy(
                        snackbarMessage = "Failed to set reminder. Check app permissions.",
                        showReminderDialog = false
                    ) }
                    return@launch
                }
                
                // Format time for message
                val timeFormat = customTime.format(DateTimeFormatter.ofPattern("h:mm a"))
                
                // Show success message
                _uiState.update { it.copy(
                    snackbarMessage = "Reminder set for ${DateUtils.formatShortDate(shoppingDateStr)} at $timeFormat",
                    showReminderDialog = false
                ) }
                
                Log.d("ShoppingListVM", "Set custom time reminder $reminderId for day $shoppingDayIndex at $customTime")
            } catch (e: Exception) {
                handleError(e, "Failed to set reminder")
            }
        }
    }
    
    /**
     * Removes a reminder for a shopping day
     */
    fun removeReminder(shoppingDayIndex: Int) {
        viewModelScope.launch {
            try {
                if (reminderManager.hasReminder(shoppingDayIndex)) {
                    reminderManager.cancelReminder(shoppingDayIndex)
                    
                    // Show success message
                    _uiState.update { it.copy(
                        snackbarMessage = "Reminder removed",
                        showReminderDialog = false
                    ) }
                    
                    Log.d("ShoppingListVM", "Removed reminder for day $shoppingDayIndex")
                }
            } catch (e: Exception) {
                handleError(e, "Failed to remove reminder")
            }
        }
    }
    
    /**
     * Checks if a reminder exists for a shopping day
     */
    fun hasReminder(shoppingDayIndex: Int): Boolean {
        return reminderManager.hasReminder(shoppingDayIndex)
    }
    
    /**
     * Shows a snackbar message
     */
    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }
    
    /**
     * Clears the exact alarm permission request flag
     */
    fun clearExactAlarmPermissionRequest() {
        _uiState.update { it.copy(needsExactAlarmPermission = false) }
    }
    
    /**
     * Gets reminder metadata for a shopping day
     */
    fun getReminderMetadata(shoppingDayIndex: Int): ShoppingReminderManager.ReminderMetadata? {
        return reminderManager.getReminderMetadata(shoppingDayIndex)
    }
}