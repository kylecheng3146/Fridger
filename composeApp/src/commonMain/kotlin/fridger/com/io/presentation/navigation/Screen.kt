package fridger.com.io.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen("home", "首頁", Icons.Default.Home)

    data object ShoppingList : Screen("shopping_list", "購物清單", Icons.Default.ShoppingCart)

    // Not shown in bottom bar, but supported for app navigation
    data object Settings : Screen("settings", "設定", Icons.Default.Settings)
}
