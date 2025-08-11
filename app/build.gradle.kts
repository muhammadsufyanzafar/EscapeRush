plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.zafar.escaperush3d"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zafar.escaperush3d"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        buildFeatures { buildConfig = true }
    }

    packaging {
        resources {
            // Keep APK lean and avoid duplicate META-INF entries
            excludes += "/META-INF/*"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Your existing deps via version catalog
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // AndroidX Lifecycle + RecyclerView (direct coords for convenience)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Unity Ads (REPLACED AppLovin)
    implementation("com.unity3d.ads:unity-ads:4.10.0")

    // Google UMP (consent)
    implementation("com.google.android.ump:user-messaging-platform:2.2.0")

    // Gson (local leaderboard persistence)
    implementation("com.google.code.gson:gson:2.10.1")
}