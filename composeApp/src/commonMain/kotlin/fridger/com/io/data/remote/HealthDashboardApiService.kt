package fridger.com.io.data.remote

import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.models.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import fridger.com.io.data.user.UserSessionManager
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone

private const val DASHBOARD_PATH = "/api/v1/health/dashboard"

open class HealthDashboardApiService(
    private val client: HttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    },
                )
            }
            install(Logging) {
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            println("🚚 HealthDashboardApi: $message")
                        }
                    }
                level = LogLevel.INFO
            }
        },
    private val baseUrl: String = BackendConfig.baseUrl,
    private val accessTokenProvider: suspend () -> String = { UserSessionManager.accessToken.first() },
    private val timeZoneProvider: () -> String = { TimeZone.currentSystemDefault().id },
) {
    open suspend fun fetchDashboard(
        userId: String,
        includeTrends: Boolean = false,
        rangeDays: Int? = null,
    ): ApiResponse<HealthDashboardMetrics> {
        val accessToken = accessTokenProvider()
        check(accessToken.isNotBlank()) { "Sign in to load your health dashboard" }
        return client
            .get("$baseUrl$DASHBOARD_PATH") {
                header("Authorization", "Bearer $accessToken")
                parameter("timeZoneId", timeZoneProvider())
                if (includeTrends) {
                    parameter("include", "trends")
                    rangeDays?.let { parameter("rangeDays", it) }
                }
            }.body<ApiResponse<HealthDashboardMetrics>>()
    }
}
