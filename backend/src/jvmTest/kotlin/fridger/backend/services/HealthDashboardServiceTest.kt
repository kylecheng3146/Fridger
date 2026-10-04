package fridger.backend.services

import fridger.backend.repositories.FridgeItemDataSource
import fridger.backend.repositories.FridgeItemRecord
import fridger.backend.repositories.HealthDashboardSnapshotRange
import fridger.backend.repositories.HealthDashboardSnapshotStore
import fridger.shared.health.HealthDashboardCalculator
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.NutritionCategory
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate as KotlinLocalDate

class HealthDashboardServiceTest {
    private val userId = UUID.randomUUID()

    @Test
    fun aggregatesInventoryIntoMetrics() {
        val dataSource =
            object : FridgeItemDataSource {
                override fun fetchItemsForUser(userId: UUID): List<FridgeItemRecord> {
                    return listOf(
                        record("Spinach", NutritionCategory.PRODUCE, 4.0, 30, 8),
                        record("Salmon", NutritionCategory.PROTEIN, 2.0, 250, 4),
                        record("Rice", NutritionCategory.REFINED_GRAIN, 1.0, 180, 20),
                    )
                }
            }
        val service =
            HealthDashboardService(
                dataSource = dataSource,
                calculator = HealthDashboardCalculator(nowProvider = { KotlinLocalDate(2024, 1, 10) }),
                nowProvider = { java.time.Instant.parse("2024-01-10T00:00:00Z") },
                snapshots = object : HealthDashboardSnapshotStore {
                    override fun users() = emptyList<Pair<UUID, String>>()
                    override fun save(
                        userId: UUID,
                        date: LocalDate,
                        timeZoneId: String,
                        metrics: HealthDashboardMetrics,
                        items: List<fridger.shared.health.InventoryItem>,
                    ) = Unit
                    override fun fetch(userId: UUID, from: LocalDate, through: LocalDate) =
                        HealthDashboardSnapshotRange(emptyList(), emptyList(), emptyList(), emptySet())
                },
            )

        val metrics: HealthDashboardMetrics = service.getDashboard(userId)

        assertEquals(33.3, metrics.nutritionDistribution[NutritionCategory.PRODUCE])
        assertEquals(33.3, metrics.nutritionDistribution[NutritionCategory.PROTEIN])
        assertEquals(33.3, metrics.nutritionDistribution[NutritionCategory.REFINED_GRAIN])
        assertTrue(metrics.expiryAlerts.any { it.itemName == "Salmon" })
        assertTrue(metrics.recommendations.isNotEmpty())
    }

    private fun record(
        name: String,
        category: NutritionCategory,
        quantity: Double,
        calories: Int,
        expiryInDays: Long,
    ): FridgeItemRecord {
        val expiryDate = LocalDate.of(2024, 1, 10).plusDays(expiryInDays)
        return FridgeItemRecord(
            id = UUID.randomUUID(),
            userId = userId,
            name = name,
            category = category,
            quantity = quantity,
            caloriesPerPortion = calories,
            expiryDate = expiryDate,
            createdAt = expiryDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
        )
    }
    @Test
    fun missingTimeZoneUsesStoredZoneAndCapturesOnlyTheActualDate() {
        var updates = 0
        val dataSource = object : FridgeItemDataSource {
            override fun fetchItemsForUser(userId: UUID) = emptyList<FridgeItemRecord>()
            override fun fetchTimeZoneForUser(userId: UUID) = "Asia/Taipei"
            override fun updateTimeZoneForUser(userId: UUID, timeZoneId: String) { updates++ }
        }
        val dates = mutableSetOf<KotlinLocalDate>()
        val snapshots = object : HealthDashboardSnapshotStore {
            override fun users() = listOf(userId to "Asia/Taipei")
            override fun save(userId: UUID, date: LocalDate, timeZoneId: String, metrics: HealthDashboardMetrics, items: List<fridger.shared.health.InventoryItem>) {
                assertEquals("Asia/Taipei", timeZoneId)
                dates += KotlinLocalDate.parse(date.toString())
            }
            override fun fetch(userId: UUID, from: LocalDate, through: LocalDate) =
                HealthDashboardSnapshotRange(emptyList(), emptyList(), emptyList(), dates.filter { it.toString() >= from.toString() && it.toString() <= through.toString() }.toSet())
        }
        val service = HealthDashboardService(dataSource, snapshots = snapshots, nowProvider = { java.time.Instant.parse("2024-01-10T17:00:00Z") })
        val metrics = service.getDashboard(userId, HealthDashboardRequestOptions(includeTrends = true))
        service.captureDailySnapshots()
        service.captureDailySnapshots()
        assertEquals(setOf(KotlinLocalDate(2024, 1, 11)), dates)
        assertEquals(0, updates)
        assertTrue(metrics.trendMetadata!!.partialRange)
    }

}
