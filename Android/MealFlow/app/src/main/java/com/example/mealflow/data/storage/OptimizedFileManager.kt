// File: app/src/main/java/com/example/mealflow/data/storage/OptimizedFileManager.kt
package com.example.mealflow.data.storage

import android.content.Context
import android.util.Log
import com.example.mealflow.data.model.CompositeShoppingList
import com.example.mealflow.data.model.PlannedMealsData
import com.example.mealflow.data.model.ShoppingPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.*
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.system.measureTimeMillis

object OptimizedFileManager {
    private const val TAG = "OptimizedFileManager"
    
    // File names with compression extension
    private const val PLANNED_MEALS_FILE = "planned_meals.json.gz"
    private const val PLANNED_MEALS_BACKUP_FILE = "planned_meals_backup.json.gz"
    private const val SHOPPING_LISTS_FILE = "shopping_lists.json.gz"
    private const val SHOPPING_LISTS_BACKUP_FILE = "shopping_lists_backup.json.gz"
    private const val SHOPPING_PLANS_FILE = "shopping_plans.json.gz"
    private const val SHOPPING_PLANS_BACKUP_FILE = "shopping_plans_backup.json.gz"
    
    // Compression threshold (1KB)
    const val COMPRESSION_THRESHOLD = 1024
    
    // Cache for frequently accessed data
    private val memoryCache = mutableMapOf<String, CachedData>()
    private const val CACHE_TIMEOUT = 5 * 60 * 1000L // 5 minutes
    
