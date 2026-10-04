package fridger.com.io.data

object QuickAddCatalog {
    // Centralized list of quick-add ingredient names
    val allNames: List<String> =
        listOf(
            "牛奶",
            "雞蛋",
            "麵包",
            "蘋果",
            "香蕉",
            "雞胸肉",
            "牛肉",
            "豬肉",
            "鮭魚",
            "蝦子",
            "豆腐",
            "優格",
            "起司",
            "馬鈴薯",
            "胡蘿蔔",
            "番茄",
            "洋蔥",
            "大蒜",
            "生菜",
            "黃瓜",
            "彩椒",
            "米",
            "義大利麵",
            "麵條",
            "醬油",
            "鹽",
            "糖",
            "麵粉",
            "食用油",
            "水",
        )

    // The recipe catalog uses English ingredient names; preserve unknown names for manual search.
    fun recipeSearchName(name: String): String = recipeNames[name.trim()] ?: name.trim()

    private val recipeNames = mapOf(
        "牛奶" to "milk", "雞蛋" to "eggs", "麵包" to "bread", "蘋果" to "apple",
        "香蕉" to "banana", "雞胸肉" to "chicken breast", "牛肉" to "beef", "豬肉" to "pork",
        "鮭魚" to "salmon", "蝦子" to "prawns", "豆腐" to "tofu", "優格" to "yogurt",
        "起司" to "cheese", "馬鈴薯" to "potatoes", "胡蘿蔔" to "carrots", "番茄" to "tomatoes",
        "洋蔥" to "onion", "大蒜" to "garlic", "生菜" to "lettuce", "黃瓜" to "cucumber",
        "彩椒" to "pepper", "米" to "rice", "義大利麵" to "spaghetti", "麵條" to "noodles",
        "醬油" to "soy sauce", "鹽" to "salt", "糖" to "sugar", "麵粉" to "flour",
        "食用油" to "vegetable oil", "水" to "water",
    )

}
