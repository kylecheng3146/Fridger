package fridger.shared.health

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
enum class NutritionCategory(val displayName: String) {
    PRODUCE("蔬果"),
    PROTEIN("蛋白質"),
    REFINED_GRAIN("穀物／澱粉"),
    OTHER("其他"),
}

@Serializable
data class InventoryItem(
    val id: String,
    val name: String,
    val category: NutritionCategory,
    // Retained for older callers; inventory insights count items and never infer calories.
    val quantity: Double = 0.0,
    val caloriesPerPortion: Int = 0,
    val expiryDate: LocalDate?,
    val ownerId: String?,
    val isClassified: Boolean = true,
)

@Serializable
data class HealthDashboardMetrics(
    val nutritionDistribution: Map<NutritionCategory, Double>,
    val diversityScore: DiversityScore,
    val expiryAlerts: List<ExpiryAlert>,
    val recommendations: List<HealthRecommendation>,
    val trendMetadata: TrendMetadata? = null,
    val trendSnapshots: List<TrendSnapshot> = emptyList(),
    val diversityHistory: List<DiversityHistoryEntry> = emptyList(),
    val expiryHeatmap: List<ExpiryHeatmapCell> = emptyList(),
    val unclassifiedPercent: Double = 0.0,
    val totalTrackedItems: Int = 0,
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
    val calorieBucket: CalorieBucket? = null,
    val itemId: String? = null,
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
    val itemName: String? = null,
)

@Serializable
data class TrendSnapshot(
    val date: LocalDate,
    val distribution: Map<NutritionCategory, Double>,
    val deficitCategories: List<NutritionCategory> = emptyList(),
    val totalTrackedItems: Int,
    val unclassifiedPercent: Double = 0.0,
)

@Serializable
data class DiversityHistoryEntry(
    val weekStart: LocalDate,
    val score: Int,
    val rating: DiversityRating,
)

@Serializable
data class ExpiryHeatmapCell(
    val date: LocalDate,
    val category: NutritionCategory,
    val count: Int,
    val severity: ExpirySeverity,
    val items: List<String> = emptyList(),
)

@Serializable
enum class ExpirySeverity {
    LOW,
    MEDIUM,
    HIGH,
}

@Serializable
data class TrendMetadata(
    val rangeDays: Int,
    val partialRange: Boolean,
    val generatedAtEpochMillis: Long,
)
