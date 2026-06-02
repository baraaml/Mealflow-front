package com.example.mealflow.utils

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.example.mealflow.MealFlowApplication
import com.example.mealflow.database.UserPreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking

/**
 * Utility class to handle different types of haptic feedback across the app
 */
object HapticFeedback {

    /**
     * Performs a light click haptic feedback - suitable for small UI interactions
     */
    fun performLightClick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    /**
     * Performs a medium click haptic feedback - suitable for confirming actions
     */
    fun performMediumClick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    /**
     * Performs a heavy click haptic feedback - suitable for important actions
     */
    fun performHeavyClick(view: View) {
        // Use the strongest native haptic feedback
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
    }

    /**
     * Performs a selection haptic feedback - suitable for item selection
     */
    fun performSelection(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    /**
     * Performs a success haptic feedback - suitable for completed actions
     */
    fun performSuccess(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    /**
     * Performs a warning/error haptic feedback - suitable for invalid actions
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun performError(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    /**
     * Performs a strong haptic feedback - suitable for reaching end of scroll
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun performScrollEnd(view: View) {
        // Use a stronger haptic feedback for end of scroll
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
        } else {
            // Use a strong fallback for older Android versions
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
        }
    }

    /**
     * Performs a custom vibration pattern for special feedback
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun performCustomVibration(context: Context, pattern: LongArray, amplitudes: IntArray? = null) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (amplitudes != null) {
                VibrationEffect.createWaveform(pattern, amplitudes, -1)
            } else {
                VibrationEffect.createWaveform(pattern, -1)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    /**
     * Performs an extra strong "bump" haptic feedback - suitable for reaching end of content
     * This combines multiple feedbacks for a more noticeable effect
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun performStrongBump(view: View, context: Context) {
        // First use the native haptic feedback
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

        // Then try to add a custom vibration if possible
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Get vibrator service
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                // Create a strong "bump" effect
                val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Stronger effect with predefined effect on Android 10+
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    } else {
                        // Strong one-shot vibration
                        VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                } else {
                    null
                }

                // Execute the vibration
                if (effect != null) {
                    vibrator.vibrate(effect)
                }
            }
        } catch (e: Exception) {
            // Fallback to standard haptic feedback if vibration fails
        }
    }
}

/**
 * Composable function to provide haptic feedback utilities
 */
@Composable
fun rememberHapticFeedback(): HapticFeedbackUtil {
    val context = LocalContext.current.applicationContext
    val view = LocalView.current
    val prefsManager = (context as MealFlowApplication).userPreferencesManager

    return remember(prefsManager) {
        HapticFeedbackUtil(view, context, prefsManager)
    }
}

/**
 * Utility class for Compose-friendly haptic feedback
 */
class HapticFeedbackUtil(
    private val view: View,
    private val context: Context,
    private val prefsManager: UserPreferencesManager
) {
    private val isHapticEnabled: Boolean by lazy {
        runBlocking {
            prefsManager.getHapticFeedbackEnabled().first()
        }
    }

    fun lightClick() {
        if (isHapticEnabled) HapticFeedback.performLightClick(view)
    }

    fun mediumClick() {
        if (isHapticEnabled) HapticFeedback.performMediumClick(view)
    }

    fun heavyClick() {
        if (isHapticEnabled) HapticFeedback.performHeavyClick(view)
    }

    fun selection() {
        if (isHapticEnabled) HapticFeedback.performSelection(view)
    }

    fun success() {
        if (isHapticEnabled) HapticFeedback.performSuccess(view)
    }

    fun error() {
        if (isHapticEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedback.performError(view)
            } else {
                // Fallback for older devices
                HapticFeedback.performHeavyClick(view)
            }
        }
    }

    fun scrollEnd() {
        if (isHapticEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedback.performScrollEnd(view)
            } else {
                // Fallback for older devices
                HapticFeedback.performHeavyClick(view)
            }
        }
    }

    /**
     * Strong bump when reaching the end of content
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun endOfContentBump() {
        if (isHapticEnabled) HapticFeedback.performStrongBump(view, context)
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun customVibration(pattern: LongArray, amplitudes: IntArray? = null) {
        if (isHapticEnabled) HapticFeedback.performCustomVibration(context, pattern, amplitudes)
    }
}

/**
 * Extension function for LazyListState that detects when scrolling reaches the end of the list
 * and provides haptic feedback
 */
@Composable
fun LazyListState.OnBottomReached(
    buffer: Int = 1, // Number of items before the end to consider as "reached"
    onBottomReached: () -> Unit = {}
) {
    val haptic = rememberHapticFeedback()

    // Create derived state to determine if bottom is reached
    val isAtBottom by remember(this) {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            lastVisibleItem.index >= layoutInfo.totalItemsCount - buffer
        }
    }

    LaunchedEffect(key1 = isAtBottom) {
        snapshotFlow { isAtBottom }
            .filter { it }
            .distinctUntilChanged()
            .collect {
                // Use stronger haptic feedback for list end
                haptic.endOfContentBump()
            }
    }
} 