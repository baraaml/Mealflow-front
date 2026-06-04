package com.example.mealflow.core.di

import com.example.mealflow.core.data.network.ApiClient
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.database.token.TokenManager
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf

val coreDataModule = module {
    single { ApiClient.client }
    singleOf(::UserPreferencesManager)
    singleOf(::TokenManager)
}
