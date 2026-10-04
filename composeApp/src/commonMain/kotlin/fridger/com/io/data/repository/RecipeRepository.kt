package fridger.com.io.data.repository

import fridger.com.data.model.remote.MealDto
import fridger.com.data.remote.RecipeApiService
import fridger.com.io.data.user.UserSessionProvider
import fridger.com.io.presentation.home.RecipeSuggestion
import fridger.shared.recipe.GeneratedRecipe
import fridger.shared.recipe.RecipeFeedbackType
import fridger.shared.recipe.SaveRecipeRequest
import kotlinx.datetime.Clock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val recipeJsonKeyPattern = Regex(
    "\"(recipeId|title|description|ingredients|instructions|cookingTime|difficulty|servings|success|data|error)\"",
    RegexOption.IGNORE_CASE,
)
private val quotedValuePattern = Regex("\"([^\"]+)\"")

private fun String.looksLikeJsonScalar(fieldName: String): Boolean =
    Regex("^\\s*[{,]?\\s*\"?$fieldName\"?\\s*:", RegexOption.IGNORE_CASE).containsMatchIn(this)

private fun String.looksLikeJsonListFragment(fieldName: String): Boolean {
    val trimmed = trim()
    return recipeJsonKeyPattern.containsMatchIn(trimmed) ||
        Regex("^\\s*\"?$fieldName\"?\\s*:", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) ||
        Regex("^\\s*\".*\"\\s*,?\\s*$").matches(trimmed) ||
        (trimmed.removePrefix("[").trimStart().startsWith("\"") && trimmed.startsWith("[")) ||
        (trimmed.startsWith("{") && trimmed.contains('"'))
}

internal fun sanitizeRecipeSuggestion(recipe: GeneratedRecipe): RecipeSuggestion =
    RecipeSuggestion(
        recipeId = sanitizeScalarRecipeText(recipe.recipeId, "recipeId"),
        title = sanitizeScalarRecipeText(recipe.title, "title"),
        description = sanitizeScalarRecipeText(recipe.description, "description"),
        ingredients = sanitizeRecipeList(recipe.ingredients, "ingredients"),
        instructions = sanitizeRecipeList(recipe.instructions, "instructions"),
        cookingTime = sanitizeScalarRecipeText(recipe.cookingTime, "cookingTime"),
        difficulty = sanitizeScalarRecipeText(recipe.difficulty, "difficulty"),
        servings = recipe.servings,
    )

internal fun sanitizeScalarRecipeText(raw: String, fieldName: String): String {
    val trimmed = raw.trim()
    if (trimmed.looksLikeJsonScalar(fieldName)) {
        val fieldPattern =
            Regex(
                "^\\s*[{,]?\\s*\"?$fieldName\"?\\s*:\\s*\"([^\"]+)\"",
                RegexOption.IGNORE_CASE,
            )
        fieldPattern.find(trimmed)?.groupValues?.getOrNull(1)?.let { return it.trim() }
    }

    val cleaned =
        trimmed
            .replace("```json", "", ignoreCase = true)
            .replace("```", "")
            .trim()

    val minimallySanitized =
        if (cleaned.looksLikeJsonScalar(fieldName)) {
            cleaned
                .replace(
                    Regex("^\\s*[{,]?\\s*\"?$fieldName\"?\\s*:\\s*", RegexOption.IGNORE_CASE),
                    "",
                ).trim()
                .trim(',', '，', ':', '：', '"', '\'', '[', ']', '{', '}')
        } else {
            cleaned
        }

    return minimallySanitized.ifBlank { trimmed }
}

