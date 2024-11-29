plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    id("com.autonomousapps.dependency-analysis")
}

android {
    namespace = "com.pkmkcub.spectragrow.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 25
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core Android Libraries
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.androidx.constraintlayout)

    // Navigation
    api(libs.androidx.navigation.common.ktx)
    api(libs.androidx.navigation.runtime.ktx)
    api(libs.androidx.navigation.compose)

    // Firebase
    api(platform(libs.firebase.bom))
    api(libs.firebase.analytics)
    api(libs.firebase.auth.ktx)
    api(libs.firebase.firestore.ktx)
    api(libs.firebase.storage.ktx)
    api(libs.firebase.crashlytics)

    // Play Services
    api(libs.play.services.maps)
    api(libs.play.services.location)
    api(libs.play.services.auth)

    // Maps
    api(libs.maps.compose)
    api(libs.places) {
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
    }

    api(libs.j2objc.annotations)

    // Coroutines
    api(libs.kotlinx.coroutines.android)

    // Compose
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.material3)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.material.icons.extended)
    api(libs.coil.compose)
    api(libs.accompanist.permissions)
    api(libs.androidx.foundation.android)
}

configurations.all {
    exclude(group = "androidx.biometric", module = "biometric")
}
