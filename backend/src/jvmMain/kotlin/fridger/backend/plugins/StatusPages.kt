package fridger.backend.plugins

import fridger.shared.models.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import fridger.backend.exceptions.UnauthorizedException
import fridger.backend.exceptions.ForbiddenException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            // Validation / bad input – message may be user-facing but still clamp length
            val safeMsg = (cause.message ?: "Invalid request").take(200)
            call.application.environment.log.warn("400 Bad Request: ${'$'}safeMsg")
            call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid request: ${'$'}safeMsg"))
        }
        exception<NoSuchElementException> { call, _ ->
            call.application.environment.log.warn("404 Not Found")
            call.respond(HttpStatusCode.NotFound, ApiResponse.fail<Unit>("Resource not found"))
        }
        exception<UnauthorizedException> { call, _ ->
            call.application.environment.log.warn("401 Unauthorized")
            call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
        }
        exception<ForbiddenException> { call, _ ->
            call.application.environment.log.warn("403 Forbidden")
            call.respond(HttpStatusCode.Forbidden, ApiResponse.fail<Unit>("Forbidden"))
        }
        exception<ExposedSQLException> { call, cause ->
            // Never leak raw SQL error outward
            call.application.environment.log.error("Database error", cause)
            call.respond(HttpStatusCode.InternalServerError, ApiResponse.fail<Unit>("Database error"))
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled server error", cause)
            call.respond(HttpStatusCode.InternalServerError, ApiResponse.fail<Unit>("Internal server error"))
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(HttpStatusCode.NotFound, ApiResponse.fail<Unit>("Not found"))
        }
    }
}
