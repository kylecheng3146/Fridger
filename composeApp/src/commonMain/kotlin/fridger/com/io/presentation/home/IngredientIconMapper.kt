package fridger.com.io.presentation.home

/**
 * Maps ingredient names to emoji icons. Keep this pure and easily testable.
 */
object IngredientIconMapper {
    fun getIcon(name: String): String {
        val n = name.lowercase()
        return when {
            // Dairy & eggs
            n.contains("蛋") || n.contains("egg") || n.contains("eggs") -> "🥚"
            n.contains("奶") || n.contains("milk") -> "🥛"
            n.contains("乳酪") || n.contains("起司") || n.contains("cheese") -> "🧀"
            n.contains("優格") || n.contains("酸奶") || n.contains("yogurt") -> "🥣"
            n.contains("奶油") || n.contains("牛油") || n.contains("butter") -> "🧈"

            // Meat & seafood
            n.contains("雞") || n.contains("雞肉") || n.contains("chicken") -> "🍗"
            n.contains("牛") || n.contains("牛肉") || n.contains("beef") -> "🥩"
            n.contains("豬") || n.contains("豬肉") || n.contains("pork") -> "🥓"
            n.contains("羊") || n.contains("羊肉") || n.contains("lamb") -> "🍖"
            n.contains("培根") || n.contains("bacon") -> "🥓"
            n.contains("火腿") || n.contains("ham") -> "🥪"
            n.contains("香腸") || n.contains("sausage") -> "🌭"
            n.contains("魚") || n.contains("fish") -> "🐟"
            n.contains("蝦") || n.contains("shrimp") || n.contains("prawn") -> "🦐"
            n.contains("蟹") || n.contains("crab") -> "🦀"

            // Vegetables
            n.contains("番茄") || n.contains("西紅柿") || n.contains("tomato") -> "🍅"
            n.contains("馬鈴薯") || n.contains("土豆") || n.contains("potato") -> "🥔"
            n.contains("洋蔥") || n.contains("onion") -> "🧅"
            n.contains("大蒜") || n.contains("蒜") || n.contains("garlic") -> "🧄"
            n.contains("胡椒") || n.contains("椒") || n.contains("pepper") -> "🫑"
            n.contains("辣椒") || n.contains("chili") || n.contains("chilli") -> "🌶️"
            n.contains("蘑菇") || n.contains("香菇") || n.contains("mushroom") -> "🍄"
            n.contains("黃瓜") || n.contains("小黃瓜") || n.contains("cucumber") -> "🥒"
            n.contains("胡蘿蔔") || n.contains("紅蘿蔔") || n.contains("carrot") -> "🥕"
            n.contains("玉米") || n.contains("corn") -> "🌽"
            n.contains("花椰菜") || n.contains("西蘭花") || n.contains("broccoli") -> "🥦"
            n.contains("菠菜") || n.contains("spinach") -> "🥬"
            n.contains("高麗菜") || n.contains("捲心菜") || n.contains("cabbage") -> "🥬"
            n.contains("櫛瓜") || n.contains("zucchini") -> "🥒"
            n.contains("茄子") || n.contains("eggplant") || n.contains("aubergine") -> "🍆"
            n.contains("生菜") || n.contains("lettuce") || n.contains("菜") || n.contains("蔬") -> "🥬"
            n.contains("豆腐") || n.contains("tofu") -> "🧊"
            n.contains("豆芽") || n.contains("bean sprout") || n.contains("sprouts") -> "🌱"
            n.contains("海帶") || n.contains("海藻") || n.contains("seaweed") -> "🪸"

            // Fruits
            n.contains("蘋果") || n.contains("apple") -> "🍎"
            n.contains("香蕉") || n.contains("banana") -> "🍌"
            n.contains("橙") || n.contains("柳橙") || n.contains("orange") -> "🍊"
            n.contains("草莓") || n.contains("strawberry") -> "🍓"
            n.contains("藍莓") || n.contains("blueberry") || n.contains("blueberries") -> "🫐"
            n.contains("葡萄") || n.contains("grape") || n.contains("grapes") -> "🍇"
            n.contains("西瓜") || n.contains("watermelon") -> "🍉"
            n.contains("鳳梨") || n.contains("菠蘿") || n.contains("pineapple") -> "🍍"
            n.contains("檸檬") || n.contains("lemon") -> "🍋"
            n.contains("萊姆") || n.contains("lime") -> "🍋"
            n.contains("酪梨") || n.contains("牛油果") || n.contains("avocado") -> "🥑"

            // Grains & staples
            n.contains("米") || n.contains("白飯") || n.contains("rice") -> "🍚"
            n.contains("麵") || n.contains("麵條") || n.contains("麵食") || n.contains("noodle") || n.contains("noodles") -> "🍜"
            n.contains("義大利麵") || n.contains("pasta") || n.contains("spaghetti") -> "🍝"
            n.contains("麵包") || n.contains("bread") -> "🍞"
            n.contains("玉米餅") || n.contains("tortilla") -> "🌮"
            n.contains("餃子") || n.contains("dumpling") || n.contains("dumplings") -> "🥟"
            n.contains("泡菜") || n.contains("kimchi") -> "🥬"

            // Condiments & misc
            n.contains("醬") || n.contains("sauce") || n.contains("ketchup") || n.contains("mayo") || n.contains("mayonnaise") -> "🥫"
            n.contains("油") || n.contains("olive oil") || n.contains("油脂") -> "🫙"

            else -> "🥫"
        }
    }
}
