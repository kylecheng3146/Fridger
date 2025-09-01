package fridger.backend.plugins

import fridger.backend.security.GoogleTokenValidator
import io.ktor.server.application.*

fun Application.configureSecurity() {
    GoogleTokenValidator.install(this)
    // Future: configure JWT auth for protected routes.
}
