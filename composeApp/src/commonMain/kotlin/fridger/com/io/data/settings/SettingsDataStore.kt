package fridger.com.io.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import fridger.com.io.presentation.settings.Language
import fridger.com.io.presentation.settings.ThemeColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class SettingsDataStore(
    private val dataStore: DataStore<Preferences>
) {
    // Keys for preferences
    private companion object {
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_THEME_COLOR = stringPreferencesKey("theme_color")
        val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val KEY_REMINDER_DAYS = intPreferencesKey("reminder_days")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_QUICK_FAVORITES = stringPreferencesKey("quick_favorites") // CSV list of names
    }

    // Read settings
    val settings: Flow<Settings> =
        dataStore.data
            .catch { exception ->
                // TODO: Add logging
                if (exception is Exception) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                Settings(
                    isDarkTheme = preferences[KEY_DARK_THEME] ?: false,
                    themeColor =
                        preferences[KEY_THEME_COLOR]?.let { colorName ->
                            ThemeColor.entries.find { it.name == colorName }
                        } ?: ThemeColor.BLUE,
                    notificationEnabled = preferences[KEY_NOTIFICATION_ENABLED] ?: true,
                    reminderDays = preferences[KEY_REMINDER_DAYS] ?: 3,
                    soundEnabled = preferences[KEY_SOUND_ENABLED] ?: true,
                    vibrationEnabled = preferences[KEY_VIBRATION_ENABLED] ?: true,
                    language =
                        preferences[KEY_LANGUAGE]?.let { langCode ->
                            Language.entries.find { it.code == langCode }
                        } ?: Language.CHINESE
                )
            }

    // Save dark theme
    suspend fun setDarkTheme(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DARK_THEME] = isDarkTheme
        }
    }

    // Save theme color
    suspend fun setThemeColor(themeColor: ThemeColor) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_COLOR] = themeColor.name
        }
    }

    // Save notification enabled
    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_ENABLED] = enabled
        }
    }

    // Save reminder days
    suspend fun setReminderDays(days: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_REMINDER_DAYS] = days
        }
    }

    // Save sound enabled
    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SOUND_ENABLED] = enabled
        }
    }

    // Save vibration enabled
    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_VIBRATION_ENABLED] = enabled
        }
    }

    // Save language
    suspend fun setLanguage(language: Language) {
        dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language.code
        }
    }

    // Quick favorites for "快速新增" dialog
    // Stored as a CSV of item names (since suggestions are simple names). Use Set to avoid duplicates.
    val quickFavorites: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[KEY_QUICK_FAVORITES]
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            ?: emptySet()
    }

    suspend fun setQuickFavorites(favorites: Set<String>) {
        dataStore.edit { prefs ->
            prefs[KEY_QUICK_FAVORITES] = favorites.joinToString(",")
        }
    }

    suspend fun toggleQuickFavorite(name: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_QUICK_FAVORITES]
                ?.split(',')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.toMutableSet() ?: mutableSetOf()
            if (!current.add(name)) {
                current.remove(name)
            }
            prefs[KEY_QUICK_FAVORITES] = current.joinToString(",")
        }
    }

    // Clear all settings
    suspend fun clearSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

data class Settings(
    val isDarkTheme: Boolean = false,
    val themeColor: ThemeColor = ThemeColor.BLUE,
    val notificationEnabled: Boolean = true,
    val reminderDays: Int = 3,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val language: Language = Language.CHINESE
)
