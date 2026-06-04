package com.example.mealflow.di

import com.example.mealflow.data.repository.MealPlannerRepository
import com.example.mealflow.data.repository.MealRepository
import com.example.mealflow.data.repository.ShoppingRepository
import com.example.mealflow.data.storage.LocalMealPlanStorage
import com.example.mealflow.network.ApiMeal
import com.example.mealflow.ui.screens.setup.SetupProfileViewModel
import com.example.mealflow.viewModel.*
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Singletons
    singleOf(::ApiMeal)
    singleOf(::MealRepository)
    singleOf(::MealPlannerRepository)
    singleOf(::LocalMealPlanStorage)
    singleOf(::ShoppingRepository)

    // ViewModels (those not yet moved to feature modules)
    viewModelOf(::MealViewModel)
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
