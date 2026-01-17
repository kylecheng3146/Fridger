package fridger.backend.services

import fridger.backend.repositories.FridgeItemDataSource
import fridger.backend.repositories.FridgeItemRecord
import fridger.shared.health.HealthDashboardCalculator
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.InventoryItem
import kotlinx.datetime.toKotlinLocalDate
import java.util.UUID

interface HealthDashboardProvider {
    fun getDashboard(userId: UUID): HealthDashboardMetrics
}

class HealthDashboardService(
    private val dataSource: FridgeItemDataSource,
    private val calculator: HealthDashboardCalculator = HealthDashboardCalculator(),
) : HealthDashboardProvider {

    override fun getDashboard(userId: UUID): HealthDashboardMetrics {
        val items: List<InventoryItem> = dataSource.fetchItemsForUser(userId).map { it.toInventoryItem() }
        return calculator.compute(items)
    }
}

private fun FridgeItemRecord.toInventoryItem(): InventoryItem =
    InventoryItem(
        id = id.toString(),
        name = name,
        category = category,
        quantity = quantity,
        caloriesPerPortion = caloriesPerPortion,
        expiryDate = expiryDate?.toKotlinLocalDate(),
        ownerId = userId.toString(),
    )
