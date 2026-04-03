package fridger.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListItemDto(
    val id: String,
    val name: String,
    val quantity: String? = null,
    val isChecked: Boolean
)
