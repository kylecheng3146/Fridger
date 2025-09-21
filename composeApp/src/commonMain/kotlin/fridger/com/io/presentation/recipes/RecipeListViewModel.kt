package fridger.com.io.presentation.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fridger.com.data.model.remote.MealDto
import fridger.com.io.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RecipeListUiState {
    data object Loading : RecipeListUiState
    data class Success(val meals: List<MealDto>) : RecipeListUiState
    data class Error(val message: String?) : RecipeListUiState
}

class RecipeListViewModel(
    private val recipeRepository: RecipeRepository,
    private val categoryName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeListUiState>(RecipeListUiState.Loading)
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = RecipeListUiState.Loading
            try {
                recipeRepository.getRecipesByCategory(categoryName)
                    .onSuccess { meals ->
                        _uiState.value = RecipeListUiState.Success(meals)
                    }
                    .onFailure { error ->
                        _uiState.value = RecipeListUiState.Error(error.message)
                    }
            } catch (e: Exception) {
                _uiState.value = RecipeListUiState.Error(e.message)
            }
        }
    }
}
