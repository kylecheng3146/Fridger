package fridger.com.io.data.repository

import fridger.com.data.model.remote.MealDto
import fridger.com.data.remote.RecipeApiService

interface RecipeRepository {
    suspend fun getRemoteRandomRecipe(): Result<MealDto>

    suspend fun getRecipesByIngredient(ingredient: String): Result<List<MealDto>>

    suspend fun getRecipeById(id: String): Result<MealDto>

    suspend fun getRecipeCategories(): Result<List<fridger.com.data.model.remote.RecipeCategoryDto>>

    suspend fun getRecipesByCategory(category: String): Result<List<MealDto>>

    suspend fun searchRecipesByName(query: String): Result<List<MealDto>>
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

    override suspend fun getRecipeCategories(): Result<List<fridger.com.data.model.remote.RecipeCategoryDto>> {
        println("🏪 REPOSITORY: Starting getRecipeCategories")
        return try {
            val response = apiService.getRecipeCategories()
            val categories = response.categories
            println("✅ REPOSITORY: Successfully got ${'$'}{categories.size} categories")
            Result.success(categories)
        } catch (e: Exception) {
            println("❌ REPOSITORY: Error getting categories - ${'$'}{e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getRecipesByCategory(category: String): Result<List<MealDto>> {
        println("🏪 REPOSITORY: Starting getRecipesByCategory for '$category'")
        return try {
            val response = apiService.getRecipesByCategory(category)
            val meals = response.meals ?: emptyList()
            if (meals.isNotEmpty()) {
                println("✅ REPOSITORY: Got ${'$'}{meals.size} meals for category '$category'")
                Result.success(meals)
            } else {
                println("⚠️ REPOSITORY: No meals found for category '$category'")
                Result.failure(Exception("No meals found for the category"))
            }
        } catch (e: Exception) {
            println("❌ REPOSITORY: Error getting meals for category '$category' - ${'$'}{e.message}")
            Result.failure(e)
        }
    }

    override suspend fun searchRecipesByName(query: String): Result<List<MealDto>> {
        println("🏪 REPOSITORY: Starting searchRecipesByName for '$query'")
        return try {
            val response = apiService.searchRecipesByName(query)
            val meals = response.meals ?: emptyList()
            if (meals.isNotEmpty()) {
                println("✅ REPOSITORY: Found ${'$'}{meals.size} meals for query '$query'")
                Result.success(meals)
            } else {
                println("⚠️ REPOSITORY: No meals found for query '$query'")
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            println("❌ REPOSITORY: Error searching meals for '$query' - ${'$'}{e.message}")
            Result.failure(e)
        }
    }
}
