package com.example.mealflow.data.model // Match your package structure

import kotlinx.serialization.Serializable
// java.time.LocalDate is fine as your minSdk will handle it or you have desugaring

@Serializable
data class ShoppingSettings(
    val lastShoppingDate: String? = null, // ISO String (e.g., "2023-10-27")
    val nextShoppingDate: String? = null,
    val shoppingCoverageDays: Int = 7 // Default coverage
)

@Serializable
data class ShoppingListItem(
    val ingredientId: String?, // Changed from Int? to String?
    val name: String,
    val quantity: Double, // We will parse this from String
    val unit: String,
    var isInInventory: Boolean = false,
    var isPurchased: Boolean = false
)

@Serializable
data class GeneratedShoppingList(
    val id: String, // Unique ID for the list, e.g., timestamp or UUID
    val generationDate: String, // ISO String for the date the list was generated
    val items: List<ShoppingListItem>,
    val mealPlanStartDate: String, // ISO String: Start date of the meal plan period this list covers
    val mealPlanEndDate: String    // ISO String: End date of the meal plan period
)

// Helper for aggregation, not serialized, just used in logic
data class AggregatedIngredient(
    val ingredientId: String?, // Changed from Int? to String?
    val name: String,
    val unit: String,
    var totalQuantity: Double
)