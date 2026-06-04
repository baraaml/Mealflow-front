package com.example.mealflow.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mealflow.navigation.Destination
import kotlinx.coroutines.launch

@Composable
fun TelegramStyleDrawer(
    isOpen: Boolean,
    onOpenChange: (Boolean) -> Unit,
    navController: NavController,
    content: @Composable () -> Unit
) {
    val drawerWidth = 280.dp
    val drawerWidthPx = with(LocalDensity.current) { drawerWidth.toPx() }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Simple animatable for drawer position
    val drawerOffset = remember { Animatable(if (isOpen) 0f else -drawerWidthPx) }

    // Animate drawer when isOpen changes
    LaunchedEffect(isOpen) {
        drawerOffset.animateTo(
            targetValue = if (isOpen) 0f else -drawerWidthPx,
            animationSpec = spring(
                dampingRatio = 0.7f, // More bounce
                stiffness = Spring.StiffnessLow // Slower, bouncier
            )
        )
    }

    // Calculate how open the drawer is (0 to 1)
    val openProgress by remember {
        derivedStateOf {
            ((drawerOffset.value + drawerWidthPx) / drawerWidthPx).coerceIn(0f, 1f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content with simple parallax
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = openProgress * 60f // Simple shift
                    scaleX = 1f - (openProgress * 0.05f) // Subtle scale
                    scaleY = 1f - (openProgress * 0.05f)
                }
                .pointerInput(drawerWidthPx) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            coroutineScope.launch {
                                val newOffset = (drawerOffset.value + dragAmount)
                                    .coerceIn(-drawerWidthPx, 0f)
                                drawerOffset.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                val shouldOpen = drawerOffset.value > -drawerWidthPx * 0.5f
                                drawerOffset.animateTo(
                                    targetValue = if (shouldOpen) 0f else -drawerWidthPx,
                                    animationSpec = spring(
                                        dampingRatio = 0.7f,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                onOpenChange(shouldOpen)
                            }
                        }
                    )
                }
        ) {
            content()
        }

        // Simple scrim
        if (openProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = openProgress * 0.3f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onOpenChange(false)
                    }
            )
        }

        // Drawer
        if (openProgress > 0f) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(drawerWidth)
                    .graphicsLayer { translationX = drawerOffset.value }
                    .shadow(8.dp, RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Simple header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(12.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "MealFlow",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Smart Meal Planner",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Navigation items
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SimpleDrawerItem(
                            icon = Icons.Default.Home,
                            text = "Home",
                            isSelected = currentDestination?.hasRoute<Destination.Home>() == true,
                            onClick = {
                                navigateAndClose(navController, Destination.Home, onOpenChange, haptic)
                            }
                        )
                        SimpleDrawerItem(
                            icon = Icons.Default.CalendarMonth,
                            text = "Meal Planner",
                            isSelected = currentDestination?.hasRoute<Destination.Planner>() == true,
                            onClick = {
                                navigateAndClose(navController, Destination.Planner, onOpenChange, haptic)
                            }
                        )
                        SimpleDrawerItem(
                            icon = Icons.Default.Search,
                            text = "Search Meals",
                            isSelected = currentDestination?.hasRoute<Destination.Search>() == true,
                            onClick = {
                                navigateAndClose(navController, Destination.Search(), onOpenChange, haptic)
                            }
                        )
                        SimpleDrawerItem(
                            icon = Icons.Default.ShoppingCart,
                            text = "Shopping List",
                            isSelected = currentDestination?.hasRoute<Destination.ShoppingList>() == true,
                            onClick = {
                                navigateAndClose(navController, Destination.ShoppingList, onOpenChange, haptic)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.2f))
                        Spacer(modifier = Modifier.height(8.dp))

                        SimpleDrawerItem(
                            icon = Icons.Default.Settings,
                            text = "Settings",
                            isSelected = currentDestination?.hasRoute<Destination.Settings>() == true,
                            onClick = {
                                navigateAndClose(navController, Destination.Settings, onOpenChange, haptic)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleDrawerItem(
    icon: ImageVector,
    text: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        },
        animationSpec = spring(dampingRatio = 0.8f),
        label = "background"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = spring(dampingRatio = 0.8f),
        label = "textColor"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

private fun navigateAndClose(
    navController: NavController,
    destination: Any,
    onOpenChange: (Boolean) -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    navController.navigate(destination) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
    onOpenChange(false)
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
}