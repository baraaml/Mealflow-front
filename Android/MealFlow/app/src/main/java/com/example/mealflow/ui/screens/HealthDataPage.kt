package com.example.mealflow.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HealthDataScreen(
    onSkip: () -> Unit,
    onSave: () -> Unit
) {
    val darkBackground = Color(0xFF121212)
    val orangeButton = Color(0xFFFF9843)

    var selectedSex by remember { mutableStateOf("Male") }
    var selectedYear by remember { mutableStateOf("2003") }
    var selectedHeight by remember { mutableIntStateOf(170) }

    var showBottomSheet by remember { mutableStateOf(false) }
    var currentField by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            when (currentField) {
                "Sex" -> {
                    val sexes = listOf("Male", "Female")
                    val initialIndex = sexes.indexOf(selectedSex)
                    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
                    var tempSex by remember { mutableStateOf(selectedSex) }

                    LaunchedEffect(listState.isScrollInProgress) {
                        if (!listState.isScrollInProgress) {
                            val centerItem = listState.firstVisibleItemIndex +
                                    if (listState.firstVisibleItemScrollOffset > 50) 1 else 0
                            tempSex = sexes.getOrElse(centerItem) { tempSex }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Select Sex",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .height(150.dp)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(vertical = 30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(sexes.size) { index ->
                                    val sex = sexes[index]
                                    val isSelected = sex == tempSex

                                    //Animation on font size
                                    val animatedFontSize by animateDpAsState(
                                        targetValue = if (isSelected) 32.dp else 20.dp,
                                        label = "fontSizeAnimation"
                                    )

                                    // Animation on text color
                                    val animatedColor by animateColorAsState(
                                        targetValue = if (isSelected) Color.White else Color.Gray,
                                        label = "colorAnimation"
                                    )

                                    Text(
                                        text = sex,
                                        fontSize = with(LocalDensity.current) { animatedFontSize.toSp() },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = animatedColor,
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .clickable {
                                                selectedSex = sex
                                                showBottomSheet = false
                                            }
                                    )
                                }
                                // Add extra space below the last element (Female)
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            // Center selection rectangle
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .height(48.dp)
                                    .fillMaxWidth()
                                    .background(Color.Transparent)
                                    .border(1.dp, Color.DarkGray.copy(alpha = 0.5f))
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                selectedSex = tempSex
                                showBottomSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9843)),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Confirm",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                "Year of birth" -> {
                    val years = (1950..2025).toList()
                    val initialIndex = years.indexOf(selectedYear.toInt())
                    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
                    var tempYear by remember { mutableIntStateOf(selectedYear.toInt()) }

                    LaunchedEffect(listState.isScrollInProgress) {
                        if (!listState.isScrollInProgress) {
                            val centerItem = listState.firstVisibleItemIndex +
                                    if (listState.firstVisibleItemScrollOffset > 50) 1 else 0
                            tempYear = years.getOrElse(centerItem) { tempYear }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(vertical = 60.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(years.size) { index ->
                                    val year = years[index]
                                    val isSelected = year == tempYear

                                    // Animation on font size
                                    val animatedFontSize by animateDpAsState(
                                        targetValue = if (isSelected) 32.dp else 20.dp,
                                        label = "fontSizeAnimation"
                                    )

                                    // Animation on text color
                                    val animatedColor by animateColorAsState(
                                        targetValue = if (isSelected) Color.White else Color.Gray,
                                        label = "colorAnimation"
                                    )

                                    Text(
                                        text = year.toString(),
                                        fontSize = with(LocalDensity.current) { animatedFontSize.toSp() },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = animatedColor,
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                    )
                                }
                                // Add extra space below the last element (Female)
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            //Center selection rectangle
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .height(48.dp)
                                    .fillMaxWidth()
                                    .background(Color.Transparent)
                                    .border(1.dp, Color.DarkGray.copy(alpha = 0.5f))
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                selectedYear = tempYear.toString()
                                showBottomSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9843)),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Save",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                "Height" -> {
                    val heights = (100..250).toList()
                    val initialIndex = heights.indexOf(selectedHeight)
                    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
                    var tempHeight by remember { mutableIntStateOf(selectedHeight) }

                    LaunchedEffect(listState.isScrollInProgress) {
                        if (!listState.isScrollInProgress) {
                            val centerItem = listState.firstVisibleItemIndex +
                                    if (listState.firstVisibleItemScrollOffset > 50) 1 else 0
                            tempHeight = heights.getOrElse(centerItem) { tempHeight }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(vertical = 60.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(heights.size) { index ->
                                    val height = heights[index]
                                    val isSelected = height == tempHeight

                                    // Animation on font size
                                    val animatedFontSize by animateDpAsState(
                                        targetValue = if (isSelected) 32.dp else 20.dp,
                                        label = "fontSizeAnimation"
                                    )

                                    // Animation on text color
                                    val animatedColor by animateColorAsState(
                                        targetValue = if (isSelected) Color.White else Color.Gray,
                                        label = "colorAnimation"
                                    )

                                    Text(
                                        text = "$height cm",
                                        fontSize = with(LocalDensity.current) { animatedFontSize.toSp() },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = animatedColor,
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                    )
                                }
                                // Add extra space below the last element (Female)
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            // Center selection rectangle
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .height(48.dp)
                                    .fillMaxWidth()
                                    .background(Color.Transparent)
                                    .border(1.dp, Color.DarkGray.copy(alpha = 0.5f))
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                selectedHeight = tempHeight
                                showBottomSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9843)),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Confirm",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skip button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, end = 8.dp)
                .clickable(onClick = onSkip)
        ) {
            Text(
                text = "Skip",
                color = Color(0xFF9E7F66),
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "Your health data",
            color = Color.LightGray,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Fill in your health profile to get personalized recommendations.",
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(50.dp))

        DataField(
            label = "Sex",
            value = selectedSex,
            onEdit = {
                currentField = "Sex"
                showBottomSheet = true
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        DataField(
            label = "Year of birth",
            value = selectedYear,
            onEdit = {
                currentField = "Year of birth"
                showBottomSheet = true
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        DataField(
            label = "Height",
            value = "$selectedHeight cm",
            onEdit = {
                currentField = "Height"
                showBottomSheet = true
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onSave },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = orangeButton)
        ) {
            Text(
                text = "Save",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DataField(label: String, value: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(end = 8.dp)
        )

        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit $label",
                tint = Color.Gray
            )
        }
    }
}

// For viewing the preview in Android Studio
@Preview(showBackground = true)
@Composable
fun HealthDataScreenPreview() {
    HealthDataScreen({},{})
}