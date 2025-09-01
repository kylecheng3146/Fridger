package fridger.backend

import fridger.backend.plugins.configureDatabase
import fridger.backend.plugins.configureLogging
import fridger.backend.plugins.configureRouting
import fridger.backend.plugins.configureSerialization
import fridger.backend.plugins.configureStatusPages
import fridger.backend.plugins.configureSecurity
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = (System.getenv("PORT") ?: "8080").toInt()) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    // Order matters: DB first, then serialization/logging/status, then security & routing
    configureDatabase()
    configureSerialization()
    configureLogging()
    configureStatusPages()
    configureSecurity() // Placeholder for future implementation
    configureRouting()
}

