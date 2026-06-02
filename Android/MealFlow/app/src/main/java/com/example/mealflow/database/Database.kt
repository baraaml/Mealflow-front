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

//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import android.content.Context
//import androidx.room.TypeConverters
//import com.example.mealflow.database.converters.CommunityMemberTypeConverter
//import com.example.mealflow.database.converters.CommunityPrivacyConverter
//import com.example.mealflow.database.converters.CommunityTypeConverter
//import com.example.mealflow.database.converters.MediaTypeConverter
//import com.example.mealflow.database.converters.PostTypeConverter
//import com.example.mealflow.database.converters.RecipeCreationPermissionConverter
//import com.example.mealflow.database.converters.RoleConverter
//import com.example.mealflow.database.dao.CommunityDao
//import com.example.mealflow.database.dao.CommunityMemberDao
//import com.example.mealflow.database.dao.PostDao
//import com.example.mealflow.database.dao.UserDao
//import com.example.mealflow.database.entity.Community
//import com.example.mealflow.database.entity.CommunityMember
//import com.example.mealflow.database.entity.Post
//import com.example.mealflow.database.entity.UserEntity
//
//@Database(
//    entities = [
//        UserEntity::class,
//        Community::class,
//        CommunityMember::class,
//        Post::class
//    ],
//    version = 1,
//    exportSchema = false
//)
//@TypeConverters(
//    CommunityPrivacyConverter::class,
//    RecipeCreationPermissionConverter::class,
//    RoleConverter::class,
//    PostTypeConverter::class,
//    MediaTypeConverter::class
//)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun userDao(): UserDao
//    abstract fun communityDao(): CommunityDao
//    abstract fun communityMemberDao(): CommunityMemberDao
//    abstract fun postDao(): PostDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "mealflow_database"
//                ).fallbackToDestructiveMigration().build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}