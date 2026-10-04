package fridger.shared.health

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthDashboardCalculatorTest {
    private val today = LocalDate(2024, 1, 10)
    private val calculator = HealthDashboardCalculator(nowProvider = { today }, expiryWarningWindowDays = 5)

    @Test
    fun computeMetrics_returnsDistributionAndDiversitySignals() {
        val metrics = calculator.compute(
            listOf(
                item("Spinach", NutritionCategory.PRODUCE, expiryDays = 8),
                item("Chicken", NutritionCategory.PROTEIN, expiryDays = 5),
                item("Rice", NutritionCategory.REFINED_GRAIN, expiryDays = 40),
                item("Yogurt", NutritionCategory.OTHER, expiryDays = 10),
            ),
        )

        assertEquals(25.0, metrics.nutritionDistribution[NutritionCategory.PRODUCE])
        assertEquals(25.0, metrics.nutritionDistribution[NutritionCategory.PROTEIN])
        assertEquals(25.0, metrics.nutritionDistribution[NutritionCategory.REFINED_GRAIN])
        assertEquals(25.0, metrics.nutritionDistribution[NutritionCategory.OTHER])
        assertEquals(DiversityRating.HIGH, metrics.diversityScore.rating)
        assertEquals(4, metrics.totalTrackedItems)
        assertTrue(metrics.recommendations.none { it.category == NutritionCategory.PRODUCE })
    }

    @Test
    fun computeMetrics_flagsExpiryRisksAndSuggestsActions() {
        val metrics = calculator.compute(
            listOf(
                item("Cheesecake", NutritionCategory.OTHER, expiryDays = 2),
                item("Salmon", NutritionCategory.PROTEIN, expiryDays = 6),
                item("Broccoli", NutritionCategory.PRODUCE, expiryDays = 1),
            ),
        )

        val alerts = metrics.expiryAlerts
        assertEquals(2, alerts.size)
        val cheesecakeAlert = alerts.first { it.itemName == "Cheesecake" }
        assertEquals(2, cheesecakeAlert.daysUntilExpiry)
        assertEquals(null, cheesecakeAlert.calorieBucket)
        assertTrue(metrics.recommendations.any { it.reason == RecommendationReason.EXPIRY_RISK })
        assertTrue(metrics.recommendations.any { it.reason == RecommendationReason.LOW_STOCK && it.category == NutritionCategory.REFINED_GRAIN })
    }

    @Test
    fun computeMetrics_countsUnclassifiedInventoryWithoutInventingCategoryShare() {
        val metrics = calculator.compute(
            listOf(
                item("Spinach", NutritionCategory.PRODUCE, expiryDays = 8),
                item("Mystery", NutritionCategory.OTHER, expiryDays = 8).copy(isClassified = false),
            ),
        )

        assertEquals(50.0, metrics.nutritionDistribution[NutritionCategory.PRODUCE])
        assertEquals(0.0, metrics.nutritionDistribution[NutritionCategory.OTHER])
        assertEquals(50.0, metrics.unclassifiedPercent)
        assertEquals(2, metrics.totalTrackedItems)
    }

    @Test
    fun computeMetrics_emptyFridgeHasNoRestockRecommendations() {
        val metrics = calculator.compute(emptyList())

        assertTrue(metrics.recommendations.isEmpty())
        assertEquals(0.0, metrics.unclassifiedPercent)
        assertEquals(0, metrics.totalTrackedItems)
    }

    private fun item(
        name: String,
        category: NutritionCategory,
        expiryDays: Int,
    ): InventoryItem {
        return InventoryItem(
            id = name.lowercase(),
            name = name,
            category = category,
            expiryDate = today.plus(DatePeriod(days = expiryDays)),
            ownerId = null,
        )
    }
}
