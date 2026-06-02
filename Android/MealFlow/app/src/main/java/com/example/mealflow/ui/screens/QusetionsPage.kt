package com.example.mealflow.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Question(
    val id: Int,
    val question: String,
    val options: List<String>
)

data class Answer(
    val questionId: Int,
    val selectedOption: String
)

@Composable
fun QuestionScreen(
    question: String,
    options: List<String>,
    onNextClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row containing back button and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {  },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Question",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.weight(1f))

            // Add skip button
            TextButton(onClick = {  }) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            text = question,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(options.size) { index ->
                val option = options[index]
                OptionCard(
                    option = option,
                    isSelected = selectedOption == option,
                    onOptionSelected = { selectedOption = option }
                )
            }
        }

        Button(
            onClick = { selectedOption?.let { onNextClicked(it) } },
            enabled = selectedOption != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Next",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun OptionCard(
    option: String,
    isSelected: Boolean,
    onOptionSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOptionSelected),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onOptionSelected,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuestionScreenPreview() {
    MaterialTheme {
        QuestionScreen(
            question = "What color is the sky?",
            options = listOf("Blue", "Green", "Red", "Yellow"),
            onNextClicked = {}
        )
    }
}


@Composable
fun MultiQuestionScreen(
    modifier: Modifier = Modifier,
    questions: List<Question>,
    onComplete: (List<Answer>) -> Unit,
    onBack: () -> Unit = {},
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    val currentQuestion = questions[currentQuestionIndex]
    val isLastQuestion = currentQuestionIndex == questions.size - 1
    val canGoNext = answers.containsKey(currentQuestion.id)
    val canGoBack = currentQuestionIndex > 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SetupTopBarQuestions(
            currentStep = currentQuestionIndex + 1,
            totalSteps = questions.size,
            onBackClick = if (canGoBack) {
                { currentQuestionIndex-- ; Unit }
            } else onBack,
            onSkipClick = {
                if (isLastQuestion) {
                    onComplete(answers.map { Answer(it.key, it.value) })
                } else {
                    currentQuestionIndex++
                }
            }
        )

        // Question content
        QuestionContent(
            question = currentQuestion,
            selectedOption = answers[currentQuestion.id],
            onOptionSelected = { option ->
                answers = answers.toMutableMap().apply {
                    put(currentQuestion.id, option)
                }
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        )

        // Navigation buttons
        NavigationButtons(
            canGoBack = canGoBack,
            canGoNext = canGoNext,
            isLastQuestion = isLastQuestion,
            onBack = { currentQuestionIndex-- },
            onNext = {
                if (isLastQuestion) {
                    onComplete(answers.map { Answer(it.key, it.value) })
                } else {
                    currentQuestionIndex++
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun SetupTopBarQuestions(
    currentStep: Int,
    totalSteps: Int,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val blueText = Color(0xFF4E79E3)
    val lightGray = Color(0xFF9E9E9E)

    Column {
        // Top bar with back and skip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            TextButton(onClick = onSkipClick) {
                Text(
                    text = "Skip",
                    color = lightGray,
                    style = TextStyle(fontSize = 16.sp)
                )
            }
        }

        // Progress indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index < currentStep) blueText else lightGray.copy(alpha = 0.3f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QuestionContent(
    question: Question,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Question title
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(20.dp),
                color = Color.White,
                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.2
            )
        }

        // Answer options
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(question.options.size) { index ->
                val option = question.options[index]
                EnhancedOptionCard(
                    option = option,
                    optionNumber = index + 1,
                    isSelected = selectedOption == option,
                    onOptionSelected = { onOptionSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun EnhancedOptionCard(
    option: String,
    optionNumber: Int,
    isSelected: Boolean,
    onOptionSelected: () -> Unit
) {
    val blueText = Color(0xFF4E79E3)
    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = tween(300),
        label = "elevation_animation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOptionSelected),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                blueText.copy(alpha = 0.2f)
            else
                Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected)
            BorderStroke(2.dp, blueText)
        else null
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option number
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isSelected)
                            blueText
                        else
                            Color(0xFF666666),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
                color = Color.White
            )

            // Selection icon
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "specific",
                    tint = blueText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun NavigationButtons(
    canGoBack: Boolean,
    canGoNext: Boolean,
    isLastQuestion: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val blueText = Color(0xFF4E79E3)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Back button
        if (canGoBack) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF666666)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Previous",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Next/Finish button
        Button(
            onClick = onNext,
            enabled = canGoNext,
            modifier = Modifier.weight(if (canGoBack) 1f else 1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = blueText,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF333333),
                disabledContentColor = Color(0xFF666666)
            )
        ) {
            Text(
                text = if (isLastQuestion) "Finish" else "Next",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isLastQuestion)
                    Icons.Default.Check
                else
                    Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QuestionScreenExample() {
    val sampleQuestions = listOf(
        Question(
            id = 1,
            question = "What is your favorite type of cuisine?",
            options = listOf("Italian", "Japanese", "Mexican", "Indian")
        ),
        Question(
            id = 2,
            question = "Which meal do you enjoy the most?",
            options = listOf("Breakfast", "Lunch", "Dinner", "Snacks")
        ),
        Question(
            id = 3,
            question = "What is your favorite dessert?",
            options = listOf("Ice Cream", "Cake", "Cookies", "Fruit Salad")
        )
    )

    MultiQuestionScreen(
        questions = sampleQuestions,
        onComplete = { answers ->
            // Handle final answers
            println("Questions completed:")
            answers.forEach { answer ->
                println("Question ${answer.questionId}: ${answer.selectedOption}")
            }
        },
        onBack = {
            // Go back to the previous screen
            println("Going back")
        }
    )
}
