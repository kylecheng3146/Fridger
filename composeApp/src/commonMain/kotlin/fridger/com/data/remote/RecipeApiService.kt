package fridger.com.data.remote

import fridger.com.data.model.remote.MealApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
    }

    private val baseUrl = "https://www.themealdb.com/api/json/v1/1"

    suspend fun getRandomRecipe(): MealApiResponse {
        return client.get("$baseUrl/random.php").body()
    }
}
