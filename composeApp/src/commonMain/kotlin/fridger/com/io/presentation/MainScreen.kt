package fridger.com.io.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import fridger.com.io.presentation.navigation.AppTab

@Composable
fun BottomNavigationBar() {
    val tabNavigator = LocalTabNavigator.current
    val tabs = listOf(AppTab.Home, AppTab.Recipes, AppTab.ShoppingList)

    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = tabNavigator.current == tab,
                onClick = { tabNavigator.current = tab },
                icon = {
                    val icon = tab.options.icon
                    if (icon != null) {
                        Icon(painter = icon, contentDescription = tab.options.title)
                    }
                },
                label = { Text(tab.options.title) }
            )
        }
    }
}

@Composable
fun MainScreen() {
    TabNavigator(AppTab.Home) {
        Scaffold(
            bottomBar = { BottomNavigationBar() },
            content = { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    CurrentTab()
                }
            }
        )
    }
}
