package fridger.backend.plugins

import fridger.backend.config.ApiPaths
import fridger.backend.models.HealthDto
import fridger.backend.models.MessageDto
import fridger.backend.routes.authRoutes
import fridger.shared.models.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get(ApiPaths.ROOT) {
            call.respond(ApiResponse.ok(MessageDto("Fridger backend running")))
        }
        get(ApiPaths.HEALTH) {
            call.respond(ApiResponse.ok(HealthDto("OK")))
        }
        authRoutes(this@configureRouting)
    }
}
