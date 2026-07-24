plugins {
    id("foneclaw.plugin")
    alias(libs.plugins.ksp)
}

foneclawPlugin {
    pluginId.set("foneclaw:youtube-downloader")
    packageName.set("ai.android.claw.plugin.media.youtubedownloader")
    displayName.set("YouTube Downloader Plugin")
    versionName.set("0.0.1")
    versionCode.set(1)
    category.set("media")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation("io.github.junkfood02.youtubedl-android:library:0.18.1")
    implementation("io.github.junkfood02.youtubedl-android:ffmpeg:0.18.1")
    ksp(libs.androidx.room.compiler)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.junit)
    testImplementation("junit:junit:4.13.2")
}
