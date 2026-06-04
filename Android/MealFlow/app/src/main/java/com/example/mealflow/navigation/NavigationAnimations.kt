package com.example.mealflow.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination

/**
 * Navigation animations utility - simple animations for smooth transitions
 */
object NavigationAnimations {
    // Animation duration
    private const val ANIMATION_DURATION = 300

    // Default enter transition - slide in from right with fade
    fun enterTransition(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
    }

    // Default exit transition - fade out
    fun exitTransition(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition {
        return fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }

    // Pop enter transition - fade in
    fun popEnterTransition(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition {
        return fadeIn(animationSpec = tween(ANIMATION_DURATION))
    }

    // Pop exit transition - slide out to right with fade
    fun popExitTransition(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(ANIMATION_DURATION)
        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
    }

    // Bottom nav transition - crossfade
    fun bottomNavTransition(from: Int, to: Int): EnterTransition {
        // Simple crossfade for bottom navigation
        return fadeIn(animationSpec = tween(ANIMATION_DURATION))
    }
}