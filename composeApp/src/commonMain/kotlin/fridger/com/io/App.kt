package fridger.com.io

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import fridger.com.io.presentation.home.HomeScreen
import fridger.com.io.presentation.navigation.Screen
import fridger.com.io.presentation.settings.SettingsManager
import fridger.com.io.presentation.settings.SettingsScreen
import fridger.com.io.presentation.settings.ThemeColor
import fridger.com.io.ui.theme.FridgerTheme
import androidx.compose.ui.graphics.Color
import fridger.com.io.presentation.shoppinglist.ShoppingListScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("ktlint:standard:function-naming")
@Composable
@Preview
fun App() {
    // Load settings from DataStore - these are now the single source of truth
    val isDarkTheme by SettingsManager.isDarkTheme.collectAsState(initial = false)
    val selectedThemeColor by SettingsManager.themeColor.collectAsState(initial = ThemeColor.BLUE)

    val navigationItems = remember { listOf(Screen.Home, Screen.ShoppingList) }
    var currentScreen: Screen by remember { mutableStateOf(Screen.Home) }

    FridgerTheme(
        darkTheme = isDarkTheme,
        themeColor = selectedThemeColor
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                ) {
                    navigationItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                val stateHolder = androidx.compose.runtime.saveable.rememberSaveableStateHolder()
                stateHolder.SaveableStateProvider(currentScreen.route) {
                    when (currentScreen) {
                        is Screen.Home -> {
                            HomeScreen(
                                onSettingsClick = { currentScreen = Screen.Settings }
                            )
                        }
                        is Screen.ShoppingList -> {
                            ShoppingListScreen()
                        }
                        is Screen.Settings -> {
                            SettingsScreen(
                                onBackClick = { currentScreen = Screen.Home }
                            )
                        }
                    }
                }
            }
        }
    }
}
