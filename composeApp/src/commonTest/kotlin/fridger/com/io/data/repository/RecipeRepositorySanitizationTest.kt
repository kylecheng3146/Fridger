package fridger.com.io.data.repository

import fridger.shared.recipe.GeneratedRecipe
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipeRepositorySanitizationTest {
    @Test
    fun `sanitizeRecipeSuggestion removes leaked json fragments from structured fields`() {
        val sanitized =
            sanitizeRecipeSuggestion(
                GeneratedRecipe(
                    recipeId = "7c16583e-8f1c-4448-b15b-72a13ae1497d",
                    title = "\"title\": \"牛肉燜米\",",
                    description = "\"description\": \"簡單的中式牛肉燜米，使用牛肉和米，口感豐富\"",
                    ingredients =
                        listOf(
                            "\"ingredients\": [",
                            "\"牛肉\",",
                            "\"米\"",
                            "],",
                        ),
                    instructions =
                        listOf(
                            "\"instructions\": [",
                            "\"準備牛肉和米，將牛肉切成小塊，米洗淨\",",
                            "\"在鍋中加入油，炒牛肉至變色\",",
                            "\"加入米和適量水，燜煮至米熟牛肉爛\"",
                            "]",
                        ),
                    cookingTime = "\"cookingTime\": \"30分鐘\"",
                    difficulty = "\"difficulty\": \"簡單\"",
                    servings = 1,
                ),
            )

        assertEquals("牛肉燜米", sanitized.title)
        assertEquals("簡單的中式牛肉燜米，使用牛肉和米，口感豐富", sanitized.description)
        assertEquals(listOf("牛肉", "米"), sanitized.ingredients)
        assertEquals(
            listOf(
                "準備牛肉和米，將牛肉切成小塊，米洗淨",
                "在鍋中加入油，炒牛肉至變色",
                "加入米和適量水，燜煮至米熟牛肉爛",
            ),
            sanitized.instructions,
        )
        assertEquals("30分鐘", sanitized.cookingTime)
        assertEquals("簡單", sanitized.difficulty)
    }

    @Test
    fun `sanitizeRecipeSuggestion preserves valid quoted text and bracketed ingredient labels`() {
        val sanitized =
            sanitizeRecipeSuggestion(
                GeneratedRecipe(
                    recipeId = "recipe-2",
                    title = "主廚『特製』燉飯",
                    description = "保留 [可選] 配料與 \"香氣\" 描述",
                    ingredients = listOf("[可選] 蔥花", "黑胡椒 \"少許\""),
                    instructions = listOf("加入 \"香氣\" 更明顯的奶油", "最後撒上 [可選] 蔥花"),
                    cookingTime = "25分鐘",
                    difficulty = "中等",
                    servings = 2,
                ),
            )

        assertEquals(listOf("[可選] 蔥花", "黑胡椒 \"少許\""), sanitized.ingredients)
        assertEquals(
            listOf("加入 \"香氣\" 更明顯的奶油", "最後撒上 [可選] 蔥花"),
            sanitized.instructions,
        )
    }

    @Test
    fun `sanitizeRecipeSuggestion keeps decimal ingredient quantities and strips only numbered instructions`() {
        val sanitized =
            sanitizeRecipeSuggestion(
                GeneratedRecipe(
                    recipeId = "recipe-3",
                    title = "奶香燉飯",
                    description = "帶有奶油香氣",
                    ingredients = listOf("1.5 杯牛奶", "2 顆蛋"),
                    instructions = listOf("1. 先加熱鍋子", "2. 倒入 1.5 杯牛奶"),
                    cookingTime = "20分鐘",
                    difficulty = "簡單",
                    servings = 1,
                ),
            )

        assertEquals(listOf("1.5 杯牛奶", "2 顆蛋"), sanitized.ingredients)
        assertEquals(listOf("先加熱鍋子", "倒入 1.5 杯牛奶"), sanitized.instructions)
    }
}
