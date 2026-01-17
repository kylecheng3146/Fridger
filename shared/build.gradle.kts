plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kover)
}

kotlin {
    androidTarget()
    jvm("desktop")
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // JSON serialization runtime
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "fridger.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
