// File: app/src/main/java/com/example/mealflow/data/model/ShoppingPlan.kt
package com.example.mealflow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingPlan(
    val id: String,
    val planStartDate: String, // ISO format date
    val planEndDate: String,   // ISO format date
    val frequency: Int,        // Number of shopping trips (1-3)
    val shoppingDays: List<String> // List of ISO format dates for shopping
)