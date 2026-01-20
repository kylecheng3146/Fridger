package fridger.backend.services

import fridger.backend.repositories.FridgeItemDataSource
import fridger.backend.repositories.FridgeItemRecord
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.NutritionCategory
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        val service = HealthDashboardService(dataSource = dataSource)

        val metrics: HealthDashboardMetrics = service.getDashboard(userId)

        assertEquals(50.0, metrics.nutritionDistribution[NutritionCategory.PRODUCE])
        assertEquals(25.0, metrics.nutritionDistribution[NutritionCategory.PROTEIN])
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
}
