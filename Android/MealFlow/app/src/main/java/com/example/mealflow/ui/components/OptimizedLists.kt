package com.example.mealflow.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*

import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.min

object OptimizedLists {

    // Performance constants
    private const val DEFAULT_PREFETCH_COUNT = 3
    private const val STAGGER_DELAY = 50L
    private const val ANIMATION_DURATION = 350
    private const val FAST_ANIMATION = 200

    /**
     * High-performance LazyColumn with built-in animations and optimizations
     */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun <T> OptimizedLazyColumn(
        items: List<T>,
        modifier: Modifier = Modifier,
        state: LazyListState = rememberLazyListState(),
        contentPadding: PaddingValues = PaddingValues(0.dp),
        verticalArrangement: Arrangement.Vertical = Arrangement.Top,
        horizontalAlignment: Alignment.Horizontal = Alignment.Start,
        enableAnimations: Boolean = true,
        enableStaggeredEntry: Boolean = false,
        prefetchCount: Int = DEFAULT_PREFETCH_COUNT,
        key: ((index: Int, item: T) -> Any)? = null,
        contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
        itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit
    ) {
        // Note: Prefetch optimization is handled automatically by LazyColumn

        LazyColumn(
            modifier = modifier,
            state = state,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
        ) {
            itemsIndexed(
                items = items,
                key = key?.let { keySelector -> { index, item -> keySelector(index, item) } },
                contentType = contentType
            ) { index, item ->
                if (enableAnimations) {
                    if (enableStaggeredEntry) {
                        StaggeredListItem(
                            index = index,
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            itemContent(index, item)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .animateItemPlacement(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                        ) {
                            itemContent(index, item)
                        }
                    }
                } else {
                    itemContent(index, item)
                }
            }
        }
    }

    /**
     * High-performance LazyRow with optimizations
     */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun <T> OptimizedLazyRow(
        items: List<T>,
        modifier: Modifier = Modifier,
        state: LazyListState = rememberLazyListState(),
        contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
        horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp),
        verticalAlignment: Alignment.Vertical = Alignment.Top,
        enableAnimations: Boolean = true,
        enableParallax: Boolean = false,
        parallaxStrength: Float = 0.5f,
        prefetchCount: Int = DEFAULT_PREFETCH_COUNT,
        key: ((index: Int, item: T) -> Any)? = null,
        contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
        itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit
    ) {
        LazyRow(
            modifier = modifier,
            state = state,
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment
        ) {
            itemsIndexed(
                items = items,
                key = key?.let { keySelector -> { index, item -> keySelector(index, item) } },
                contentType = contentType
            ) { index, item ->
                val itemModifier = if (enableAnimations) {
                    Modifier.animateItemPlacement()
                } else {
                    Modifier
                }

                val parallaxModifier = if (enableParallax) {
                    val parallaxOffset = remember(state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset) {
                        (state.firstVisibleItemIndex + state.firstVisibleItemScrollOffset / 1000f) * parallaxStrength
                    }
                    itemModifier.graphicsLayer {
                        translationX = parallaxOffset * index
                    }
                } else {
                    itemModifier
                }

                Box(modifier = parallaxModifier) {
                    itemContent(index, item)
                }
            }
        }
    }

