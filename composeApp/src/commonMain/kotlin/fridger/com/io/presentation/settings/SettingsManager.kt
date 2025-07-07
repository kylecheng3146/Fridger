package fridger.com.io.presentation.settings

import fridger.com.io.data.settings.SettingsDataStore
import fridger.com.io.data.settings.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SettingsManager {
    private val settingsDataStore by lazy { SettingsDataStore(createDataStore()) }

    val settings = settingsDataStore.settings

    // Theme settings
    val isDarkTheme: Flow<Boolean> = settings.map { it.isDarkTheme }
    val themeColor: Flow<ThemeColor> = settings.map { it.themeColor }

    // Notification settings
    val notificationEnabled: Flow<Boolean> = settings.map { it.notificationEnabled }
    val reminderDays: Flow<Int> = settings.map { it.reminderDays }
    val soundEnabled: Flow<Boolean> = settings.map { it.soundEnabled }
    val vibrationEnabled: Flow<Boolean> = settings.map { it.vibrationEnabled }

    // Language setting
    val language: Flow<Language> = settings.map { it.language }

    // Update methods
    suspend fun setDarkTheme(isDarkTheme: Boolean) {
        settingsDataStore.setDarkTheme(isDarkTheme)
    }

    suspend fun setThemeColor(themeColor: ThemeColor) {
        settingsDataStore.setThemeColor(themeColor)
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        settingsDataStore.setNotificationEnabled(enabled)
    }

    suspend fun setReminderDays(days: Int) {
        settingsDataStore.setReminderDays(days)
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        settingsDataStore.setSoundEnabled(enabled)
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        settingsDataStore.setVibrationEnabled(enabled)
    }

    suspend fun setLanguage(language: Language) {
        settingsDataStore.setLanguage(language)
    }

    suspend fun clearSettings() {
        settingsDataStore.clearSettings()
    }
}
