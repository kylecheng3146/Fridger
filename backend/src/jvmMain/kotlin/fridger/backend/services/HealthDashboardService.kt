package fridger.backend.services

import fridger.backend.repositories.FridgeItemDataSource
import fridger.backend.repositories.FridgeItemRecord
import fridger.backend.repositories.HealthDashboardSnapshotRepository
import fridger.backend.repositories.HealthDashboardSnapshotStore
import fridger.shared.health.HealthDashboardCalculator
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.InventoryItem
import fridger.shared.health.TrendMetadata
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.datetime.LocalDate as KotlinLocalDate

interface HealthDashboardProvider {
    fun getDashboard(
        userId: UUID,
        options: HealthDashboardRequestOptions = HealthDashboardRequestOptions(),
    ): HealthDashboardMetrics
}

class HealthDashboardService(
    private val dataSource: FridgeItemDataSource,
    private val calculator: HealthDashboardCalculator = HealthDashboardCalculator(),
    private val snapshots: HealthDashboardSnapshotStore = HealthDashboardSnapshotRepository(),
    private val nowProvider: () -> Instant = { Instant.now() },
) : HealthDashboardProvider {
    override fun getDashboard(
        userId: UUID,
        options: HealthDashboardRequestOptions,
    ): HealthDashboardMetrics {
        val timeZoneId = options.timeZoneId ?: dataSource.fetchTimeZoneForUser(userId)
        if (options.timeZoneId != null) dataSource.updateTimeZoneForUser(userId, timeZoneId)
        val zoneId = ZoneId.of(timeZoneId)
        val today = nowProvider().atZone(zoneId).toLocalDate()
        val items: List<InventoryItem> = dataSource.fetchItemsForUser(userId).map { it.toInventoryItem() }
        val metrics = calculator.compute(items, kotlinx.datetime.LocalDate(today.year, today.monthValue, today.dayOfMonth))
        snapshots.save(userId, today, timeZoneId, metrics, items)
        if (!options.includeTrends) return metrics
        val fromDate = today.minusDays((options.rangeDays - 1).toLong())
        val history = snapshots.fetch(userId, fromDate, today)
        return metrics.copy(
            trendMetadata =
                TrendMetadata(
                    rangeDays = options.rangeDays,
                    partialRange = history.snapshotDates.size < options.rangeDays,
                    generatedAtEpochMillis = Instant.now().toEpochMilli(),
                ),
            trendSnapshots = history.snapshots,
            diversityHistory = history.diversityHistory,
            expiryHeatmap = history.expiryHeatmap,
        )
    }

    fun captureDailySnapshots() {
        snapshots.users().forEach { (userId, storedZone) ->
            runCatching {
                val timeZoneId = runCatching { ZoneId.of(storedZone).id }.getOrDefault("UTC")
                val today = nowProvider().atZone(ZoneId.of(timeZoneId)).toLocalDate()
                val todayKey = KotlinLocalDate(today.year, today.monthValue, today.dayOfMonth)
                if (todayKey in snapshots.fetch(userId, today, today).snapshotDates) return@runCatching
                val items = dataSource.fetchItemsForUser(userId).map { it.toInventoryItem() }
                val metrics = calculator.compute(items, todayKey)
                snapshots.save(userId, today, timeZoneId, metrics, items)
            }.onFailure {
                org.slf4j.LoggerFactory.getLogger(HealthDashboardService::class.java)
                    .warn("Daily inventory snapshot failed for account {}", userId, it)
            }
        }
    }
}

private fun FridgeItemRecord.toInventoryItem(): InventoryItem =
    InventoryItem(
        id = id.toString(),
        name = name,
        category = category,
        expiryDate = expiryDate?.let { KotlinLocalDate(it.year, it.monthValue, it.dayOfMonth) },
        ownerId = userId.toString(),
        isClassified = isClassified,
    )

const val DEFAULT_TREND_RANGE_DAYS = 7
val SUPPORTED_TREND_RANGE_DAYS = setOf(7, 30)

data class HealthDashboardRequestOptions(
    val includeTrends: Boolean = false,
    val rangeDays: Int = DEFAULT_TREND_RANGE_DAYS,
    val timeZoneId: String? = null,
) {
    init {
        require(rangeDays in SUPPORTED_TREND_RANGE_DAYS) {
            "Unsupported trend range: $rangeDays"
        }
    }
}
