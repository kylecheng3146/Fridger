package fridger.shared.health

import kotlinx.serialization.Serializable

@Serializable
enum class InventoryCategory(val displayName: String) {
    VEGETABLES("蔬菜"),
    FRUITS("水果"),
    MEAT("肉類"),
    DAIRY("乳品／蛋"),
    SEAFOOD("海鮮"),
    GRAINS("穀物／澱粉"),
    OTHERS("其他"),
    UNCATEGORIZED("未分類"),
}

fun InventoryCategory.toNutritionCategory(): NutritionCategory? =
    when (this) {
        InventoryCategory.VEGETABLES, InventoryCategory.FRUITS -> NutritionCategory.PRODUCE
        InventoryCategory.MEAT, InventoryCategory.SEAFOOD, InventoryCategory.DAIRY -> NutritionCategory.PROTEIN
        InventoryCategory.GRAINS -> NutritionCategory.REFINED_GRAIN
        InventoryCategory.OTHERS -> NutritionCategory.OTHER
        InventoryCategory.UNCATEGORIZED -> null
    }
