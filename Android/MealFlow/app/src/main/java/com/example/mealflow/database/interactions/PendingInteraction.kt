package com.example.mealflow.database.interactions

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pending_interactions")
data class PendingInteraction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val mealId: String,
    val interactionType: String,
    val reviewText: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) 