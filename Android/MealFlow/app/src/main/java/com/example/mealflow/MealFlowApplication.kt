package com.example.mealflow

import android.app.Application
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.ApiClient

class MealFlowApplication : Application() {
    lateinit var userPreferencesManager: UserPreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        userPreferencesManager = UserPreferencesManager(this)
        ApiClient.init(this)
    }
}