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
        // Load saved settings from SettingsManager
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
            SettingsManager.setDarkTheme(isDarkTheme)
            _uiState.update { it.copy(isDarkTheme = isDarkTheme) }
        }
    }

    fun onThemeColorChange(themeColor: ThemeColor) {
        viewModelScope.launch {
            SettingsManager.setThemeColor(themeColor)
            _uiState.update { it.copy(selectedThemeColor = themeColor) }
        }
    }

    fun onNotificationEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            SettingsManager.setNotificationEnabled(enabled)
            _uiState.update { it.copy(notificationEnabled = enabled) }
        }
    }

    fun onReminderDaysChange(days: Int) {
        viewModelScope.launch {
            SettingsManager.setReminderDays(days)
            _uiState.update { it.copy(reminderDays = days) }
        }
    }

    fun onSoundEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            SettingsManager.setSoundEnabled(enabled)
            _uiState.update { it.copy(soundEnabled = enabled) }
        }
    }

    fun onVibrationEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            SettingsManager.setVibrationEnabled(enabled)
            _uiState.update { it.copy(vibrationEnabled = enabled) }
        }
    }

    fun onLanguageChange(language: Language) {
        viewModelScope.launch {
            SettingsManager.setLanguage(language)
            _uiState.update { it.copy(selectedLanguage = language) }
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
    val primaryLight: Color
) {
    BLUE(
        displayName = "經典藍",
        primary = Color(0xFF2196F3),
        primaryDark = Color(0xFF1976D2),
        primaryLight = Color(0xFF64B5F6)
    ),
    TEAL(
        displayName = "青綠色",
        primary = Color(0xFF009688),
        primaryDark = Color(0xFF00796B),
        primaryLight = Color(0xFF4DB6AC)
    ),
    PURPLE(
        displayName = "優雅紫",
        primary = Color(0xFF9C27B0),
        primaryDark = Color(0xFF7B1FA2),
        primaryLight = Color(0xFFBA68C8)
    ),
    ORANGE(
        displayName = "活力橙",
        primary = Color(0xFFFF6D00),
        primaryDark = Color(0xFFE65100),
        primaryLight = Color(0xFFFF9100)
    ),
    PINK(
        displayName = "櫻花粉",
        primary = Color(0xFFE91E63),
        primaryDark = Color(0xFFC2185B),
        primaryLight = Color(0xFFF06292)
    )
}
