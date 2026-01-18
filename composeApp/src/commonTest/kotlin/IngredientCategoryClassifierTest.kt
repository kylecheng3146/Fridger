package fridger.com.io.data

import fridger.com.io.data.model.IngredientCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class IngredientCategoryClassifierTest {
    @Test
    fun classifyVegetables() {
        assertEquals(IngredientCategory.VEGETABLES, IngredientCategoryClassifier.classify("菠菜"))
        assertEquals(IngredientCategory.VEGETABLES, IngredientCategoryClassifier.classify("Cherry Tomato"))
    }

    @Test
    fun classifyFruits() {
        assertEquals(IngredientCategory.FRUITS, IngredientCategoryClassifier.classify("芒果"))
        assertEquals(IngredientCategory.FRUITS, IngredientCategoryClassifier.classify("BANANA"))
    }

    @Test
    fun classifyProteinBuckets() {
        assertEquals(IngredientCategory.MEAT, IngredientCategoryClassifier.classify("chicken breast"))
        assertEquals(IngredientCategory.SEAFOOD, IngredientCategoryClassifier.classify("鮭魚"))
        assertEquals(IngredientCategory.DAIRY, IngredientCategoryClassifier.classify("優格"))
    }

    @Test
    fun classifyGrains() {
        assertEquals(IngredientCategory.GRAINS, IngredientCategoryClassifier.classify("米飯"))
        assertEquals(IngredientCategory.GRAINS, IngredientCategoryClassifier.classify("Whole Grain Bread"))
    }

    @Test
    fun fallbackToOthers() {
        assertEquals(IngredientCategory.OTHERS, IngredientCategoryClassifier.classify("醬油"))
        assertEquals(IngredientCategory.OTHERS, IngredientCategoryClassifier.classify(""))
    }

    @Test
    fun avoidEggKeywordFalsePositives() {
        assertEquals(IngredientCategory.VEGETABLES, IngredientCategoryClassifier.classify("Eggplant"))
        assertEquals(IngredientCategory.OTHERS, IngredientCategoryClassifier.classify("蛋糕"))
    }
}
