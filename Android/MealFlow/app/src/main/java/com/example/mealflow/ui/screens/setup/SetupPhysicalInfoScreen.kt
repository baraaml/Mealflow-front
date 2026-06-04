package com.example.mealflow.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mealflow.navigation.Destination

@Composable
fun SetupPhysicalInfoScreen(
    navController: NavController,
    viewModel: SetupProfileViewModel = viewModel()
) {
    val darkBackground = Color(0xFF121212)
    val blueText = Color(0xFF4E79E3)
    val lightGray = Color(0xFF9E9E9E)

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Top Progress Bar
        SetupTopBar(
            currentStep = 2,
            totalSteps = 3,
            onBackClick = { navController.popBackStack() },
            onSkipClick = { viewModel.skipSetupPhysicalInfoScreen(navController) }
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
                text = "Physical Information",
                color = Color.White,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Optional: Help us personalize your experience",
                color = lightGray,
                style = TextStyle(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Height
            SetupTextField(
                label = "Height (cm)",
                value = if (viewModel.height > 0) viewModel.height.toString() else "",
                onValueChange = { viewModel.setHeight(it) },
                placeholder = "Enter your height",
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Weight
            SetupTextField(
                label = "Weight (kg)",
                value = if (viewModel.weight > 0) viewModel.weight.toString() else "",
                onValueChange = { viewModel.setWeight(it) },
                placeholder = "Enter your weight",
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Continue Button
            Button(
                onClick = {
                    viewModel.physicalInfoSetup(context, navController)
                    navController.navigate(Destination.SetupPhotos)
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = blueText,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Continue",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skip this step
            TextButton(
                onClick = { navController.navigate(Destination.SetupPhotos) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Skip this step",
                    color = lightGray,
                    style = TextStyle(fontSize = 16.sp)
                )
            }
        }
    }
}