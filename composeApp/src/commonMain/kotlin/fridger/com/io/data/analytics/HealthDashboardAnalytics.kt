package fridger.com.io.data.analytics

import fridger.com.io.presentation.home.dashboard.DashboardSection

enum class DashboardSectionAction {
    EXPANDED,
    COLLAPSED,
}

enum class DashboardStateSyncSource {
    APP,
    DESKTOP,
}

interface HealthDashboardAnalytics {
    fun trackSectionToggle(
        section: DashboardSection,
        action: DashboardSectionAction,
        previousState: Boolean,
    )

    fun trackCollapsedImpression(
        section: DashboardSection,
        durationMillis: Long,
        isDefaultState: Boolean,
    )

    fun trackStateSync(
        sectionStates: Map<DashboardSection, Boolean>,
        source: DashboardStateSyncSource,
    )
}

/**
 * Default analytics implementation that simply logs to console.
 * Real implementation can be swapped via dependency injection.
 */
class ConsoleHealthDashboardAnalytics : HealthDashboardAnalytics {
    override fun trackSectionToggle(
        section: DashboardSection,
        action: DashboardSectionAction,
        previousState: Boolean,
    ) {
        println("📊 [Analytics] SectionToggle section=${section.id} action=$action previous=$previousState")
    }

    override fun trackCollapsedImpression(
        section: DashboardSection,
        durationMillis: Long,
        isDefaultState: Boolean,
    ) {
        println("📊 [Analytics] CollapsedImpression section=${section.id} durationMs=$durationMillis default=$isDefaultState")
    }

    override fun trackStateSync(
        sectionStates: Map<DashboardSection, Boolean>,
        source: DashboardStateSyncSource,
    ) {
        val formattedState =
            sectionStates.entries.joinToString { "${it.key.id}:${if (it.value) "expanded" else "collapsed"}" }
        println("📊 [Analytics] StateSync source=$source state=[$formattedState]")
    }
}
