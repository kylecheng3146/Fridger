package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.services.DEFAULT_TREND_RANGE_DAYS
import fridger.backend.services.HealthDashboardProvider
import fridger.backend.services.HealthDashboardRequestOptions
import fridger.backend.services.SUPPORTED_TREND_RANGE_DAYS
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
        val userId =
            runCatching { UUID.fromString(userIdParam) }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid userId"))
                return@get
            }
        val includeParam = call.request.queryParameters["include"]?.lowercase()
        val includeTrends =
            when (includeParam) {
                null, "", "basic" -> false
                "trends", "all" -> true
                else -> {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid include"))
                    return@get
                }
            }
        val parsedRange =
            if (!includeTrends) {
                DEFAULT_TREND_RANGE_DAYS
            } else {
                val rangeParam = call.request.queryParameters["rangeDays"]
                if (rangeParam.isNullOrBlank()) {
                    DEFAULT_TREND_RANGE_DAYS
                } else {
                    val value = rangeParam.toIntOrNull()
                    if (value == null || value !in SUPPORTED_TREND_RANGE_DAYS) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid rangeDays"))
                        return@get
                    } else {
                        value
                    }
                }
            }

        val options =
            HealthDashboardRequestOptions(
                includeTrends = includeTrends,
                rangeDays = parsedRange,
            )
        val metrics = provider.getDashboard(userId, options)
        call.respond(ApiResponse.ok(metrics))
    }
}
