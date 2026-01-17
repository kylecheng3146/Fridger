package fridger.shared.health

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
enum class NutritionCategory(val displayName: String) {
    PRODUCE("蔬果"),
    PROTEIN("蛋白質"),
    REFINED_GRAIN("精緻澱粉"),
    OTHER("其他"),
}

@Serializable
data class InventoryItem(
    val id: String,
    val name: String,
    val category: NutritionCategory,
    val quantity: Double,
    val caloriesPerPortion: Int,
    val expiryDate: LocalDate?,
    val ownerId: String?,
)

@Serializable
data class HealthDashboardMetrics(
    val nutritionDistribution: Map<NutritionCategory, Double>,
    val diversityScore: DiversityScore,
    val expiryAlerts: List<ExpiryAlert>,
    val recommendations: List<HealthRecommendation>,
)

@Serializable
data class DiversityScore(
    val value: Int,
    val rating: DiversityRating,
)

@Serializable
enum class DiversityRating {
    LOW,
    BALANCED,
    HIGH,
}

@Serializable
data class ExpiryAlert(
    val itemName: String,
    val category: NutritionCategory,
    val daysUntilExpiry: Int,
    val calorieBucket: CalorieBucket,
)

@Serializable
enum class CalorieBucket {
    LOW,
    MODERATE,
    HIGH,
}

@Serializable
enum class RecommendationReason {
    LOW_STOCK,
    EXPIRY_RISK,
    DIVERSITY,
}

@Serializable
data class HealthRecommendation(
    val category: NutritionCategory,
    val reason: RecommendationReason,
    val message: String,
)
