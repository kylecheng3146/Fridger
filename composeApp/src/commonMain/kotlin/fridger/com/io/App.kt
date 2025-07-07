package fridger.com.io

import androidx.compose.runtime.*
import fridger.com.io.presentation.home.HomeScreen
import fridger.com.io.presentation.settings.SettingsManager
import fridger.com.io.presentation.settings.SettingsScreen
import fridger.com.io.presentation.settings.ThemeColor
import fridger.com.io.ui.theme.FridgerTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("ktlint:standard:function-naming")
@Composable
@Preview
fun App() {
    // Load settings from DataStore
    val isDarkTheme by SettingsManager.isDarkTheme.collectAsState(initial = false)
    val selectedThemeColor by SettingsManager.themeColor.collectAsState(initial = ThemeColor.BLUE)

    var currentScreen by remember { mutableStateOf(Screen.Home) }

    FridgerTheme(
        darkTheme = isDarkTheme,
        themeColor = selectedThemeColor
    ) {
        when (currentScreen) {
            Screen.Home -> {
                HomeScreen(
                    onSettingsClick = { currentScreen = Screen.Settings }
                )
            }

            Screen.Settings -> {
                SettingsScreen(
                    isDarkTheme = isDarkTheme,
                    selectedThemeColor = selectedThemeColor,
                    onThemeChange = { /* Handled by ViewModel */ },
                    onThemeColorChange = { /* Handled by ViewModel */ },
                    onBackClick = { currentScreen = Screen.Home }
                )
            }
        }
    }
}

enum class Screen {
    Home,
    Settings
}
