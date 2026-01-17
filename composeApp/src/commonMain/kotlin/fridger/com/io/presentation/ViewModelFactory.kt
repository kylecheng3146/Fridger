package fridger.com.io.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import fridger.com.io.data.remote.HealthDashboardApiService
import fridger.com.io.data.repository.HealthDashboardRepositoryImpl
import fridger.com.io.data.repository.IngredientRepositoryImpl
import fridger.com.io.data.repository.RecipeRepositoryImpl
import fridger.com.io.data.user.DemoUserSessionProvider
import fridger.com.io.presentation.home.HomeViewModel
import fridger.com.io.presentation.settings.SettingsViewModel
import fridger.com.io.presentation.shoppinglist.ShoppingListViewModel
import fridger.com.io.presentation.recipes.RecipesViewModel
import fridger.com.data.remote.RecipeApiService
import fridger.com.domain.translator.MockTranslator
import kotlin.reflect.KClass

/**
 * A factory for creating ViewModels in a multiplatform context.
 */
class ViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: KClass<T>,
        extras: CreationExtras
    ): T =
        when (modelClass) {
            HomeViewModel::class ->
                HomeViewModel(
                    IngredientRepositoryImpl(),
                    RecipeRepositoryImpl(RecipeApiService()),
                    MockTranslator(),
                    HealthDashboardRepositoryImpl(HealthDashboardApiService()),
                    DemoUserSessionProvider,
                ) as T
            SettingsViewModel::class -> SettingsViewModel() as T
            ShoppingListViewModel::class ->
                ShoppingListViewModel() as T
            RecipesViewModel::class ->
                RecipesViewModel(
                    RecipeRepositoryImpl(RecipeApiService())
                ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
        }
}

/**
 * Provides a singleton instance of ViewModelFactory.
 */
object ViewModelFactoryProvider {
    val factory = ViewModelFactory()
}
