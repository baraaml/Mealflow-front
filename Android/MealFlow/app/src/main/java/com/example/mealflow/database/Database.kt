package com.example.mealflow.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mealflow.database.community.CommunityDao
import com.example.mealflow.database.community.GetCommunityEntity
import com.example.mealflow.database.interactions.PendingInteraction
import com.example.mealflow.database.interactions.PendingInteractionDao
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [GetCommunityEntity::class, PendingInteraction::class], version = 3, exportSchema = true)

@TypeConverters(Converters::class)
abstract class CommunityDatabase : RoomDatabase() {

    abstract fun communityDao(): CommunityDao
    abstract fun pendingInteractionDao(): PendingInteractionDao
//    abstract fun tokenDao(): TokenDao

    companion object {
        @Volatile
        private var INSTANCE: CommunityDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pending_interactions ADD COLUMN reviewText TEXT")
            }
        }

        fun getDatabase(context: Context): CommunityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CommunityDatabase::class.java,
                    "community_database"
                )
                .addMigrations(MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}