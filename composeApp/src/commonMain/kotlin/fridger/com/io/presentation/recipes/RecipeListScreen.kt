package fridger.com.io.presentation.recipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import fridger.com.data.remote.RecipeApiService
import fridger.com.io.data.repository.RecipeRepositoryImpl
import kotlin.reflect.KClass

class RecipeListScreen(
    private val categoryName: String
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // Provide a simple per-screen factory to pass params
        val vm: RecipeListViewModel =
            viewModel(
                factory =
                    object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(
                            modelClass: KClass<T>,
                            extras: CreationExtras
                        ): T {
                            val repo = RecipeRepositoryImpl(RecipeApiService())
                            @Suppress("UNCHECKED_CAST")
                            return RecipeListViewModel(repo, categoryName) as T
                        }
                    }
            )

        val uiState by vm.uiState.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top title bar
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            when (val state = uiState) {
                is RecipeListUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is RecipeListUiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message ?: "載入失敗", color = MaterialTheme.colorScheme.error) }
                is RecipeListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.meals) { meal ->
                            Card(
                                modifier =
                                    Modifier.fillMaxWidth().clickable {
                                        meal.idMeal?.let { navigator.push(RecipeDetailScreen(it)) }
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    AsyncImage(
                                        model = meal.strMealThumb,
                                        contentDescription = meal.strMeal,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth().height(180.dp)
                                    )
                                    Text(
                                        text = meal.strMeal ?: "",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
