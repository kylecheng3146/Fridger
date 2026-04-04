package fridger.backend.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fridger.backend.config.AppConfig
import fridger.backend.config.AppConfigAttribute
import fridger.backend.config.ApiPaths
import fridger.backend.config.JwtClaims
import fridger.backend.config.TokenTypes
import fridger.backend.services.RecipeFeedbackService
import fridger.backend.security.configureJwtAuth
import fridger.shared.models.ApiResponse
import fridger.shared.recipe.RecipeFeedbackType
import fridger.shared.recipe.SubmitRecipeFeedbackRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
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

class RecipeFeedbackRoutesTest {
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
    fun `stores recipe feedback when request is valid`() =
        testApplication {
            val service = FakeRecipeFeedbackService()
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000123")
            application {
                attributes.put(AppConfigAttribute, appConfig)
                this.install(ContentNegotiation) { json() }
                configureJwtAuth()
                this.routing {
                    recipeFeedbackRoutes(service)
                }
            }
            val jsonClient = createClient {
                install(ClientContentNegotiation) { json() }
            }

            val response =
                jsonClient.post(ApiPaths.RECIPE_FEEDBACK) {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer ${createAccessToken(userId)}")
                    setBody(
                        SubmitRecipeFeedbackRequest(
                            recipeId = "recipe-123",
                            feedbackType = RecipeFeedbackType.LIKE,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val payload = Json.decodeFromString<ApiResponse<Unit>>(response.bodyAsText())
            assertTrue(payload.success)
            assertEquals(userId, service.lastUserId)
            assertEquals(SubmitRecipeFeedbackRequest(recipeId = "recipe-123", feedbackType = RecipeFeedbackType.LIKE), service.lastRequest)
        }

    @Test
    fun `rejects blank recipeId in feedback request`() =
        testApplication {
            application {
                attributes.put(AppConfigAttribute, appConfig)
                this.install(ContentNegotiation) { json() }
                configureJwtAuth()
                this.routing {
                    recipeFeedbackRoutes(FakeRecipeFeedbackService())
                }
            }
            val jsonClient = createClient {
                install(ClientContentNegotiation) { json() }
            }

            val response =
                jsonClient.post(ApiPaths.RECIPE_FEEDBACK) {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer ${createAccessToken(UUID.fromString("00000000-0000-0000-0000-000000000123"))}")
                    setBody(
                        SubmitRecipeFeedbackRequest(
                            recipeId = "   ",
                            feedbackType = RecipeFeedbackType.DISLIKE,
                        ),
                    )
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    private class FakeRecipeFeedbackService : RecipeFeedbackService {
        var lastUserId: UUID? = null
        var lastRequest: SubmitRecipeFeedbackRequest? = null

        override suspend fun submitFeedback(userId: UUID, request: SubmitRecipeFeedbackRequest) {
            lastUserId = userId
            lastRequest = request
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
