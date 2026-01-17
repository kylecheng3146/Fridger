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
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

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
) {
    open suspend fun fetchDashboard(userId: String): ApiResponse<HealthDashboardMetrics> {
        return client
            .get("$baseUrl$DASHBOARD_PATH") {
                parameter("userId", userId)
            }.body<ApiResponse<HealthDashboardMetrics>>()
    }
}
