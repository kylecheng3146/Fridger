package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.models.HealthDashboardEventRequest
import fridger.backend.repositories.HealthDashboardEventRepository
import fridger.backend.services.DEFAULT_TREND_RANGE_DAYS
import fridger.backend.services.HealthDashboardProvider
import fridger.backend.services.HealthDashboardRequestOptions
import fridger.backend.services.SUPPORTED_TREND_RANGE_DAYS
import fridger.backend.security.userId
import fridger.shared.models.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.*
import io.ktor.server.request.receive
import io.ktor.server.routing.*
import java.time.ZoneId
import java.time.Instant
import java.util.UUID

fun Route.healthDashboardRoutes(
    provider: HealthDashboardProvider,
    eventRepository: HealthDashboardEventRepository = HealthDashboardEventRepository(),
) {
    authenticate("access") {
      get(ApiPaths.HEALTH_DASHBOARD) {
        val userId = call.principal<JWTPrincipal>()?.userId()
            ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
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
            run {
            val rawTimeZoneId = call.request.queryParameters["timeZoneId"]
            val timeZoneId = if (rawTimeZoneId == null) null else runCatching { ZoneId.of(rawTimeZoneId).id }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid timeZoneId"))
                return@get
            }
            HealthDashboardRequestOptions(
                includeTrends = includeTrends,
                rangeDays = parsedRange,
                timeZoneId = timeZoneId,
            )
            }
        val metrics = provider.getDashboard(userId, options)
        call.respond(ApiResponse.ok(metrics))
      }

        post(ApiPaths.HEALTH_DASHBOARD_EVENTS) {
            val userId = call.principal<JWTPrincipal>()?.userId()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("Authentication failed"))
            if ((call.request.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: 0L) > 4_096L) {
                return@post call.respond(HttpStatusCode.PayloadTooLarge, ApiResponse.fail<Unit>("Event payload is too large"))
            }
            val request = call.receive<HealthDashboardEventRequest>()
            val allowedPayloadKeys = when (request.eventName) {
                "health_dash_section_toggle" -> setOf("section", "action", "previous")
                "collapsed_impression" -> setOf("section", "durationMs", "isDefault")
                "state_sync" -> setOf("source", "sections")
                "dashboard_view" -> emptySet()
                "recommendation_action" -> setOf("reason", "action")
                else -> return@post call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid eventName"))
            }
            if (request.payload.size > 4 || request.payload.keys.any { it !in allowedPayloadKeys } ||
                request.payload.values.any { it.length > 512 }
            ) {
                return@post call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid event payload"))
            }
            val nowMillis = Instant.now().toEpochMilli()
            if (request.occurredAtEpochMillis < nowMillis - 90 * 24 * 60 * 60_000L ||
                request.occurredAtEpochMillis > nowMillis + 5 * 60_000L
            ) {
                return@post call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("Invalid event timestamp"))
            }
            val occurredAt = Instant.ofEpochMilli(request.occurredAtEpochMillis)
            eventRepository.record(userId, request.eventName, request.payload, occurredAt)
            call.respond(ApiResponse.ok(Unit))
        }
    }
}
