package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.services.DEFAULT_TREND_RANGE_DAYS
import fridger.backend.services.HealthDashboardProvider
import fridger.backend.services.HealthDashboardRequestOptions
import fridger.shared.health.CalorieBucket
import fridger.shared.health.DiversityRating
import fridger.shared.health.DiversityScore
import fridger.shared.health.ExpiryAlert
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.HealthRecommendation
import fridger.shared.health.NutritionCategory
import fridger.shared.health.RecommendationReason
import fridger.shared.models.ApiResponse
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthDashboardRoutesTest {
    private val userId = UUID.randomUUID()

    @Test
    fun returnsDashboardMetricsForUser() = testApplication {
        val provider = FakeProvider()
        application {
            install(ContentNegotiation) {
                json()
            }
            routing {
                healthDashboardRoutes(provider)
            }
        }

        val response = client.get("${ApiPaths.HEALTH_DASHBOARD}?userId=$userId")
        assertEquals(HttpStatusCode.OK, response.status)
        val payload = Json.decodeFromString<ApiResponse<HealthDashboardMetrics>>(response.bodyAsText())
        assertTrue(payload.success)
        assertEquals(provider.metrics, payload.data)
        assertEquals(false, provider.lastOptions?.includeTrends)
    }

    @Test
    fun returnsBadRequestWhenMissingUserId() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            routing { healthDashboardRoutes(FakeProvider()) }
        }

        val response = client.get(ApiPaths.HEALTH_DASHBOARD)
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun parsesIncludeAndRangeOptions() = testApplication {
        val provider = FakeProvider()
        application {
            install(ContentNegotiation) { json() }
            routing { healthDashboardRoutes(provider) }
        }

        val response = client.get("${ApiPaths.HEALTH_DASHBOARD}?userId=$userId&include=trends&rangeDays=30")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(provider.lastOptions?.includeTrends == true)
        assertEquals(30, provider.lastOptions?.rangeDays)
    }

    @Test
    fun rejectsInvalidIncludeParameter() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            routing { healthDashboardRoutes(FakeProvider()) }
        }
        val response = client.get("${ApiPaths.HEALTH_DASHBOARD}?userId=$userId&include=oops")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun rejectsInvalidRange() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            routing { healthDashboardRoutes(FakeProvider()) }
        }
        val response = client.get("${ApiPaths.HEALTH_DASHBOARD}?userId=$userId&include=trends&rangeDays=3")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun ignoresRangeWhenTrendsNotRequested() = testApplication {
        val provider = FakeProvider()
        application {
            install(ContentNegotiation) { json() }
            routing { healthDashboardRoutes(provider) }
        }
        val response = client.get("${ApiPaths.HEALTH_DASHBOARD}?userId=$userId&rangeDays=3")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(false, provider.lastOptions?.includeTrends)
        assertEquals(DEFAULT_TREND_RANGE_DAYS, provider.lastOptions?.rangeDays)
    }

    private class FakeProvider : HealthDashboardProvider {
        val metrics = HealthDashboardMetrics(
            nutritionDistribution = mapOf(
                NutritionCategory.PRODUCE to 60.0,
                NutritionCategory.PROTEIN to 30.0,
                NutritionCategory.OTHER to 10.0,
            ),
            diversityScore = DiversityScore(70, DiversityRating.BALANCED),
            expiryAlerts = listOf(
                ExpiryAlert(
                    itemName = "Berry Mix",
                    category = NutritionCategory.PRODUCE,
                    daysUntilExpiry = 2,
                    calorieBucket = CalorieBucket.LOW,
                ),
            ),
            recommendations = listOf(
                HealthRecommendation(
                    category = NutritionCategory.REFINED_GRAIN,
                    reason = RecommendationReason.LOW_STOCK,
                    message = "Add grains",
                ),
            ),
        )

        var lastOptions: HealthDashboardRequestOptions? = null

        override fun getDashboard(
            userId: UUID,
            options: HealthDashboardRequestOptions,
        ): HealthDashboardMetrics {
            lastOptions = options
            return metrics
        }
    }
}
