package fridger.com.io.data

import fridger.com.io.data.model.IngredientCategory

/**
 * Lightweight keyword-based classifier that infers the ingredient category directly from a name.
 * This unlocks better nutrition distribution when the local database does not persist categories.
 */
object IngredientCategoryClassifier {
    fun classify(rawName: String): IngredientCategory {
        val name = rawName.trim().lowercase()
        if (name.isEmpty()) return IngredientCategory.OTHERS

        return when {
            matches(name, dairyKeywords) -> IngredientCategory.DAIRY
            matches(name, seafoodKeywords) -> IngredientCategory.SEAFOOD
            matches(name, meatKeywords) -> IngredientCategory.MEAT
            matches(name, vegetableKeywords) -> IngredientCategory.VEGETABLES
            matches(name, fruitKeywords) -> IngredientCategory.FRUITS
            matches(name, grainKeywords) -> IngredientCategory.GRAINS
            else -> IngredientCategory.OTHERS
        }
    }

    private fun matches(
        name: String,
        keywords: List<Keyword>
    ): Boolean = keywords.any { keyword -> keyword.matches(name) }

    private val dairyKeywords =
        listOf(
            wholeWord("egg"),
            wholeWord("eggs"),
            substring("奶"),
            substring("milk"),
            substring("乳酪"),
            substring("起司"),
            substring("cheese"),
            substring("優格"),
            substring("酸奶"),
            substring("yogurt"),
            substring("奶油"),
            substring("butter"),
            substring("cream"),
            substring("鮮奶油"),
            substring("豆漿"),
            substring("soy milk"),
            substring("雞蛋"),
            substring("鴨蛋"),
            substring("蛋白"),
            exact("蛋"),
        )

    private val seafoodKeywords =
        keywords(
            "魚",
            "salmon",
            "鮭",
            "鮪",
            "tuna",
            "prawn",
            "shrimp",
            "蝦",
            "蟹",
            "crab",
            "牡蠣",
            "蚌",
            "clam",
            "蛤",
            "貝",
        )

    private val meatKeywords =
        keywords(
            "雞",
            "chicken",
            "牛",
            "beef",
            "豬",
            "pork",
            "羊",
            "lamb",
            "肉",
            "火腿",
            "ham",
            "培根",
            "bacon",
            "香腸",
            "sausage",
            "steak",
            "排骨",
            "里肌",
            "turkey",
            "鴨",
            "duck",
            "鵝",
            "tofu",
            "豆腐",
            "豆干",
            "豆皮",
        )

    private val vegetableKeywords =
        keywords(
            "蔬",
            "菜",
            "番茄",
            "西紅柿",
            "tomato",
            "馬鈴薯",
            "土豆",
            "potato",
            "洋蔥",
            "onion",
            "大蒜",
            "蒜",
            "garlic",
            "胡蘿蔔",
            "紅蘿蔔",
            "carrot",
            "黃瓜",
            "小黃瓜",
            "cucumber",
            "生菜",
            "lettuce",
            "菠菜",
            "spinach",
            "芹菜",
            "celery",
            "彩椒",
            "青椒",
            "bell pepper",
            "pepper",
            "辣椒",
            "chili",
            "chilli",
            "玉米",
            "corn",
            "花椰菜",
            "西蘭花",
            "broccoli",
            "高麗菜",
            "捲心菜",
            "cabbage",
            "豆芽",
            "sprout",
            "蘆筍",
            "asparagus",
            "蘑菇",
            "香菇",
            "mushroom",
            "茄子",
            "eggplant",
            "番瓜",
            "南瓜",
            "pumpkin",
            "地瓜",
            "番薯",
            "sweet potato",
            "薑",
            "ginger",
            "韭菜",
            "leek",
        )

    private val fruitKeywords =
        keywords(
            "蘋果",
            "apple",
            "香蕉",
            "banana",
            "橙",
            "柳橙",
            "橘",
            "orange",
            "草莓",
            "strawberry",
            "莓",
            "berry",
            "藍莓",
            "blueberry",
            "葡萄",
            "grape",
            "西瓜",
            "watermelon",
            "鳳梨",
            "菠蘿",
            "pineapple",
            "檸檬",
            "lemon",
            "萊姆",
            "lime",
            "酪梨",
            "牛油果",
            "avocado",
            "奇異果",
            "kiwi",
            "芒果",
            "mango",
            "桃",
            "peach",
            "梨",
            "pear",
            "火龍果",
            "dragon fruit",
            "百香果",
            "passion fruit",
        )

    private val grainKeywords =
        keywords(
            "米",
            "rice",
            "飯",
            "麥",
            "oat",
            "燕麥",
            "oatmeal",
            "穀",
            "grain",
            "麵",
            "noodle",
            "noodles",
            "麵條",
            "義大利麵",
            "pasta",
            "spaghetti",
            "通心麵",
            "macaroni",
            "麵包",
            "bread",
            "饅頭",
            "bun",
            "餅",
            "wrap",
            "餃",
            "dumpling",
            "flour",
            "麵粉",
            "糯米",
            "雜糧",
            "cereal",
            "餛飩",
            "餅乾",
            "cracker",
        )

    private fun keywords(vararg values: String): List<Keyword> = values.map { substring(it) }

    private fun substring(value: String) = Keyword(value, MatchMode.SUBSTRING)

    private fun wholeWord(value: String) = Keyword(value, MatchMode.WHOLE_WORD)

    private fun exact(value: String) = Keyword(value, MatchMode.EXACT)

    private data class Keyword(
        val value: String,
        val matchMode: MatchMode,
    ) {
        fun matches(name: String): Boolean =
            when (matchMode) {
                MatchMode.SUBSTRING -> name.contains(value)
                MatchMode.WHOLE_WORD -> containsWholeWord(name, value)
                MatchMode.EXACT -> name == value
            }
    }

    private enum class MatchMode {
        SUBSTRING,
        WHOLE_WORD,
        EXACT,
    }

    private fun containsWholeWord(
        input: String,
        keyword: String,
    ): Boolean {
        if (keyword.isEmpty()) return false
        val pattern = Regex("(?<!\\p{L})${Regex.escape(keyword)}(?!\\p{L})")
        return pattern.containsMatchIn(input)
    }
}
