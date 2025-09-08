package fridger.com.io.presentation.home

/**
 * Centralized thresholds for Home screen expiry groupings to avoid magic numbers.
 */
object IngredientConstants {
    // 0..1 -> Today or tomorrow
    val TodayOrTomorrowRange: IntRange = 0..1

    // 2..7 -> Within this week
    val WeekRange: IntRange = 2..7

    // Default days offset for quick-add expiry
    const val DEFAULT_QUICK_ADD_EXPIRY_DAYS: Int = 7
}
