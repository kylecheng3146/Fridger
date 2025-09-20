package fridger.com.io.data.repository

import fridger.com.data.model.remote.MealDto
import fridger.com.data.remote.RecipeApiService

interface RecipeRepository {
    suspend fun getRemoteRandomRecipe(): Result<MealDto>
    suspend fun getRecipesByIngredient(ingredient: String): Result<List<MealDto>>
    suspend fun getRecipeById(id: String): Result<MealDto>
}

class RecipeRepositoryImpl(
    private val apiService: RecipeApiService,
) : RecipeRepository {

    override suspend fun getRecipesByIngredient(ingredient: String): Result<List<MealDto>> {
        println("🏪 REPOSITORY: Starting getRecipesByIngredient for '$ingredient'")
        
        return try {
            // Step 1: Get list of recipes by ingredient (basic info only)
            val response = apiService.getRecipesByIngredient(ingredient)
            if (response.meals != null) {
                println("✅ REPOSITORY: Successfully got ${response.meals.size} basic meals from filter API")
                
                // Step 2: Get detailed info for first few recipes (limit to avoid too many API calls)
                val detailedMeals = mutableListOf<MealDto>()
                val maxRecipes = minOf(5, response.meals.size) // Limit to 5 recipes for performance
                
                println("🔄 REPOSITORY: Fetching detailed info for first $maxRecipes recipes")
                
                response.meals.take(maxRecipes).forEachIndexed { index, basicMeal ->
                    basicMeal.idMeal?.let { id ->
                        try {
                            val detailResponse = apiService.getRecipeById(id)
                            detailResponse.meals?.firstOrNull()?.let { detailedMeal ->
                                detailedMeals.add(detailedMeal)
                                println("✅ REPOSITORY: Got detailed info for recipe ${index + 1}: ${detailedMeal.strMeal}")
                            }
                        } catch (e: Exception) {
                            println("⚠️ REPOSITORY: Failed to get details for recipe ID $id: ${e.message}")
                            // If detailed fetch fails, use basic info
                            detailedMeals.add(basicMeal)
                        }
                    }
                }
                
                println("✅ REPOSITORY: Successfully processed ${detailedMeals.size} detailed meals")
                Result.success(detailedMeals)
            } else {
                println("⚠️ REPOSITORY: API returned null meals for ingredient '$ingredient'")
                Result.failure(Exception("No meals found for the ingredient"))
            }
        } catch (e: Exception) {
            println("❌ REPOSITORY: Error getting recipes for ingredient '$ingredient' - ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getRecipeById(id: String): Result<MealDto> {
        println("🏪 REPOSITORY: Starting getRecipeById for '$id'")
        
        return try {
            val response = apiService.getRecipeById(id)
            val meal = response.meals?.firstOrNull()
            if (meal != null) {
                println("✅ REPOSITORY: Successfully got detailed meal: ${meal.strMeal}")
                Result.success(meal)
            } else {
                println("⚠️ REPOSITORY: API returned no meal for ID '$id'")
                Result.failure(Exception("No meal found for the given ID"))
            }
        } catch (e: Exception) {
            println("❌ REPOSITORY: Error getting recipe for ID '$id' - ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getRemoteRandomRecipe(): Result<MealDto> {
        println("🏪 REPOSITORY: Starting getRemoteRandomRecipe")
        
        return try {
            val response = apiService.getRandomRecipe()
            val meal = response.meals?.firstOrNull()
            if (meal != null) {
                println("✅ REPOSITORY: Successfully got random meal: ${meal.strMeal}")
                Result.success(meal)
            } else {
                println("⚠️ REPOSITORY: API returned no meals for random recipe")
                Result.failure(Exception("No meal found in API response"))
            }
        } catch (e: Exception) {
            println("❌ REPOSITORY: Error getting random recipe - ${e.message}")
            Result.failure(e)
        }
    }
}
