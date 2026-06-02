package com.example.mealflow.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecentSearch(search: RecentSearchEntity)

    @Query("SELECT query FROM recent_searches ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<String>>

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches()
}