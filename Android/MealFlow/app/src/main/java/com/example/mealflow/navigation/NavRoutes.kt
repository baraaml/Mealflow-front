// File: app/src/main/java/com/example/mealflow/navigation/NavRoutes.kt
// Modify existing file
package com.example.mealflow.navigation

sealed class NavRoutes(val route: String) {
    // Main navigation routes
    object HomePage : NavRoutes("Home Page")
    object SearchPage : NavRoutes("Search Page")
    // PlannerPage will now be the entry point for the new flow (PlanMealsScreen)
    object PlannerPage : NavRoutes("Planner Page") // Stays as is, but content changes
    object ShoppingListPage : NavRoutes("Shopping List Page") // Stays as is, content changes
    object CommunityHome : NavRoutes("Community Home")
    object SettingsPage : NavRoutes("Settings Page")

    // Authentication routes
    object StartPage : NavRoutes("Start Page")
    object LoginPage : NavRoutes("Login Page")
    object RegisterPage : NavRoutes("Register Page")
    object OtpPage : NavRoutes("Otp Page")
    object ForgetPasswordPage : NavRoutes("Forget Password Page")
    object ResetPasswordPage : NavRoutes("Reset Password Page?token={token}")
    object QuickLoginPage : NavRoutes("QuickLogin Page")

    // Profile routes
    object ProfilePage : NavRoutes("Profile Page")
    object UserPage : NavRoutes("User Page")

    // Community routes
    object CommunityPage : NavRoutes("Community Page")
    object MembersPage : NavRoutes("Members Page")
    object SetAdminPage : NavRoutes("SetAdmin Page")
    object PostCreationPage : NavRoutes("PostCreationPage?communityId={communityId}")

    // Create Community routes
    object FirstStepPage : NavRoutes("FirstStep Page")
    object SecondStepPage : NavRoutes("SecondStep Page")
    object ThirdStepPage : NavRoutes("ThirdStep Page")
    object FourthStepPage : NavRoutes("FourthStep Page")

    // Onboarding routes
    object QuestionsPage : NavRoutes("Questions Page")
    object HealthDataPage : NavRoutes("HealthData Page")

    // Content routes
    object MealDetailPage : NavRoutes("meal_detail/{mealId}")
    object AllMealsPage : NavRoutes("all_meals/{title}")

    // --- New Meal Planning Flow Routes ---
    object DaysSelectionScreen : NavRoutes("DaysSelectionScreen")
    // PlanConfigScreen needs daysCount as an argument
    object PlanConfigScreen : NavRoutes("PlanConfigScreen/{daysCount}") {
        fun createRoute(daysCount: Int) = "PlanConfigScreen/$daysCount"
    }
    // AddMealsScreen is not a separate screen but part of PlanConfigScreen's interaction
    // Or if it is, it would need date and mealType. The guide implies interaction within PlanConfig.
    // Let's assume "Add Meal" on PlanConfigScreen navigates to a meal search/selection (e.g., your SearchPage)
    // then returns the selected meal to PlanConfigScreen's ViewModel.

    object ShoppingFrequencyScreen : NavRoutes("ShoppingFrequencyScreen")
    // ShoppingListScreen is already ShoppingListPage, content will be updated.
    object AddCustomItemScreen : NavRoutes("AddCustomItemScreen")
    object AllPlansCalendarViewScreen : NavRoutes("AllPlansCalendarViewScreen") // For the "See All Plans" calendar

    // Helper functions for parameterized routes
    fun createMealDetailRoute(mealId: String) = "meal_detail/$mealId"
    fun createAllMealsRoute(title: String) = "all_meals/$title"
    fun createPostCreationRoute(communityId: String?) =
        if (communityId != null) "PostCreationPage?communityId=$communityId"
        else "PostCreationPage"
    fun createResetPasswordRoute(token: String?) =
        if (token != null) "Reset Password Page?token=$token"
        else "Reset Password Page"
}