plugins {
    id("com.android.application")
}

android {
    namespace = "ai.android.claw.extension.demo"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "ai.android.claw.extension.demo"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1"
    }

    buildFeatures {
        aidl = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
