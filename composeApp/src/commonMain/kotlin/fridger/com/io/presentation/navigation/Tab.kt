package fridger.com.io.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import fridger.com.io.presentation.ViewModelFactoryProvider
import fridger.com.io.presentation.home.HomeScreen
import fridger.com.io.presentation.recipes.RecipesScreen
import fridger.com.io.presentation.recipes.RecipesViewModel
import fridger.com.io.presentation.shoppinglist.ShoppingListScreen

private object RecipesRootScreen : Screen {
    @Composable
    override fun Content() {
        val vm: RecipesViewModel = viewModel(factory = ViewModelFactoryProvider.factory)
        RecipesScreen(viewModel = vm)
    }
}

sealed class AppTab(
    private val tabKey: String,
    private val title: String,
    private val icon: ImageVector
) : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val tabTitle = title
            val painter =
                androidx.compose.ui.graphics.vector
                    .rememberVectorPainter(icon)
            return remember(tabKey) { TabOptions(index = 0u, title = tabTitle, icon = painter) }
        }

    object Home : AppTab("home", "首頁", Icons.Filled.Home) {
        @Composable
        override fun Content() {
            HomeScreen()
        }
    }

    object Recipes : AppTab("recipes", "食譜", Icons.Filled.Restaurant) {
        @Composable
        override fun Content() {
            Navigator(RecipesRootScreen)
        }
    }

    object ShoppingList : AppTab("shopping_list", "購物清單", Icons.Filled.ShoppingCart) {
        @Composable
        override fun Content() {
            ShoppingListScreen()
        }
    }
}
