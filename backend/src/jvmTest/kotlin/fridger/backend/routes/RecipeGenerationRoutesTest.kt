package fridger.backend.routes

import fridger.backend.config.ApiPaths
import fridger.backend.services.InventoryRecipeGenerator
import fridger.shared.models.ApiResponse
import fridger.shared.recipe.GenerateRecipeRequest
import fridger.shared.recipe.GeneratedRecipe
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecipeGenerationRoutesTest {
    @Test
    fun `returns generated recipe for inventory ingredients`() =
        testApplication {
            val generator = FakeRecipeGenerator()
            application {
                this.install(ContentNegotiation) { json() }
                this.routing {
                    recipeGenerationRoutes(generator)
                }
            }
            val jsonClient = createClient {
                install(ClientContentNegotiation) { json() }
            }

            val response =
                jsonClient.post(
                    ApiPaths.RECIPE_GENERATION,
                ) {
                    contentType(ContentType.Application.Json)
                    setBody(GenerateRecipeRequest(listOf("Eggs", "Tomato")))
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val payload = Json.decodeFromString<ApiResponse<GeneratedRecipe>>(response.bodyAsText())
            assertTrue(payload.success)
            assertEquals(listOf("Eggs", "Tomato"), generator.lastIngredients)
            assertEquals("番茄炒蛋", payload.data?.title)
        }

    @Test
    fun `rejects empty ingredient requests`() =
        testApplication {
            application {
                this.install(ContentNegotiation) { json() }
                this.routing {
                    recipeGenerationRoutes(FakeRecipeGenerator())
                }
            }
            val jsonClient = createClient {
                install(ClientContentNegotiation) { json() }
            }

            val response =
                jsonClient.post(
                    ApiPaths.RECIPE_GENERATION,
                ) {
                    contentType(ContentType.Application.Json)
                    setBody(GenerateRecipeRequest(emptyList()))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    private class FakeRecipeGenerator : InventoryRecipeGenerator {
        var lastIngredients: List<String>? = null

        override suspend fun generate(ingredients: List<String>): GeneratedRecipe {
            lastIngredients = ingredients
            return GeneratedRecipe(
                recipeId = "recipe-test-1",
                title = "番茄炒蛋",
                description = "用庫存快速完成的家常菜",
                ingredients = ingredients,
                instructions = listOf("切番茄", "炒蛋", "拌炒收汁"),
                cookingTime = "15 分鐘",
                difficulty = "簡單",
                servings = 2,
            )
        }
    }
}
