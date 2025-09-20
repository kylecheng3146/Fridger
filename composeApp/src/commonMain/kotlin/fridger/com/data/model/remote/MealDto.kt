package fridger.com.data.model.remote

import kotlinx.serialization.Serializable

@Serializable
data class MealApiResponse(
    val meals: List<MealDto>?
)

@Serializable
data class MealDto(
    val idMeal: String? = null,
    val strMeal: String? = null,
    val strCategory: String? = null,
    val strArea: String? = null,
    val strInstructions: String? = null,
    val strMealThumb: String? = null,
    val strTags: String? = null,
    val strYoutube: String? = null,
    val strIngredient1: String? = null,
    val strIngredient2: String? = null,
    // ... (可繼續添加至 strIngredient20)
    val strMeasure1: String? = null,
    val strMeasure2: String? = null,
    // ... (可繼續添加至 strMeasure20)
)
