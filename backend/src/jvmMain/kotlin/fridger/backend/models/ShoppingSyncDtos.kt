package fridger.backend.models

import kotlinx.serialization.Serializable

@Serializable
enum class ShoppingSyncActionType {
    ADD,
    UPDATE,
    DELETE,
    CLEAR
}

@Serializable
data class ShoppingSyncActionDto(
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
    val actions: List<ShoppingSyncActionDto>
)

@Serializable
data class ShoppingSyncResultDto(
    val actionId: String,
    val success: Boolean,
    val error: String? = null
)

@Serializable
data class ShoppingSyncResponse(
    val results: List<ShoppingSyncResultDto>
)
