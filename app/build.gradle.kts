plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services) // Google Services plugin
}

android {
    namespace = "com.example.emedibotsimpleuserlogin"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.emedibotsimpleuserlogin"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core & lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    //okhttp+gson
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.code.gson:gson:2.11.0")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //voice command
    implementation ("androidx.activity:activity-compose:1.7.2")
    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
// Firebase Auth
    implementation("com.google.firebase:firebase-auth-ktx:23.2.1")

// Google Sign-In (for Firebase)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Accompanist SwipeRefresh - compatible with Compose BOM 2024.09.00
    implementation("com.google.accompanist:accompanist-swiperefresh:0.34.0")
    implementation(libs.material)

// Coil for Jetpack Compose
    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
//    implementation("com.google.firebase:firebase-functions-ktx")

    implementation("com.google.firebase:firebase-ai")

    //lotte animation and data store
    implementation("com.airbnb.android:lottie-compose:6.1.0")
    implementation ("androidx.datastore:datastore-preferences:1.1.1")

        // ... other dependencies
        implementation("com.google.ai.client.generativeai:generativeai:0.6.0") // Check for the latest version

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
