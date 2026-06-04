package com.example.mealflow.navigation

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.toRoute
import com.example.mealflow.navigation.Destination

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val nonSelectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null,
    val route: Destination
)

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    navigationItems: List<BottomNavigationItem>,
    selectedItemIndex: Int,
    onItemSelected: (Int) -> Unit,
    context: Context
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        navigationItems.forEachIndexed { index, item ->
            val selected = selectedItemIndex == index
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.2f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "scale"
            )

            val iconColor by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                },
                animationSpec = tween(300),
                label = "iconColor"
            )

            NavigationBarItem(
                selected = selected,
                onClick = {
                    try {
                        onItemSelected(index)
                        Log.d("Navigation", "Navigating to ${item.route}")
                        
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } catch (e: Exception) {
                        Log.e("Navigation", "Error navigating to screen", e)
                        Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = iconColor,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                alwaysShowLabel = true,
                icon = {
                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BadgedBox(badge = {}) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.nonSelectedIcon,
                                contentDescription = item.title,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            )
        }
    }
}

fun shouldShowBottomBar(navBackStackEntry: NavBackStackEntry?): Boolean {
    val destination = navBackStackEntry?.destination ?: return false

    // Main screens that show the bottom bar
    val isMainScreen = destination.hasRoute<Destination.Home>() ||
            destination.hasRoute<Destination.Planner>() ||
            destination.hasRoute<Destination.ShoppingList>() ||
            destination.hasRoute<Destination.CommunityHome>() ||
            destination.hasRoute<Destination.Profile>()

    // Handle Search separately due to returnToPlanner logic
    if (destination.hasRoute<Destination.Search>()) {
        val returnToPlanner = try {
            val search: Destination.Search = navBackStackEntry.toRoute()
            search.returnToPlanner
        } catch (e: Exception) {
            false
        }
        return !returnToPlanner
    }

    return isMainScreen
}

fun shouldShowTopBar(navBackStackEntry: NavBackStackEntry?): Boolean {
    val destination = navBackStackEntry?.destination ?: return false
    
    // Screens with top bar
    return destination.hasRoute<Destination.Home>() ||
            destination.hasRoute<Destination.CommunityHome>()
}
