import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    application
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

// Load local properties if the file exists
val localProperties = Properties()
val localPropertiesFile = file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    main {
        kotlin.srcDirs("src/jvmMain/kotlin")
        resources.srcDirs("src/jvmMain/resources")
    }
    test {
        kotlin.srcDirs("src/jvmTest/kotlin")
        resources.srcDirs("src/jvmTest/resources")
    }
}

application {
    mainClass = "fridger.backend.ApplicationKt"
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.status.pages)

    // Ktor client (for fetching Google JWKS if needed later)
    implementation(libs.ktor.client.cio)

    // Auth & crypto
    implementation(libs.bcrypt)
    implementation(libs.jwks.rsa)
    implementation(libs.java.jwt)

    // Database & ORM
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgresql)
    implementation(libs.exposed.java.time)

    // Migration
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)

    // Logging
    runtimeOnly(libs.logback.classic)

    // Tests (placeholder)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.h2)
    testImplementation(libs.kotlinx.coroutines.test)

    implementation(project(":shared"))

    implementation(libs.kotlinx.coroutines.core)
}

ktlint {
    android.set(false)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}

tasks.named<JavaExec>("run") {
    val jwtSecret = localProperties.getProperty("JWT_SECRET")
    if (jwtSecret != null) {
        environment("JWT_SECRET", jwtSecret)
    }
}

// Simple task to print effective classpath (debug aid)
tasks.register("printRuntimeClasspath") {
    doLast {
        println("Runtime classpath:\n" + configurations.runtimeClasspath.get().files.joinToString("\n") { it.name })
    }
}
