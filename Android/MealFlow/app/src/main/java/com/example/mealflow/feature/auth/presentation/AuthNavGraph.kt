package com.example.mealflow.feature.auth.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.mealflow.feature.auth.presentation.forget_password.ForgetPasswordRoot
import com.example.mealflow.feature.auth.presentation.login.LoginRoot
import com.example.mealflow.feature.auth.presentation.otp.OtpRoot
import com.example.mealflow.feature.auth.presentation.quick_login.QuickLoginRoot
import com.example.mealflow.feature.auth.presentation.register.RegisterRoot
import com.example.mealflow.feature.auth.presentation.reset_password.ResetPasswordRoot
import com.example.mealflow.navigation.Destination
import com.example.mealflow.navigation.NavigationAnimations
import com.example.mealflow.ui.screens.*

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: (String) -> Unit, // email
    onOtpSuccess: () -> Unit,
    onForgetPasswordClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgetPasswordSuccess: (String) -> Unit,
    onBack: () -> Unit,
    onResetPasswordSuccess: () -> Unit
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
    ) {
        LoginRoot(
            onLoginSuccess = onLoginSuccess,
            onNavigateToRegister = onNavigateToRegister,
            onNavigateToForgetPassword = onForgetPasswordClick
        )
    }

    composable<Destination.Register>(
        enterTransition = { NavigationAnimations.enterTransition(this) },
        exitTransition = { NavigationAnimations.exitTransition(this) },
        popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
        popExitTransition = { NavigationAnimations.popExitTransition(this) }
    ) { 
        RegisterRoot(
            onRegisterSuccess = onRegisterSuccess,
            onNavigateToLogin = onNavigateToLogin
        )
    }

    composable<Destination.Otp>(
        enterTransition = { NavigationAnimations.enterTransition(this) },
        exitTransition = { NavigationAnimations.exitTransition(this) },
        popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
        popExitTransition = { NavigationAnimations.popExitTransition(this) }
    ) {
        OtpRoot(
            onVerificationSuccess = onOtpSuccess
        )
    }

    composable<Destination.ForgetPassword>(
        enterTransition = { NavigationAnimations.enterTransition(this) },
        exitTransition = { NavigationAnimations.exitTransition(this) },
        popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
        popExitTransition = { NavigationAnimations.popExitTransition(this) }
    ) { 
        ForgetPasswordRoot(
            onSuccess = onForgetPasswordSuccess,
            onBack = onBack
        )
    }

    composable<Destination.ResetPassword>(
        deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "https://iiacbca.r.bh.d.sendibt3.com/tr/cl?token={token}" }),
        enterTransition = { NavigationAnimations.enterTransition(this) },
        exitTransition = { NavigationAnimations.exitTransition(this) },
        popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
        popExitTransition = { NavigationAnimations.popExitTransition(this) }
    ) { 
        ResetPasswordRoot(
            onSuccess = onResetPasswordSuccess,
            onBack = onBack
        )
    }

    composable<Destination.CheckEmail> { backStackEntry ->
        val checkEmail: Destination.CheckEmail = backStackEntry.toRoute()
        CheckEmailPage(navController = navController, email = checkEmail.email)
    }

    composable<Destination.QuickLogin>(
        enterTransition = { NavigationAnimations.enterTransition(this) },
        exitTransition = { NavigationAnimations.exitTransition(this) },
        popEnterTransition = { NavigationAnimations.popEnterTransition(this) },
        popExitTransition = { NavigationAnimations.popExitTransition(this) }
    ) { 
        QuickLoginRoot(
            onQuickLoginSuccess = onLoginSuccess
        )
    }
}
