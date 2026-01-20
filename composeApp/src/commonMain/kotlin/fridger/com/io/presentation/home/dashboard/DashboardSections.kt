package fridger.com.io.presentation.home.dashboard

/**
 * Logical sections inside the health dashboard surface.
 * Each entry exposes an identifier for analytics/storage and a default expansion state.
 */
enum class DashboardSection(
    val id: String,
    val defaultExpanded: Boolean,
) {
    INDICATORS(id = "indicators", defaultExpanded = true),
    RECOMMENDATIONS(id = "recommendations", defaultExpanded = false),
    HISTORY(id = "history", defaultExpanded = false);

    companion object {
        fun fromId(id: String): DashboardSection? = entries.firstOrNull { it.id == id }
    }
}

object DashboardSectionDefaults {
    fun defaultStates(): Map<DashboardSection, Boolean> = DashboardSection.entries.associateWith { it.defaultExpanded }

    fun isDefaultState(
        section: DashboardSection,
        isExpanded: Boolean
    ): Boolean = section.defaultExpanded == isExpanded
}
