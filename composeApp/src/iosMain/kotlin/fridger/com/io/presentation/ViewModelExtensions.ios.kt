package fridger.com.io.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * iOS-specific implementation of viewModel that uses the custom factory.
 * This ensures proper ViewModel creation on iOS platform.
 */
@Composable
inline fun <reified T : ViewModel> iosViewModel(): T {
    return viewModel<T>(factory = ViewModelFactoryProvider.factory)
}