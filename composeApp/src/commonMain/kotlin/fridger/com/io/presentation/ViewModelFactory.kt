package fridger.com.io.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import fridger.com.io.presentation.home.HomeViewModel
import fridger.com.io.presentation.settings.SettingsViewModel
import kotlin.reflect.KClass

/**
 * A factory for creating ViewModels in a multiplatform context.
 */
class ViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return when (modelClass) {
            HomeViewModel::class -> HomeViewModel(fridger.com.io.data.repository.IngredientRepositoryImpl()) as T
            SettingsViewModel::class -> SettingsViewModel() as T
            fridger.com.io.presentation.shoppinglist.ShoppingListViewModel::class -> fridger.com.io.presentation.shoppinglist.ShoppingListViewModel() as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
        }
    }

}

/**
 * Provides a singleton instance of ViewModelFactory.
 */
object ViewModelFactoryProvider {
    val factory = ViewModelFactory()
}
