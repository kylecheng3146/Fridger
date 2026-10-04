package fridger.backend.repositories

import fridger.backend.BaseDbTest
import fridger.backend.db.UsersTable
import fridger.shared.health.HealthDashboardCalculator
import fridger.shared.health.InventoryItem
import fridger.shared.health.NutritionCategory
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate as KotlinLocalDate
import kotlinx.datetime.plus
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthDashboardSnapshotRepositoryTest : BaseDbTest() {
    @Test
    fun savesItemCountHistoryAndExpiringInventory() {
        val userId = UUID.randomUUID()
        transaction {
            UsersTable.insert {
                it[id] = userId
                it[name] = "Test"
                it[email] = "${userId}@example.com"
                it[googleId] = null
                it[pictureUrl] = null
                it[createdAt] = Instant.now()
            }
        }
        val today = LocalDate.of(2024, 1, 10)
        val kotlinToday = KotlinLocalDate(2024, 1, 10)
        val items = listOf(
            inventoryItem(UUID.randomUUID(), "Spinach", NutritionCategory.PRODUCE, kotlinToday.plus(DatePeriod(days = 1))),
            inventoryItem(UUID.randomUUID(), "Salmon", NutritionCategory.PROTEIN, kotlinToday.plus(DatePeriod(days = 2))),
            inventoryItem(UUID.randomUUID(), "Mystery", NutritionCategory.OTHER, kotlinToday.plus(DatePeriod(days = 3)), isClassified = false),
        )
        val metrics = HealthDashboardCalculator(nowProvider = { kotlinToday }).compute(items, kotlinToday)
        val repository = HealthDashboardSnapshotRepository()

        repository.save(userId, today, "Asia/Taipei", metrics, items)
        val history = repository.fetch(userId, today.minusDays(6), today)

        assertEquals(setOf(kotlinToday), history.snapshotDates)
        assertEquals(3, history.snapshots.single().totalTrackedItems)
        assertEquals(33.3, history.snapshots.single().distribution[NutritionCategory.PRODUCE])
        assertEquals(33.3, history.snapshots.single().unclassifiedPercent)
        assertEquals(3, history.expiryHeatmap.sumOf { it.count })
        assertTrue(history.diversityHistory.isNotEmpty())
    }

    private fun inventoryItem(
        id: UUID,
        name: String,
        category: NutritionCategory,
        expiryDate: KotlinLocalDate,
        isClassified: Boolean = true,
    ) = InventoryItem(
        id = id.toString(),
        name = name,
        category = category,
        expiryDate = expiryDate,
        ownerId = null,
        isClassified = isClassified,
    )
}