    private val json = Json {
        prettyPrint = false // Disable for better compression
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Performance monitoring
    private val performanceStats = mutableMapOf<String, PerformanceStats>()

    // PlannedMealsData Functions
    suspend fun savePlannedMeals(context: Context, data: PlannedMealsData) {
        val cacheKey = "planned_meals"
        val jsonString = json.encodeToString(PlannedMealsData.serializer(), data)
        
        // Update cache
        memoryCache[cacheKey] = CachedData(data, System.currentTimeMillis())
        
        // Save to file with backup
        saveWithBackup(
            context = context,
            mainFile = PLANNED_MEALS_FILE,
            backupFile = PLANNED_MEALS_BACKUP_FILE,
            jsonString = jsonString,
            operationName = "save_planned_meals"
        )
    }

    suspend fun loadPlannedMeals(context: Context): PlannedMealsData {
        val cacheKey = "planned_meals"
        
        // Check cache first
        memoryCache[cacheKey]?.let { cached ->
            if (System.currentTimeMillis() - cached.timestamp < CACHE_TIMEOUT) {
                Log.d(TAG, "Cache hit for planned meals")
                return cached.data as PlannedMealsData
            } else {
                memoryCache.remove(cacheKey)
            }
        }
        
        // Load from file
        val data = loadWithFallback(
            context = context,
            mainFile = PLANNED_MEALS_FILE,
            backupFile = PLANNED_MEALS_BACKUP_FILE,
            defaultValue = PlannedMealsData(),
            deserializer = { json.decodeFromString<PlannedMealsData>(it) },
            operationName = "load_planned_meals"
        )
        
        // Cache the result
        memoryCache[cacheKey] = CachedData(data, System.currentTimeMillis())
        return data
    }

    // ShoppingPlan Functions
    suspend fun saveShoppingPlans(context: Context, data: List<ShoppingPlan>) {
        val cacheKey = "shopping_plans"
        val jsonString = json.encodeToString(kotlinx.serialization.serializer<List<ShoppingPlan>>(), data)
        
        memoryCache[cacheKey] = CachedData(data, System.currentTimeMillis())
        
        saveWithBackup(
            context = context,
            mainFile = SHOPPING_PLANS_FILE,
            backupFile = SHOPPING_PLANS_BACKUP_FILE,
            jsonString = jsonString,
            operationName = "save_shopping_plans"
        )
    }

    suspend fun loadShoppingPlans(context: Context): List<ShoppingPlan> {
        val cacheKey = "shopping_plans"
        
        memoryCache[cacheKey]?.let { cached ->
            if (System.currentTimeMillis() - cached.timestamp < CACHE_TIMEOUT) {
                Log.d(TAG, "Cache hit for shopping plans")
                return cached.data as List<ShoppingPlan>
            } else {
                memoryCache.remove(cacheKey)
            }
        }
        
        val data = loadWithFallback(
            context = context,
            mainFile = SHOPPING_PLANS_FILE,
            backupFile = SHOPPING_PLANS_BACKUP_FILE,
            defaultValue = emptyList<ShoppingPlan>(),
            deserializer = { json.decodeFromString<List<ShoppingPlan>>(it) },
            operationName = "load_shopping_plans"
        )
        
        memoryCache[cacheKey] = CachedData(data, System.currentTimeMillis())
        return data
    }

    // CompositeShoppingList Functions
    suspend fun saveCompositeShoppingLists(context: Context, data: List<CompositeShoppingList>) {
        val cacheKey = "shopping_lists"
        val jsonString = json.encodeToString(kotlinx.serialization.serializer<List<CompositeShoppingList>>(), data)
        
        memoryCache[cacheKey] = CachedData(data, System.currentTimeMillis())
        
        saveWithBackup(
            context = context,
            mainFile = SHOPPING_LISTS_FILE,
            backupFile = SHOPPING_LISTS_BACKUP_FILE,
            jsonString = jsonString,
            operationName = "save_shopping_lists"
        )
    }

    suspend fun loadCompositeShoppingLists(context: Context): List<CompositeShoppingList> {
        val cacheKey = "shopping_lists"
        
        memoryCache[cacheKey]?.let { cached ->
            if (System.currentTimeMillis() - cached.timestamp < CACHE_TIMEOUT) {
                Log.d(TAG, "Cache hit for shopping lists")
                return cached.data as List<CompositeShoppingList>
            } else {
                memoryCache.remove(cacheKey)
            }
        }
        
        val data = loadWithFallback(
            context = context,
            mainFile = SHOPPING_LISTS_FILE,
            backupFile = SHOPPING_LISTS_BACKUP_FILE,
            defaultValue = emptyList<CompositeShoppingList>(),
            deserializer = { json.decodeFromString<List<CompositeShoppingList>>(it) },
            operationName = "load_shopping_lists"
        )
        
        memoryCache[cacheKey] = CachedData(data, System.currentTimeMillis())
        return data
    }

    // Generic save with backup and compression
    private suspend fun saveWithBackup(
        context: Context,
        mainFile: String,
        backupFile: String,
        jsonString: String,
        operationName: String
    ) = withContext(Dispatchers.IO) {
        val executionTime = measureTimeMillis {
            try {
                // Create backup of existing file
                if (context.fileExists(mainFile)) {
                    context.copyFile(mainFile, backupFile)
                    Log.d(TAG, "Created backup: $backupFile")
                }
                
                // Write new file with compression
                context.writeCompressedFile(mainFile, jsonString)
                
                // Log file size information
                val fileSize = context.getFileSize(mainFile)
                val compressionRatio = if (jsonString.length > 0) {
                    ((jsonString.length - fileSize) * 100.0 / jsonString.length)
                } else 0.0
                
                Log.d(TAG, "Saved $mainFile - Original: ${jsonString.length} bytes, " +
                        "Compressed: $fileSize bytes, Compression: ${compressionRatio.toInt()}%")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error saving $mainFile", e)
                throw e
            }
        }
        
        updatePerformanceStats(operationName, executionTime, true)
    }

    // Generic load with fallback and decompression
    private suspend fun <T> loadWithFallback(
        context: Context,
        mainFile: String,
        backupFile: String,
        defaultValue: T,
        deserializer: (String) -> T,
        operationName: String
    ): T = withContext(Dispatchers.IO) {
        var result = defaultValue
        val executionTime = measureTimeMillis {
            result = try {
                // Try to load main file
                val jsonString = context.readCompressedFile(mainFile)
                if (jsonString.isNullOrEmpty()) {
                    Log.w(TAG, "$mainFile is empty, trying backup")
                    loadFromBackup(context, backupFile, deserializer, defaultValue)
                } else {
                    deserializer(jsonString)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading $mainFile", e)
                // Try backup file
                loadFromBackup(context, backupFile, deserializer, defaultValue)
            }
        }
        
        updatePerformanceStats(operationName, executionTime, false)
        result
    }

    private suspend fun <T> loadFromBackup(
        context: Context,
        backupFile: String,
        deserializer: (String) -> T,
        defaultValue: T
    ): T {
        return try {
            val backupJson = context.readCompressedFile(backupFile)
            if (backupJson.isNullOrEmpty()) {
                Log.w(TAG, "Backup file $backupFile is also empty, using default")
                defaultValue
            } else {
                Log.i(TAG, "Successfully loaded from backup: $backupFile")
                deserializer(backupJson)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading backup $backupFile", e)
            defaultValue
        }
    }

    // Cache management
    fun invalidateCache(key: String? = null) {
        if (key != null) {
            val removed = memoryCache.remove(key)
            if (removed != null) {
                Log.d(TAG, "Invalidated cache for $key")
            } else {
                Log.w(TAG, "Attempted to invalidate non-existent cache key: $key")
            }
        } else {
            val sizeBefore = memoryCache.size
            memoryCache.clear()
            Log.d(TAG, "Cleared all cache (was $sizeBefore items)")
        }
    }

    fun getCacheStats(): Map<String, Any> {
        val stats = mapOf(
            "cacheSize" to memoryCache.size,
            "cacheKeys" to memoryCache.keys.toList(),
            "oldestEntry" to (memoryCache.values.minByOrNull { it.timestamp }?.timestamp ?: 0),
            "newestEntry" to (memoryCache.values.maxByOrNull { it.timestamp }?.timestamp ?: 0)
        )
        
        // Log cache stats periodically
        if (memoryCache.size > 0) {
            Log.d(TAG, "Cache Stats: ${stats["cacheSize"]} items, keys: ${stats["cacheKeys"]}")
        }
        
        return stats
    }

    // Performance monitoring
    fun getPerformanceStats(): Map<String, PerformanceStats> {
        val stats = performanceStats.toMap()
        
        // Log performance summary
        if (stats.isNotEmpty()) {
            Log.i(TAG, "Performance Stats Summary:")
            stats.forEach { (operation, stat) ->
                Log.i(TAG, "  $operation: ${stat.totalCalls} calls, avg: ${stat.averageTimeMs}ms")
            }
        }
        
        return stats
    }

    private fun updatePerformanceStats(operation: String, timeMs: Long, isWrite: Boolean) {
        val current = performanceStats[operation] ?: PerformanceStats(operation, 0, 0, 0, 0)
        performanceStats[operation] = current.copy(
            totalCalls = current.totalCalls + 1,
            totalTimeMs = current.totalTimeMs + timeMs,
            averageTimeMs = (current.totalTimeMs + timeMs) / (current.totalCalls + 1),
            lastExecutionMs = timeMs
        )
        
        // Enhanced performance logging
        when {
            timeMs > 2000 -> Log.e(TAG, "Very slow ${if (isWrite) "write" else "read"} operation: $operation took ${timeMs}ms")
            timeMs > 1000 -> Log.w(TAG, "Slow ${if (isWrite) "write" else "read"} operation: $operation took ${timeMs}ms")
            timeMs > 500 -> Log.i(TAG, "Moderate ${if (isWrite) "write" else "read"} operation: $operation took ${timeMs}ms")
            else -> Log.d(TAG, "Fast ${if (isWrite) "write" else "read"} operation: $operation took ${timeMs}ms")
        }
        
        // Log average performance every 10 operations
        val newStats = performanceStats[operation]!!
        if (newStats.totalCalls % 10L == 0L) {
            Log.i(TAG, "Performance milestone for $operation: ${newStats.totalCalls} calls, avg: ${newStats.averageTimeMs}ms")
        }
    }

    // Data classes
    private data class CachedData(val data: Any, val timestamp: Long)
    
    data class PerformanceStats(
        val operationName: String,
        val totalCalls: Long,
        val totalTimeMs: Long,
        val averageTimeMs: Long,
        val lastExecutionMs: Long
    )
    
    // Debug utility functions
    fun logCacheAndPerformanceStats() {
        Log.i(TAG, "=== OptimizedFileManager Stats ===")
        getCacheStats()
        getPerformanceStats()
        Log.i(TAG, "=== End Stats ===")
    }
    
    fun clearAllStats() {
        performanceStats.clear()
        invalidateCache()
        Log.i(TAG, "Cleared all performance stats and cache")
    }
}

// Extension functions for Context
private suspend fun Context.writeCompressedFile(filename: String, content: String) {
    val bytes = content.toByteArray(Charsets.UTF_8)
    
    if (bytes.size > OptimizedFileManager.COMPRESSION_THRESHOLD) {
        // Use GZIP compression for larger files
        openFileOutput(filename, Context.MODE_PRIVATE).use { fileStream: FileOutputStream ->
            GZIPOutputStream(fileStream).use { gzipStream: GZIPOutputStream ->
                gzipStream.write(bytes)
            }
        }
    } else {
        // Write directly for small files
        openFileOutput(filename, Context.MODE_PRIVATE).use { stream: FileOutputStream ->
            stream.write(bytes)
        }
    }
}

private suspend fun Context.readCompressedFile(filename: String): String? {
    return try {
        openFileInput(filename).use { fileStream: FileInputStream ->
            // Try to read as GZIP first
            try {
                GZIPInputStream(fileStream).bufferedReader().use { reader: BufferedReader ->
                    reader.readText()
                }
            } catch (e: Exception) {
                // If GZIP fails, try reading as plain text
                fileStream.close()
                openFileInput(filename).bufferedReader().use { reader: BufferedReader ->
                    reader.readText()
                }
            }
        }
    } catch (e: FileNotFoundException) {
        Log.w("OptimizedFileManager", "$filename not found")
        null
    } catch (e: Exception) {
        Log.e("OptimizedFileManager", "Error reading $filename", e)
        null
    }
}

private fun Context.fileExists(filename: String): Boolean {
    return try {
        openFileInput(filename).use { _: FileInputStream -> true }
    } catch (e: FileNotFoundException) {
        false
    }
}

private suspend fun Context.copyFile(sourceFilename: String, destFilename: String) {
    try {
        openFileInput(sourceFilename).use { input: FileInputStream ->
            openFileOutput(destFilename, Context.MODE_PRIVATE).use { output: FileOutputStream ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        Log.e("OptimizedFileManager", "Error copying $sourceFilename to $destFilename", e)
    }
}

private fun Context.getFileSize(filename: String): Long {
    return try {
        File(filesDir, filename).length()
    } catch (e: Exception) {
        0L
    }
}