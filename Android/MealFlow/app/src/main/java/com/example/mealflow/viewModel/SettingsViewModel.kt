package com.example.mealflow.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealflow.MealFlowApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferencesManager = (application as MealFlowApplication).userPreferencesManager

    var themeSetting by mutableStateOf("System")
        private set
    var notificationsEnabled by mutableStateOf(true)
        private set
    var hapticFeedbackEnabled by mutableStateOf(true)
        private set

    init {
        viewModelScope.launch {
            // Load initial settings
            themeSetting = userPreferencesManager.getThemeSetting().first()
            notificationsEnabled = userPreferencesManager.getNotificationsEnabled().first()
            hapticFeedbackEnabled = userPreferencesManager.getHapticFeedbackEnabled().first()
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            userPreferencesManager.setThemeSetting(theme)
            themeSetting = theme
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesManager.setNotificationsEnabled(enabled)
            notificationsEnabled = enabled
        }
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesManager.setHapticFeedbackEnabled(enabled)
            hapticFeedbackEnabled = enabled
        }
    }
} 