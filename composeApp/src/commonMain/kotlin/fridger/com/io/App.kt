package fridger.com.io

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fridger.com.io.presentation.home.HomeScreen
import fridger.com.io.ui.theme.FridgerTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("ktlint:standard:function-naming")
@Composable
@Preview
fun App() {
    var isDarkTheme by remember { mutableStateOf(false) }
    
    FridgerTheme(darkTheme = isDarkTheme) {
        HomeScreen(
            onThemeToggle = { isDarkTheme = !isDarkTheme }
        )
    }
}
