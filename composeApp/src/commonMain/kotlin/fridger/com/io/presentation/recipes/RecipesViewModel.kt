package fridger.com.io.presentation.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fridger.com.data.model.remote.RecipeCategoryDto
import fridger.com.data.model.remote.MealDto
import fridger.com.io.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

sealed interface RecipesUiState {
    data object Loading : RecipesUiState
    data class Categories(val categories: List<RecipeCategoryDto>) : RecipesUiState
    data class Meals(val meals: List<MealDto>) : RecipesUiState
    data class Error(val message: String?) : RecipesUiState
}

class RecipesViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipesUiState>(RecipesUiState.Loading)
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var cachedCategories: List<RecipeCategoryDto> = emptyList()

    init {
        // Load categories initially
        viewModelScope.launch {
            _uiState.value = RecipesUiState.Loading
            try {
                recipeRepository.getRecipeCategories()
                    .onSuccess { categories ->
                        cachedCategories = categories
                        _uiState.value = RecipesUiState.Categories(categories)
                    }
                    .onFailure { error ->
                        _uiState.value = RecipesUiState.Error(error.message)
                    }
            } catch (e: Exception) {
                _uiState.value = RecipesUiState.Error(e.message)
            }
        }

        // Observe search query with debounce
        viewModelScope.launch {
            searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        // Show categories again
                        _uiState.value = RecipesUiState.Categories(cachedCategories)
                    } else {
                        _uiState.value = RecipesUiState.Loading
                        try {
                            recipeRepository.searchRecipesByName(query)
                                .onSuccess { meals ->
                                    _uiState.value = RecipesUiState.Meals(meals)
                                }
                                .onFailure { error ->
                                    _uiState.value = RecipesUiState.Error(error.message)
                                }
                        } catch (e: Exception) {
                            _uiState.value = RecipesUiState.Error(e.message)
                        }
                    }
                }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
