package fridger.com.io.presentation.home

import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

/**
 * Maps ingredient names to SVG icons. Keep this pure and easily testable.
 */
object IngredientIconMapper {
    fun getIcon(name: String): DrawableResource {
        val n = name.lowercase()
        return when {
            // Dairy & eggs
            n.contains("蛋") || n.contains("egg") || n.contains("eggs") -> Res.drawable.ic_ingredient_egg
            n.contains("奶") || n.contains("milk") -> Res.drawable.ic_ingredient_milk
            n.contains("乳酪") || n.contains("起司") || n.contains("cheese") -> Res.drawable.ic_ingredient_cheese
            n.contains("優格") || n.contains("酸奶") || n.contains("yogurt") -> Res.drawable.ic_ingredient_yogurt
            n.contains("奶油") || n.contains("牛油") || n.contains("butter") -> Res.drawable.ic_ingredient_butter

            // Meat & seafood
            n.contains("雞") || n.contains("雞肉") || n.contains("chicken") -> Res.drawable.ic_ingredient_chicken
            n.contains("牛") || n.contains("牛肉") || n.contains("beef") -> Res.drawable.ic_ingredient_beef
            n.contains("豬") || n.contains("豬肉") || n.contains("pork") -> Res.drawable.ic_ingredient_bacon // Using bacon for pork generic if needed or specific? Script had bacon.
            n.contains("羊") || n.contains("羊肉") || n.contains("lamb") -> Res.drawable.ic_ingredient_lamb
            n.contains("培根") || n.contains("bacon") -> Res.drawable.ic_ingredient_bacon
            n.contains("火腿") || n.contains("ham") -> Res.drawable.ic_ingredient_sandwich // Script mapped ham -> sandwich
            n.contains("香腸") || n.contains("sausage") -> Res.drawable.ic_ingredient_sausage
            n.contains("魚") || n.contains("fish") -> Res.drawable.ic_ingredient_fish
            n.contains("蝦") || n.contains("shrimp") || n.contains("prawn") -> Res.drawable.ic_ingredient_shrimp
            n.contains("蟹") || n.contains("crab") -> Res.drawable.ic_ingredient_crab

            // Vegetables
            n.contains("番茄") || n.contains("西紅柿") || n.contains("tomato") -> Res.drawable.ic_ingredient_tomato
            n.contains("馬鈴薯") || n.contains("土豆") || n.contains("potato") -> Res.drawable.ic_ingredient_potato
            n.contains("洋蔥") || n.contains("onion") -> Res.drawable.ic_ingredient_onion
            n.contains("大蒜") || n.contains("蒜") || n.contains("garlic") -> Res.drawable.ic_ingredient_garlic
            n.contains("胡椒") || n.contains("椒") || n.contains("pepper") -> Res.drawable.ic_ingredient_bell_pepper
            n.contains("辣椒") || n.contains("chili") || n.contains("chilli") -> Res.drawable.ic_ingredient_bell_pepper // Fallback as chilli failed download? No, checking logic. Chilli failed. Use bell_pepper or generic? Use bell_pepper for now.
            n.contains("蘑菇") || n.contains("香菇") || n.contains("mushroom") -> Res.drawable.ic_ingredient_mushroom
            n.contains("黃瓜") || n.contains("小黃瓜") || n.contains("cucumber") -> Res.drawable.ic_ingredient_cucumber
            n.contains("胡蘿蔔") || n.contains("紅蘿蔔") || n.contains("carrot") -> Res.drawable.ic_ingredient_carrot
            n.contains("玉米") || n.contains("corn") -> Res.drawable.ic_ingredient_corn
            n.contains("花椰菜") || n.contains("西蘭花") || n.contains("broccoli") -> Res.drawable.ic_ingredient_broccoli
            n.contains("菠菜") || n.contains("spinach") -> Res.drawable.ic_ingredient_leafy_green
            n.contains("高麗菜") || n.contains("捲心菜") || n.contains("cabbage") -> Res.drawable.ic_ingredient_leafy_green
            n.contains("櫛瓜") || n.contains("zucchini") -> Res.drawable.ic_ingredient_cucumber
            n.contains("茄子") || n.contains("eggplant") || n.contains("aubergine") -> Res.drawable.ic_ingredient_eggplant
            n.contains("生菜") || n.contains("lettuce") || n.contains("菜") || n.contains("蔬") -> Res.drawable.ic_ingredient_leafy_green
            n.contains("豆腐") || n.contains("tofu") -> Res.drawable.ic_ingredient_tofu
            n.contains("豆芽") || n.contains("bean sprout") || n.contains("sprouts") -> Res.drawable.ic_ingredient_sprout
            n.contains("海帶") || n.contains("海藻") || n.contains("seaweed") -> Res.drawable.ic_ingredient_seaweed

            // Fruits
            n.contains("蘋果") || n.contains("apple") -> Res.drawable.ic_ingredient_apple
            n.contains("香蕉") || n.contains("banana") -> Res.drawable.ic_ingredient_banana
            n.contains("橙") || n.contains("柳橙") || n.contains("orange") -> Res.drawable.ic_ingredient_orange
            n.contains("草莓") || n.contains("strawberry") -> Res.drawable.ic_ingredient_strawberry
            n.contains("藍莓") || n.contains("blueberry") || n.contains("blueberries") -> Res.drawable.ic_ingredient_blueberry
            n.contains("葡萄") || n.contains("grape") || n.contains("grapes") -> Res.drawable.ic_ingredient_grapes
            n.contains("西瓜") || n.contains("watermelon") -> Res.drawable.ic_ingredient_watermelon
            n.contains("鳳梨") || n.contains("菠蘿") || n.contains("pineapple") -> Res.drawable.ic_ingredient_pineapple
            n.contains("檸檬") || n.contains("lemon") -> Res.drawable.ic_ingredient_lemon
            n.contains("萊姆") || n.contains("lime") -> Res.drawable.ic_ingredient_lemon
            n.contains("酪梨") || n.contains("牛油果") || n.contains("avocado") -> Res.drawable.ic_ingredient_avocado

            // Grains & staples
            n.contains("米") || n.contains("白飯") || n.contains("rice") -> Res.drawable.ic_ingredient_rice
            n.contains("麵") || n.contains("麵條") || n.contains("麵食") || n.contains("noodle") || n.contains("noodles") -> Res.drawable.ic_ingredient_noodle
            n.contains("義大利麵") || n.contains("pasta") || n.contains("spaghetti") -> Res.drawable.ic_ingredient_spaghetti
            n.contains("麵包") || n.contains("bread") -> Res.drawable.ic_ingredient_bread
            n.contains("玉米餅") || n.contains("tortilla") -> Res.drawable.ic_ingredient_taco
            n.contains("餃子") || n.contains("dumpling") || n.contains("dumplings") -> Res.drawable.ic_ingredient_dumpling
            n.contains("泡菜") || n.contains("kimchi") -> Res.drawable.ic_ingredient_leafy_green

            // Condiments & misc
            n.contains("醬") || n.contains("sauce") || n.contains("ketchup") || n.contains("mayo") || n.contains("mayonnaise") -> Res.drawable.ic_ingredient_canned_food
            n.contains("油") || n.contains("olive oil") || n.contains("油脂") -> Res.drawable.ic_ingredient_oil

            else -> Res.drawable.ic_ingredient_canned_food
        }
    }
}
