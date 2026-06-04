package com.example.mealflow.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {
    // Main navigation routes
    @Serializable data object Home : Destination
    @Serializable data class Search(
        val returnToPlanner: Boolean = false,
        val date: String? = null,
        val mealType: String? = null,
        val sourceScreen: String? = null
    ) : Destination
    @Serializable data object Planner : Destination
    @Serializable data object ShoppingList : Destination
    @Serializable data object CommunityHome : Destination
    @Serializable data object Settings : Destination

    // Authentication routes
    @Serializable data object Start : Destination
    @Serializable data object Login : Destination
    @Serializable data object Register : Destination
    @Serializable data class Otp(val email: String) : Destination
    @Serializable data object ForgetPassword : Destination
    @Serializable data class ResetPassword(val token: String? = null) : Destination
    @Serializable data object QuickLogin : Destination
    @Serializable data class CheckEmail(val email: String) : Destination

    // Profile routes
    @Serializable data object Profile : Destination
    @Serializable data object User : Destination
    @Serializable data object UpdateProfile : Destination
    @Serializable data object UpdateCommunity : Destination

    // Community routes
    @Serializable data object CommunityPage : Destination
    @Serializable data object Members : Destination
    @Serializable data object SetAdmin : Destination
    @Serializable data class PostCreation(val communityId: String? = null) : Destination
    @Serializable data class RecipeCreation(val communityId: String? = null) : Destination
    @Serializable data object AllCommunities : Destination
    @Serializable data object MyCommunities : Destination
    @Serializable data class PostDetails(val postId: String) : Destination
    @Serializable data object RemoveMembers : Destination

    // Create Community routes
    @Serializable data object FirstStep : Destination
    @Serializable data object SecondStep : Destination
    @Serializable data object ThirdStep : Destination

    // Social routes
    @Serializable data object Following : Destination
    @Serializable data object UserFollowing : Destination
    @Serializable data object Followers : Destination
    @Serializable data object UserFollowers : Destination

    // Onboarding routes
    @Serializable data object Questions : Destination
    @Serializable data object HealthData : Destination
    @Serializable data object SetupWelcome : Destination
    @Serializable data object SetupBasicInfo : Destination
    @Serializable data object SetupPhysicalInfo : Destination
    @Serializable data object SetupPhotos : Destination

    // Content routes
    @Serializable data class MealDetail(val mealId: String) : Destination
    @Serializable data class AllMeals(val title: String) : Destination
    @Serializable data class CookingMode(val mealJson: String) : Destination

    // New Meal Planning Flow Routes
    @Serializable data object DaysSelection : Destination
    @Serializable data class PlanConfig(val daysCount: Int) : Destination
    @Serializable data class ShoppingFrequency(
        val planStartDate: String,
        val planEndDate: String
    ) : Destination
    @Serializable data object AddCustomItem : Destination
    @Serializable data object AllPlansCalendarView : Destination

    // Static pages
    @Serializable data object PrivacyPolicy : Destination
    @Serializable data object TermsOfService : Destination
    @Serializable data object HelpCenter : Destination
    @Serializable data object ReportBug : Destination
    @Serializable data object SearchResults : Destination
}
