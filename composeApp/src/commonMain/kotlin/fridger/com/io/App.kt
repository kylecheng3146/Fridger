package fridger.com.io

import androidx.compose.runtime.Composable
import fridger.com.io.presentation.home.HomeScreen
import fridger.com.io.ui.theme.FridgerTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("ktlint:standard:function-naming")
@Composable
@Preview
fun App() {
    FridgerTheme {
        HomeScreen()
    }
}
