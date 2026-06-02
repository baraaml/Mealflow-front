// File: app/src/main/java/com/example/mealflow/data/model/EnhancedShoppingListItem.kt
package com.example.mealflow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EnhancedShoppingListItem(
    val ingredientId: String?,
    val name: String,
    val quantity: Double,
    val unit: String,
    var isPurchased: Boolean = false,
    var isInInventory: Boolean = false, // isInInventory seems more relevant here than in ShoppingListItem for initial check
    val shoppingDayIndex: Int, // Which shopping day this belongs to (0, 1, or 2)
    val mealDate: String? = null // Date when this ingredient is needed for a meal (ISO format)
)