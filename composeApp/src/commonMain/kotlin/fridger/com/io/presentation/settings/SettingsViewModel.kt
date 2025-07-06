package fridger.com.io.presentation.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onThemeChange(isDarkTheme: Boolean) {
        _uiState.update { it.copy(isDarkTheme = isDarkTheme) }
    }

    fun onNotificationEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(notificationEnabled = enabled) }
    }

    fun onReminderDaysChange(days: Int) {
        _uiState.update { it.copy(reminderDays = days) }
    }

    fun onSoundEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(soundEnabled = enabled) }
    }

    fun onVibrationEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(vibrationEnabled = enabled) }
    }

    fun onLanguageChange(language: Language) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }
}

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val notificationEnabled: Boolean = true,
    val reminderDays: Int = 3,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val selectedLanguage: Language = Language.CHINESE
)

enum class Language(val displayName: String, val code: String) {
    CHINESE("繁體中文", "zh-TW"),
    ENGLISH("English", "en"),
    JAPANESE("日本語", "ja")
}
