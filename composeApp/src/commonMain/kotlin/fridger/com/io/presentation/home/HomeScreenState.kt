package fridger.com.io.presentation.home

import fridger.com.io.data.model.Freshness

// UI state for Home screen

data class HomeUiState(
    val todayExpiringItems: List<ExpiringItem> = emptyList(),
    val weekExpiringItems: List<ExpiringItem> = emptyList(),
    val expiredItems: List<ExpiringItem> = emptyList(),
    val fridgeCapacityPercentage: Float = 0f,
    // Sorted (and filtered) list to render when not grouped
    val refrigeratedItems: List<RefrigeratedItem> = emptyList(),
    // Grouped items by freshness when grouping is enabled
    val groupedRefrigeratedItems: Map<Freshness, List<RefrigeratedItem>> = emptyMap(),
    // User preferences for sorting and grouping
    val sortOption: SortOption = SortOption.EXPIRY,
    val groupOption: GroupOption = GroupOption.NONE,
    val showAddNewItemDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Sorting and grouping options

enum class SortOption { EXPIRY, NAME, ADDED_DATE }

enum class GroupOption { NONE, FRESHNESS }

// Presentation of expiry for i18n-safe mapping in UI layer
sealed class ExpiryDisplay {
    data class Overdue(val days: Int) : ExpiryDisplay()
    data object DueToday : ExpiryDisplay()
    data class Until(val days: Int) : ExpiryDisplay()
}

// Items

data class ExpiringItem(
    val id: String,
    val name: String,
    val icon: String,
    val count: Int,
    val daysUntil: Int,
    val expiryDisplay: ExpiryDisplay = when {
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
    val freshness: Freshness,
    val hasWarning: Boolean = false,
    val expiryDisplay: ExpiryDisplay = when {
        daysUntilExpiry < 0 -> ExpiryDisplay.Overdue(kotlin.math.abs(daysUntilExpiry))
        daysUntilExpiry == 0 -> ExpiryDisplay.DueToday
        else -> ExpiryDisplay.Until(daysUntilExpiry)
    }
)
