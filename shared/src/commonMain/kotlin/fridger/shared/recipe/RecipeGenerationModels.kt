package fridger.shared.recipe

import kotlinx.serialization.Serializable

@Serializable
data class GenerateRecipeRequest(
    val ingredients: List<String>,
    val styles: List<String> = emptyList(),
)

@Serializable
data class GeneratedRecipe(
    val recipeId: String = "",
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val cookingTime: String,
    val difficulty: String,
    val servings: Int,
)

@Serializable
enum class RecipeFeedbackType {
    LIKE,
    DISLIKE,
}

@Serializable
data class SubmitRecipeFeedbackRequest(
    val recipeId: String,
    val feedbackType: RecipeFeedbackType,
)

@Serializable
data class SaveRecipeRequest(
    val recipeId: String,
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val cookingTime: String,
    val difficulty: String,
    val servings: Int,
)
