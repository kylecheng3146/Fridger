package fridger.com.data.model.remote

import kotlinx.serialization.Serializable

@Serializable
data class CategoriesApiResponse(
    val categories: List<RecipeCategoryDto>
)

@Serializable
data class RecipeCategoryDto(
    val idCategory: String,
    val strCategory: String,
    val strCategoryThumb: String,
    val strCategoryDescription: String
)
