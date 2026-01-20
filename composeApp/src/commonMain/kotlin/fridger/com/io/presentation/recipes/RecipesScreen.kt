package fridger.com.io.presentation.recipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage

@Composable
fun RecipesScreen(viewModel: RecipesViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    Column(modifier = Modifier.fillMaxSize()) {
        fridger.com.io.presentation.components
            .AppTopTitle(title = "食譜")
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            placeholder = { Text("搜尋食譜...") }
        )

        when (uiState) {
            is RecipesUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RecipesUiState.Error -> {
                val message = (uiState as RecipesUiState.Error).message ?: "載入失敗"
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
            }
            is RecipesUiState.Categories -> {
                val categories = (uiState as RecipesUiState.Categories).categories
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            name = category.strCategory,
                            imageUrl = category.strCategoryThumb,
                            onClick = { navigator.push(RecipeListScreen(category.strCategory)) }
                        )
                    }
                }
            }
            is RecipesUiState.Meals -> {
                val meals = (uiState as RecipesUiState.Meals).meals
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(meals) { meal ->
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

@Composable
private fun CategoryCard(
    name: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )
        Box(modifier = Modifier.padding(8.dp)) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
