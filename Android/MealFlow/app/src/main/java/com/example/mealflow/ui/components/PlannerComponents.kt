package com.example.mealflow.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DinnerDining
import androidx.compose.material.icons.rounded.FoodBank
import androidx.compose.material.icons.rounded.Icecream
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LunchDining
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mealflow.R
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.model.MealType
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.random.Random


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateNavigator(
    currentDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    isToday: Boolean
) {
    // Add logging to debug the date changes
    LaunchedEffect(currentDate) {
        println("DateNavigator received date: $currentDate")
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = "Navigate to previous day" }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Use remember with key to ensure recomposition when date changes
                val displayText = remember(currentDate, isToday) {
                    if (isToday) {
                        "Today"
                    } else {
                        currentDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                    }
                }

                Text(
                    text = displayText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                if (!isToday) {
                    val dayOfWeekText = remember(currentDate) {
                        currentDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    }

                    Text(
                        text = dayOfWeekText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onNextDay,
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = "Navigate to next day" }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
@Composable
fun MealStatusIndicator(
    mealType: MealType,
    isCooked: Boolean,
    isPlanned: Boolean
) {
    val (backgroundColor, icon) = when {
        isCooked -> Pair(
            MaterialTheme.colorScheme.tertiary,
            Icons.Rounded.CheckCircle
        )
        isPlanned -> Pair(
            MaterialTheme.colorScheme.primary,
            Icons.Rounded.Schedule
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            null
        )
    }

    val statusText = when {
        isCooked -> "Cooked"
        isPlanned -> "Planned"
        else -> "Not Planned"
    }

    Surface(
        color = backgroundColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, backgroundColor.copy(alpha = 0.2f)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(backgroundColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = backgroundColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
@Composable
fun MealPlanCard(
    meal: Meal,
    mealType: MealType,
    onCardClick: () -> Unit,
    onCookClick: () -> Unit,
    isCooked: Boolean = false,
    isPlanned: Boolean = false
) {
    // Use the new MealimePlanCard instead
    MealimePlanCard(
        meal = meal,
        mealType = mealType,
        onCardClick = onCardClick,
        onCookClick = onCookClick,
        isCooked = isCooked,
        isPlanned = isPlanned
    )
}
