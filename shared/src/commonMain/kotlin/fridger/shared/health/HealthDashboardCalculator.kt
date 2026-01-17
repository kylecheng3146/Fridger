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
    private val highCalorieThreshold: Int = 400,
    private val targetDistribution: Map<NutritionCategory, Double> = defaultTargetDistribution,
    private val distributionTolerance: Double = 0.05,
) {

    fun compute(items: List<InventoryItem>): HealthDashboardMetrics {
        val today = nowProvider()
        val distribution = computeDistribution(items)
        val diversity = computeDiversity(items)
        val expiryAlerts = computeExpiryAlerts(items, today)
        val recommendations = buildRecommendations(distribution, expiryAlerts, diversity)
        return HealthDashboardMetrics(
            nutritionDistribution = distribution,
            diversityScore = diversity,
            expiryAlerts = expiryAlerts,
            recommendations = recommendations,
        )
    }

    private fun computeDistribution(items: List<InventoryItem>): Map<NutritionCategory, Double> {
        val totalsByCategory = items.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.quantity.coerceAtLeast(0.0) } }
        val totalQuantity = totalsByCategory.values.sum()
        if (totalQuantity == 0.0) {
            return NutritionCategory.entries.associateWith { 0.0 }
        }
        return NutritionCategory.entries.associateWith { category ->
            val share = (totalsByCategory[category] ?: 0.0) / totalQuantity
            (share * 100.0).roundToSingleDecimal()
        }
    }

    private fun computeDiversity(items: List<InventoryItem>): DiversityScore {
        if (items.isEmpty()) {
            return DiversityScore(value = 0, rating = DiversityRating.LOW)
        }
        val distinctCategories = items.map { it.category }.toSet().size
        val distinctItems = items.map { it.name.lowercase() }.toSet().size
        val categoryScore = (distinctCategories.toDouble() / NutritionCategory.entries.size) * 60.0
        val varietyScore = (distinctItems.coerceAtMost(10).toDouble() / 10.0) * 40.0
        val score = (categoryScore + varietyScore).roundToInt().coerceIn(0, 100)
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
                calorieBucket = bucketCalories(item.caloriesPerPortion),
            )
        }.sortedBy { it.daysUntilExpiry }
    }

    private fun buildRecommendations(
        distribution: Map<NutritionCategory, Double>,
        expiryAlerts: List<ExpiryAlert>,
        diversity: DiversityScore,
    ): List<HealthRecommendation> {
        val recommendations = mutableListOf<HealthRecommendation>()
        distribution.forEach { (category, percent) ->
            val target = targetDistribution[category] ?: 0.0
            if (target <= 0.0) return@forEach
            val actual = percent / 100.0
            if (actual + distributionTolerance < target) {
                recommendations += HealthRecommendation(
                    category = category,
                    reason = RecommendationReason.LOW_STOCK,
                    message = "目前 ${category.displayName} 佔比僅 ${percent.toInt()}%，建議補貨以達成 ${target.times(100).roundToInt()}%。",
                )
            }
        }
        if (expiryAlerts.isNotEmpty()) {
            recommendations += HealthRecommendation(
                category = expiryAlerts.first().category,
                reason = RecommendationReason.EXPIRY_RISK,
                message = "有 ${expiryAlerts.size} 項食材將在 ${expiryWarningWindowDays} 天內過期，請優先食用。",
            )
        }
        if (diversity.rating == DiversityRating.LOW) {
            recommendations += HealthRecommendation(
                category = NutritionCategory.PRODUCE,
                reason = RecommendationReason.DIVERSITY,
                message = "膳食多樣性偏低，嘗試補充至少三種不同類別的食材。",
            )
        }
        return recommendations
    }

    private fun bucketCalories(calories: Int): CalorieBucket = when {
        calories >= highCalorieThreshold -> CalorieBucket.HIGH
        calories >= 200 -> CalorieBucket.MODERATE
        else -> CalorieBucket.LOW
    }

    private fun Double.roundToSingleDecimal(): Double =
        (this * 10.0).roundToInt() / 10.0

    companion object {
        private val defaultTargetDistribution = mapOf(
            NutritionCategory.PRODUCE to 0.4,
            NutritionCategory.PROTEIN to 0.3,
            NutritionCategory.REFINED_GRAIN to 0.2,
            NutritionCategory.OTHER to 0.1,
        )
    }
}
