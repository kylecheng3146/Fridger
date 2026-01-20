package fridger.com.io.presentation.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

class RecipeDetailScreen(
    private val mealId: String
) : Screen {
    @Composable
    override fun Content() {
        val vm: RecipeDetailViewModel =
            viewModel(
                factory =
                    object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(
                            modelClass: KClass<T>,
                            extras: CreationExtras
                        ): T {
                            val repo = RecipeRepositoryImpl(RecipeApiService())
                            @Suppress("UNCHECKED_CAST")
                            return RecipeDetailViewModel(repo, mealId) as T
                        }
                    }
            )

        val navigator = LocalNavigator.currentOrThrow
        val uiState by vm.uiState.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top back button
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                }
            }

            when (val state = uiState) {
                is RecipeDetailUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is RecipeDetailUiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message ?: "載入失敗", color = MaterialTheme.colorScheme.error) }
                is RecipeDetailUiState.Success -> {
                    val meal = state.meal
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = meal.strMealThumb,
                            contentDescription = meal.strMeal,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                        )
                        Text(text = meal.strMeal ?: "", style = MaterialTheme.typography.titleLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = meal.strCategory ?: "", style = MaterialTheme.typography.bodyMedium)
                            Text(text = meal.strArea ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(text = meal.strInstructions ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
