package fridger.backend.services

import fridger.backend.repositories.FridgeItemDataSource
import fridger.backend.repositories.FridgeItemRecord
import fridger.shared.health.HealthDashboardCalculator
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.InventoryItem
import fridger.shared.health.TrendMetadata
import java.time.Instant
import kotlinx.datetime.LocalDate as KotlinLocalDate
import java.util.UUID

interface HealthDashboardProvider {
    fun getDashboard(
        userId: UUID,
        options: HealthDashboardRequestOptions = HealthDashboardRequestOptions(),
    ): HealthDashboardMetrics
}

class HealthDashboardService(
    private val dataSource: FridgeItemDataSource,
    private val calculator: HealthDashboardCalculator = HealthDashboardCalculator(),
) : HealthDashboardProvider {

    override fun getDashboard(
        userId: UUID,
        options: HealthDashboardRequestOptions,
    ): HealthDashboardMetrics {
        val items: List<InventoryItem> = dataSource.fetchItemsForUser(userId).map { it.toInventoryItem() }
        val metrics = calculator.compute(items)
        if (!options.includeTrends) return metrics
        return metrics.copy(
            trendMetadata =
                TrendMetadata(
                    rangeDays = options.rangeDays,
                    partialRange = true,
                    generatedAtEpochMillis = Instant.now().toEpochMilli(),
                ),
            trendSnapshots = emptyList(),
            diversityHistory = emptyList(),
            expiryHeatmap = emptyList(),
        )
    }
}

private fun FridgeItemRecord.toInventoryItem(): InventoryItem =
    InventoryItem(
        id = id.toString(),
        name = name,
        category = category,
        quantity = quantity,
        caloriesPerPortion = caloriesPerPortion,
        expiryDate = expiryDate?.let { KotlinLocalDate(it.year, it.monthValue, it.dayOfMonth) },
        ownerId = userId.toString(),
    )

const val DEFAULT_TREND_RANGE_DAYS = 7
val SUPPORTED_TREND_RANGE_DAYS = setOf(7, 30)

data class HealthDashboardRequestOptions(
    val includeTrends: Boolean = false,
    val rangeDays: Int = DEFAULT_TREND_RANGE_DAYS,
) {
    init {
        require(rangeDays in SUPPORTED_TREND_RANGE_DAYS) {
            "Unsupported trend range: $rangeDays"
        }
    }
}
