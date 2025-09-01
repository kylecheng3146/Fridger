plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover) apply false
    id("org.jetbrains.kotlin.jvm") version "2.1.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21" apply false
}

// Gradle tasks to build and run the iOS app on Simulator without opening Xcode
val iosSimulatorName = (findProperty("iosSimulator") as String?) ?: "iPhone 15"

val derivedData: String = layout.projectDirectory.dir("iosApp/build/DerivedData").asFile.absolutePath
val appProductDir = "$derivedData/Build/Products/Debug-iphonesimulator"
val appBundlePath = "$appProductDir/iosApp.app"
val iosBundleId = "fridger.com.io.Fridger"

tasks.register("iosRunDebugOnSimulator") {
    group = "iOS"
    description = "Builds and runs the iOS app on the specified Simulator using xcodebuild and simctl. Set -PiosSimulator=\"<Device Name>\" to choose device."
    doLast {
        // Boot simulator if needed
        exec {
            commandLine("bash", "-lc", "xcrun simctl bootstatus \"$iosSimulatorName\" -b || xcrun simctl boot \"$iosSimulatorName\"")
        }
        // Build the app with a deterministic DerivedData path
        exec {
            commandLine(
                "xcodebuild",
                "-scheme", "iosApp",
                "-project", "iosApp/iosApp.xcodeproj",
                "-configuration", "Debug",
                "-derivedDataPath", derivedData,
                "-destination", "platform=iOS Simulator,name=$iosSimulatorName",
                "build"
            )
        }
        // Install and launch
        exec { commandLine("xcrun", "simctl", "install", "booted", appBundlePath) }
        exec { commandLine("xcrun", "simctl", "launch", "booted", iosBundleId) }
    }
}

// Custom tasks to run subprojects individually
tasks.register("runBackend") {
    group = "application"
    description = "Runs the backend server."
    dependsOn(":backend:run")
}

tasks.register("runDesktop") {
    group = "application"
    description = "Runs the desktop application."
    dependsOn(":composeApp:run")
}
