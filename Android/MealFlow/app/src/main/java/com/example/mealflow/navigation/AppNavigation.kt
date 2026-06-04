package com.example.mealflow.navigation

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.example.mealflow.data.model.Meal
import com.example.mealflow.data.model.MealType
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import com.example.mealflow.network.CommentApiService
import com.example.mealflow.network.Resource
import com.example.mealflow.ui.components.post.PostDetailsScreen
import com.example.mealflow.ui.screens.AddCustomItemScreen
import com.example.mealflow.ui.screens.AllCommunitiesScreen
import com.example.mealflow.ui.screens.AllMealsPage
import com.example.mealflow.ui.screens.CheckEmailPage
import com.example.mealflow.ui.screens.CommunityHome
import com.example.mealflow.ui.screens.CommunityPage
import com.example.mealflow.ui.screens.CookingModeScreen
import com.example.mealflow.ui.screens.FollowersPage
import com.example.mealflow.ui.screens.FollowingPage
import com.example.mealflow.ui.screens.ForgetPasswordPage
import com.example.mealflow.ui.screens.HealthDataScreen
import com.example.mealflow.ui.screens.HelpCenterPage
import com.example.mealflow.ui.screens.HomePage
import com.example.mealflow.ui.screens.LoginPage
import com.example.mealflow.ui.screens.MealDetailScreen
import com.example.mealflow.ui.screens.MembersPage
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
import com.example.mealflow.ui.screens.SearchResultsScreen
import com.example.mealflow.ui.screens.SetAdminPage
import com.example.mealflow.ui.screens.SettingsPage
import com.example.mealflow.ui.screens.StartPage
import com.example.mealflow.ui.screens.TermsOfServicePage
import com.example.mealflow.ui.screens.UpdateCommunityPage
import com.example.mealflow.ui.screens.UpdateProfileScreen
import com.example.mealflow.ui.screens.UserFollowersPage
import com.example.mealflow.ui.screens.UserFollowingPage
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
import com.example.mealflow.ui.screens.search.SearchPage
import com.example.mealflow.ui.screens.setup.SetupBasicInfoScreen
import com.example.mealflow.ui.screens.setup.SetupPhotosScreen
import com.example.mealflow.ui.screens.setup.SetupPhysicalInfoScreen
import com.example.mealflow.ui.screens.setup.SetupProfileViewModel
import com.example.mealflow.ui.screens.setup.SetupWelcomeScreen
import com.example.mealflow.utils.DateUtils
import com.example.mealflow.utils.ObservePlannerResult
import com.example.mealflow.utils.calculateShoppingDays
import com.example.mealflow.utils.rememberHapticFeedback
import com.example.mealflow.viewModel.CommunityMembersViewModel
import com.example.mealflow.viewModel.CreateCommunityViewModel
import com.example.mealflow.viewModel.FeedViewModel
import com.example.mealflow.viewModel.GetUserCommunitiesViewModel
import com.example.mealflow.viewModel.MealDetailsViewModel
import com.example.mealflow.viewModel.MealPlannerViewModel
import com.example.mealflow.viewModel.MealSearchViewModel
import com.example.mealflow.viewModel.MealViewModel
import com.example.mealflow.viewModel.MyCommunitiesViewModel
import com.example.mealflow.viewModel.PostDropdownViewModel
import com.example.mealflow.viewModel.PostsViewModel
import com.example.mealflow.viewModel.RegisterViewModel
import com.example.mealflow.viewModel.ShoppingListViewModel
import com.example.mealflow.viewModel.SingleCommunityViewModel
import com.example.mealflow.viewModel.UpdatePostViewModel
import com.example.mealflow.viewModel.UserPostViewModel
import com.google.gson.Gson
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("ContextCastToActivity", "StateFlowValueCalledInComposition")
@Composable
fun AppNavHost(
    navController: NavHostController,
    mealViewModel: MealViewModel,
    userPreferencesManager: UserPreferencesManager,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val registerViewModel: RegisterViewModel = koinViewModel()
    val createCommunityViewModel: CreateCommunityViewModel = koinViewModel()
    val singleCommunityViewModel: SingleCommunityViewModel = koinViewModel()
    val setupViewModel: SetupProfileViewModel = koinViewModel()
    val myCommunitiesViewModel: MyCommunitiesViewModel = koinViewModel()
    val communityMembers: CommunityMembersViewModel = koinViewModel()
    val communitiesUserViewModel: GetUserCommunitiesViewModel = koinViewModel()
    val postsUserViewModel: UserPostViewModel = koinViewModel()
    val communityIdState by userPreferencesManager.getCommunityId().collectAsState(initial = null)

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

    val startDestination: Any = remember {
        val accessToken = tokenManager.getAccessToken()
        val refreshToken = tokenManager.getRefreshToken()
        when {
            accessToken.isNullOrEmpty() && refreshToken.isNullOrEmpty() -> Destination.Start
            accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty() -> Destination.QuickLogin
            else -> Destination.Home
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Destination.Start>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { StartPage(navController) }

        composable<Destination.Login>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { LoginPage(navController) }

        composable<Destination.Register>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { RegisterPage(navController, registerViewModel) }

        composable<Destination.Otp>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { backStackEntry ->
            val otp: Destination.Otp = backStackEntry.toRoute()
            OtpPage(navController = navController, registerViewModel = registerViewModel, email = otp.email)
        }

        composable<Destination.ForgetPassword>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { ForgetPasswordPage(navController) }

        composable<Destination.CheckEmail> { backStackEntry ->
            val checkEmail: Destination.CheckEmail = backStackEntry.toRoute()
            CheckEmailPage(navController = navController, email = checkEmail.email)
        }

        composable<Destination.Profile>(
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

        composable<Destination.UpdateProfile>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { UpdateProfileScreen(navController = navController, snackbarHostState = snackbarHostState, context = context) }

        composable<Destination.UpdateCommunity>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { UpdateCommunityPage(navController = navController, snackbarHostState =  snackbarHostState, context = context, communityId = communityIdState) }

        composable<Destination.User>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { UserPage(communitiesViewModel = communitiesUserViewModel, mealSearchViewModel = mealSearchViewModel, postsViewModel = postsUserViewModel, commentApiService = commentApiService, userPreferencesManager = userPreferencesManager, navController = navController) }

        composable<Destination.QuickLogin>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { QuickLoginPage(context, navController) }

        composable<Destination.ResetPassword>(
            deepLinks = listOf(navDeepLink { uriPattern = "https://iiacbca.r.bh.d.sendibt3.com/tr/cl?token={token}" }),
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { backStackEntry ->
            val reset: Destination.ResetPassword = backStackEntry.toRoute()
            ResetPasswordPage(navController, reset.token)
        }

        composable<Destination.CommunityHome>(
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
        }

        composable<Destination.SearchResults> { SearchResultsScreen(navController = navController) }

        composable<Destination.RemoveMembers> { RemoveMembersPage(navController = navController) }

        composable<Destination.CommunityPage>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { CommunityPage(singleCommunityViewModel, commentApiService = commentApiService, userPreferencesManager = userPreferencesManager, navController = navController) }

        composable<Destination.MyCommunities>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { MyCommunitiesScreen(navController = navController) }

        composable<Destination.AllCommunities>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { AllCommunitiesScreen(navController = navController) }

        composable<Destination.PostDetails> { backStackEntry ->
            val details: Destination.PostDetails = backStackEntry.toRoute()
            val commentApiService = remember { CommentApiService(context) }
            PostDetailsScreen(postId = details.postId, navController = navController, commentApiService = commentApiService)
        }

        composable<Destination.FirstStep>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { FirstStep(navController, createCommunityViewModel) }

        composable<Destination.SecondStep>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { SecondStep(navController, createCommunityViewModel) }

        composable<Destination.ThirdStep>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { ThirdStep(navController, createCommunityViewModel) }

        composable<Destination.Members>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { MembersPage(viewModel = communityMembers, navController) }

        composable<Destination.Following>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { FollowingPage(navController) }

        composable<Destination.UserFollowing>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { UserFollowingPage(navController) }

        composable<Destination.Followers>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { FollowersPage(navController) }

        composable<Destination.UserFollowers>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { UserFollowersPage(navController) }

        composable<Destination.SetAdmin>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { SetAdminPage(context,navController) }

        composable<Destination.SetupWelcome> { SetupWelcomeScreen(navController) }
        composable<Destination.SetupBasicInfo> { SetupBasicInfoScreen(navController, setupViewModel) }
        composable<Destination.SetupPhysicalInfo> { SetupPhysicalInfoScreen(navController, setupViewModel) }
        composable<Destination.SetupPhotos> { SetupPhotosScreen(navController, snackbarHostState, context, setupViewModel) }

        composable<Destination.Questions>(
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
            MultiQuestionScreen(questions = sampleQuestions, onComplete = { answers -> navController.navigate(Destination.Home) }, onBack = { navController.popBackStack() })
        }

        composable<Destination.HealthData>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { HealthDataScreen({ navController.navigate(Destination.CommunityHome) }, { navController.navigate(Destination.CommunityHome) }) }

        composable<Destination.PostCreation>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { backStackEntry ->
            val creation: Destination.PostCreation = backStackEntry.toRoute()
            PostCreationPage(navController = navController, snackbarHostState = snackbarHostState, context = context, communityId = creation.communityId)
        }

        composable<Destination.RecipeCreation>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { backStackEntry ->
            val creation: Destination.RecipeCreation = backStackEntry.toRoute()
            RecipeCreationPage(navController = navController, context = context, communityId = creation.communityId)
        }

        composable<Destination.Home> {
            var username by remember { mutableStateOf("Loading...") }
            LaunchedEffect(key1 = Unit) {
                userPreferencesManager.getUsername().collect { name -> username = if (name == "Unknown") "Sweetie" else name }
            }
            HomePage(userName = username, onMealClick = { meal -> navController.navigate(Destination.MealDetail(meal.mealId)) }, onPlannedMealClick = { mealIdString -> navController.navigate(Destination.MealDetail(mealIdString)) }, mealViewModel = mealViewModel, mealPlannerViewModel = mealPlannerViewModel, navController = navController)
        }

        composable<Destination.Search> { backStackEntry ->
            val search: Destination.Search = backStackEntry.toRoute()
            SearchPage(
                onMealClick = { meal ->
                    if (search.returnToPlanner && search.date != null && search.mealType != null) {
                        navController.previousBackStackEntry?.savedStateHandle?.set("selectedMealId", meal.mealId)
                        navController.previousBackStackEntry?.savedStateHandle?.set("targetDate", search.date)
                        navController.previousBackStackEntry?.savedStateHandle?.set("targetMealType", search.mealType)
                        navController.previousBackStackEntry?.savedStateHandle?.set("sourceScreenResult", search.sourceScreen)
                        navController.popBackStack()
                    } else {
                        navController.navigate(Destination.MealDetail(meal.mealId))
                    }
                },
                mealViewModel = mealViewModel,
                navController = navController,
                returnToPlanner = search.returnToPlanner
            )
        }

        composable<Destination.Planner> { backStackEntry ->
            ObservePlannerResult(backStackEntry, mealViewModel, mealPlannerViewModel, "PlannerPage")
            PlannerPage(
                viewModel = mealPlannerViewModel,
                mealViewModel = mealViewModel,
                navController = navController,
                onNavigateToMealDetails = { mealId -> navController.navigate(Destination.MealDetail(mealId)) },
                onPlanMealsClick = {
                    val currentDateInPlanner = mealPlannerViewModel.currentDate.value
                    val targetMealType = MealType.BREAKFAST
                    navController.navigate(Destination.Search(returnToPlanner = true, date = DateUtils.formatToISO(currentDateInPlanner), mealType = targetMealType.name, sourceScreen = "PlannerPage"))
                },
                onSeeAllPlansClick = { navController.navigate(Destination.AllPlansCalendarView) }
            )
        }

        composable<Destination.AllPlansCalendarView> { backStackEntry ->
            ObservePlannerResult(backStackEntry, mealViewModel, mealPlannerViewModel, "AllPlansScreen")
            AllPlansScreen(
                viewModel = mealPlannerViewModel,
                navController = navController,
                onNavigateBack = { navController.popBackStack() },
                onAddMealToDate = { date, mealType ->
                    navController.navigate(Destination.Search(returnToPlanner = true, date = date, mealType = mealType.name, sourceScreen = "AllPlansScreen"))
                }
            )
        }

        composable<Destination.DaysSelection> {
            DaysSelectionScreen(
                onDaysSelected = { daysCount ->
                    mealPlannerViewModel.clearNewPlan()
                    navController.navigate(Destination.PlanConfig(daysCount))
                },
                onNavigateBack = {
                    mealPlannerViewModel.finalizeAndClearNewPlanBuilder()
                    navController.popBackStack()
                },
                startDate = mealPlannerViewModel.currentDate.value
            )
        }

        composable<Destination.PlanConfig> { backStackEntry ->
            val config: Destination.PlanConfig = backStackEntry.toRoute()
            ObservePlannerResult(backStackEntry, mealViewModel, mealPlannerViewModel, "PlanConfigScreen")
            PlanConfigScreen(
                daysCount = config.daysCount,
                viewModel = mealPlannerViewModel,
                onNavigateBack = {
                    mealPlannerViewModel.finalizeAndClearNewPlanBuilder()
                    navController.popBackStack()
                },
                onSavePlanClick = {
                    val planStartDate = mealPlannerViewModel.newPlanMeals.value.minOfOrNull { it.date }
                    val planEndDate = mealPlannerViewModel.newPlanMeals.value.maxOfOrNull { it.date }
                    if (planStartDate != null && planEndDate != null) {
                        navController.navigate(Destination.ShoppingFrequency(planStartDate, planEndDate))
                    } else {
                        Log.e("AppNavHost", "PlanConfig: Start or End date is null.")
                        navController.popBackStack()
                    }
                },
                onAddMealToDay = { date, mealType ->
                    navController.navigate(Destination.Search(returnToPlanner = true, date = date, mealType = mealType.name, sourceScreen = "PlanConfigScreen"))
                }
            )
        }

        composable<Destination.ShoppingFrequency> { backStackEntry ->
            val freq: Destination.ShoppingFrequency = backStackEntry.toRoute()
            val planStartDate = DateUtils.parseLocalDate(freq.planStartDate) ?: LocalDate.now()
            val planEndDate = DateUtils.parseLocalDate(freq.planEndDate) ?: LocalDate.now().plusDays(6)
            ShoppingFrequencyScreen(
                onFrequencySelected = { frequency ->
                    val shoppingDays = backStackEntry.savedStateHandle.get<List<LocalDate>>("selectedShoppingDays")
                        ?: calculateShoppingDays(planStartDate, ChronoUnit.DAYS.between(planStartDate, planEndDate).toInt() + 1, frequency)
                    shoppingListViewModel.createNewShoppingList(planStartDate, planEndDate, frequency, shoppingDays)
                    mealPlannerViewModel.finalizeAndClearNewPlanBuilder()
                    navController.navigate(Destination.ShoppingList) { popUpTo(Destination.Planner) { inclusive = false } }
                },
                onNavigateBack = { navController.popBackStack() },
                planStartDate = planStartDate,
                planEndDate = planEndDate,
                navController = navController
            )
        }

        composable<Destination.ShoppingList> {
            ShoppingListScreen(
                onNavigateToAddCustomItem = { navController.navigate(Destination.AddCustomItem) },
                viewModel = shoppingListViewModel,
                onNavigateBack = { navController.popBackStack() },
                navController = navController
            )
        }

        composable<Destination.AddCustomItem> {
            AddCustomItemScreen(
                viewModel = shoppingListViewModel,
                onAddClickAndNavigateBack = { navController.popBackStack() },
                onCancelClick = {
                    shoppingListViewModel.prepareToAddCustomItem()
                    navController.popBackStack()
                }
            )
        }

        composable<Destination.MealDetail> { backStackEntry ->
            val detail: Destination.MealDetail = backStackEntry.toRoute()
            val mealId = detail.mealId
            val cachedMeal = mealViewModel.findMealById(mealId)
            val mealDetailsViewModel: MealDetailsViewModel = koinViewModel(parameters = { parametersOf(mealId, cachedMeal) })
            val mealDetailsState by mealDetailsViewModel.mealDetails.collectAsState()
            val isPlanned = mealPlannerViewModel.allPlannedMealsData.value.plannedMeals.any { it.meal.mealId == mealId }

            when (val resource = mealDetailsState) {
                is Resource.Loading -> { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                is Resource.Success -> {
                    resource.data?.let { meal ->
                        MealDetailScreen(meal = meal, onNavigateBack = { navController.popBackStack() }, navController = navController, mealViewModel = mealViewModel, isPlanned = isPlanned)
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Meal not found", style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(resource.message ?: "Unknown error", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = { navController.popBackStack() }) { Text("Go Back") }
                        }
                    }
                }
            }
        }

        composable<Destination.AllMeals> { backStackEntry ->
            val all: Destination.AllMeals = backStackEntry.toRoute()
            val title = all.title
            AllMealsPage(title = title, onMealClick = { meal -> navController.navigate(Destination.MealDetail(meal.mealId)) }, onBackClick = { navController.popBackStack() })
        }

        composable<Destination.Settings> { SettingsPage(navController = navController) }
        composable<Destination.PrivacyPolicy> { PrivacyPolicyPage(navController) }
        composable<Destination.TermsOfService> { TermsOfServicePage(navController) }
        composable<Destination.HelpCenter> { HelpCenterPage(navController) }
        composable<Destination.ReportBug> { ReportBugPage(navController) }

        composable<Destination.CookingMode>(
            enterTransition = { NavigationAnimations.enterTransition(this) },
            exitTransition = { NavigationAnimations.exitTransition(this) },
            popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
            popExitTransition = { NavigationAnimations.popExitTransition(this) }
        ) { backStackEntry ->
            val cooking: Destination.CookingMode = backStackEntry.toRoute()
            val hapticUtil = rememberHapticFeedback()
            val meal = try {
                val decodedMealJson = URLDecoder.decode(cooking.mealJson, StandardCharsets.UTF_8.toString())
                Gson().fromJson(decodedMealJson, Meal::class.java)
            } catch (e: Exception) {
                Log.e("AppNavHost", "Error deserializing/decoding meal for CookingModeScreen", e)
                null
            }
            if (meal != null) {
                CookingModeScreen(meal = meal, onNavigateBack = { navController.popBackStack() }, haptic = hapticUtil)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error loading cooking mode. Please try again.", style = MaterialTheme.typography.bodyLarge) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    navController.popBackStack()
                }
            }
        }
    }
}
