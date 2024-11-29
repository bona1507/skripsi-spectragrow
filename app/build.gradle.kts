plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.firebase.firebase-perf")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.pkmkcub.spectragrow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pkmkcub.spectragrow"
        minSdk = 25
        targetSdk = 34
        versionCode = 5
        versionName = "1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "MAPS_API_KEY", "\"AIzaSyBV_FNrjVFisLwuOrAvhpRkxLCXDhBL0bs\"")
        buildConfigField("String", "OPENWEATHERMAP_API_KEY", "\"d0c1293638c591e305574e585d92e9a8\"")
        buildConfigField("String", "WEB_CLIENT_ID", "\"966096908244-p1q5rg1og554vi9r4250nv2juera9d38.apps.googleusercontent.com\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    dynamicFeatures += setOf(":story", ":maps")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.firebase.perf)
    androidTestImplementation(project(":maps"))
    androidTestImplementation(project(":story"))
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.ui.test.junit4.android)
    androidTestImplementation(libs.androidx.navigation.testing)

    // Debugging
    debugImplementation(libs.androidx.ui.test.manifest)

    // Identity
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Splash screen
    implementation(libs.androidx.core.splashscreen)
}