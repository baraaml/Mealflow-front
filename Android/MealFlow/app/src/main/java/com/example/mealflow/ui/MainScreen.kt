package com.example.mealflow.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.navigation.AppNavHost
import com.example.mealflow.navigation.BottomNavigationBar
import com.example.mealflow.navigation.BottomNavigationItem
import com.example.mealflow.navigation.Destination
import com.example.mealflow.navigation.shouldShowBottomBar
import com.example.mealflow.navigation.shouldShowTopBar
import com.example.mealflow.ui.screens.ModernTopBar
import com.example.mealflow.viewModel.MealViewModel
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    mealViewModel: MealViewModel,
    userPreferencesManager: UserPreferencesManager
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val shouldShowBottomBar = shouldShowBottomBar(navBackStackEntry)
    val shouldShowTopBar = shouldShowTopBar(navBackStackEntry)
    
    val navigationItems = listOf(
        BottomNavigationItem(title = "Home", selectedIcon = Icons.Filled.Home, nonSelectedIcon = Icons.Outlined.Home, hasNews = false, route = Destination.Home),
        BottomNavigationItem(title = "Shopping", selectedIcon = Icons.Filled.ShoppingCart, nonSelectedIcon = Icons.Outlined.ShoppingCart, hasNews = false, route = Destination.ShoppingList),
        BottomNavigationItem(title = "Search", selectedIcon = Icons.Filled.Search, nonSelectedIcon = Icons.Outlined.Search, hasNews = false, route = Destination.Search()),
        BottomNavigationItem(title = "Community", selectedIcon = Icons.Filled.People, nonSelectedIcon = Icons.Outlined.PeopleOutline, hasNews = false, route = Destination.CommunityHome),
        BottomNavigationItem(title = "Planner", selectedIcon = Icons.Filled.CalendarToday, nonSelectedIcon = Icons.Outlined.CalendarToday, hasNews = false, route = Destination.Planner)
    )

    val currentIndex = navigationItems.indexOfFirst { item ->
        val destination = navBackStackEntry?.destination
        when (item.route) {
            is Destination.Home -> destination?.hasRoute<Destination.Home>() == true || destination?.hasRoute<Destination.MealDetail>() == true
            is Destination.Search -> destination?.hasRoute<Destination.Search>() == true
            is Destination.ShoppingList -> destination?.hasRoute<Destination.ShoppingList>() == true
            is Destination.CommunityHome -> destination?.hasRoute<Destination.CommunityHome>() == true
            is Destination.Planner -> destination?.hasRoute<Destination.Planner>() == true
            else -> false
        }
    }.let { index -> if (index < 0) 0 else index }

    var selectedItemIndex by rememberSaveable { mutableStateOf(currentIndex) }

    LaunchedEffect(navBackStackEntry) {
        val index = navigationItems.indexOfFirst { item ->
            val destination = navBackStackEntry?.destination
            when (item.route) {
                is Destination.Home -> destination?.hasRoute<Destination.Home>() == true || destination?.hasRoute<Destination.MealDetail>() == true
                is Destination.Search -> destination?.hasRoute<Destination.Search>() == true
                is Destination.ShoppingList -> destination?.hasRoute<Destination.ShoppingList>() == true
                is Destination.CommunityHome -> destination?.hasRoute<Destination.CommunityHome>() == true
                is Destination.Planner -> destination?.hasRoute<Destination.Planner>() == true
                else -> false
            }
        }
        selectedItemIndex = if (index >= 0) index else selectedItemIndex
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            if (shouldShowTopBar) {
                ModernTopBar(navController = navController)
            }
        },
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    navigationItems = navigationItems,
                    selectedItemIndex = selectedItemIndex,
                    onItemSelected = { selectedItemIndex = it },
                    context = context
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            mealViewModel = mealViewModel,
            userPreferencesManager = userPreferencesManager,
            snackbarHostState = snackbarHostState,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        )
    }
}
