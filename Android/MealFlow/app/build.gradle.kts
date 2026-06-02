plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.mealflow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mealflow"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx.v1101)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.lifecycle.process)

    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.runtime.livedata)

    // Navigation
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Image loading
    implementation(libs.coil.compose)

    // Network and API
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)

    // Coroutines
    implementation(libs.coroutines.android)

    // Ktor client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.slf4j)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // ViewModel
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // UI & System features
    implementation(libs.androidx.security)
    implementation(libs.androidx.security.identity.credential)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.swiperefresh)

    implementation(libs.androidx.datastore)

    implementation(libs.exoplayer)

    implementation(libs.paging.runtime)
    implementation(libs.paging.compose) // لو هتستخدمها مع Jetpack Compose
    implementation(libs.lottie.compose)


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.accompanist.navigation.animation)

    /// For Pull-to-Refresh
    implementation(libs.accompanist.swiperefresh)
    // OR for Material 3 pull refresh
    implementation(libs.material3) // You likely already have this

    // For Lottie animations (optional)
    implementation(libs.lottie.compose)

    // For SwipeToDismiss
    implementation(libs.androidx.material)

    // Google Sign-In dependencies
    implementation(platform(libs.play.services.auth))
    implementation(libs.play.services.auth)
    implementation (platform(libs.firebase.bom))
    implementation (libs.firebase.auth)
    implementation(libs.firebase.dynamic.links)

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")

    // WorkManager for background tasks
    val work_version = "2.10.1"
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // Other dependencies
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
}