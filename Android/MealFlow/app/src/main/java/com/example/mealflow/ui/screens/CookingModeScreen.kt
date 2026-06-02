package com.example.mealflow.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mealflow.data.model.Meal // Ensure this import is correct
import com.example.mealflow.data.model.MealIngredient // Ensure this import is correct
import com.example.mealflow.utils.HapticFeedbackUtil // Ensure this import is correct
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    meal: Meal,
    onNavigateBack: () -> Unit,
    haptic: HapticFeedbackUtil,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var showStepOverview by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    val cookingSteps = remember(meal.instructions, meal.ingredients) {
        parseCookingSteps(meal.instructions, meal.ingredients)
    }

    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CookingModeTopBar(
            onBack = { showExitDialog = true },
            onShowOverview = { showStepOverview = true },
            haptic = haptic
        )

        Box(modifier = Modifier.weight(1f)) {
            if (cookingSteps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No cooking steps found for this meal.", style = MaterialTheme.typography.bodyLarge)
                }
            } else if (currentStep < cookingSteps.size) {
                CookingStepContent(
                    step = cookingSteps[currentStep],
                    stepNumber = currentStep + 1,
                    totalSteps = cookingSteps.size,
                    haptic = haptic,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CookingCompleteScreen(
                    meal = meal,
                    onFinish = onNavigateBack,
                    haptic = haptic
                )
            }
        }

        if (cookingSteps.isNotEmpty() && currentStep < cookingSteps.size) {
            CookingModeBottomBar(
                currentStep = currentStep,
                totalSteps = cookingSteps.size,
                onPrevious = {
                    if (currentStep > 0) {
                        haptic.lightClick()
                        currentStep--
                    }
                },
                onNext = {
                    haptic.lightClick()
                    currentStep++
                },
                haptic = haptic
            )
        }
    }

    if (showStepOverview) {
        StepOverviewBottomSheet(
            steps = cookingSteps,
            currentStep = currentStep,
            onStepSelected = { step ->
                currentStep = step
                showStepOverview = false
                haptic.mediumClick()
            },
            onDismiss = { showStepOverview = false }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Leave cooking mode?") },
            text = { Text("If you leave cooking mode, your active timers will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.lightClick()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("LEAVE COOKING MODE")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        haptic.lightClick()
                        showExitDialog = false
                    }
                ) {
                    Text("CONTINUE COOKING")
                }
            }
        )
    }
}

@Composable
fun CookingModeTopBar(
    onBack: () -> Unit,
    onShowOverview: () -> Unit,
    haptic: HapticFeedbackUtil,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { haptic.lightClick(); onBack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
        }
        IconButton(onClick = { haptic.lightClick(); onShowOverview() }) {
            Icon(Icons.Default.List, "Show all steps")
        }
    }
}

@Composable
fun CookingStepContent(
    step: CookingStep,
    stepNumber: Int,
    totalSteps: Int,
    haptic: HapticFeedbackUtil,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stepNumber.toString(),
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = step.instruction,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 30.sp
        )
        Spacer(Modifier.height(24.dp))
        if (step.ingredients.isNotEmpty()) {
            Text(
                text = "Ingredients for this step:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            step.ingredients.forEach { ingredient ->
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(ingredient, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        if (step.timerDuration > 0) {
            CookingTimer(
                duration = step.timerDuration,
                stepInstruction = step.instruction, // Pass the instruction from the current step
                currentStepDisplay = stepNumber,
                haptic = haptic
            )
        }
    }
}

@Composable
fun CookingTimer(
    duration: Int, // in minutes
    stepInstruction: String, // This is the instruction for the current step's timer
    currentStepDisplay: Int,
    haptic: HapticFeedbackUtil,
    modifier: Modifier = Modifier
) {
    var isRunning by remember { mutableStateOf(false) }
    var timeRemaining by remember(duration) { mutableIntStateOf(duration * 60) }
    var showTimerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, timeRemaining) {
        if (isRunning && timeRemaining > 0) {
            delay(1000)
            timeRemaining--
            if (timeRemaining == 0) {
                haptic.heavyClick()
                isRunning = false
            }
        } else if (timeRemaining == 0 && isRunning) {
            isRunning = false
        }
    }

    Column(modifier = modifier) {
        Button(
            onClick = {
                haptic.mediumClick()
                if (!isRunning && timeRemaining > 0) {
                    isRunning = true
                } else if (timeRemaining == 0 && !isRunning) {
                    timeRemaining = duration * 60
                    isRunning = true
                }
                showTimerDialog = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (isRunning) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Timer else Icons.Default.HourglassTop,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (timeRemaining == 0 && !isRunning) "Start $duration min timer"
                else if (isRunning) "Timer: ${formatTime(timeRemaining)}"
                else "Paused: ${formatTime(timeRemaining)}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    if (showTimerDialog) {
        TimerDialog(
            timeRemaining = timeRemaining,
            totalTime = duration * 60,
            stepInstruction = stepInstruction, // Use the passed stepInstruction here
            currentStepDisplay = currentStepDisplay,
            isRunning = isRunning,
            onDismiss = { showTimerDialog = false },
            onCancel = {
                isRunning = false
                timeRemaining = duration * 60
                showTimerDialog = false
                haptic.lightClick()
            },
            onAddMinute = {
                if (timeRemaining > 0 || isRunning) {
                    timeRemaining += 60
                    haptic.lightClick()
                }
            },
            onPauseResume = {
                isRunning = !isRunning
                haptic.lightClick()
            }
        )
    }
}

@Composable
fun TimerDialog(
    timeRemaining: Int,
    totalTime: Int,
    stepInstruction: String,
    currentStepDisplay: Int,
    isRunning: Boolean,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onAddMinute: () -> Unit,
    onPauseResume: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { if (totalTime > 0) (totalTime - timeRemaining).toFloat() / totalTime else 0f },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 10.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        formatTime(timeRemaining),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stepInstruction.take(120) + if (stepInstruction.length > 120) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    if (timeRemaining > 0) {
                        OutlinedButton(
                            onClick = onPauseResume,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) { Text(if (isRunning) "Pause" else "Resume") }
                    }
                    OutlinedButton(
                        onClick = onAddMinute,
                        enabled = timeRemaining > 0 || isRunning,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                    ) { Text("+1 min") }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(onClick = onCancel, Modifier.weight(1f)) { Text("Reset Timer") }
                    TextButton(onClick = onDismiss, Modifier.weight(1f)) { Text("Back to Step $currentStepDisplay") }
                }
            }
        }
    }
}

