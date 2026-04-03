package fridger.com.io.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import fridger.com.data.remote.RecipeApiService
import fridger.com.domain.translator.MockTranslator
import fridger.com.io.data.analytics.ConsoleHealthDashboardAnalytics
import fridger.com.io.data.remote.HealthDashboardApiService
import fridger.com.io.data.repository.HealthDashboardRepositoryImpl
import fridger.com.io.data.repository.IngredientRepositoryImpl
import fridger.com.io.data.repository.RecipeRepositoryImpl
import fridger.com.io.data.settings.DataStoreHealthDashboardPreferences
import fridger.com.io.data.settings.SharedDataStoreProvider
import fridger.com.io.data.user.AppUserSessionProvider
import fridger.com.io.data.remote.ShoppingSyncApiService
import fridger.com.io.data.remote.ShoppingListApiService
import fridger.com.io.data.sync.ApiShoppingSyncProcessor
import fridger.com.io.presentation.home.HomeViewModel
import fridger.com.io.presentation.recipes.RecipesViewModel
import fridger.com.io.presentation.settings.SettingsViewModel
import fridger.com.io.presentation.shoppinglist.ShoppingListViewModel
import kotlin.reflect.KClass

/**
 * A factory for creating ViewModels in a multiplatform context.
 */
class ViewModelFactory : ViewModelProvider.Factory {
    private val dashboardPreferences by lazy { DataStoreHealthDashboardPreferences(SharedDataStoreProvider.instance) }
    private val dashboardAnalytics by lazy { ConsoleHealthDashboardAnalytics() }

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
                    AppUserSessionProvider,
                    dashboardPreferences,
                    dashboardAnalytics,
                ) as T
            SettingsViewModel::class -> SettingsViewModel() as T
            ShoppingListViewModel::class ->
                ShoppingListViewModel(
                    syncProcessor =
                        ApiShoppingSyncProcessor(
                            listIdProvider = {
                                // Use current list from ShoppingListsManager if available
                                fridger.com.io.data.settings.ShoppingListsManager.currentList?.id.orEmpty()
                            },
                            tokenProvider = {
                                AppUserSessionProvider.accessToken()
                            },
                            api = ShoppingSyncApiService()
                        ),
                    listApi = ShoppingListApiService(),
                    sessionProvider = AppUserSessionProvider
                ) as T
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
