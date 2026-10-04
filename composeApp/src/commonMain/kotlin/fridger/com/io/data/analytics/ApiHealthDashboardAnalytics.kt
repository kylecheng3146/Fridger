package fridger.com.io.data.analytics

import fridger.com.io.data.remote.HealthDashboardEventDto
import fridger.com.io.data.remote.HealthDashboardEventsApiService
import fridger.com.io.data.user.UserSessionManager
import fridger.com.io.presentation.home.dashboard.DashboardSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ApiHealthDashboardAnalytics(
    private val api: HealthDashboardEventsApiService = HealthDashboardEventsApiService(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : HealthDashboardAnalytics {
    override fun trackSectionToggle(section: DashboardSection, action: DashboardSectionAction, previousState: Boolean) {
        send("health_dash_section_toggle", mapOf("section" to section.id, "action" to action.name.lowercase(), "previous" to previousState.toString()))
    }

    override fun trackCollapsedImpression(section: DashboardSection, durationMillis: Long, isDefaultState: Boolean) {
        send("collapsed_impression", mapOf("section" to section.id, "durationMs" to durationMillis.coerceAtLeast(0).toString(), "isDefault" to isDefaultState.toString()))
    }

    override fun trackStateSync(sectionStates: Map<DashboardSection, Boolean>, source: DashboardStateSyncSource) {
        val states = sectionStates.entries.sortedBy { it.key.id }.joinToString(",") { "${it.key.id}:${if (it.value) "expanded" else "collapsed"}" }
        send("state_sync", mapOf("source" to source.name.lowercase(), "sections" to states))
    }

    override fun trackDashboardView() {
        send("dashboard_view", emptyMap())
    }

    override fun trackRecommendationAction(reason: String, action: String) {
        send("recommendation_action", mapOf("reason" to reason, "action" to action))
    }

    private fun send(eventName: String, payload: Map<String, String>) {
        scope.launch {
            runCatching {
                val token = UserSessionManager.accessToken.first()
                if (token.isNotBlank()) {
                    api.send(HealthDashboardEventDto(eventName, payload, Clock.System.now().toEpochMilliseconds()), token)
                }
            }
        }
    }
}
