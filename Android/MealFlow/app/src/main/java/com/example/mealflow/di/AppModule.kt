package com.example.mealflow.di

import com.example.mealflow.data.repository.MealPlannerRepository
import com.example.mealflow.data.repository.MealRepository
import com.example.mealflow.data.repository.ShoppingRepository
import com.example.mealflow.data.storage.LocalMealPlanStorage
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.ApiMeal
import com.example.mealflow.ui.screens.setup.SetupProfileViewModel
import com.example.mealflow.viewModel.CommunityMembersViewModel
import com.example.mealflow.viewModel.CreateCommunityViewModel
import com.example.mealflow.viewModel.FeedViewModel
import com.example.mealflow.viewModel.GetAllCommunitiesViewModel
import com.example.mealflow.viewModel.GetUserCommunitiesViewModel
import com.example.mealflow.viewModel.LoginViewModel
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
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Singletons
    singleOf(::UserPreferencesManager)
    singleOf(::ApiMeal)
    singleOf(::MealRepository)
    singleOf(::MealPlannerRepository)
    singleOf(::LocalMealPlanStorage)
    singleOf(::ShoppingRepository)

    // ViewModels
    viewModelOf(::MealViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::CreateCommunityViewModel)
    viewModelOf(::SingleCommunityViewModel)
    viewModelOf(::SetupProfileViewModel)
    viewModelOf(::GetAllCommunitiesViewModel)
    viewModelOf(::MyCommunitiesViewModel)
    viewModelOf(::CommunityMembersViewModel)
    viewModelOf(::GetUserCommunitiesViewModel)
    viewModelOf(::UserPostViewModel)
    viewModelOf(::PostsViewModel)
    viewModelOf(::MealSearchViewModel)
    viewModelOf(::PostDropdownViewModel)
    viewModelOf(::UpdatePostViewModel)
    viewModelOf(::FeedViewModel)
    viewModelOf(::ShoppingListViewModel)

    // ViewModels with factory parameters
    viewModel { (mealId: String, initialMeal: com.example.mealflow.data.model.Meal?) ->
        MealDetailsViewModel(get(), mealId, initialMeal)
    }

    viewModel {
        MealPlannerViewModel(get(), get(), get(), get<ShoppingListViewModel>(), get<MealViewModel>())
    }
}
