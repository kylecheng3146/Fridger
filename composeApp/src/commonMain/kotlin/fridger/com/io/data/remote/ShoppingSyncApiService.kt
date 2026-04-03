package fridger.com.io.data.remote

import fridger.com.io.data.sync.PendingSyncAction
import fridger.com.io.data.sync.ShoppingSyncActionType
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

private const val SHOPPING_SYNC_PATH = "/api/v1/shopping-lists"

@Serializable
data class ShoppingSyncActionRequest(
    val actionId: String,
    val type: ShoppingSyncActionType,
    val itemId: String? = null,
    val itemName: String? = null,
    val quantity: String? = null,
    val isChecked: Boolean? = null,
    val createdAtEpochMillis: Long
)

@Serializable
data class ShoppingSyncRequest(
    val actions: List<ShoppingSyncActionRequest>
)

@Serializable
data class ShoppingSyncResult(
    val actionId: String,
    val success: Boolean,
    val error: String? = null
)

@Serializable
data class ShoppingSyncResponse(
    val results: List<ShoppingSyncResult>
)

class ShoppingSyncApiService(
    private val client: HttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }
        },
    private val baseUrl: String = BackendConfig.baseUrl,
) {
    suspend fun sync(
        listId: String,
        actions: List<PendingSyncAction>,
        accessToken: String
    ): ApiResponse<ShoppingSyncResponse> {
        val payload =
            ShoppingSyncRequest(
                actions =
                    actions.map { action ->
                        ShoppingSyncActionRequest(
                            actionId = action.id,
                            type = action.type,
                            itemId = action.itemId,
                            itemName = action.itemName,
                            quantity = action.quantity,
                            isChecked = action.isChecked,
                            createdAtEpochMillis = action.createdAtEpochMillis
                        )
                    }
            )
        return client
            .post("$baseUrl$SHOPPING_SYNC_PATH/$listId/sync") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                setBody(payload)
            }.body()
    }
}
