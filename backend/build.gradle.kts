plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    application
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    main {
        kotlin.srcDirs("src/jvmMain/kotlin")
        resources.srcDirs("src/jvmMain/resources")
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

    // Database & ORM
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgresql)

    // Migration
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)

    // Logging
    runtimeOnly(libs.logback.classic)

    // Tests (placeholder)
    testImplementation(libs.kotlin.test)

    implementation(project(":shared"))

    implementation(libs.kotlinx.coroutines.core)
}

// Simple task to print effective classpath (debug aid)
tasks.register("printRuntimeClasspath") {
    doLast {
        println("Runtime classpath:\n" + configurations.runtimeClasspath.get().files.joinToString("\n") { it.name })
    }
}
