package fridger.com.io.data.repository

import fridger.com.data.model.remote.MealDto
import fridger.com.data.remote.RecipeApiService

interface RecipeRepository {
    suspend fun getRemoteRandomRecipe(): Result<MealDto>
}

class RecipeRepositoryImpl(
    private val apiService: RecipeApiService,
) : RecipeRepository {

    override suspend fun getRemoteRandomRecipe(): Result<MealDto> {
        return try {
            val response = apiService.getRandomRecipe()
            val meal = response.meals?.firstOrNull()
            if (meal != null) {
                Result.success(meal)
            } else {
                Result.failure(Exception("No meal found in API response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
