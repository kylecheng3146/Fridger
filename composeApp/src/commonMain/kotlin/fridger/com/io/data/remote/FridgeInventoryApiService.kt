package fridger.com.io.data.remote

import fridger.shared.health.InventoryCategory
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import fridger.shared.models.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val FRIDGE_ITEMS_PATH = "/api/v1/fridge/items"

@Serializable
data class FridgeItemDto(
    val id: String,
    val name: String,
    val addDate: LocalDate,
    val expirationDate: LocalDate?,
    val category: InventoryCategory,
    val timeZoneId: String = "UTC",
)

open class FridgeInventoryApiService(
    private val client: HttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; explicitNulls = false })
            }
        },
    private val baseUrl: String = BackendConfig.baseUrl,
) {
    open suspend fun fetchItems(accessToken: String, timeZoneId: String): ApiResponse<List<FridgeItemDto>> =
        client.get("$baseUrl$FRIDGE_ITEMS_PATH") {
            header("Authorization", "Bearer $accessToken")
            parameter("timeZoneId", timeZoneId)
        }.body()

    open suspend fun upsert(item: FridgeItemDto, accessToken: String): ApiResponse<Unit> =
        client.put("$baseUrl$FRIDGE_ITEMS_PATH/${item.id}") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $accessToken")
            setBody(item)
        }.body()

    open suspend fun delete(id: String, accessToken: String): ApiResponse<Unit> =
        client.delete("$baseUrl$FRIDGE_ITEMS_PATH/$id") {
            header("Authorization", "Bearer $accessToken")
        }.body()
}
