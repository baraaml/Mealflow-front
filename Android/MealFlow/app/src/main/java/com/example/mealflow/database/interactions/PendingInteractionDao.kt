package com.example.mealflow.database.interactions

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingInteractionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pendingInteraction: PendingInteraction)

    @Query("SELECT * FROM pending_interactions ORDER BY timestamp ASC")
    suspend fun getAll(): List<PendingInteraction>

    @Query("DELETE FROM pending_interactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pending_interactions")
    suspend fun clearAll()
} 