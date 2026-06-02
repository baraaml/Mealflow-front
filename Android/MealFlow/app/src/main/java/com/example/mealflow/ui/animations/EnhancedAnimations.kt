package com.example.mealflow.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import kotlin.math.*

object EnhancedAnimations {

    private const val FAST_ANIMATION = 200
    const val MEDIUM_ANIMATION = 350
    private const val SLOW_ANIMATION = 500
    internal const val STAGGER_DELAY = 50L

    // Simplified easing curves - with explicit type annotations
    // Remove the recursive LinearEasing definition
    val StandardEasing: Easing = FastOutSlowInEasing // Standard Android easing, simple and effective
    val SmoothEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val DecelerateEasing: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val CustomBounceEasing: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

    // Pre-defined animation specs for reuse - simplified to avoid bouncing
    val standardTween: TweenSpec<Float> = tween(
        durationMillis = MEDIUM_ANIMATION,
        easing = StandardEasing
    )

    val simpleTween: TweenSpec<Float> = tween(
        durationMillis = MEDIUM_ANIMATION,
        easing = androidx.compose.animation.core.LinearEasing // Use fully qualified name
    )

    val simpleIntOffsetTween: TweenSpec<IntOffset> = tween(
        durationMillis = MEDIUM_ANIMATION,
        easing = StandardEasing
    )

    @Composable
    fun ParallaxEffect(
        lazyListState: LazyListState,
        rate: Float = 0.5f,
        content: @Composable (Float) -> Unit
    ) {
        val firstVisibleItemIndex by remember {
            derivedStateOf { lazyListState.firstVisibleItemIndex }
        }
        val firstVisibleItemScrollOffset by remember {
            derivedStateOf { lazyListState.firstVisibleItemScrollOffset }
        }

        val parallaxOffset = remember(firstVisibleItemIndex, firstVisibleItemScrollOffset) {
            (firstVisibleItemIndex + firstVisibleItemScrollOffset / 1000f) * rate
        }

        content(parallaxOffset)
    }

    fun slideInWithDepth(
        direction: SlideDirection = SlideDirection.Left,
        durationMs: Int = MEDIUM_ANIMATION
    ): EnterTransition {
        val slideDistance = when (direction) {
            SlideDirection.Left -> { fullWidth: Int -> fullWidth }
            SlideDirection.Right -> { fullWidth: Int -> -fullWidth }
            SlideDirection.Up -> { fullHeight: Int -> fullHeight }
            SlideDirection.Down -> { fullHeight: Int -> -fullHeight }
        }

        return slideInHorizontally(
            animationSpec = tween(durationMs, easing = StandardEasing),
            initialOffsetX = slideDistance
        ) + fadeIn(
            animationSpec = tween(durationMs)
        )
    }

    fun slideOutWithDepth(
        direction: SlideDirection = SlideDirection.Left,
        durationMs: Int = FAST_ANIMATION
    ): ExitTransition {
        val slideDistance = when (direction) {
            SlideDirection.Left -> { fullWidth: Int -> -fullWidth }
            SlideDirection.Right -> { fullWidth: Int -> fullWidth }
            SlideDirection.Up -> { fullHeight: Int -> -fullHeight }
            SlideDirection.Down -> { fullHeight: Int -> fullHeight }
        }

        return slideOutHorizontally(
            animationSpec = tween(durationMs, easing = StandardEasing),
            targetOffsetX = slideDistance
        ) + fadeOut(
            animationSpec = tween(durationMs)
        )
    }

    fun simpleSlideIn(
        direction: SlideDirection = SlideDirection.Left,
        durationMs: Int = MEDIUM_ANIMATION
    ): EnterTransition {
        val slideDistance = when (direction) {
            SlideDirection.Left -> { fullWidth: Int -> fullWidth }
            SlideDirection.Right -> { fullWidth: Int -> -fullWidth }
            SlideDirection.Up -> { fullHeight: Int -> fullHeight }
            SlideDirection.Down -> { fullHeight: Int -> -fullHeight }
        }

        return slideInHorizontally(
            animationSpec = tween(durationMs, easing = androidx.compose.animation.core.LinearEasing),
            initialOffsetX = slideDistance
        ) + fadeIn(
            animationSpec = tween(durationMs)
        )
    }

