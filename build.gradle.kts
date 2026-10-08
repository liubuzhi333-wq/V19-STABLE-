plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.liufy.thermaldisplay"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.liufy.thermaldisplay"
        minSdk = 23
        targetSdk = 35
        versionCode = 191
        versionName = "V19-Stable-Touch-Boot"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
