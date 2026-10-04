package fridger.shared.health

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

class HealthDashboardCalculator(
    private val nowProvider: () -> LocalDate = {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    },
    private val expiryWarningWindowDays: Int = 5,
) {

    fun compute(items: List<InventoryItem>): HealthDashboardMetrics {
        return compute(items, nowProvider())
    }

    fun compute(items: List<InventoryItem>, today: LocalDate): HealthDashboardMetrics {
        val distribution = computeDistribution(items)
        val unclassifiedPercent = if (items.isEmpty()) 0.0 else {
            (items.count { !it.isClassified }.toDouble() / items.size * 100.0).roundToSingleDecimal()
        }
        val diversity = computeDiversity(items.filter { it.isClassified })
        val expiryAlerts = computeExpiryAlerts(items, today)
        val recommendations = buildRecommendations(items, distribution, expiryAlerts)
        return HealthDashboardMetrics(
            nutritionDistribution = distribution,
            diversityScore = diversity,
            expiryAlerts = expiryAlerts,
            recommendations = recommendations,
            unclassifiedPercent = unclassifiedPercent,
            totalTrackedItems = items.size,
        )
    }

    private fun computeDistribution(items: List<InventoryItem>): Map<NutritionCategory, Double> {
        val classified = items.filter { it.isClassified }
        if (items.isEmpty()) {
            return NutritionCategory.entries.associateWith { 0.0 }
        }
        val countsByCategory = classified.groupingBy { it.category }.eachCount()
        return NutritionCategory.entries.associateWith { category ->
            val share = (countsByCategory[category] ?: 0).toDouble() / items.size
            (share * 100.0).roundToSingleDecimal()
        }
    }

    private fun computeDiversity(items: List<InventoryItem>): DiversityScore {
        if (items.isEmpty()) {
            return DiversityScore(value = 0, rating = DiversityRating.LOW)
        }
        val distinctCategories = items.map { it.category }.toSet().size
        val score = (distinctCategories.toDouble() / NutritionCategory.entries.size * 100.0).roundToInt()
        val rating = when {
            score >= 75 -> DiversityRating.HIGH
            score >= 50 -> DiversityRating.BALANCED
            else -> DiversityRating.LOW
        }
        return DiversityScore(value = score, rating = rating)
    }

    private fun computeExpiryAlerts(items: List<InventoryItem>, today: LocalDate): List<ExpiryAlert> {
        return items.mapNotNull { item ->
            val expiryDate = item.expiryDate ?: return@mapNotNull null
            val daysUntilExpiry = today.daysUntil(expiryDate)
            if (daysUntilExpiry < 0 || daysUntilExpiry > expiryWarningWindowDays) {
                return@mapNotNull null
            }
            ExpiryAlert(
                itemName = item.name,
                category = item.category,
                daysUntilExpiry = daysUntilExpiry,
                calorieBucket = null,
                itemId = item.id,
            )
        }.sortedBy { it.daysUntilExpiry }
    }

    private fun buildRecommendations(
        items: List<InventoryItem>,
        distribution: Map<NutritionCategory, Double>,
        expiryAlerts: List<ExpiryAlert>,
    ): List<HealthRecommendation> {
        val recommendations = mutableListOf<HealthRecommendation>()
        if (items.any { it.isClassified }) {
            listOf(NutritionCategory.PRODUCE, NutritionCategory.PROTEIN, NutritionCategory.REFINED_GRAIN).forEach { category ->
                val percent = distribution[category] ?: 0.0
                if (percent == 0.0) {
                    recommendations += HealthRecommendation(
                        category = category,
                        reason = RecommendationReason.LOW_STOCK,
                        message = "目前庫存沒有${category.displayName}品項，可考慮補充一項。",
                    )
                }
            }
        }
        if (expiryAlerts.isNotEmpty()) {
            val firstAlert = expiryAlerts.first()
            recommendations += HealthRecommendation(
                category = firstAlert.category,
                reason = RecommendationReason.EXPIRY_RISK,
                message = "有 ${expiryAlerts.size} 項食材將在 ${expiryWarningWindowDays} 天內過期，請優先食用。",
                itemName = firstAlert.itemName,
            )
        }
        return recommendations
    }

    private fun Double.roundToSingleDecimal(): Double =
        (this * 10.0).roundToInt() / 10.0
}
