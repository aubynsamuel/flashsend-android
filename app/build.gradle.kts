plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.baselineprofile)
    id("com.google.gms.google-services")
    id("kotlin-kapt")

}

android {
    namespace = "com.aubynsamuel.flashsend"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aubynsamuel.flashsend"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            val bool = true
            isMinifyEnabled = bool
            isShrinkResources = bool
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
    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.room.compiler)
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.room.compiler)

    // Status Bar
    implementation(libs.accompanist.systemuicontroller)

    //Datastore
    implementation(libs.androidx.datastore.preferences)

    // Lottie animations
    implementation(libs.lottie.compose)

    // Google maps
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location) // Latest version as of now


    // Media player
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    // Gson
    implementation(libs.gson)

    // Material Icons
    implementation(libs.androidx.material.icons.extended)

    // Baseline profile
    implementation(libs.androidx.profileinstaller)
    "baselineProfile"(project(":baselineprofile"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.benchmark.macro.junit4)

    // Async Image
    implementation(libs.coil.compose)

    // Firebase libs
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)

    // Navigation lib
    implementation(libs.androidx.navigation.compose)

    // Default libs
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}