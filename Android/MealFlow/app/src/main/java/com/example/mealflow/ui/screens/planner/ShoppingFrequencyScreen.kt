package com.example.mealflow.ui.screens.planner

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mealflow.utils.DateUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingFrequencyScreen(
    onFrequencySelected: (Int) -> Unit, // Callback with selected frequency
    onNavigateBack: () -> Unit,
    planStartDate: LocalDate? = null,
    planEndDate: LocalDate? = null,
    navController: NavController? = null
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    
    // Use provided dates or default to current date range
    val actualStartDate = planStartDate ?: LocalDate.now()
    val actualEndDate = planEndDate ?: actualStartDate.plusDays(6)
    
    // Generate all dates in the plan period
    val allDates = remember(actualStartDate, actualEndDate) {
        generateDateRange(actualStartDate, actualEndDate)
    }
    
    // State for frequency and selected shopping days
    var frequency by remember { mutableStateOf(1) }
    var sliderPosition by remember { mutableStateOf(frequency.toFloat()) }
    
    // Track selected shopping days
    var selectedDays by remember { mutableStateOf<List<LocalDate>>(listOf(actualStartDate)) }
    
    // Automatically update selected days when frequency changes
    LaunchedEffect(frequency, allDates) {
        selectedDays = when (frequency) {
            0 -> listOf(actualStartDate) // Just the start date
            1 -> { // Weekly
                val planDurationDays = java.time.temporal.ChronoUnit.DAYS.between(actualStartDate, actualEndDate).toInt() + 1
                val weeks = (planDurationDays / 7) + 1
                (0 until min(weeks, 3)).map { actualStartDate.plusDays(it * 7L) }
            }
            2 -> { // Bi-weekly
                val planDurationDays = java.time.temporal.ChronoUnit.DAYS.between(actualStartDate, actualEndDate).toInt() + 1
                val biWeeks = (planDurationDays / 14) + 1
                (0 until min(biWeeks, 2)).map { actualStartDate.plusDays(it * 14L) }
            }
            3 -> { // Custom - keep current selection or default to start date
                if (selectedDays.isEmpty()) listOf(actualStartDate) else selectedDays.take(3)
            }
            else -> listOf(actualStartDate)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan Your Shopping") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Plan date range info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Meal Plan Period",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${formatDisplayDate(actualStartDate)} - ${formatDisplayDate(actualEndDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Shopping frequency selection
            Text(
                "How do you want to plan your shopping trips?",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Shopping frequency options
            ShoppingFrequencyOptions(
                selectedOption = frequency,
                onOptionSelected = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    frequency = it 
                    sliderPosition = it.toFloat()
                }
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Shopping days selection
            Text(
                "Select Your Shopping Days",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                if (frequency == 3) 
                    "Tap on days to select when you want to shop" 
                else 
                    "These days will be your shopping trips",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Maximum of 3 shopping trips allowed",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Calendar-like day selector
            ShoppingDaysSelector(
                allDates = allDates,
                selectedDays = selectedDays,
                onDaySelected = { date, isSelected ->
                    if (frequency == 3) { // Only allow custom selection in custom mode
                        if (isSelected) {
                            // If already selected, remove it
                            selectedDays = (selectedDays - date).sorted()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } else if (selectedDays.size < 3) {
                            // If not at limit, add the date
                            selectedDays = (selectedDays + date).sorted()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } else {
                            // At limit - provide feedback but don't add
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Visual feedback is provided by the text we added above
                        }
                    }
                },
                isCustomMode = frequency == 3
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Selected days summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${selectedDays.size} Shopping ${if (selectedDays.size == 1) "Trip" else "Trips"}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    Text(
                        selectedDays.joinToString(", ") { formatDisplayDate(it) },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(16.dp))
            
            // Generate button
            Button(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    // Save selected days to the backstack entry for the caller to retrieve
                    navController?.currentBackStackEntry?.savedStateHandle?.set("selectedShoppingDays", selectedDays)
                    // Pass the frequency (for backward compatibility) - the actual selected days will be used
                    onFrequencySelected(frequency)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedDays.isNotEmpty()
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Generate Shopping Lists")
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ShoppingFrequencyOptions(
    selectedOption: Int,
    onOptionSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FrequencyOption(
            title = "Once at the start",
            description = "One shopping trip at the beginning",
            isSelected = selectedOption == 0,
            onClick = { onOptionSelected(0) }
        )
        
        FrequencyOption(
            title = "Weekly",
            description = "Shop once every week",
            isSelected = selectedOption == 1,
            onClick = { onOptionSelected(1) }
        )
        
        FrequencyOption(
            title = "Bi-weekly",
            description = "Shop once every two weeks",
            isSelected = selectedOption == 2,
            onClick = { onOptionSelected(2) }
        )
        
        FrequencyOption(
            title = "Custom",
            description = "Select specific days to shop",
            isSelected = selectedOption == 3,
            onClick = { onOptionSelected(3) }
        )
    }
}

@Composable
fun FrequencyOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else 
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShoppingDaysSelector(
    allDates: List<LocalDate>,
    selectedDays: List<LocalDate>,
    onDaySelected: (LocalDate, Boolean) -> Unit,
    isCustomMode: Boolean
) {
    // Group dates by week for better organization
    val datesByWeek = allDates.groupBy { it.get(java.time.temporal.WeekFields.of(Locale.getDefault()).weekOfYear()) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        datesByWeek.forEach { (week, dates) ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Week ${week}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(dates) { date ->
                        val isSelected = selectedDays.contains(date)
                        DayItem(
                            date = date,
                            isSelected = isSelected,
                            onClick = { onDaySelected(date, isSelected) },
                            enabled = isCustomMode
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayItem(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val dayOfMonth = date.dayOfMonth
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Text(
                text = dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun generateDateRange(start: LocalDate, end: LocalDate): List<LocalDate> {
    val numOfDays = java.time.temporal.ChronoUnit.DAYS.between(start, end).toInt() + 1
    return (0 until numOfDays).map { start.plusDays(it.toLong()) }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDisplayDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("MMM d"))
}

// import com.example.mealflow.viewModel.ShoppingListViewModel // ViewModel will be used in AppNavHost to call a method
