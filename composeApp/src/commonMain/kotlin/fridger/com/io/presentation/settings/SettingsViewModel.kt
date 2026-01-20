package fridger.com.io.presentation.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Load saved settings from SettingsManager - single source of truth
        viewModelScope.launch {
            SettingsManager.settings.collect { settings ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isDarkTheme = settings.isDarkTheme,
                        selectedThemeColor = settings.themeColor,
                        notificationEnabled = settings.notificationEnabled,
                        reminderDays = settings.reminderDays,
                        soundEnabled = settings.soundEnabled,
                        vibrationEnabled = settings.vibrationEnabled,
                        selectedLanguage = settings.language
                    )
                }
            }
        }
    }

    fun onThemeChange(isDarkTheme: Boolean) {
        viewModelScope.launch {
            // Update the single source of truth (SettingsManager)
            SettingsManager.setDarkTheme(isDarkTheme)
            // The UI state will be automatically updated via the collect in init
        }
    }

    fun onThemeColorChange(themeColor: ThemeColor) {
        viewModelScope.launch {
            // Update the single source of truth (SettingsManager)
            SettingsManager.setThemeColor(themeColor)
            // The UI state will be automatically updated via the collect in init
        }
    }

    fun onNotificationEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            SettingsManager.setNotificationEnabled(enabled)
        }
    }

    fun onReminderDaysChange(days: Int) {
        viewModelScope.launch {
            SettingsManager.setReminderDays(days)
        }
    }

    fun onSoundEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            SettingsManager.setSoundEnabled(enabled)
        }
    }

    fun onVibrationEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            SettingsManager.setVibrationEnabled(enabled)
        }
    }

    fun onLanguageChange(language: Language) {
        viewModelScope.launch {
            SettingsManager.setLanguage(language)
        }
    }
}

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val selectedThemeColor: ThemeColor = ThemeColor.BLUE,
    val notificationEnabled: Boolean = true,
    val reminderDays: Int = 3,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val selectedLanguage: Language = Language.CHINESE
)

enum class Language(
    val displayName: String,
    val code: String
) {
    CHINESE("繁體中文", "zh-TW"),
    ENGLISH("English", "en"),
    JAPANESE("日本語", "ja")
}

enum class ThemeColor(
    val displayName: String,
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val secondary: Color
) {
    BLUE(
        displayName = "經典藍",
        primary = Color(0xFF2196F3),
        primaryDark = Color(0xFF1976D2),
        primaryLight = Color(0xFF64B5F6),
        secondary = Color(0xFFFFC107)
    ),
    TEAL(
        displayName = "青綠色",
        primary = Color(0xFF227466),
        primaryDark = Color(0xFF175046),
        primaryLight = Color(0xFF529A8E),
        secondary = Color(0xFF2a9486)
    ),
    PURPLE(
        displayName = "優雅紫",
        primary = Color(0xFF9C27B0),
        primaryDark = Color(0xFF7B1FA2),
        primaryLight = Color(0xFFBA68C8),
        secondary = Color(0xFFFFC107)
    ),
    ORANGE(
        displayName = "活力橙",
        primary = Color(0xFFFF6D00),
        primaryDark = Color(0xFFE65100),
        primaryLight = Color(0xFFFF9100),
        secondary = Color(0xFFFFC107)
    ),
    PINK(
        displayName = "櫻花粉",
        primary = Color(0xFFE91E63),
        primaryDark = Color(0xFFC2185B),
        primaryLight = Color(0xFFF06292),
        secondary = Color(0xFFFFC107)
    )
}
