package fridger.backend.models

import fridger.shared.health.InventoryCategory
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class FridgeItemUpsertRequest(
    val name: String,
    val addDate: LocalDate,
    val expirationDate: LocalDate,
    val category: InventoryCategory,
    val timeZoneId: String,
)

@Serializable
data class FridgeItemResponse(
    val id: String,
    val name: String,
    val addDate: LocalDate,
    val expirationDate: LocalDate?,
    val category: InventoryCategory,
)

@Serializable
data class HealthDashboardEventRequest(
    val eventName: String,
    val payload: Map<String, String> = emptyMap(),
    val occurredAtEpochMillis: Long,
)
