package com.example.mealflow

//import com.example.mealflow.ui.screens.createCommunity.FourthStep
import FollowingPage
import UserFollowingPage
import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.model.MealType
import com.example.mealflow.data.repository.MealRepository
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.navigation.BottomNavigationBar
import com.example.mealflow.navigation.NavRoutes
import com.example.mealflow.navigation.NavRoutes.HomePage
import com.example.mealflow.navigation.NavRoutes.PlanConfigScreen
import com.example.mealflow.navigation.NavigationAnimations
import com.example.mealflow.navigation.shouldShowBottomBar
import com.example.mealflow.navigation.shouldShowTopBar
import com.example.mealflow.network.ApiMeal
import com.example.mealflow.network.CommentApiService
import com.example.mealflow.network.Resource
import com.example.mealflow.ui.components.AppState
import com.example.mealflow.ui.components.post.PostDetailsScreen
import com.example.mealflow.ui.screens.AddCustomItemScreen
import com.example.mealflow.ui.screens.AllCommunitiesScreen
import com.example.mealflow.ui.screens.AllMealsPage
import com.example.mealflow.ui.screens.CheckEmailPage
import com.example.mealflow.ui.screens.CommunityHome
import com.example.mealflow.ui.screens.CommunityPage
import com.example.mealflow.ui.screens.CookingModeScreen
import com.example.mealflow.ui.screens.FollowersPage
import com.example.mealflow.ui.screens.ForgetPasswordPage
import com.example.mealflow.ui.screens.HealthDataScreen
import com.example.mealflow.ui.screens.HelpCenterPage
import com.example.mealflow.ui.screens.HomePage
import com.example.mealflow.ui.screens.LoginPage
import com.example.mealflow.ui.screens.MealDetailScreen
import com.example.mealflow.ui.screens.MembersPage
import com.example.mealflow.ui.screens.ModernTopBar
import com.example.mealflow.ui.screens.MultiQuestionScreen
import com.example.mealflow.ui.screens.MyCommunitiesScreen
import com.example.mealflow.ui.screens.OtpPage
import com.example.mealflow.ui.screens.PostCreationPage
import com.example.mealflow.ui.screens.PrivacyPolicyPage
import com.example.mealflow.ui.screens.ProfilePage
import com.example.mealflow.ui.screens.Question
import com.example.mealflow.ui.screens.QuickLoginPage
import com.example.mealflow.ui.screens.RecipeCreationPage
import com.example.mealflow.ui.screens.RegisterPage
import com.example.mealflow.ui.screens.RemoveMembersPage
import com.example.mealflow.ui.screens.ReportBugPage
import com.example.mealflow.ui.screens.ResetPasswordPage
import com.example.mealflow.ui.screens.SearchPage
import com.example.mealflow.ui.screens.SearchResultsScreen
import com.example.mealflow.ui.screens.SetAdminPage
import com.example.mealflow.ui.screens.SettingsPage
import com.example.mealflow.ui.screens.StartPage
import com.example.mealflow.ui.screens.TermsOfServicePage
import com.example.mealflow.ui.screens.UpdateCommunityPage
import com.example.mealflow.ui.screens.UpdateProfileScreen
import com.example.mealflow.ui.screens.UserFollowersPage
import com.example.mealflow.ui.screens.UserPage
import com.example.mealflow.ui.screens.createCommunity.FirstStep
import com.example.mealflow.ui.screens.createCommunity.SecondStep
import com.example.mealflow.ui.screens.createCommunity.ThirdStep
import com.example.mealflow.ui.screens.planner.AllPlansScreen
import com.example.mealflow.ui.screens.planner.DaysSelectionScreen
import com.example.mealflow.ui.screens.planner.PlanConfigScreen
import com.example.mealflow.ui.screens.planner.PlannerPage
import com.example.mealflow.ui.screens.planner.ShoppingFrequencyScreen
import com.example.mealflow.ui.screens.planner.ShoppingListScreen
import com.example.mealflow.ui.screens.setup.SetupBasicInfoScreen
import com.example.mealflow.ui.screens.setup.SetupPhotosScreen
import com.example.mealflow.ui.screens.setup.SetupPhysicalInfoScreen
import com.example.mealflow.ui.screens.setup.SetupProfileViewModel
import com.example.mealflow.ui.screens.setup.SetupWelcomeScreen
import com.example.mealflow.ui.theme.MealFlowTheme
import com.example.mealflow.utils.DateUtils
import com.example.mealflow.utils.rememberHapticFeedback
import com.example.mealflow.viewModel.CommunityMembersViewModel
import com.example.mealflow.viewModel.CreateCommunityViewModel
import com.example.mealflow.viewModel.FeedViewModel
import com.example.mealflow.viewModel.GetAllCommunitiesViewModel
import com.example.mealflow.viewModel.GetUserCommunitiesViewModel
import com.example.mealflow.viewModel.LoginViewModel
import com.example.mealflow.viewModel.MealDetailsViewModel
import com.example.mealflow.viewModel.MealDetailsViewModelFactory
import com.example.mealflow.viewModel.MealPlannerViewModel
import com.example.mealflow.viewModel.MealPlannerViewModelFactory
import com.example.mealflow.viewModel.MealSearchViewModel
import com.example.mealflow.viewModel.MealViewModel
import com.example.mealflow.viewModel.MealViewModelFactory
import com.example.mealflow.viewModel.MyCommunitiesViewModel
import com.example.mealflow.viewModel.PostDropdownViewModel
import com.example.mealflow.viewModel.PostsViewModel
import com.example.mealflow.viewModel.RegisterViewModel
import com.example.mealflow.viewModel.ShoppingListViewModel
import com.example.mealflow.viewModel.ShoppingListViewModelFactory
import com.example.mealflow.viewModel.SingleCommunityViewModel
import com.example.mealflow.viewModel.UpdatePostViewModel
import com.example.mealflow.viewModel.UserPostViewModel
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.min

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val nonSelectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null,
    val route: String
)

