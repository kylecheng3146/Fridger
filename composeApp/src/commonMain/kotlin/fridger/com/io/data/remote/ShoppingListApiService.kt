package fridger.com.io.data.remote

import fridger.shared.models.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.get
import io.ktor.client.request.delete
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val SHOPPING_LISTS_PATH = "/api/v1/shopping-lists"

@Serializable
data class ShoppingListUpsertRequest(
    val id: String,
    val name: String,
    val date: String? = null
)

@Serializable
data class ShoppingListResponse(
    val id: String,
    val name: String,
    val date: String? = null
)

class ShoppingListApiService(
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
    suspend fun fetchLists(accessToken: String): ApiResponse<List<ShoppingListResponse>> {
        return client
            .get("$baseUrl$SHOPPING_LISTS_PATH") {
                header("Authorization", "Bearer $accessToken")
            }.body()
    }

    suspend fun fetchItems(listId: String, accessToken: String): ApiResponse<List<ShoppingListItemResponse>> {
        return client
            .get("$baseUrl$SHOPPING_LISTS_PATH/$listId") {
                header("Authorization", "Bearer $accessToken")
            }.body()
    }

    suspend fun deleteList(id: String, accessToken: String): ApiResponse<Unit> {
        return client
            .delete("$baseUrl$SHOPPING_LISTS_PATH/$id") {
                header("Authorization", "Bearer $accessToken")
            }.body()
    }
    suspend fun createList(
        id: String,
        name: String,
        date: String?,
        accessToken: String
    ): ApiResponse<ShoppingListResponse> {
        return client
            .post("$baseUrl$SHOPPING_LISTS_PATH") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                setBody(ShoppingListUpsertRequest(id, name, date))
            }.body()
    }

    suspend fun updateList(
        id: String,
        name: String,
        date: String?,
        accessToken: String
    ): ApiResponse<ShoppingListResponse> {
        return client
            .put("$baseUrl$SHOPPING_LISTS_PATH/$id") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                setBody(ShoppingListUpsertRequest(id, name, date))
            }.body()
    }
}

@Serializable
data class ShoppingListItemResponse(
    val id: String,
    val name: String,
    val quantity: String? = null,
    val isChecked: Boolean
)
