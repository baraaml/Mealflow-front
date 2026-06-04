package com.example.mealflow.ui.screens.setup

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mealflow.navigation.Destination
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupBasicInfoScreen(
    navController: NavController,
    viewModel: SetupProfileViewModel = viewModel()
) {
    val darkBackground = Color(0xFF121212)
    val blueText = Color(0xFF4E79E3)
    val darkGray = Color(0xFF1E1E1E)
    val lightGray = Color(0xFF9E9E9E)

    // Date picker setup
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Top Progress Bar
        SetupTopBar(
            currentStep = 1,
            totalSteps = 3,
            onBackClick = { navController.popBackStack() },
            onSkipClick = { viewModel.skipSetupBasicInfoScreen(navController) }
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            Text(
                text = "Basic Information",
                color = Color.White,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tell us a bit about yourself",
                color = lightGray,
                style = TextStyle(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // First Name
            SetupTextField(
                label = "First Name",
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                placeholder = "Enter your first name"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Last Name
            SetupTextField(
                label = "Last Name",
                value = viewModel.lastName,
                onValueChange = { viewModel.lastName = it },
                placeholder = "Enter your last name"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Birth Date
            Column {
                Text(
                    text = "Birth Date",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(darkGray)
                        .clickable { showDatePicker = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Birth Date",
                        tint = lightGray,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (viewModel.birthDate.isNotBlank()) {
                            try {
                                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                                val date = inputFormat.parse(viewModel.birthDate)
                                outputFormat.format(date ?: Date())
                            } catch (e: Exception) {
                                viewModel.birthDate
                            }
                        } else "Select your birth date",
                        color = if (viewModel.birthDate.isBlank()) lightGray else Color.White,
                        style = TextStyle(fontSize = 16.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gender
            Column {
                Text(
                    text = "Gender",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val genderOptions = listOf("Male", "Female", "Other")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    genderOptions.forEach { option ->
                        val isSelected = viewModel.gender.equals(option, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) blueText else darkGray
                                )
                                .clickable { viewModel.gender = option },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                color = Color.White,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Continue Button
            Button(
                onClick = {
                    viewModel.basicInfoSetup(context, navController)
                    navController.navigate(Destination.SetupPhysicalInfo)
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = blueText,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = viewModel.name.isNotBlank() && viewModel.lastName.isNotBlank()
            ) {
                Text(
                    text = "Continue",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                            format.timeZone = TimeZone.getTimeZone("UTC")
                            viewModel.birthDate = format.format(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}