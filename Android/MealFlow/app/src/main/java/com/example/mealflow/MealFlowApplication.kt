package com.example.mealflow

import android.app.Application
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.di.appModule
import com.example.mealflow.core.di.coreDataModule
import com.example.mealflow.feature.auth.di.authDataModule
import com.example.mealflow.feature.auth.di.authPresentationModule
import com.example.mealflow.core.data.network.ApiClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MealFlowApplication : Application() {
    lateinit var userPreferencesManager: UserPreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MealFlowApplication)
            modules(
                coreDataModule,
                authDataModule,
                authPresentationModule,
                appModule
            )
        }

        userPreferencesManager = UserPreferencesManager(this)
        ApiClient.init(this)
    }
}
