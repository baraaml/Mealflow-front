// File: app/src/main/java/com/example/mealflow/ui/screens/MealFlowIcons.kt
package com.example.mealflow.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.mealflow.R

/**
 * Utility object for accessing icons used in the MealFlow app
 */
object MealFlowIcons {
    /**
     * Returns a painter for the meal icon (fork and knife)
     */
    @Composable
    fun mealIcon(): Painter {
        // Replace R.drawable.ic_meal with your actual resource ID
        // If you don't have this resource yet, you'll need to create it
        return painterResource(id = R.drawable.ic_meal)
    }
}