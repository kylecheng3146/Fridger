package fridger.com.io.presentation.home

import kotlin.test.Test
import kotlin.test.assertEquals

class AiRecipeGeneratorSheetTest {
    @Test
    fun `sheet stays compact before generation`() {
        assertEquals(
            COLLAPSED_AI_RECIPE_SHEET_HEIGHT_FRACTION,
            aiRecipeSheetHeightFraction(AiRecipeUiState())
        )
    }

    @Test
    fun `sheet expands while generating`() {
        assertEquals(
            EXPANDED_AI_RECIPE_SHEET_HEIGHT_FRACTION,
            aiRecipeSheetHeightFraction(AiRecipeUiState(isLoading = true)),
        )
    }

    @Test
    fun `sheet expands when generated recipe is available`() {
        assertEquals(
            EXPANDED_AI_RECIPE_SHEET_HEIGHT_FRACTION,
            aiRecipeSheetHeightFraction(
                AiRecipeUiState(
                    generatedRecipe =
                        RecipeSuggestion(
                            recipeId = "recipe-1",
                            title = "蛋炒飯",
                            description = "快速晚餐",
                            ingredients = listOf("蛋", "飯"),
                            instructions = listOf("熱鍋", "拌炒"),
                            cookingTime = "15 分鐘",
                            difficulty = "簡單",
                            servings = 2,
                        ),
                ),
            ),
        )
    }

    @Test
    fun `sheet expands when generation fails so message stays visible`() {
        assertEquals(
            EXPANDED_AI_RECIPE_SHEET_HEIGHT_FRACTION,
            aiRecipeSheetHeightFraction(AiRecipeUiState(error = "生成失敗")),
        )
    }

    @Test
    fun `save icon is enabled only when recipe exists and save is idle`() {
        assertEquals(false, isRecipeSaveEnabled(AiRecipeUiState()))
        assertEquals(
            true,
            isRecipeSaveEnabled(
                AiRecipeUiState(
                    generatedRecipe = sampleRecipeSuggestion(),
                ),
            ),
        )
        assertEquals(
            false,
            isRecipeSaveEnabled(
                AiRecipeUiState(
                    generatedRecipe = sampleRecipeSuggestion(),
                    isSavingRecipe = true,
                ),
            ),
        )
        assertEquals(
            false,
            isRecipeSaveEnabled(
                AiRecipeUiState(
                    generatedRecipe = sampleRecipeSuggestion(),
                    isSaved = true,
                ),
            ),
        )
    }

    @Test
    fun `save icon fills while saving and after saved`() {
        assertEquals(false, isRecipeSaveFilled(AiRecipeUiState()))
        assertEquals(true, isRecipeSaveFilled(AiRecipeUiState(isSavingRecipe = true)))
        assertEquals(true, isRecipeSaveFilled(AiRecipeUiState(isSaved = true)))
    }

    private fun sampleRecipeSuggestion() =
        RecipeSuggestion(
            recipeId = "recipe-1",
            title = "蛋炒飯",
            description = "快速晚餐",
            ingredients = listOf("蛋", "飯"),
            instructions = listOf("熱鍋", "拌炒"),
            cookingTime = "15 分鐘",
            difficulty = "簡單",
            servings = 2,
        )
}
