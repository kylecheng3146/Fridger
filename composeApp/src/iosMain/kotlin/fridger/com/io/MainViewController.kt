package fridger.com.io

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import fridger.com.io.presentation.settings.SettingsManager

fun MainViewController(onThemeChange: (Boolean) -> Unit = {}) =
ComposeUIViewController {
    val isDarkTheme by SettingsManager.isDarkTheme.collectAsState(initial = false)

    LaunchedEffect(isDarkTheme) {
        onThemeChange(isDarkTheme)
    }

    App()
}
