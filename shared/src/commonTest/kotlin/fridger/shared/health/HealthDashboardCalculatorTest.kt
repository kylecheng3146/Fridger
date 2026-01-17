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
                item("Spinach", NutritionCategory.PRODUCE, quantity = 5.0, calories = 35, expiryDays = 8),
                item("Chicken", NutritionCategory.PROTEIN, quantity = 3.0, calories = 240, expiryDays = 5),
                item("Rice", NutritionCategory.REFINED_GRAIN, quantity = 1.0, calories = 180, expiryDays = 40),
                item("Yogurt", NutritionCategory.OTHER, quantity = 1.0, calories = 120, expiryDays = 10),
            ),
        )

        assertEquals(50.0, metrics.nutritionDistribution[NutritionCategory.PRODUCE])
        assertEquals(30.0, metrics.nutritionDistribution[NutritionCategory.PROTEIN])
        assertEquals(10.0, metrics.nutritionDistribution[NutritionCategory.REFINED_GRAIN])
        assertEquals(10.0, metrics.nutritionDistribution[NutritionCategory.OTHER])
        assertEquals(DiversityRating.BALANCED, metrics.diversityScore.rating)
        assertTrue(metrics.recommendations.none { it.category == NutritionCategory.PRODUCE })
    }

    @Test
    fun computeMetrics_flagsExpiryRisksAndSuggestsActions() {
        val metrics = calculator.compute(
            listOf(
                item("Cheesecake", NutritionCategory.OTHER, quantity = 2.0, calories = 510, expiryDays = 2),
                item("Salmon", NutritionCategory.PROTEIN, quantity = 2.0, calories = 260, expiryDays = 6),
                item("Broccoli", NutritionCategory.PRODUCE, quantity = 4.0, calories = 45, expiryDays = 1),
            ),
        )

        val alerts = metrics.expiryAlerts
        assertEquals(2, alerts.size)
        val cheesecakeAlert = alerts.first { it.itemName == "Cheesecake" }
        assertEquals(2, cheesecakeAlert.daysUntilExpiry)
        assertEquals(CalorieBucket.HIGH, cheesecakeAlert.calorieBucket)
        assertTrue(metrics.recommendations.any { it.reason == RecommendationReason.EXPIRY_RISK })
        assertTrue(metrics.recommendations.any { it.reason == RecommendationReason.LOW_STOCK && it.category == NutritionCategory.REFINED_GRAIN })
    }

    private fun item(
        name: String,
        category: NutritionCategory,
        quantity: Double,
        calories: Int,
        expiryDays: Int,
    ): InventoryItem {
        return InventoryItem(
            id = name.lowercase(),
            name = name,
            category = category,
            quantity = quantity,
            caloriesPerPortion = calories,
            expiryDate = today.plus(DatePeriod(days = expiryDays)),
            ownerId = null,
        )
    }
}
