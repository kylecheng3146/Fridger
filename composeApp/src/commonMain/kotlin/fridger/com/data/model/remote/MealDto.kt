package fridger.com.data.model.remote

import kotlinx.serialization.Serializable

@Serializable
data class MealApiResponse(
    val meals: List<MealDto>?
)

@Serializable
data class MealDto(
    val idMeal: String?,
    val strMeal: String?,
    val strCategory: String?,
    val strArea: String?,
    val strInstructions: String?,
    val strMealThumb: String?,
    val strTags: String?,
    val strYoutube: String?,
    val strIngredient1: String?,
    val strIngredient2: String?,
    // ... (可繼續添加至 strIngredient20)
    val strMeasure1: String?,
    val strMeasure2: String?,
    // ... (可繼續添加至 strMeasure20)
)
