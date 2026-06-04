package com.example.mealflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mealflow.navigation.Destination
import androidx.navigation.compose.rememberNavController
import com.example.mealflow.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StartPage(navController: NavController) {
    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    val buttonScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    // Modern animation specs
    val gentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Trigger animations on composition
    LaunchedEffect(Unit) {
        headerVisible = true
    }

    Box(modifier = Modifier.fillMaxSize() .background(MaterialTheme.colorScheme.background)) {
        // Header with elegant slide + fade
        AnimatedVisibility(
            visible = headerVisible,
            enter = slideInHorizontally(
                initialOffsetX = { -it/2 },
                animationSpec = tween(600, delayMillis = 100)
            ) + fadeIn(animationSpec = tween(800)),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = stringResource(id = R.string.Header),
                Modifier.padding(start = 20.dp, top = 100.dp, end = 20.dp),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.sf_pro_rounded_heavy))
            )
        }

        Column(
            modifier = Modifier.align(alignment = Alignment.BottomCenter)
        ) {
            // Modern button with press effect
            Button(
                onClick = {
                    coroutineScope.launch {

                        // Press animation
                        buttonScale.animateTo(0.95f, gentleSpring)

                        // Release animation with slight overshoot
                        buttonScale.animateTo(1.03f, gentleSpring)

                        // Return to normal
                        buttonScale.animateTo(1f, gentleSpring)

                        // Navigate with delay to see animation complete
                        delay(100)
                        navController.navigate(Destination.Register) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier
                    .padding(20.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .width(150.dp)
                    .scale(buttonScale.value),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(id = R.string.Get_Started),
                    fontFamily = FontFamily(Font(R.font.sfpro)),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Login row with staggered fade
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(animationSpec = tween(delayMillis = 400))
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 100.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.Already_member),
                        fontFamily = FontFamily(Font(R.font.sflightit)),
                    )
                    Text(
                        text = "Login",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily(Font(R.font.sfmedit)),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable {
                                coroutineScope.launch {
                                    navController.navigate(Destination.Login) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}
// ----------------------- Function to preview StartPage ---------------------------
//------------------------------------------------------------------
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewStartPage()
{
    StartPage(navController = rememberNavController())
}