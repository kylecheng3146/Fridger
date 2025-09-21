package fridger.com.io

import androidx.compose.runtime.*
import fridger.com.io.presentation.settings.SettingsManager
import fridger.com.io.presentation.settings.ThemeColor
import fridger.com.io.ui.theme.FridgerTheme
import fridger.com.io.presentation.MainScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("ktlint:standard:function-naming")
@Composable
@Preview
fun App() {
    val isDarkTheme by SettingsManager.isDarkTheme.collectAsState(initial = false)
    val selectedThemeColor by SettingsManager.themeColor.collectAsState(initial = ThemeColor.BLUE)

    FridgerTheme(
        darkTheme = isDarkTheme,
        themeColor = selectedThemeColor
    ) {
        MainScreen()
    }
}
