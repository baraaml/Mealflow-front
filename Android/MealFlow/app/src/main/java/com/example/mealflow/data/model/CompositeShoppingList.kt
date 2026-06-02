// File: app/src/main/java/com/example/mealflow/data/model/CompositeShoppingList.kt
package com.example.mealflow.data.model

import kotlinx.serialization.Serializable

// Assuming ShoppingListItem is your existing model in com.example.mealflow.data.model.ShoppingListModels
// If EnhancedShoppingListItem is meant to replace ShoppingListItem entirely, adjust accordingly.
// The guide specifies `inHouseItems: List<ShoppingListItem>`, so using existing one.

@Serializable
data class CompositeShoppingList(
    val id: String,
    val shoppingPlanId: String,
    val items: List<EnhancedShoppingListItem>,
    val inHouseItems: List<ShoppingListItem> // Using existing ShoppingListItem as per guide
)