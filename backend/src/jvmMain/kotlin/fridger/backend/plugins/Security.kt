package fridger.backend.plugins

import fridger.backend.security.GoogleTokenValidator
import fridger.backend.security.configureJwtAuth
import io.ktor.server.application.*

fun Application.configureSecurity() {
    GoogleTokenValidator.install(this)
    configureJwtAuth()
}
