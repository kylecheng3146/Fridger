package fridger.com.io.presentation.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fridger.com.data.model.remote.MealDto
import fridger.com.io.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RecipeDetailUiState {
    data object Loading : RecipeDetailUiState

    data class Success(
        val meal: MealDto
    ) : RecipeDetailUiState

    data class Error(
        val message: String?
    ) : RecipeDetailUiState
}

class RecipeDetailViewModel(
    private val recipeRepository: RecipeRepository,
    private val mealId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow<RecipeDetailUiState>(RecipeDetailUiState.Loading)
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = RecipeDetailUiState.Loading
            try {
                recipeRepository
                    .getRecipeById(mealId)
                    .onSuccess { meal ->
                        _uiState.value = RecipeDetailUiState.Success(meal)
                    }.onFailure { error ->
                        _uiState.value = RecipeDetailUiState.Error(error.message)
                    }
            } catch (e: Exception) {
                _uiState.value = RecipeDetailUiState.Error(e.message)
            }
        }
    }
}
