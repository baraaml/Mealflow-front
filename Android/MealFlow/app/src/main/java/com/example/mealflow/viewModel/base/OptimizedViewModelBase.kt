package com.example.mealflow.viewModel.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

abstract class OptimizedViewModelBase : ViewModel() {

    protected val TAG = this::class.java.simpleName

    // Performance monitoring
    private val performanceMetrics = mutableMapOf<String, PerformanceMetric>()
    
    // Memory-efficient caching with LRU eviction
    private val cache = LRUCache<String, Any>(maxSize = 50)
    
    // Debounced operations tracking
    private val debouncedJobs = ConcurrentHashMap<String, Job>()
    
    // Weak references for cleanup
    private val weakReferences = mutableListOf<WeakReference<*>>()
    
    // Common loading states
    protected val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    protected val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    // Cache operations
    protected fun <T> putCache(key: String, value: T) {
        cache.put(key, value as Any)
    }
    
    @Suppress("UNCHECKED_CAST")
    protected fun <T> getCache(key: String): T? {
        return cache.get(key) as? T
    }
    
    protected fun invalidateCache(key: String) {
        cache.remove(key)
    }
    
    protected fun clearCache() {
        cache.clear()
    }
    
    // Performance monitoring
    protected suspend fun <T> measurePerformance(
        operationName: String,
        operation: suspend () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        val result = operation()
        val duration = System.currentTimeMillis() - startTime
        
        performanceMetrics[operationName] = PerformanceMetric(
            operationName = operationName,
            averageDuration = duration,
            lastExecuted = System.currentTimeMillis(),
            executionCount = (performanceMetrics[operationName]?.executionCount ?: 0) + 1
        )
        
        if (duration > 1000) { // Log slow operations
            Log.w(TAG, "Slow operation detected: $operationName took ${duration}ms")
        }
        
        return result
    }
    
    // Debounced execution to prevent rapid successive calls
    protected fun debounce(
        key: String,
        delayMs: Long = 300L,
        action: suspend () -> Unit
    ) {
        debouncedJobs[key]?.cancel()
        debouncedJobs[key] = viewModelScope.launch {
            delay(delayMs)
            action()
            debouncedJobs.remove(key)
        }
    }
    
    // Memory-efficient data loading with caching
    protected suspend fun <T> loadWithCache(
        cacheKey: String,
        forceRefresh: Boolean = false,
        loader: suspend () -> T
    ): T {
        if (!forceRefresh) {
            getCache<T>(cacheKey)?.let { cached ->
                Log.d(TAG, "Cache hit for $cacheKey")
                return cached
            }
        }
        
        Log.d(TAG, "Cache miss for $cacheKey, loading fresh data")
        return measurePerformance("load_$cacheKey") {
            val result = loader()
            putCache(cacheKey, result)
            result
        }
    }
    
    // Safe async execution with error handling
    protected fun safeAsync(
        showLoading: Boolean = true,
        onError: ((Exception) -> Unit)? = null,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (showLoading) _isLoading.value = true
                _errorState.value = null
                action()
            } catch (e: Exception) {
                Log.e(TAG, "Error in async operation", e)
                _errorState.value = e.message
                onError?.invoke(e)
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }
    
    // Background processing with context switching
    protected suspend fun <T> runInBackground(operation: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            operation()
        }
    }
    
    // Main thread operations
    protected suspend fun runOnMain(operation: suspend () -> Unit) {
        withContext(Dispatchers.Main) {
            operation()
        }
    }
    
    // Memory management
    protected fun <T> addWeakReference(obj: T): WeakReference<T> {
        val weakRef = WeakReference(obj)
        weakReferences.add(weakRef)
        return weakRef
    }
    
    protected fun cleanupWeakReferences() {
        weakReferences.removeAll { it.get() == null }
    }
    
    // Flow transformation utilities
    protected fun <T> Flow<T>.debounceLatest(timeoutMs: Long = 300L): Flow<T> {
        return this.debounce(timeoutMs).distinctUntilChanged()
    }
    
    protected fun <T> Flow<T>.cacheLatest(): Flow<T> {
        return this.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            replay = 1
        )
    }
    
    // Performance metrics access
    fun getPerformanceMetrics(): Map<String, PerformanceMetric> {
        return performanceMetrics.toMap()
    }
    
    fun getMemoryStats(): MemoryStats {
        val runtime = Runtime.getRuntime()
        return MemoryStats(
            totalMemory = runtime.totalMemory(),
            freeMemory = runtime.freeMemory(),
            usedMemory = runtime.totalMemory() - runtime.freeMemory(),
            cacheSize = cache.size(),
            weakReferencesActive = weakReferences.count { it.get() != null }
        )
    }
    
    // Cleanup
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "Cleaning up ViewModel")
        
        // Cancel debounced jobs
        debouncedJobs.values.forEach { it.cancel() }
        debouncedJobs.clear()
        
        // Clean up weak references
        cleanupWeakReferences()
        
        // Clear cache
        clearCache()
        
        // Log final performance metrics
        if (performanceMetrics.isNotEmpty()) {
            Log.d(TAG, "Final performance metrics: $performanceMetrics")
        }
    }
}

// Data classes for monitoring
data class PerformanceMetric(
    val operationName: String,
    val averageDuration: Long,
    val lastExecuted: Long,
    val executionCount: Int
)

data class MemoryStats(
    val totalMemory: Long,
    val freeMemory: Long,
    val usedMemory: Long,
    val cacheSize: Int,
    val weakReferencesActive: Int
)

// Simple LRU Cache implementation
class LRUCache<K, V>(private val maxSize: Int) {
    private val cache = linkedMapOf<K, V>()
    
    fun get(key: K): V? {
        val value = cache.remove(key)
        return if (value != null) {
            cache[key] = value
            value
        } else null
    }
    
    fun put(key: K, value: V): V? {
        val previous = cache.remove(key)
        cache[key] = value
        
        if (cache.size > maxSize) {
            val firstKey = cache.keys.first()
            cache.remove(firstKey)
        }
        
        return previous
    }
    
    fun remove(key: K): V? = cache.remove(key)
    
    fun clear() = cache.clear()
    
    fun size(): Int = cache.size
}