internal fun sanitizeRecipeList(values: List<String>, fieldName: String): List<String> {
    val combined = values.joinToString("\n").trim()
    val extractedQuotedValues =
        quotedValuePattern
            .findAll(combined)
            .map { it.groupValues[1].trim() }
            .filterNot { it.isBlank() || recipeJsonKeyPattern.matches("\"$it\"") }
            .toList()

    if (combined.looksLikeJsonListFragment(fieldName) && extractedQuotedValues.isNotEmpty()) {
        return extractedQuotedValues
    }

    return values
        .flatMap { value ->
            val cleaned =
                value
                    .replace("```json", "", ignoreCase = true)
                    .replace("```", "")
                    .trim()

            val normalized =
                cleaned
                    .let {
                        if (it.looksLikeJsonListFragment(fieldName)) {
                            it.replace(
                                Regex("^\\s*[{,]?\\s*\"?$fieldName\"?\\s*:\\s*", RegexOption.IGNORE_CASE),
                                "",
                            )
                        } else {
                            it
                        }
                    }.let {
                        if (fieldName == "instructions") {
                            it.replace(Regex("^\\s*\\d+\\.\\s+"), "")
                        } else {
                            it
                        }
                    }.trim()

            val nestedQuotedValues =
                if (normalized.looksLikeJsonListFragment(fieldName)) {
                    quotedValuePattern
                        .findAll(normalized)
                        .map { it.groupValues[1].trim() }
                        .filterNot { it.isBlank() || recipeJsonKeyPattern.matches("\"$it\"") }
                        .toList()
                } else {
                    emptyList()
                }

            when {
                nestedQuotedValues.isNotEmpty() -> nestedQuotedValues
                else ->
                    listOf(
                        if (normalized.looksLikeJsonListFragment(fieldName)) {
                            normalized.trim(',', '，', ':', '：', '"', '\'', '[', ']', '{', '}')
                        } else {
                            normalized
                        },
                    )
            }
        }.map { it.trim() }
        .filter { it.isNotBlank() }
}

interface RecipeRepository {
    suspend fun getRemoteRandomRecipe(): Result<MealDto>

    suspend fun generateRecipeFromInventory(
        ingredients: List<String>,
        styles: List<String> = emptyList(),
    ): Result<RecipeSuggestion>

    suspend fun saveRecipeLocally(recipe: RecipeSuggestion): Result<Unit>

    suspend fun saveRecipeRemotely(recipe: RecipeSuggestion, accessToken: String): Result<Unit>

    suspend fun submitRecipeFeedback(
        recipeId: String,
        feedbackType: RecipeFeedbackType,
        accessToken: String,
    ): Result<Unit>

    suspend fun getRecipesByIngredient(ingredient: String): Result<List<MealDto>>

    suspend fun getRecipeById(id: String): Result<MealDto>

    suspend fun getRecipeCategories(): Result<List<fridger.com.data.model.remote.RecipeCategoryDto>>

    suspend fun getRecipesByCategory(category: String): Result<List<MealDto>>

    suspend fun searchRecipesByName(query: String): Result<List<MealDto>>
}

class RecipeRepositoryImpl(
    private val apiService: RecipeApiService,
    private val database: fridger.com.io.database.FridgerDatabase,
    private val userSessionProvider: UserSessionProvider? = null,
) : RecipeRepository {
    override suspend fun generateRecipeFromInventory(
        ingredients: List<String>,
        styles: List<String>,
    ): Result<RecipeSuggestion> =
        try {
            val response = apiService.generateRecipeFromInventory(ingredients, styles)
            val recipe = response.data
            if (response.success && recipe != null) {
                Result.success(sanitizeRecipeSuggestion(recipe))
            } else {
                Result.failure(IllegalStateException(response.error ?: "Failed to generate recipe"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun saveRecipeLocally(recipe: RecipeSuggestion): Result<Unit> =
        try {
            val json = Json { encodeDefaults = true }
            val stringListSerializer = ListSerializer(String.serializer())
            database.fridgerDatabaseQueries.insertSavedRecipe(
                id = recipe.recipeId,
                title = recipe.title,
                description = recipe.description,
                ingredients = json.encodeToString(stringListSerializer, recipe.ingredients),
                instructions = json.encodeToString(stringListSerializer, recipe.instructions),
                cookingTime = recipe.cookingTime,
                difficulty = recipe.difficulty,
                servings = recipe.servings.toLong(),
                savedAt = Clock.System.now().toEpochMilliseconds(),
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun saveRecipeRemotely(
        recipe: RecipeSuggestion,
        accessToken: String,
    ): Result<Unit> =
        try {
            val resolvedToken = accessToken.ifBlank { userSessionProvider?.accessToken().orEmpty() }
            if (resolvedToken.isBlank()) {
                Result.failure(IllegalStateException("Missing access token for remote recipe save"))
            } else {
                apiService.saveRecipe(
                    payload =
                        SaveRecipeRequest(
                            recipeId = recipe.recipeId,
                            title = recipe.title,
                            description = recipe.description,
                            ingredients = recipe.ingredients,
                            instructions = recipe.instructions,
                            cookingTime = recipe.cookingTime,
                            difficulty = recipe.difficulty,
                            servings = recipe.servings,
                        ),
                    accessToken = resolvedToken,
                )
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun submitRecipeFeedback(
        recipeId: String,
        feedbackType: RecipeFeedbackType,
        accessToken: String,
    ): Result<Unit> =
        try {
            apiService.submitRecipeFeedback(recipeId, feedbackType, accessToken)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

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