class MainActivity : ComponentActivity() {
    private var isFirstLaunch = true
    private lateinit var userPreferencesManager: UserPreferencesManager

    // Add permission launcher for notifications
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
            var keepSplashScreen = true
            splashScreen.setKeepOnScreenCondition { keepSplashScreen }
            Handler(Looper.getMainLooper()).postDelayed({
                keepSplashScreen = false
            }, 1000)
        }
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        userPreferencesManager = UserPreferencesManager(this)
        AppState.setup()

        enableEdgeToEdge()

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    Log.d("MainActivity", "Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Explain why we need the permission
                    Log.d("MainActivity", "Should explain notification permission")
                    // Show explanation dialog if needed
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        Thread.setDefaultUncaughtExceptionHandler { _, exception ->
            exception.printStackTrace()
        }

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                val themeSetting by userPreferencesManager.getThemeSetting().collectAsState(initial = "System")
                val useDarkTheme = when (themeSetting) {
                    "Dark" -> true
                    "Light" -> false
                    else -> isSystemInDarkTheme()
                }

                MealFlowTheme(darkTheme = useDarkTheme) {
                    val navController = rememberNavController()

                    // Handle deep links from password reset
                    LaunchedEffect(intent?.data) {
                        intent?.data?.getQueryParameter("token")?.let { token ->
                            Log.d("MainActivity", "🔹 Token received: $token")
                            navController.navigate("Reset Password Page?token=$token")
                        }
                    }

                    // Handle navigation from shopping trip notifications
                    LaunchedEffect(intent) {
                        val localContext = this@MainActivity // Use activity context directly
                        val navigateTo = intent?.getStringExtra("NAVIGATE_TO")
                        val shoppingDayIndex = intent?.getIntExtra("SHOPPING_DAY_INDEX", -1) ?: -1

                        if (navigateTo == NavRoutes.ShoppingListPage.route && shoppingDayIndex >= 0) {
                            Log.d("MainActivity", "Navigating to shopping list from notification, day index: $shoppingDayIndex")

                            // Clear any existing notifications for this shopping day
                            val notificationManager = localContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancel(2000 + shoppingDayIndex) // Using same ID calculation as in receiver

                            // Navigate to shopping list with clear backstack
                            navController.navigate(navigateTo) {
                                // Clear backstack to avoid navigation issues
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }

                    val viewModel: MealViewModel = koinViewModel()
                    val loginViewModel: LoginViewModel = koinViewModel()

                    LaunchedEffect(Unit) {
                        Log.d("MainActivity", "Triggering initial meal fetch")
                        viewModel.fetchRecommendedMeals()
                    }

                    val loginSuccessful by loginViewModel.loginSuccessful.observeAsState(false)
                    LaunchedEffect(loginSuccessful) {
                        if (loginSuccessful) {
                            Log.d("MainActivity", "Login successful, refreshing meals")
                            viewModel.refreshMeals()
                        }
                    }

                    AppNavHost(navController, viewModel, userPreferencesManager)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ContextCastToActivity", "StateFlowValueCalledInComposition")
    @Composable
    fun AppNavHost(
        navController: NavHostController,
        mealViewModel: MealViewModel = koinViewModel(),
        userPreferencesManager: UserPreferencesManager
    ) {
        val registerViewModel: RegisterViewModel = koinViewModel()
        val createCommunityViewModel: CreateCommunityViewModel = koinViewModel()
        val singleCommunityViewModel: SingleCommunityViewModel = koinViewModel()
        val setupViewModel: SetupProfileViewModel = koinViewModel()
        val getAllCommunitiesViewModel: GetAllCommunitiesViewModel = koinViewModel()
        val myCommunitiesViewModel: MyCommunitiesViewModel = koinViewModel()
        val communityMembers: CommunityMembersViewModel = koinViewModel()
        val communitiesUserViewModel: GetUserCommunitiesViewModel = koinViewModel()
        val postsUserViewModel: UserPostViewModel = koinViewModel()
        val snackbarHostState = remember { SnackbarHostState() }
        val meals by mealViewModel.meals.collectAsState()
        val isLoading by mealViewModel.isLoading.collectAsState()
        val errorMessage by mealViewModel.errorMessage.collectAsState()
        val navigationItems = getNavigationItems()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val shouldShowBottomBar = shouldShowBottomBar(currentRoute, navBackStackEntry)
        val communityid = userPreferencesManager.getCommunityId()

        val postsViewModel: PostsViewModel = koinViewModel()
        val myCommunitiesProfileViewModel: MyCommunitiesViewModel = koinViewModel()
        val mealSearchViewModel: MealSearchViewModel = koinViewModel()
        val postDropdownViewModel: PostDropdownViewModel = koinViewModel()
        val updatePostViewModel: UpdatePostViewModel = koinViewModel()
        val feedViewModel: FeedViewModel = koinViewModel()


        val context = LocalContext.current
        val commentApiService = remember { CommentApiService(context) }

        val shoppingListViewModel: ShoppingListViewModel = koinViewModel()
        val mealPlannerViewModel: MealPlannerViewModel = koinViewModel()

        val tokenManager = TokenManager(context)

        // Use remember so startDestination is calculated only once
        val startDestination = remember {
            val accessToken = tokenManager.getAccessToken()
            val refreshToken = tokenManager.getRefreshToken()

            Log.d("startDestination", "Calculating startDestination - accessToken: ${accessToken != null}, refreshToken: ${refreshToken != null}")

            when {
                accessToken.isNullOrEmpty() && refreshToken.isNullOrEmpty() -> "Start Page"
                accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty() && isFirstLaunch -> "QuickLogin Page"
                else -> "Home Page"
            }
        }

        LaunchedEffect(startDestination) {
            Log.d("startDestination", "LaunchedEffect triggered with destination: $startDestination")
            isFirstLaunch = (startDestination == "QuickLogin Page")
            if (startDestination == "QuickLogin Page") {
                isFirstLaunch = true
            } else {
                isFirstLaunch = false
            }
        }

        val currentIndex = navigationItems.indexOfFirst {
            currentRoute == it.route || (it.route == "Home Page" && currentRoute?.startsWith("meal_detail") == true)
        }.let { index -> if (index < 0) 0 else index }

        var selectedItemIndex by rememberSaveable { mutableStateOf(currentIndex) }

        LaunchedEffect(currentRoute) {
            val index = navigationItems.indexOfFirst {
                currentRoute == it.route || (it.route == "Home Page" && currentRoute?.startsWith("meal_detail") == true)
            }
            selectedItemIndex = if (index >= 0) index else selectedItemIndex
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                if (shouldShowTopBar(currentRoute)) {
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
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .padding(innerPadding)
                    .imePadding()
            ) {
                composable(
                    "Start Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { StartPage(navController) }

                composable(
                    "Login Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { LoginPage(navController) }

                composable(
                    "Register Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { RegisterPage(navController, registerViewModel) }

                composable(
                    route = "Otp Page/{email}",
                    arguments = listOf(navArgument("email") { type = NavType.StringType }),
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email")
                    if (email != null) {
                        OtpPage(
                            navController = navController,
                            registerViewModel = registerViewModel,
                            email = email
                        )
                    } else {
                        Log.e("Navigation", "Email was null in OtpPage route")
                    }
                }

                composable(
                    "Forget Password Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { ForgetPasswordPage(navController) }

                // إضافة هذا في الـ NavGraph الخاص بك

                composable(
                    route = "check_email/{email}",
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    CheckEmailPage(
                        navController = navController,
                        email = email
                    )
                }

                composable(
                    "Profile Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) {
                    ProfilePage(
                        navController = navController,
                        postsViewModel = postsViewModel,
                        communitiesViewModel = myCommunitiesProfileViewModel,
                        mealSearchViewModel = mealSearchViewModel,
                        viewModel = postDropdownViewModel,
                        viewModelUpdate = updatePostViewModel,
                        commentApiService = commentApiService,
                        userPreferencesManager = userPreferencesManager
                    )
                }

                composable(
                    "Update Profile",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) {
                    UpdateProfileScreen(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        context = context,
                    )
                }

                composable(
                    "Update Community",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) {
                    UpdateCommunityPage(
                        navController = navController,
                        snackbarHostState =  snackbarHostState,
                        context = context,
                        communityId = communityid.toString()
                    )
                }

                composable(
                    "User Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { UserPage(
                    communitiesViewModel = communitiesUserViewModel,
                    mealSearchViewModel = mealSearchViewModel,
                    postsViewModel = postsUserViewModel,
                    commentApiService = commentApiService,
                    userPreferencesManager = userPreferencesManager,
                    navController = navController
                ) }

                composable(
                    "QuickLogin Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { QuickLoginPage(context, navController) }

                composable(
                    route = "Reset Password Page?token={token}",
                    arguments = listOf(navArgument("token") { nullable = true }),
                    deepLinks = listOf(navDeepLink {
                        uriPattern = "https://iiacbca.r.bh.d.sendibt3.com/tr/cl?token={token}"
                    }),
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { backStackEntry ->
                    val token = backStackEntry.arguments?.getString("token")
                    ResetPasswordPage(navController, token)
                }

                composable(
                    "Community Home",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) {
                    CommunityHome(
                        navController = navController,
                        feedViewModel = feedViewModel,
                        viewModel = postDropdownViewModel,
                        viewModelUpdate = updatePostViewModel,
                        commentApiService = commentApiService,
                        viewModelGetCommunities = myCommunitiesViewModel
                    )
//                    CommunityHome(navController, myCommunitiesViewModel)
                }

                composable("search_results") {
                    SearchResultsScreen(navController = navController)
                }

                composable("remove_members") {
                    RemoveMembersPage(navController = navController)
                }

                composable(
                    "Community Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { CommunityPage(
                    singleCommunityViewModel,
                    commentApiService = commentApiService,
                    userPreferencesManager = userPreferencesManager,
                    navController = navController
                ) }

                composable(
                    "MyCommunities Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { MyCommunitiesScreen(navController = navController) }

                composable(
                    "AllCommunities Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { AllCommunitiesScreen(navController = navController) }

                composable(
                    route = "post_details/{postId}",
                    arguments = listOf(navArgument("postId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                    val commentApiService = remember { CommentApiService(context) }

                    PostDetailsScreen(
                        postId = postId,
                        navController = navController,
                        commentApiService = commentApiService
                    )
                }

                composable(
                    "FirstStep Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { FirstStep(navController, createCommunityViewModel) }

                composable(
                    "SecondStep Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { SecondStep(navController, createCommunityViewModel) }

                composable(
                    "ThirdStep Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { ThirdStep(navController, createCommunityViewModel) }

//                composable(
//                    "FourthStep Page",
//                    enterTransition = { NavigationAnimations.enterTransition(this) },
//                    exitTransition = { NavigationAnimations.exitTransition(this) },
//                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
//                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
//                ) { FourthStep(navController, createCommunityViewModel) }

                composable(
                    "Members Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { MembersPage(
                    viewModel = communityMembers,
                    navController
                ) }

                composable(
                    "Following Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { FollowingPage(navController) }

                composable(
                    "UserFollowing Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { UserFollowingPage(navController) }

                composable(
                    "Followers Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { FollowersPage(navController) }

                composable(
                    "UserFollowers Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { UserFollowersPage(navController) }

                composable(
                    "SetAdmin Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { SetAdminPage(context,navController) }

                composable("setup_welcome") {
                    SetupWelcomeScreen(navController)
                }

                composable("setup_basic_info") {
                    SetupBasicInfoScreen(navController, setupViewModel)
                }

                composable("setup_physical_info") {
                    SetupPhysicalInfoScreen(navController, setupViewModel)
                }

                composable("setup_photos") {
                    SetupPhotosScreen(navController, snackbarHostState, context, setupViewModel)
                }

                composable(
                    "Questions Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) {
                    val sampleQuestions = listOf(
                        Question(id = 1, question = "What is your favorite type of cuisine?", options = listOf("Italian", "Japanese", "Mexican", "Indian")),
                        Question(id = 2, question = "Which meal do you enjoy the most?", options = listOf("Breakfast", "Lunch", "Dinner", "Snacks")),
                        Question(id = 3, question = "What is your favorite dessert?", options = listOf("Ice Cream", "Cake", "Cookies", "Fruit Salad"))
                    )
                    MultiQuestionScreen(
                        questions = sampleQuestions,
                        onComplete = { answers ->
                            println("Questions completed:")
                            answers.forEach { answer -> println("Question ${answer.questionId}: ${answer.selectedOption}") }
                            navController.navigate("Home Page")
                        },
                        onBack = { println("Going back") }
                    )
                }

                composable(
                    "HealthData Page",
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) {
                    HealthDataScreen(
                        { navController.navigate("Community Page") },
                        { navController.navigate("Community Page") }
                    )
                }

                composable(
                    route = "PostCreationPage?communityId={communityId}",
                    arguments = listOf(navArgument("communityId") { type = NavType.StringType; nullable = true; defaultValue = null }),
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { backStackEntry ->
                    val communityId = backStackEntry.arguments?.getString("communityId")
                    PostCreationPage(navController = navController, snackbarHostState = snackbarHostState, context = context, communityId = communityId)
                }

                composable(
                    route = "RecipeCreationPage?communityId={communityId}",
                    arguments = listOf(navArgument("communityId") { type = NavType.StringType; nullable = true; defaultValue = null }),
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { backStackEntry ->
                    val communityId = backStackEntry.arguments?.getString("communityId")
                    RecipeCreationPage(navController = navController, context = context, communityId = communityId)
                }

                composable(HomePage.route) {
                    var username by remember { mutableStateOf("Loading...") }
                    LaunchedEffect(key1 = Unit) {
                        userPreferencesManager.getUsername().collect { name -> username = if (name == "Unknown") "Sweetie" else name }
                    }
                    HomePage(
                        userName = username,
                        onMealClick = { meal -> navController.navigate(NavRoutes.MealDetailPage.createMealDetailRoute(meal.mealId.toString())) },
                        onPlannedMealClick = { mealIdString -> navController.navigate(NavRoutes.MealDetailPage.createMealDetailRoute(mealIdString)) },
                        mealViewModel = mealViewModel,
                        mealPlannerViewModel = mealPlannerViewModel,
                        navController = navController
                    )
                }
                composable(
                    route = "${NavRoutes.SearchPage.route}?returnToPlanner={returnToPlanner}&date={date}&mealType={mealType}&sourceScreen={sourceScreen}",
                    arguments = listOf(
                        navArgument("returnToPlanner") { type = NavType.BoolType; defaultValue = false },
                        navArgument("date") { type = NavType.StringType; nullable = true },
                        navArgument("mealType") { type = NavType.StringType; nullable = true },
                        navArgument("sourceScreen") { type = NavType.StringType; nullable = true }
                    )
                ) { backStackEntry ->
                    val returnToPlanner = backStackEntry.arguments?.getBoolean("returnToPlanner") ?: false
                    val dateForPlanner = backStackEntry.arguments?.getString("date")
                    val mealTypeForPlannerString = backStackEntry.arguments?.getString("mealType")
                    val mealTypeForPlanner = mealTypeForPlannerString?.let { runCatching { MealType.valueOf(it) }.getOrNull() }
                    val sourceScreen = backStackEntry.arguments?.getString("sourceScreen")
                    SearchPage(
                        onMealClick = { meal ->
                            if (returnToPlanner && dateForPlanner != null && mealTypeForPlanner != null) {
                                navController.previousBackStackEntry?.savedStateHandle?.set("selectedMealId", meal.mealId)
                                navController.previousBackStackEntry?.savedStateHandle?.set("targetDate", dateForPlanner)
                                navController.previousBackStackEntry?.savedStateHandle?.set("targetMealType", mealTypeForPlanner.name)
                                navController.previousBackStackEntry?.savedStateHandle?.set("sourceScreenResult", sourceScreen)
                                navController.popBackStack()
                            } else {
                                navController.navigate(NavRoutes.MealDetailPage.createMealDetailRoute(meal.mealId.toString()))
                            }
                        },
                        mealViewModel = mealViewModel,
                        navController = navController,
                        returnToPlanner = returnToPlanner
                    )
                }
                composable(NavRoutes.PlannerPage.route) { backStackEntry ->
                    ObservePlannerResult(backStackEntry, mealViewModel, mealPlannerViewModel, "PlannerPage")
                    PlannerPage(
                        viewModel = mealPlannerViewModel,
                        mealViewModel = mealViewModel,
                        navController = navController,
                        onNavigateToMealDetails = { mealId -> navController.navigate(NavRoutes.MealDetailPage.createMealDetailRoute(mealId)) },
                        onPlanMealsClick = {
                            val currentDateInPlanner = mealPlannerViewModel.currentDate.value
                            val targetMealType = MealType.BREAKFAST // Default or determined by UI
                            navController.navigate("${NavRoutes.SearchPage.route}?returnToPlanner=true&date=${DateUtils.formatToISO(currentDateInPlanner)}&mealType=${targetMealType.name}&sourceScreen=PlannerPage")
                        },
                        onSeeAllPlansClick = { navController.navigate(NavRoutes.AllPlansCalendarViewScreen.route) }
                    )
                }
                composable(NavRoutes.AllPlansCalendarViewScreen.route) { backStackEntry ->
                    ObservePlannerResult(backStackEntry, mealViewModel, mealPlannerViewModel, "AllPlansScreen")
                    AllPlansScreen(
                        viewModel = mealPlannerViewModel,
                        navController = navController,
                        onNavigateBack = { navController.popBackStack() },
                        onAddMealToDate = { date, mealType ->
                            navController.navigate("${NavRoutes.SearchPage.route}?returnToPlanner=true&date=$date&mealType=${mealType.name}&sourceScreen=AllPlansScreen")
                        }
                    )
                }
                composable(NavRoutes.DaysSelectionScreen.route) {
                    DaysSelectionScreen(
                        onDaysSelected = { daysCount ->
                            mealPlannerViewModel.clearNewPlan()
                            navController.navigate(PlanConfigScreen.createRoute(daysCount))
                        },
                        onNavigateBack = {
                            mealPlannerViewModel.finalizeAndClearNewPlanBuilder()
                            navController.popBackStack()
                        },
                        startDate = mealPlannerViewModel.currentDate.value
                    )
                }
                composable(
                    route = PlanConfigScreen.route,
                    arguments = listOf(navArgument("daysCount") { type = NavType.IntType })
                ) { backStackEntry ->
                    ObservePlannerResult(backStackEntry, mealViewModel, mealPlannerViewModel, "PlanConfigScreen")
                    val daysCount = backStackEntry.arguments?.getInt("daysCount") ?: 1
                    PlanConfigScreen(
                        daysCount = daysCount,
                        viewModel = mealPlannerViewModel,
                        onNavigateBack = {
                            mealPlannerViewModel.finalizeAndClearNewPlanBuilder()
                            navController.popBackStack()
                        },
                        onSavePlanClick = {
                            val planStartDate = mealPlannerViewModel.newPlanMeals.value.minOfOrNull { it.date }
                            val planEndDate = mealPlannerViewModel.newPlanMeals.value.maxOfOrNull { it.date }
                            if (planStartDate != null && planEndDate != null) {
                                navController.navigate("${NavRoutes.ShoppingFrequencyScreen.route}?planStartDate=$planStartDate&planEndDate=$planEndDate")
                            } else {
                                Log.e("AppNavHost", "PlanConfig: Start or End date is null.")
                                navController.popBackStack(NavRoutes.PlannerPage.route, inclusive = false)
                            }
                        },
                        onAddMealToDay = { date, mealType ->
                            navController.navigate("${NavRoutes.SearchPage.route}?returnToPlanner=true&date=$date&mealType=${mealType.name}&sourceScreen=PlanConfigScreen")
                        }
                    )
                }
                composable(
                    route = "${NavRoutes.ShoppingFrequencyScreen.route}?planStartDate={planStartDate}&planEndDate={planEndDate}",
                    arguments = listOf(
                        navArgument("planStartDate") { type = NavType.StringType },
                        navArgument("planEndDate") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val planStartDateStr = backStackEntry.arguments?.getString("planStartDate")
                    val planEndDateStr = backStackEntry.arguments?.getString("planEndDate")
                    if (planStartDateStr == null || planEndDateStr == null) {
                        Log.e("AppNavHost", "ShoppingFrequencyScreen: Missing date arguments.")
                        navController.popBackStack()
                        return@composable
                    }
                    val planStartDate = DateUtils.parseLocalDate(planStartDateStr) ?: LocalDate.now()
                    val planEndDate = DateUtils.parseLocalDate(planEndDateStr) ?: LocalDate.now().plusDays(6)
                    ShoppingFrequencyScreen(
                        onFrequencySelected = { frequency ->
                            // Pass the selected days directly to the ShoppingListViewModel
                            val shoppingDays = backStackEntry.savedStateHandle.get<List<LocalDate>>("selectedShoppingDays")
                                ?: calculateShoppingDays(planStartDate, ChronoUnit.DAYS.between(planStartDate, planEndDate).toInt() + 1, frequency)

                            // Generate shopping list with improved consolidation
                            shoppingListViewModel.createNewShoppingList(planStartDate, planEndDate, frequency, shoppingDays)
                            mealPlannerViewModel.finalizeAndClearNewPlanBuilder()
                            navController.navigate(NavRoutes.ShoppingListPage.route) { popUpTo(NavRoutes.PlannerPage.route) { inclusive = false } }
                        },
                        onNavigateBack = { navController.popBackStack() },
                        planStartDate = planStartDate,
                        planEndDate = planEndDate,
                        navController = navController
                    )
                }
                composable(NavRoutes.ShoppingListPage.route) {
                    ShoppingListScreen(
                        onNavigateToAddCustomItem = { navController.navigate(NavRoutes.AddCustomItemScreen.route) },
                        viewModel = shoppingListViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        navController = navController
                    )
                }
                composable(NavRoutes.AddCustomItemScreen.route) {
                    AddCustomItemScreen(
                        viewModel = shoppingListViewModel,
                        onAddClickAndNavigateBack = { navController.popBackStack() },
                        onCancelClick = {
                            shoppingListViewModel.prepareToAddCustomItem()
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    route = "meal_detail/{mealId}",
                    arguments = listOf(navArgument("mealId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val mealId = backStackEntry.arguments?.getString("mealId") ?: ""
                    // Try to get the meal from the ViewModel's cache first
                    val cachedMeal = mealViewModel.findMealById(mealId)
                    val mealDetailsViewModel: MealDetailsViewModel = koinViewModel(
                        parameters = { parametersOf(mealId, cachedMeal) }
                    )
                    val mealDetailsState by mealDetailsViewModel.mealDetails.collectAsState()
                    val isPlanned = mealPlannerViewModel.allPlannedMealsData.value.plannedMeals.any { it.meal.mealId == mealId }

                    when (val resource = mealDetailsState) {
                        is Resource.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is Resource.Success -> {
                            resource.data?.let { meal ->
                                MealDetailScreen(
                                    meal = meal,
                                    onNavigateBack = { navController.popBackStack() },
                                    navController = navController,
                                    mealViewModel = mealViewModel,
                                    isPlanned = isPlanned
                                )
                            }
                        }
                        is Resource.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Meal not found", style = MaterialTheme.typography.headlineMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(resource.message ?: "Unknown error", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TextButton(onClick = { navController.popBackStack() }) {
                                        Text("Go Back")
                                    }
                                }
                            }
                        }
                    }
                }
                composable(
                    route = "all_meals/{title}",
                    arguments = listOf(navArgument("title") { type = NavType.StringType }),
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { backStackEntry ->
                    val title = backStackEntry.arguments?.getString("title") ?: "Meals"
                    AllMealsPage(
                        title = title,
                        onMealClick = { meal -> navController.navigate(NavRoutes.MealDetailPage.createMealDetailRoute(meal.mealId.toString())) },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.SettingsPage.route) {
                    SettingsPage(navController = navController)
                }

                composable("privacy_policy") { PrivacyPolicyPage(navController) }
                composable("terms_of_service") { TermsOfServicePage(navController) }
                composable("help_center") { HelpCenterPage(navController) }
                composable("report_bug") { ReportBugPage(navController) }

                composable(
                    route = "cooking_mode/{mealJson}",
                    arguments = listOf(navArgument("mealJson") { type = NavType.StringType }),
                    enterTransition = { NavigationAnimations.enterTransition(this) },
                    exitTransition = { NavigationAnimations.exitTransition(this) },
                    popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
                    popExitTransition = { NavigationAnimations.popExitTransition(this) }
                ) { backStackEntry ->
                    val mealJson = backStackEntry.arguments?.getString("mealJson")
                    val hapticUtil = rememberHapticFeedback() // Get haptic utility here

                    if (mealJson != null) {
                        val meal = try {
                            val decodedMealJson = URLDecoder.decode(mealJson, StandardCharsets.UTF_8.toString())
                            Gson().fromJson(decodedMealJson, Meal::class.java)
                        } catch (e: Exception) {
                            Log.e("AppNavHost", "Error deserializing/decoding meal for CookingModeScreen", e)
                            null
                        }

                        if (meal != null) {
                            CookingModeScreen(
                                meal = meal,
                                onNavigateBack = { navController.popBackStack() },
                                haptic = hapticUtil
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Error loading cooking mode. Please try again.", style = MaterialTheme.typography.bodyLarge)
                            }
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(3000) // Show error for 3 seconds
                                navController.popBackStack()
                            }
                        }
                    } else {
                        Log.e("AppNavHost", "mealJson is null for CookingModeScreen route")
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error: Meal data not found.", style = MaterialTheme.typography.bodyLarge)
                        }
                        LaunchedEffect(Unit) { // Auto navigate back if data is missing
                            kotlinx.coroutines.delay(3000)
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }

    fun getNavigationItems(): List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(title = "Home", selectedIcon = Icons.Filled.Home, nonSelectedIcon = Icons.Outlined.Home, hasNews = false, route = HomePage.route),
            BottomNavigationItem(title = "Shopping", selectedIcon = Icons.Filled.ShoppingCart, nonSelectedIcon = Icons.Outlined.ShoppingCart, hasNews = false, route = NavRoutes.ShoppingListPage.route),
            BottomNavigationItem(title = "Search", selectedIcon = Icons.Filled.Search, nonSelectedIcon = Icons.Outlined.Search, hasNews = false, route = NavRoutes.SearchPage.route),
            BottomNavigationItem(title = "Community", selectedIcon = Icons.Filled.People, nonSelectedIcon = Icons.Outlined.PeopleOutline, hasNews = false, route = NavRoutes.CommunityHome.route),
            BottomNavigationItem(title = "Planner", selectedIcon = Icons.Filled.CalendarToday, nonSelectedIcon = Icons.Outlined.CalendarToday, hasNews = false, route = NavRoutes.PlannerPage.route)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ObservePlannerResult(
    backStackEntry: NavBackStackEntry,
    mealViewModel: MealViewModel,
    mealPlannerViewModel: MealPlannerViewModel,
    currentScreenIdentifier: String
) {
    val selectedMealId by backStackEntry.savedStateHandle.getStateFlow<String?>("selectedMealId", null).collectAsState()
    val targetDate by backStackEntry.savedStateHandle.getStateFlow<String?>("targetDate", null).collectAsState()
    val targetMealTypeName by backStackEntry.savedStateHandle.getStateFlow<String?>("targetMealType", null).collectAsState()
    val resultSourceScreen by backStackEntry.savedStateHandle.getStateFlow<String?>("sourceScreenResult", null).collectAsState()

    LaunchedEffect(selectedMealId, targetDate, targetMealTypeName, resultSourceScreen) {
        if (resultSourceScreen == currentScreenIdentifier &&
            selectedMealId != null && targetDate != null && targetMealTypeName != null) {
            Log.d("ObservePlannerResult", "Result for $currentScreenIdentifier: MealId=$selectedMealId, Date=$targetDate, Type=$targetMealTypeName")
            val mealTypeForResult = runCatching { MealType.valueOf(targetMealTypeName!!) }.getOrNull()

            // Show loading indicator or toast to let user know the meal is being added
            val dateForMeal = DateUtils.parseLocalDate(targetDate!!)

            // Use the optimized fetchMealById function
            mealViewModel.fetchMealById(selectedMealId) { fetchedMeal ->
                if (fetchedMeal != null) {
                    processMealForPlanner(fetchedMeal, mealTypeForResult, targetDate!!, dateForMeal, currentScreenIdentifier, backStackEntry, mealPlannerViewModel)
                } else {
                    Log.e("ObservePlannerResult", "Failed to fetch meal with ID: $selectedMealId")
                }
            }

            // Clear the state handle is done after processing
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun processMealForPlanner(
    meal: Meal,
    mealType: MealType?,
    targetDateString: String,
    dateForMeal: LocalDate?,
    currentScreenIdentifier: String,
    backStackEntry: NavBackStackEntry,
    mealPlannerViewModel: MealPlannerViewModel
) {
    if (mealType != null && dateForMeal != null) {
        when (currentScreenIdentifier) {
            "PlanConfigScreen" -> {
                Log.d("ObservePlannerResult", "Adding to new plan for PlanConfigScreen")
                mealPlannerViewModel.addMealToNewPlan(targetDateString, meal, mealType)
            }
            "PlannerPage" -> {
                Log.d("ObservePlannerResult", "Setting up for AddToPlanDialog for PlannerPage")
                backStackEntry.savedStateHandle["mealIdForDialog"] = meal.mealId
                backStackEntry.savedStateHandle["initialDateForDialog"] = targetDateString
            }
            "AllPlansScreen" -> {
                Log.d("ObservePlannerResult", "Adding to storage for $currentScreenIdentifier")
                mealPlannerViewModel.addSingleMealToStorage(meal, dateForMeal, mealType)
            }
        }
    } else {
        Log.e("ObservePlannerResult", "MealType or date was null. MealType: $mealType, Date: $dateForMeal")
    }

            backStackEntry.savedStateHandle.remove<String?>("selectedMealId")
            backStackEntry.savedStateHandle.remove<String?>("targetDate")
            backStackEntry.savedStateHandle.remove<String?>("targetMealType")
            backStackEntry.savedStateHandle.remove<String?>("sourceScreenResult")
        }

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateShoppingDays(planStartDate: LocalDate, planDurationDays: Int, frequency: Int): List<LocalDate> {
    return when (frequency) {
        0 -> listOf(planStartDate) // Once at start
        1 -> { // Weekly
            val weeks = (planDurationDays / 7) + 1
            (0 until min(weeks, 3)).map { planStartDate.plusDays(it * 7L) }
        }
        2 -> { // Bi-weekly
            val biWeeks = (planDurationDays / 14) + 1
            (0 until min(biWeeks, 2)).map { planStartDate.plusDays(it * 14L) }
        }
        else -> listOf(planStartDate)
    }
}
