package fridger.com.io.data.remote

import fridger.shared.models.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class HealthDashboardEventDto(
    val eventName: String,
    val payload: Map<String, String> = emptyMap(),
    val occurredAtEpochMillis: Long,
)

class HealthDashboardEventsApiService(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }
    },
    private val baseUrl: String = BackendConfig.baseUrl,
) {
    suspend fun send(event: HealthDashboardEventDto, accessToken: String) {
        val response = client.post("$baseUrl/api/v1/health/dashboard/events") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $accessToken")
            setBody(event)
        }.body<ApiResponse<Unit>>()
        check(response.success) { response.error ?: "Dashboard event was rejected" }
    }
}
