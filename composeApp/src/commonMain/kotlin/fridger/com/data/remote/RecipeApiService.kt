package fridger.com.data.remote

import fridger.com.data.model.remote.MealApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class RecipeApiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                }
            )
        }
        
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("🌐 API DEBUG: $message")
                }
            }
            level = LogLevel.ALL
        }
    }

    private val baseUrl = "https://www.themealdb.com/api/json/v1/1"

    suspend fun getRandomRecipe(): MealApiResponse {
        println("📞 API CALL: Getting random recipe")
        
        return try {
            val response = client.get("$baseUrl/random.php").body<MealApiResponse>()
            println("✅ API SUCCESS: Random recipe received")
            response
        } catch (e: Exception) {
            println("❌ API ERROR: Failed to get random recipe - ${e.message}")
            throw e
        }
    }

    suspend fun getRecipesByIngredient(ingredient: String): MealApiResponse {
        println("📞 API CALL: Getting recipes by ingredient '$ingredient'")

        return try {
            val response = client.get("$baseUrl/filter.php") {
                url { parameters.append("i", ingredient) }
            }.body<MealApiResponse>()

            println("✅ API SUCCESS: Recipes by ingredient received")
            response
        } catch (e: Exception) {
            println("❌ API ERROR: Failed to get recipes for ingredient '$ingredient' - ${e.message}")
            throw e
        }
    }

    suspend fun getRecipeById(id: String): MealApiResponse {
        println("📞 API CALL: Getting recipe details by ID '$id'")
        
        return try {
            val response = client.get("$baseUrl/lookup.php") {
                url { parameters.append("i", id) }
            }.body<MealApiResponse>()

            println("✅ API SUCCESS: Recipe details received")
            response
        } catch (e: Exception) {
            println("❌ API ERROR: Failed to get recipe details for ID '$id' - ${e.message}")
            throw e
        }
    }
}
