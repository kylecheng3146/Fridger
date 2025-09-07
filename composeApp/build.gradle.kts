import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.ktlint)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // Explicit bundle ID to satisfy Kotlin/Native requirement for iOS framework
            binaryOptions["bundleId"] = "fridger.com.io.composeapp"
        }
        // Link system SQLite on iOS to fix undefined sqlite3* symbols
        iosTarget.binaries.all {
            linkerOpts("-lsqlite3")
        }
    }

    jvm("desktop")

    // Temporarily comment out WASM platform as SQLDelight doesn't support it yet
    // @OptIn(ExperimentalWasmDsl::class)
    // wasmJs {
    //     moduleName = "composeApp"
    //     browser {
    //         val rootDirPath = project.rootDir.path
    //         val projectDirPath = project.projectDir.path
    //         commonWebpackConfig {
    //             outputFileName = "composeApp.js"
    //             devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
    //                 static = (static ?: mutableListOf()).apply {
    //                     // Serve sources to debug inside browser
    //                     add(rootDirPath)
    //                     add(projectDirPath)
    //                 }
    //             }
    //         }
    //     }
    //     binaries.executable()
    // }

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.sqldelight.androidDriver)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.datastore.preferences)
            implementation(libs.okio)
            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            // Removed MockK from commonTest to keep tests multiplatform-compatible
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sqldelight.sqliteDriver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.nativeDriver)
        }
        // wasmJsMain.dependencies {
        //     // SQLDelight doesn't support WASM yet
        // }
    }
}

android {
    namespace = "fridger.com.io"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "fridger.com.io"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "fridger.com.io.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "fridger.com.io"
            packageVersion = "1.0.0"
        }
    }
}

sqldelight {
    databases {
        create("FridgerDatabase") {
            packageName.set("fridger.com.io.database")
        }
    }
}

// ktlint 配置
ktlint {
    version.set("1.5.0")
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    filter {
        // 排除生成的代碼
        exclude("**/build/**")
        exclude("**/generated/**")
        exclude { element -> element.file.path.contains("generated") }
        exclude { element -> element.file.path.contains("build/") }
    }
}

// 創建只檢查 Android 和 Common 的任務
tasks.register("ktlintCheckAndroidAndCommon") {
    group = "verification"
    description = "Check Kotlin code style for Android and Common source sets only"

    dependsOn(
        tasks.matching { task ->
            task.name.startsWith("ktlint") &&
                task.name.contains("Check") &&
                (
                    task.name.contains("AndroidMain") ||
                        task.name.contains("CommonMain") ||
                        task.name.contains("AndroidTest") ||
                        task.name.contains("CommonTest")
                ) &&
                !task.name.contains("Ios") &&
                !task.name.contains("Desktop") &&
                !task.name.contains("WasmJs")
        }
    )
}

tasks.register("ktlintFormatAndroidAndCommon") {
    group = "formatting"
    description = "Fix Kotlin code style for Android and Common source sets only"

    dependsOn(
        tasks.matching { task ->
            task.name.startsWith("ktlint") &&
                task.name.contains("Format") &&
                (
                    task.name.contains("AndroidMain") ||
                        task.name.contains("CommonMain") ||
                        task.name.contains("AndroidTest") ||
                        task.name.contains("CommonTest")
                ) &&
                !task.name.contains("Ios") &&
                !task.name.contains("Desktop") &&
                !task.name.contains("WasmJs")
        }
    )
}

// 禁用不需要的 ktlint 任務
afterEvaluate {
    tasks
        .matching { task ->
            task.name.contains("ktlint") &&
                (
                    task.name.contains("Ios") ||
                        task.name.contains("Desktop") ||
                        task.name.contains("WasmJs")
                )
        }.configureEach {
            enabled = false
        }
}
