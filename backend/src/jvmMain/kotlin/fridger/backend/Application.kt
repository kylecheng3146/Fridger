package fridger.backend

import fridger.backend.config.loadAndStoreConfig
import fridger.backend.config.loadAppConfig
import fridger.backend.plugins.configureDatabase
import fridger.backend.plugins.configureLogging
import fridger.backend.plugins.configureRouting
import fridger.backend.plugins.configureSecurity
import fridger.backend.plugins.configureSerialization
import fridger.backend.plugins.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val cfg = loadAppConfig()
    embeddedServer(Netty, port = cfg.port) {
        // store in Application attributes
        attributes.put(fridger.backend.config.AppConfigAttribute, cfg)
        module()
    }.start(wait = true)
}

fun Application.module() {
    // ensure config present (idempotent if already stored)
    if (!attributes.contains(fridger.backend.config.AppConfigAttribute)) {
        loadAndStoreConfig()
    }
    // Order matters: DB first, then serialization/logging/status, then security & routing
    configureDatabase()
    configureSerialization()
    configureLogging()
    configureStatusPages()
    configureSecurity() // Placeholder for future implementation
    configureRouting()
}
