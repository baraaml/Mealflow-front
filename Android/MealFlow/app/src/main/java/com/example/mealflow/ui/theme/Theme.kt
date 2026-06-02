package com.example.mealflow.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = MossGreen,           // More vibrant for dark mode visibility
    onPrimary = White,             // White text on primary
    primaryContainer = DarkGreen,  // Container background
    onPrimaryContainer = PastelGreen, // Text on container

    // Secondary colors
    secondary = PastelGreen,       // Secondary actions
    onSecondary = DarkGreen,       // Text on secondary
    secondaryContainer = DeepOlive,// Secondary container background
    onSecondaryContainer = PastelGreen, // Text on secondary container

    // Tertiary colors (accent)
    tertiary = GoldenBrown,        // Accent color
    onTertiary = Black,            // Text on accent
    tertiaryContainer = GoldenBrown.copy(alpha = 0.2f),
    onTertiaryContainer = GoldenBrown,

    // Background elements
    background = DarkBackground,   // Main background
    onBackground = TransparentWhite, // Text on background
    surface = DarkBackground,      // Surface elements
    onSurface = White,             // Text on surface
    surfaceVariant = DarkBackground.copy(alpha = 0.6f),
    onSurfaceVariant = TransparentWhite.copy(alpha = 0.8f),

    // Other elements
    error = ErrorRed,              // Error states
    outline = Color(0xFF6B9B76),   // Outlines for components
    outlineVariant = Color(0xFF4D6E57), // Secondary outlines
    scrim = TransparentBlack       // Modal overlays
)

private val LightColorScheme = lightColorScheme(
    // Primary colors
    primary = NiceGreen,           // Main brand color
    onPrimary = White,             // Text on primary
    primaryContainer = PastelGreen.copy(alpha = 0.5f), // Container background
    onPrimaryContainer = ForestGreen, // Text on container

    // Secondary colors
    secondary = ForestGreen,       // Secondary actions
    onSecondary = White,           // Text on secondary
    secondaryContainer = PastelGreen.copy(alpha = 0.7f), // Secondary container
    onSecondaryContainer = ForestGreen, // Text on secondary container

    // Tertiary colors (accent)
    tertiary = BrightYellow,       // Accent color
    onTertiary = Black,            // Text on accent
    tertiaryContainer = BrightYellow.copy(alpha = 0.2f),
    onTertiaryContainer = Color(0xFF775B01),

    // Background elements
    background = PaleCream,        // Main background
    onBackground = Black,          // Text on background
    surface = White,               // Surface elements
    onSurface = Black,             // Text on surface
    surfaceVariant = PastelGreen.copy(alpha = 0.15f),
    onSurfaceVariant = ForestGreen.copy(alpha = 0.8f),

    // Other elements
    error = ErrorRed,              // Error states
    outline = ForestGreen.copy(alpha = 0.5f), // Outlines for components
    outlineVariant = NiceGreen.copy(alpha = 0.3f), // Secondary outlines
    scrim = TransparentBlack       // Modal overlays
)

@Composable
fun MealFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled by default to maintain brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}