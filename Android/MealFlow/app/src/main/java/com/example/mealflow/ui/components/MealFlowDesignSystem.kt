package com.example.mealflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mealflow.ui.theme.*

/**
 * A standardized design system for MealFlow components to ensure visual consistency
 * across the application, using the app's custom color palette.
 */
object MealFlowDesignSystem {

    // Spacing constants
    object Spacing {
        val xxs = 2.dp
        val xs = 4.dp
        val small = 8.dp
        val medium = 16.dp
        val large = 24.dp
        val xl = 32.dp
        val xxl = 48.dp
    }

    // Elevation constants
    object Elevation {
        val none = 0.dp
        val low = 1.dp
        val medium = 2.dp
        val high = 4.dp
        val extraHigh = 8.dp
    }

    // Card styles
    @Composable
    fun PrimaryCard(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Card(
            modifier = modifier,
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.medium),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            content()
        }
    }

    @Composable
    fun SecondaryCard(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Card(
            modifier = modifier,
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            content()
        }
    }

    @Composable
    fun GreenCard(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Card(
            modifier = modifier,
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.medium),
            colors = CardDefaults.cardColors(
                containerColor = PastelGreen.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            content()
        }
    }

    // Button styles
    @Composable
    fun PrimaryButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        leadingIcon: @Composable (() -> Unit)? = null
    ) {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = NiceGreen,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(Modifier.width(Spacing.small))
            }
            Text(text)
        }
    }

    @Composable
    fun SecondaryButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        leadingIcon: @Composable (() -> Unit)? = null
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ForestGreen
            )
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(Modifier.width(Spacing.small))
            }
            Text(text)
        }
    }

    @Composable
    fun AccentButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        leadingIcon: @Composable (() -> Unit)? = null
    ) {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrightYellow,
                contentColor = Black
            )
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(Modifier.width(Spacing.small))
            }
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }

    // Text styles with consistent typography
    @Composable
    fun HeadingLarge(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = modifier
        )
    }

    @Composable
    fun HeadingMedium(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = ForestGreen
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = modifier
        )
    }

    @Composable
    fun HeadingSmall(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = modifier
        )
    }

    @Composable
    fun TitleLarge(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = NiceGreen
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = modifier
        )
    }

    @Composable
    fun TitleMedium(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = modifier
        )
    }

    @Composable
    fun BodyLarge(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            modifier = modifier
        )
    }

    @Composable
    fun BodyMedium(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            modifier = modifier
        )
    }

    @Composable
    fun BodySmall(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = modifier
        )
    }

    // Standard section container
    @Composable
    fun Section(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium, vertical = Spacing.small)
        ) {
            content()
        }
    }

    // Standard divider
    @Composable
    fun StandardDivider(
        modifier: Modifier = Modifier
    ) {
        Divider(
            modifier = modifier.padding(vertical = Spacing.small),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }

    // Loading indicator
    @Composable
    fun LoadingIndicator(
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = NiceGreen,
                modifier = Modifier.size(48.dp)
            )
        }
    }

    // Empty state
    @Composable
    fun EmptyState(
        title: String,
        message: String,
        icon: @Composable () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(Spacing.medium))
            TitleMedium(
                text = title,
                modifier = Modifier.padding(bottom = Spacing.small)
            )
            BodyMedium(
                text = message,
                modifier = Modifier.padding(horizontal = Spacing.large)
            )
        }
    }

    // Progress indicator
    @Composable
    fun ProgressBar(
        progress: Float,
        modifier: Modifier = Modifier
    ) {
        Box(modifier = modifier.fillMaxWidth()) {
            // Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Foreground
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(NiceGreen)
            )
        }
    }

    // Status indicators
    @Composable
    fun SuccessChip(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            color = SuccessGreen.copy(alpha = 0.2f)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = SuccessGreen,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }

    @Composable
    fun WarningChip(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            color = WarningYellow.copy(alpha = 0.2f)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = WarningYellow,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }

    @Composable
    fun ErrorChip(
        text: String,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            color = ErrorRed.copy(alpha = 0.2f)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = ErrorRed,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }

    // App Bar Title component
    @Composable
    fun AppBarTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = modifier
        )
    }
    
    // Card Title component
    @Composable
    fun CardTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = modifier
        )
    }
    
    // Card Subtitle component
    @Composable
    fun CardSubtitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            modifier = modifier
        )
    }
    
    // Meal Title component
    @Composable
    fun MealTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = modifier
        )
    }
}
