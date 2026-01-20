package fridger.com.io.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fridger.com.io.presentation.home.dashboard.DashboardSection
import fridger.com.io.presentation.home.dashboard.DashboardSectionDefaults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface HealthDashboardPreferences {
    val sectionStates: Flow<Map<DashboardSection, Boolean>>

    suspend fun setSectionStates(states: Map<DashboardSection, Boolean>)
}

class DataStoreHealthDashboardPreferences(
    private val dataStore: DataStore<Preferences>,
) : HealthDashboardPreferences {
    private val sectionStateKey = stringPreferencesKey("health-dashboard:sectionState")

    override val sectionStates: Flow<Map<DashboardSection, Boolean>> =
        dataStore.data.map { prefs ->
            val encoded = prefs[sectionStateKey]
            if (encoded.isNullOrBlank()) {
                DashboardSectionDefaults.defaultStates()
            } else {
                decodeStates(encoded)
            }
        }

    override suspend fun setSectionStates(states: Map<DashboardSection, Boolean>) {
        val encoded = encodeStates(states)
        dataStore.edit { prefs ->
            prefs[sectionStateKey] = encoded
        }
    }

    private fun encodeStates(states: Map<DashboardSection, Boolean>): String =
        DashboardSection.entries.joinToString(separator = "|") { section ->
            val isExpanded = states[section] ?: section.defaultExpanded
            "${section.id}:${if (isExpanded) 1 else 0}"
        }

    private fun decodeStates(raw: String): Map<DashboardSection, Boolean> {
        val defaults = DashboardSectionDefaults.defaultStates().toMutableMap()
        raw
            .split("|")
            .mapNotNull { token ->
                val parts = token.split(":")
                if (parts.size == 2) {
                    val section = DashboardSection.fromId(parts[0]) ?: return@mapNotNull null
                    val isExpanded = parts[1] == "1"
                    section to isExpanded
                } else {
                    null
                }
            }.forEach { (section, value) ->
                defaults[section] = value
            }
        return defaults
    }
}