    /**
     * Optimized LazyVerticalGrid with staggered animations
     */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun <T> OptimizedLazyVerticalGrid(
        items: List<T>,
        columns: GridCells,
        modifier: Modifier = Modifier,
        state: LazyGridState = rememberLazyGridState(),
        contentPadding: PaddingValues = PaddingValues(12.dp),
        horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp),
        verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
        enableAnimations: Boolean = true,
        enableStaggeredEntry: Boolean = true,
        maxStaggerDelay: Long = 500L,
        key: ((index: Int, item: T) -> Any)? = null,
        span: (LazyGridItemSpanScope.(index: Int, item: T) -> GridItemSpan)? = null,
        contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
        itemContent: @Composable LazyGridItemScope.(index: Int, item: T) -> Unit
    ) {
        LazyVerticalGrid(
            columns = columns,
            modifier = modifier,
            state = state,
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
            verticalArrangement = verticalArrangement
        ) {
            itemsIndexed(
                items = items,
                key = key?.let { keySelector -> { index, item -> keySelector(index, item) } },
                span = span?.let { spanSelector -> { index, item -> spanSelector(index, item) } },
                contentType = contentType
            ) { index, item ->
                if (enableAnimations) {
                    if (enableStaggeredEntry) {
                        StaggeredGridItem(
                            index = index,
                            maxDelay = maxStaggerDelay,
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            itemContent(index, item)
                        }
                    } else {
                        Box(
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            itemContent(index, item)
                        }
                    }
                } else {
                    itemContent(index, item)
                }
            }
        }
    }

    /**
     * Performance-optimized staggered grid
     */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun <T> OptimizedLazyStaggeredVerticalGrid(
        items: List<T>,
        columns: StaggeredGridCells,
        modifier: Modifier = Modifier,
        state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
        contentPadding: PaddingValues = PaddingValues(12.dp),
        horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
        verticalItemSpacing: Dp = 8.dp,
        enableAnimations: Boolean = true,
        key: ((index: Int, item: T) -> Any)? = null,
        contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
        itemContent: @Composable LazyStaggeredGridItemScope.(index: Int, item: T) -> Unit
    ) {
        LazyVerticalStaggeredGrid(
            columns = columns,
            modifier = modifier,
            state = state,
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
            verticalItemSpacing = verticalItemSpacing
        ) {
            itemsIndexed(
                items = items,
                key = key?.let { keySelector -> { index, item -> keySelector(index, item) } },
                contentType = contentType
            ) { index, item ->
                val itemModifier = if (enableAnimations) {
                    Modifier.animateItemPlacement()
                } else {
                    Modifier
                }

                Box(modifier = itemModifier) {
                    itemContent(index, item)
                }
            }
        }
    }

    /**
     * Interactive list item with press animations and haptic feedback
     */
    @Composable
    fun InteractiveListItem(
        modifier: Modifier = Modifier,
        onClick: (() -> Unit)? = null,
        onLongClick: (() -> Unit)? = null,
        enableHapticFeedback: Boolean = true,
        pressScale: Float = 0.95f,
        content: @Composable () -> Unit
    ) {
        var isPressed by remember { mutableStateOf(false) }
        val haptic = LocalHapticFeedback.current

        val scale by animateFloatAsState(
            targetValue = if (isPressed) pressScale else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            ),

            label = "itemScale"
        )

        val alpha by animateFloatAsState(
            targetValue = if (isPressed) 0.8f else 1f,
            animationSpec = tween(100),
            label = "itemAlpha"
        )

        Box(
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .pointerInput(onClick, onLongClick) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            if (enableHapticFeedback) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick?.invoke() },
                        onLongPress = { onLongClick?.invoke() }
                    )
                }
        ) {
            content()
        }
    }

    /**
     * Smooth scroll utilities
     */
    @Composable
    fun rememberOptimizedScrollBehavior(
        state: LazyListState,
        smoothScrollThreshold: Int = 10
    ): ScrollBehavior {
        return remember(state) {
            ScrollBehavior(state, smoothScrollThreshold)
        }
    }

    class ScrollBehavior(
        private val state: LazyListState,
        private val smoothScrollThreshold: Int
    ) {
        suspend fun scrollToItem(index: Int, scrollOffset: Int = 0) {
            val currentIndex = state.firstVisibleItemIndex
            if (kotlin.math.abs(index - currentIndex) <= smoothScrollThreshold) {
                state.animateScrollToItem(index, scrollOffset)
            } else {
                state.scrollToItem(index, scrollOffset)
            }
        }

        suspend fun scrollToTop() {
            scrollToItem(0)
        }
    }

    /**
     * Memory-efficient item pooling for large lists
     */
    @Composable
    fun <T> rememberItemPool(
        items: List<T>,
        poolSize: Int = 20
    ): ItemPool<T> {
        return remember(items.hashCode()) {
            ItemPool(items, poolSize)
        }
    }

    class ItemPool<T>(
        private val items: List<T>,
        private val maxPoolSize: Int
    ) {
        private val pool = mutableMapOf<Int, T>()
        
        fun getItem(index: Int): T? {
            return if (index in items.indices) {
                pool.getOrPut(index) { items[index] }.also {
                    // Remove oldest items if pool is too large
                    if (pool.size > maxPoolSize) {
                        val oldestKey = pool.keys.minOrNull()
                        oldestKey?.let { pool.remove(it) }
                    }
                }
            } else null
        }
        
        fun preloadRange(startIndex: Int, endIndex: Int) {
            for (i in startIndex..min(endIndex, items.lastIndex)) {
                if (!pool.containsKey(i)) {
                    pool[i] = items[i]
                }
            }
        }
        
        fun clearPool() {
            pool.clear()
        }
    }

    // Internal helper composables
    @Composable
    private fun StaggeredListItem(
        index: Int,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        var visible by remember { mutableStateOf(false) }
        
        LaunchedEffect(index) {
            delay(index * STAGGER_DELAY)
            visible = true
        }

        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(ANIMATION_DURATION),
            label = "staggerAlpha"
        )

        val scale by animateFloatAsState(
            targetValue = if (visible) 1f else 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "staggerScale"
        )

        Box(
            modifier = modifier.graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
        ) {
            content()
        }
    }

    @Composable
    private fun StaggeredGridItem(
        index: Int,
        maxDelay: Long,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        var visible by remember { mutableStateOf(false) }
        
        LaunchedEffect(index) {
            val delay = min(index * STAGGER_DELAY, maxDelay)
            delay(delay)
            visible = true
        }

        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(ANIMATION_DURATION),
            label = "gridStaggerAlpha"
        )

        val translateY by animateFloatAsState(
            targetValue = if (visible) 0f else 40f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "gridStaggerTranslate"
        )

        Box(
            modifier = modifier.graphicsLayer {
                this.alpha = alpha
                this.translationY = translateY
            }
        ) {
            content()
        }
    }
}

// Extension functions for easier integration
@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
fun LazyListState.isScrollingDown(): Boolean = !isScrollingUp()

@Composable
fun LazyListState.isAtTop(): Boolean {
    return remember(this) {
        derivedStateOf {
            firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
        }
    }.value
}

@Composable
fun LazyListState.isAtBottom(itemCount: Int): Boolean {
    return remember(this, itemCount) {
        derivedStateOf {
            layoutInfo.visibleItemsInfo.lastOrNull()?.index == itemCount - 1
        }
    }.value
}