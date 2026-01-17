package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.services.HealthDashboardProvider
import fridger.shared.models.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.healthDashboardRoutes(provider: HealthDashboardProvider) {
    get(ApiPaths.HEALTH_DASHBOARD) {
        val userIdParam = call.request.queryParameters["userId"]
        if (userIdParam.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Missing userId"))
            return@get
        }
        val userId = runCatching { UUID.fromString(userIdParam) }.getOrElse {
            call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid userId"))
            return@get
        }
        val metrics = provider.getDashboard(userId)
        call.respond(ApiResponse.ok(metrics))
    }
}
