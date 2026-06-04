package com.example.mealflow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.ui.MainScreen
import com.example.mealflow.ui.components.AppState
import com.example.mealflow.ui.theme.MealFlowTheme
import com.example.mealflow.feature.auth.presentation.login.LoginViewModel
import com.example.mealflow.feature.auth.presentation.login.LoginEvent
import com.example.mealflow.core.presentation.util.ObserveAsEvents
import com.example.mealflow.viewModel.MealViewModel
import com.google.firebase.FirebaseApp
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private lateinit var userPreferencesManager: UserPreferencesManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
            var keepSplashScreen = true
            splashScreen.setKeepOnScreenCondition { keepSplashScreen }
            Handler(Looper.getMainLooper()).postDelayed({
                keepSplashScreen = false
            }, 1000)
        }
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        userPreferencesManager = UserPreferencesManager(this)
        AppState.setup()

        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        Thread.setDefaultUncaughtExceptionHandler { _, exception ->
            exception.printStackTrace()
        }

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                val themeSetting by userPreferencesManager.getThemeSetting().collectAsState(initial = "System")
                val useDarkTheme = when (themeSetting) {
                    "Dark" -> true
                    "Light" -> false
                    else -> isSystemInDarkTheme()
                }

                MealFlowTheme(darkTheme = useDarkTheme) {
                    val viewModel: MealViewModel = koinViewModel()
                    val loginViewModel: LoginViewModel = koinViewModel()

                    ObserveAsEvents(loginViewModel.events) { event ->
                        if (event is LoginEvent.LoginSuccess) {
                            Log.d("MainActivity", "Login successful, refreshing meals")
                            viewModel.refreshMeals()
                        }
                    }

                    LaunchedEffect(Unit) {
                        Log.d("MainActivity", "Triggering initial meal fetch")
                        viewModel.fetchRecommendedMeals()
                    }

                    MainScreen(
                        mealViewModel = viewModel,
                        userPreferencesManager = userPreferencesManager
                    )
                }
            }
        }
    }
}