@Composable
fun CookingModeBottomBar(
    currentStep: Int,
    totalSteps: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    haptic: HapticFeedbackUtil,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious, enabled = currentStep > 0) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, "Previous step",
                    tint = if (currentStep > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val maxDots = 5
                val displayDots = minOf(totalSteps, maxDots)
                repeat(displayDots) { index ->
                    val dotIndex = if (totalSteps <= maxDots) index
                    else {
                        val start = when {
                            currentStep < maxDots / 2 -> 0
                            currentStep >= totalSteps - (maxDots - maxDots / 2) -> totalSteps - maxDots
                            else -> currentStep - maxDots / 2
                        }
                        start + index
                    }
                    Box(
                        Modifier
                            .size(if (dotIndex == currentStep) 10.dp else 8.dp)
                            .background(
                                if (dotIndex == currentStep) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                CircleShape
                            )
                    )
                }
            }
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (currentStep < totalSteps - 1) "Next" else "Finish",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepOverviewBottomSheet(
    steps: List<CookingStep>,
    currentStep: Int,
    onStepSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.navigationBarsPadding()) {
            Text(
                "Recipe Steps",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 12.dp)
            )
            LazyColumn(
                Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                itemsIndexed(steps) { index, step ->
                    StepOverviewItem(
                        stepNumber = index + 1,
                        instruction = step.instruction,
                        isActive = index == currentStep,
                        onClick = { onStepSelected(index) }
                    )
                    if (index < steps.lastIndex) {
                        HorizontalDivider(Modifier.padding(start = 56.dp, end = 16.dp, top = 4.dp, bottom = 4.dp))
                    }
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun StepOverviewItem(
    stepNumber: Int,
    instruction: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Text(
                stepNumber.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(32.dp).align(Alignment.CenterVertically)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                instruction,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CookingCompleteScreen(
    meal: Meal,
    onFinish: () -> Unit,
    haptic: HapticFeedbackUtil,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircleOutline, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text("Cooking Complete!", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            "Enjoy your ${meal.name}!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = { haptic.mediumClick(); onFinish() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) { Text("Done", color = MaterialTheme.colorScheme.onPrimary) }
    }
}

data class CookingStep(
    val instruction: String,
    val ingredients: List<String> = emptyList(),
    val timerDuration: Int = 0 // in minutes
)

// Ensure Meal and MealIngredient are correctly defined and imported
// For example:
// import com.example.mealflow.data.model.Meal
// import com.example.mealflow.data.model.MealIngredient

fun parseCookingSteps(instructions: List<String>?, ingredients: List<MealIngredient>?): List<CookingStep> {
    if (instructions.isNullOrEmpty()) return emptyList()
    val allIngredients = ingredients ?: emptyList()

    return instructions.map { instruction ->
        val timerDuration = extractTimerDuration(instruction)
        val stepIngredients = extractIngredientsForStep(instruction, allIngredients)
        CookingStep(
            instruction = instruction.trim(),
            ingredients = stepIngredients,
            timerDuration = timerDuration
        )
    }
}

fun extractTimerDuration(instruction: String): Int {
    val timePattern = Regex("""(\d+)(?:\s*(?:to|-)\s*(\d+))?\s*min(?:s|utes?)?""", RegexOption.IGNORE_CASE)
    val match = timePattern.find(instruction)
    return match?.let {
        val firstTime = it.groupValues[1].toIntOrNull() ?: 0
        val secondTime = it.groupValues[2].takeIf { groupVal -> groupVal.isNotBlank() }?.toIntOrNull()
        if (secondTime != null && secondTime > firstTime) {
            (firstTime + secondTime) / 2
        } else {
            firstTime
        }
    } ?: 0
}

fun extractIngredientsForStep(instruction: String, allIngredients: List<MealIngredient>): List<String> {
    if (instruction.isBlank() || allIngredients.isEmpty()) return emptyList()
    val lowerInstruction = instruction.lowercase()

    return allIngredients.mapNotNull { ingredient ->
        val quantityText = ingredient.quantity?.takeIf { it.isNaN() }?.let { "$it " } ?: ""
        val unitText = ingredient.unit?.takeIf { it.isNotBlank() }?.let { "$it " } ?: ""
        // Assuming MealIngredient.phrase is the main name of the ingredient
        val phraseText = ingredient.phrase?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val fullIngredientText = "${quantityText}${unitText}${phraseText}".trim()

        val ingredientPhraseLower = phraseText.lowercase()
        val ingredientWords = ingredientPhraseLower.split(" ").filter { it.length > 2 }

        if (lowerInstruction.contains(ingredientPhraseLower) ||
            ingredientWords.any { word -> lowerInstruction.contains(word) }) {
            fullIngredientText.ifEmpty { null }
        } else null
    }
}

fun formatTime(seconds: Int): String {
    if (seconds < 0) return "0:00"
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${minutes}:${remainingSeconds.toString().padStart(2, '0')}"
}