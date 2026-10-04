package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.config.JwtClaims
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
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthDashboardRoutesTest {
    private val userId = UUID.randomUUID()
    private val otherUserId = UUID.randomUUID()
    private val jwtSecret = "test-health-dashboard-secret"

    @Test
    fun returnsDashboardMetricsForUser() =
        testApplication {
            val provider = FakeProvider()
            application {
                install(ContentNegotiation) {
                    json()
                }
                installTestAuthentication()
                routing {
                    healthDashboardRoutes(provider)
                }
            }

            val response = client.get("${ApiPaths.HEALTH_DASHBOARD}?userId=$otherUserId") {
                header(HttpHeaders.Authorization, "Bearer ${accessToken(userId)}")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val payload = Json.decodeFromString<ApiResponse<HealthDashboardMetrics>>(response.bodyAsText())
            assertTrue(payload.success)
            assertEquals(provider.metrics, payload.data)
            assertEquals(false, provider.lastOptions?.includeTrends)
            assertEquals(userId, provider.lastUserId)
            assertEquals(null, provider.lastOptions?.timeZoneId)
        }

    @Test
    fun requiresAnAuthenticatedUser() =
        testApplication {
            application {
                install(ContentNegotiation) { json() }
                installTestAuthentication()
                routing { healthDashboardRoutes(FakeProvider()) }
            }

            val response = client.get(ApiPaths.HEALTH_DASHBOARD)
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

    @Test
    fun parsesIncludeAndRangeOptions() =
        testApplication {
            val provider = FakeProvider()
            application {
                install(ContentNegotiation) { json() }
                installTestAuthentication()
                routing { healthDashboardRoutes(provider) }
            }

            val response = client.get("${ApiPaths.HEALTH_DASHBOARD}?include=trends&rangeDays=30") {
                header(HttpHeaders.Authorization, "Bearer ${accessToken(userId)}")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(provider.lastOptions?.includeTrends == true)
            assertEquals(30, provider.lastOptions?.rangeDays)
        }

    @Test
    fun rejectsInvalidIncludeParameter() =
        testApplication {
            application {
                install(ContentNegotiation) { json() }
                installTestAuthentication()
                routing { healthDashboardRoutes(FakeProvider()) }
            }
            val response = client.get("${ApiPaths.HEALTH_DASHBOARD}?include=oops") {
                header(HttpHeaders.Authorization, "Bearer ${accessToken(userId)}")
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun rejectsInvalidRange() =
        testApplication {
            application {
                install(ContentNegotiation) { json() }
                installTestAuthentication()
                routing { healthDashboardRoutes(FakeProvider()) }
            }
            val response = client.get("${ApiPaths.HEALTH_DASHBOARD}?include=trends&rangeDays=3") {
                header(HttpHeaders.Authorization, "Bearer ${accessToken(userId)}")
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun ignoresRangeWhenTrendsNotRequested() =
        testApplication {
            val provider = FakeProvider()
            application {
                install(ContentNegotiation) { json() }
                installTestAuthentication()
                routing { healthDashboardRoutes(provider) }
            }
            val response = client.get("${ApiPaths.HEALTH_DASHBOARD}?rangeDays=3") {
                header(HttpHeaders.Authorization, "Bearer ${accessToken(userId)}")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(false, provider.lastOptions?.includeTrends)
            assertEquals(DEFAULT_TREND_RANGE_DAYS, provider.lastOptions?.rangeDays)
        }

    private class FakeProvider : HealthDashboardProvider {
        val metrics =
            HealthDashboardMetrics(
                nutritionDistribution =
                    mapOf(
                        NutritionCategory.PRODUCE to 60.0,
                        NutritionCategory.PROTEIN to 30.0,
                        NutritionCategory.OTHER to 10.0,
                    ),
                diversityScore = DiversityScore(70, DiversityRating.BALANCED),
                expiryAlerts =
                    listOf(
                        ExpiryAlert(
                            itemName = "Berry Mix",
                            category = NutritionCategory.PRODUCE,
                            daysUntilExpiry = 2,
                            calorieBucket = CalorieBucket.LOW,
                        ),
                    ),
                recommendations =
                    listOf(
                        HealthRecommendation(
                            category = NutritionCategory.REFINED_GRAIN,
                            reason = RecommendationReason.LOW_STOCK,
                            message = "Add grains",
                        ),
                    ),
            )

        var lastOptions: HealthDashboardRequestOptions? = null
        var lastUserId: UUID? = null

        override fun getDashboard(
            userId: UUID,
            options: HealthDashboardRequestOptions,
        ): HealthDashboardMetrics {
            lastUserId = userId
            lastOptions = options
            return metrics
        }
    }


    @Test
    fun validatesTimeZoneWithoutResettingOmittedValues() = testApplication {
        val provider = FakeProvider()
        application {
            install(ContentNegotiation) { json() }
            installTestAuthentication()
            routing { healthDashboardRoutes(provider) }
        }
        val valid = client.get("${ApiPaths.HEALTH_DASHBOARD}?timeZoneId=Asia%2FTaipei") {
            header(HttpHeaders.Authorization, "Bearer ${accessToken(userId)}")
        }
        assertEquals(HttpStatusCode.OK, valid.status)
        assertEquals("Asia/Taipei", provider.lastOptions?.timeZoneId)
        val invalid = client.get("${ApiPaths.HEALTH_DASHBOARD}?timeZoneId=not-a-zone") {
            header(HttpHeaders.Authorization, "Bearer ${accessToken(userId)}")
        }
        assertEquals(HttpStatusCode.BadRequest, invalid.status)
    }

    private fun Application.installTestAuthentication() {
        install(Authentication) {
            jwt("access") {
                verifier(JWT.require(Algorithm.HMAC256(jwtSecret)).build())
                validate { JWTPrincipal(it.payload) }
            }
        }
    }

    private fun accessToken(id: UUID): String =
        JWT.create().withClaim(JwtClaims.USER_ID, id.toString()).sign(Algorithm.HMAC256(jwtSecret))
}
