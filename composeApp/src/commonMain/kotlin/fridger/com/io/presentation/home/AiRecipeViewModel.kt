package fridger.com.io.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fridger.com.io.data.repository.IngredientRepository
import fridger.com.io.data.repository.RecipeRepository
import fridger.com.io.data.user.UserSessionProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiRecipeUiState(
    val selectedStyles: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val generatedRecipe: RecipeSuggestion? = null,
    val isSavingRecipe: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
)

class AiRecipeViewModel(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val userSessionProvider: UserSessionProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiRecipeUiState())
    val uiState: StateFlow<AiRecipeUiState> = _uiState.asStateFlow()

    // Single-select: tapping a style selects only it; tapping again deselects.
    fun toggleStyle(style: String) {
        _uiState.update { state ->
            val newStyles =
                if (state.selectedStyles.contains(style)) {
                    emptyList()
                } else {
                    listOf(style)
                }
            state.copy(selectedStyles = newStyles)
        }
    }

    fun generateRecipe() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    generatedRecipe = null,
                    isSavingRecipe = false,
                    error = null,
                    isSaved = false,
                )
            }

            try {
                val ingredients =
                    ingredientRepository.getIngredientsStream().firstOrNull()?.map { it.name }
                        ?: emptyList()
                val styles = _uiState.value.selectedStyles

                val result = recipeRepository.generateRecipeFromInventory(ingredients, styles)

                if (result.isSuccess) {
                    val recipe = result.getOrThrow()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generatedRecipe = recipe,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun saveRecipe() {
        val recipe = _uiState.value.generatedRecipe ?: return
        if (_uiState.value.isSavingRecipe || _uiState.value.isSaved) return

        viewModelScope.launch {
            _uiState.update { it.copy(error = null, isSavingRecipe = true) }

            val localResult = recipeRepository.saveRecipeLocally(recipe)
            if (localResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isSavingRecipe = false,
                        error = localResult.exceptionOrNull()?.message ?: "收藏失敗，請稍後再試",
                    )
                }
                return@launch
            }

            val remoteResult =
                recipeRepository.saveRecipeRemotely(recipe, userSessionProvider.accessToken())
            if (remoteResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isSavingRecipe = false,
                        isSaved = true,
                        error = "已收藏到本機，雲端同步稍後重試",
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isSavingRecipe = false, isSaved = true) }
        }
    }
}
