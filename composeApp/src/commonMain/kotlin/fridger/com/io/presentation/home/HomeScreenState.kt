package fridger.com.io.presentation.home

import fridger.com.io.data.model.Freshness
import kotlinx.coroutines.Job

// UI state for Home screen

data class PendingDeletion(
    val item: RefrigeratedItem,
    val job: Job
)

data class HomeUiState(
    val todayExpiringItems: List<ExpiringItem> = emptyList(),
    val weekExpiringItems: List<ExpiringItem> = emptyList(),
    val expiredItems: List<ExpiringItem> = emptyList(),
    val fridgeCapacityPercentage: Float = 0f,
    val refrigeratedItems: List<RefrigeratedItem> = emptyList(),
    val groupedRefrigeratedItems: Map<Freshness, List<RefrigeratedItem>> = emptyMap(),
    val sortOption: SortOption = SortOption.EXPIRY,
    val groupOption: GroupOption = GroupOption.NONE,
    val showAddNewItemDialog: Boolean = false,
    val quickAddSearchText: String = "",
    val quickAddSuggestions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val pendingDeletion: PendingDeletion? = null,
    val selectedItemIds: Set<String> = emptySet(),
    val healthDashboard: HealthDashboardUiState = HealthDashboardUiState(),
)

data class HealthDashboardUiState(
    val isLoading: Boolean = true,
    val metrics: fridger.shared.health.HealthDashboardMetrics? = null,
    val error: String? = null,
    val lastUpdatedEpochMillis: Long? = null,
)

// Sorting and grouping options

enum class SortOption { EXPIRY, NAME, ADDED_DATE }

enum class GroupOption { NONE, FRESHNESS }

// Presentation of expiry for i18n-safe mapping in UI layer
sealed class ExpiryDisplay {
    data class Overdue(
        val days: Int
    ) : ExpiryDisplay()

    data object DueToday : ExpiryDisplay()

    data class Until(
        val days: Int
    ) : ExpiryDisplay()
}

// Items

data class ExpiringItem(
    val id: String,
    val name: String,
    val icon: String,
    val count: Int,
    val daysUntil: Int,
    val expiryDisplay: ExpiryDisplay =
        when {
            daysUntil < 0 -> ExpiryDisplay.Overdue(kotlin.math.abs(daysUntil))
            daysUntil == 0 -> ExpiryDisplay.DueToday
            else -> ExpiryDisplay.Until(daysUntil)
        }
)

data class RefrigeratedItem(
    val id: String,
    val name: String,
    val icon: String,
    val quantity: String,
    val daysUntilExpiry: Int,
    val ageDays: Int,
    val category: fridger.com.io.data.model.IngredientCategory,
    val freshness: fridger.com.io.data.model.Freshness,
    val hasWarning: Boolean = false,
    val expiryDisplay: ExpiryDisplay =
        when {
            daysUntilExpiry < 0 -> ExpiryDisplay.Overdue(kotlin.math.abs(daysUntilExpiry))
            daysUntilExpiry == 0 -> ExpiryDisplay.DueToday
            else -> ExpiryDisplay.Until(daysUntilExpiry)
        }
)

// Model for batch quick-add

data class NewItem(
    val name: String,
    val quantity: String? = null,
    val expiryDateDisplay: String? = null
)

// Recipe suggestion data class
data class RecipeSuggestion(
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val cookingTime: String,
    val difficulty: String,
    val servings: Int
)