    fun simpleSlideOut(
        direction: SlideDirection = SlideDirection.Left,
        durationMs: Int = FAST_ANIMATION
    ): ExitTransition {
        val slideDistance = when (direction) {
            SlideDirection.Left -> { fullWidth: Int -> -fullWidth }
            SlideDirection.Right -> { fullWidth: Int -> fullWidth }
            SlideDirection.Up -> { fullHeight: Int -> -fullHeight }
            SlideDirection.Down -> { fullHeight: Int -> fullHeight }
        }

        return slideOutHorizontally(
            animationSpec = tween(durationMs, easing = androidx.compose.animation.core.LinearEasing),
            targetOffsetX = slideDistance
        ) + fadeOut(
            animationSpec = tween(durationMs)
        )
    }

    fun simpleFadeIn(
        durationMs: Int = MEDIUM_ANIMATION
    ): EnterTransition {
        return fadeIn(
            animationSpec = tween(durationMs)
        )
    }

    fun simpleFadeOut(
        durationMs: Int = FAST_ANIMATION
    ): ExitTransition {
        return fadeOut(
            animationSpec = tween(durationMs)
        )
    }

    fun bounceIn(
        durationMs: Int = MEDIUM_ANIMATION
    ): EnterTransition {
        return scaleIn(
            initialScale = 0.3f,
            animationSpec = tween(durationMs, easing = CustomBounceEasing)
        ) + fadeIn(
            animationSpec = tween(durationMs / 2, durationMs / 4)
        )
    }

    fun bounceOut(
        durationMs: Int = FAST_ANIMATION
    ): ExitTransition {
        return scaleOut(
            targetScale = 0.3f,
            animationSpec = tween(durationMs, easing = CustomBounceEasing)
        ) + fadeOut(
            animationSpec = tween(durationMs)
        )
    }

    fun zoomIn(
        durationMs: Int = MEDIUM_ANIMATION
    ): EnterTransition {
        return scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(durationMs, easing = DecelerateEasing)
        ) + fadeIn(
            animationSpec = tween(durationMs / 2, durationMs / 4)
        )
    }

    fun zoomOut(
        durationMs: Int = FAST_ANIMATION
    ): ExitTransition {
        return scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(durationMs, easing = FastOutSlowInEasing)
        ) + fadeOut(
            animationSpec = tween(durationMs)
        )
    }

    @Composable
    fun EnhancedPulsatingEffect(
        minScale: Float = 0.6f,
        maxScale: Float = 1.0f,
        minAlpha: Float = 0.3f,
        maxAlpha: Float = 0.7f,
        durationMs: Int = 1000,
        content: @Composable () -> Unit
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulsating")
        
        val scale by infiniteTransition.animateFloat(
            initialValue = minScale,
            targetValue = maxScale,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMs, easing = SmoothEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulsatingScale"
        )

        val alpha by infiniteTransition.animateFloat(
            initialValue = minAlpha,
            targetValue = maxAlpha,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMs, easing = SmoothEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulsatingAlpha"
        )

        Box(
            modifier = Modifier.graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha
            }
        ) {
            content()
        }
    }

    class AnimationMetrics {
        private var frameDrops = 0
        private var totalFrames = 0
        
        fun recordFrame(dropped: Boolean = false) {
            totalFrames++
            if (dropped) frameDrops++
        }
        
        fun getDropRate(): Float = if (totalFrames > 0) frameDrops.toFloat() / totalFrames else 0f
        fun reset() {
            frameDrops = 0
            totalFrames = 0
        }
    }

    enum class SlideDirection { Left, Right, Up, Down }
}
