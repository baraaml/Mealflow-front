package com.example.mealflow.feature.auth.di

import com.example.mealflow.feature.auth.data.AuthRemoteDataSource
import com.example.mealflow.feature.auth.data.AuthRepositoryImpl
import com.example.mealflow.feature.auth.domain.AuthRepository
import com.example.mealflow.feature.auth.presentation.login.LoginViewModel
import com.example.mealflow.feature.auth.presentation.quick_login.QuickLoginViewModel
import com.example.mealflow.feature.auth.presentation.register.RegisterViewModel
import com.example.mealflow.feature.auth.presentation.otp.OtpViewModel
import com.example.mealflow.feature.auth.presentation.forget_password.ForgetPasswordViewModel
import com.example.mealflow.feature.auth.presentation.reset_password.ResetPasswordViewModel
import com.example.mealflow.utils.googleSignIn.GoogleSignInViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val authDataModule = module {
    singleOf(::AuthRemoteDataSource)
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
}

val authPresentationModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::QuickLoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::OtpViewModel)
    viewModelOf(::ForgetPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
    viewModelOf(::GoogleSignInViewModel)
}
