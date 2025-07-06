package fridger.com.io

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fridger.com.io.presentation.home.HomeScreen
import fridger.com.io.presentation.settings.SettingsScreen
import fridger.com.io.presentation.settings.ThemeColor
import fridger.com.io.ui.theme.FridgerTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("ktlint:standard:function-naming")
@Composable
@Preview
fun App() {
    var isDarkTheme by remember { mutableStateOf(false) }
    var selectedThemeColor by remember { mutableStateOf(ThemeColor.BLUE) }
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
                    onThemeChange = { isDarkTheme = it },
                    onThemeColorChange = { selectedThemeColor = it },
                    onBackClick = { currentScreen = Screen.Home }
                )
            }
        }
    }
}

enum class Screen {
    Home, Settings
}
