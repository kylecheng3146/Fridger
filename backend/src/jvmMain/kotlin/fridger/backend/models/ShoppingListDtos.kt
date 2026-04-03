package fridger.backend.models

import kotlinx.serialization.Serializable

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
