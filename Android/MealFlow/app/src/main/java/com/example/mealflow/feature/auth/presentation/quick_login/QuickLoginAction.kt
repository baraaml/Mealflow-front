package com.example.mealflow.feature.auth.presentation.quick_login

sealed interface QuickLoginAction {
    data object OnQuickLoginClick : QuickLoginAction
}
