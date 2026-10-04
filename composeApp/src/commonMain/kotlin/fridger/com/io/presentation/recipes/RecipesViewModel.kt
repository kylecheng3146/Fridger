package fridger.com.io.presentation.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fridger.com.data.model.remote.MealDto
import fridger.com.data.model.remote.RecipeCategoryDto
import fridger.com.io.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface RecipesUiState {
    data object Loading : RecipesUiState

    data class Categories(
        val categories: List<RecipeCategoryDto>
    ) : RecipesUiState

    data class Meals(
        val meals: List<MealDto>
    ) : RecipesUiState

    data class Error(
        val message: String?
    ) : RecipesUiState
}

object DashboardRecipeSearchIntent {
    private val _ingredient = MutableStateFlow<String?>(null)
    val ingredient: StateFlow<String?> = _ingredient.asStateFlow()

    fun request(ingredient: String) {
        _ingredient.value = ingredient
    }

    fun consume(ingredient: String) {
        if (_ingredient.value == ingredient) _ingredient.value = null
    }
}

class RecipesViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<RecipesUiState>(RecipesUiState.Loading)
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _ingredientSearch = MutableStateFlow(false)
    val ingredientSearch: StateFlow<Boolean> = _ingredientSearch.asStateFlow()

    private val ingredientRequest = MutableStateFlow(0)
    private var cachedCategories: List<RecipeCategoryDto> = emptyList()

    init {
        // Load categories initially
        viewModelScope.launch {
            _uiState.value = RecipesUiState.Loading
            try {
                recipeRepository
                    .getRecipeCategories()
                    .onSuccess { categories ->
                        cachedCategories = categories
                        if (_searchQuery.value.isBlank()) _uiState.value = RecipesUiState.Categories(categories)
                    }.onFailure { error ->
                        _uiState.value = RecipesUiState.Error(error.message)
                    }
            } catch (e: Exception) {
                _uiState.value = RecipesUiState.Error(e.message)
            }
        }

        // Observe search query with debounce
        viewModelScope.launch {
            combine(searchQuery, ingredientSearch, ingredientRequest) { query, byIngredient, request -> Triple(query, byIngredient, request) }
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { (query, byIngredient, _) ->
                    if (query.isBlank()) {
                        // Show categories again
                        _uiState.value = RecipesUiState.Categories(cachedCategories)
                    } else {
                        _uiState.value = RecipesUiState.Loading
                        try {
                            val searchResult = if (byIngredient) {
                                recipeRepository.getRecipesByIngredient(fridger.com.io.data.QuickAddCatalog.recipeSearchName(query))
                            } else {
                                recipeRepository.searchRecipesByName(query)
                            }
                            searchResult
                                .onSuccess { meals ->
                                    _uiState.value = RecipesUiState.Meals(meals)
                                }.onFailure { error ->
                                    _uiState.value = RecipesUiState.Error(error.message)
                                }
                        } catch (e: Exception) {
                            _uiState.value = RecipesUiState.Error(e.message)
                        }
                    }
                }
        }

        viewModelScope.launch {
            DashboardRecipeSearchIntent.ingredient.collect { ingredient ->
                if (!ingredient.isNullOrBlank()) {
                    _ingredientSearch.value = true
                    _searchQuery.value = ingredient
                    ingredientRequest.value++
                    DashboardRecipeSearchIntent.consume(ingredient)
                }
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _ingredientSearch.value = false
        _searchQuery.value = newQuery
    }
}
