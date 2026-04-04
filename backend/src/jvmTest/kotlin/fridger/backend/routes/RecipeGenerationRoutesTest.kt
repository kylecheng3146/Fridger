package fridger.backend.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fridger.backend.config.ApiPaths
import fridger.backend.config.AppConfig
import fridger.backend.config.AppConfigAttribute
import fridger.backend.config.JwtClaims
import fridger.backend.config.TokenTypes
import fridger.backend.security.configureJwtAuth
import fridger.backend.services.InventoryRecipeGenerator
import fridger.shared.models.ApiResponse
import fridger.shared.recipe.GenerateRecipeRequest
import fridger.shared.recipe.GeneratedRecipe
import fridger.shared.recipe.SaveRecipeRequest
import io.ktor.client.request.header
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
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

class RecipeGenerationRoutesTest {
    private val appConfig =
        AppConfig(
            port = 8080,
            dbUrl = "jdbc:h2:mem:test",
            dbUser = "sa",
            dbPassword = "",
            jwtSecret = "test-secret-that-is-long-enough-for-hmac-signing",
            jwtIssuer = "test-issuer",
            googleClientIds = emptySet(),
            jwksUrl = "https://example.com/jwks",
            accessTokenMinutes = 15,
            refreshTokenDays = 30,
            refreshTokenBytes = 64,
            jwksRefreshIntervalHours = 6,
            groqApiKey = null,
            groqModel = "test-model",
            groqBaseUrl = "https://example.com",
        )

    @Test
    fun `returns generated recipe for inventory ingredients`() =
        testApplication {
            val generator = FakeRecipeGenerator()
            application {
                attributes.put(AppConfigAttribute, appConfig)
                this.install(ContentNegotiation) { json() }
                configureJwtAuth()
                this.routing {
                    recipeGenerationRoutes(generator)
                }
            }
            val jsonClient =
                createClient {
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
            assertEquals(emptyList(), generator.lastStyles)
            assertEquals("番茄炒蛋", payload.data?.title)
        }

    @Test
    fun `passes cuisine styles to generator`() =
        testApplication {
            val generator = FakeRecipeGenerator()
            application {
                attributes.put(AppConfigAttribute, appConfig)
                this.install(ContentNegotiation) { json() }
                configureJwtAuth()
                this.routing {
                    recipeGenerationRoutes(generator)
                }
            }
            val jsonClient =
                createClient {
                    install(ClientContentNegotiation) { json() }
                }

            val response =
                jsonClient.post(
                    ApiPaths.RECIPE_GENERATION,
                ) {
                    contentType(ContentType.Application.Json)
                    setBody(GenerateRecipeRequest(listOf("雞蛋", "番茄"), styles = listOf("中式", "韓式")))
                }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(listOf("中式", "韓式"), generator.lastStyles)
        }

    @Test
    fun `accepts authenticated save recipe request`() =
        testApplication {
            application {
                attributes.put(AppConfigAttribute, appConfig)
                this.install(ContentNegotiation) { json() }
                configureJwtAuth()
                this.routing {
                    recipeGenerationRoutes(FakeRecipeGenerator())
                }
            }
            val jsonClient =
                createClient {
                    install(ClientContentNegotiation) { json() }
                }

            val response =
                jsonClient.post(ApiPaths.RECIPE_SAVE) {
                    contentType(ContentType.Application.Json)
                    header(
                        "Authorization",
                        "Bearer ${createAccessToken(UUID.fromString("00000000-0000-0000-0000-000000000123"))}"
                    )
                    setBody(
                        SaveRecipeRequest(
                            recipeId = "recipe-1",
                            title = "番茄炒蛋",
                            description = "快速家常菜",
                            ingredients = listOf("雞蛋", "番茄"),
                            instructions = listOf("打蛋", "拌炒"),
                            cookingTime = "15 分鐘",
                            difficulty = "簡單",
                            servings = 2,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val payload = Json.decodeFromString<ApiResponse<Unit>>(response.bodyAsText())
            assertTrue(payload.success)
        }

    @Test
    fun `rejects empty ingredient requests`() =
        testApplication {
            application {
                attributes.put(AppConfigAttribute, appConfig)
                this.install(ContentNegotiation) { json() }
                configureJwtAuth()
                this.routing {
                    recipeGenerationRoutes(FakeRecipeGenerator())
                }
            }
            val jsonClient =
                createClient {
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
        var lastStyles: List<String> = emptyList()

        override suspend fun generate(
            ingredients: List<String>,
            styles: List<String>
        ): GeneratedRecipe {
            lastIngredients = ingredients
            lastStyles = styles
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

    private fun createAccessToken(userId: UUID): String =
        JWT
            .create()
            .withIssuer(appConfig.jwtIssuer)
            .withClaim(JwtClaims.USER_ID, userId.toString())
            .withClaim(JwtClaims.TYPE, TokenTypes.ACCESS)
            .sign(Algorithm.HMAC256(appConfig.jwtSecret))
}
