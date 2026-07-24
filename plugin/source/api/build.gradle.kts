plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ai.android.claw.plugin.api"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
    }

    buildFeatures {
        aidl = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
