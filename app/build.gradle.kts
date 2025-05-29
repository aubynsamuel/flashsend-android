plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.baselineprofile)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.aubynsamuel.flashsend"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aubynsamuel.flashsend"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            val boolean = false
            isMinifyEnabled = boolean
            isShrinkResources = boolean
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
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.mlkit.vision)
    implementation(libs.barcode.scanning)

    // Horizontal pager
    implementation(libs.accompanist.pager)

    // Credentials Manager
    implementation(libs.androidx.credentials)

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
    implementation(libs.play.services.location)

    // Image Cropping
    implementation(libs.ucrop)

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
    implementation(libs.firebase.messaging.ktx)

    // Navigation lib
    implementation(libs.androidx.navigation.compose)

    //Retrofit API
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    implementation(libs.material3)

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