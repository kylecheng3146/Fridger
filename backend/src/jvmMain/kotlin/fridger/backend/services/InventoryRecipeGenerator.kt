package fridger.backend.services

import fridger.backend.config.AppConfig
import fridger.backend.exceptions.UpstreamServiceException
import fridger.shared.recipe.GeneratedRecipe
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.util.UUID

interface InventoryRecipeGenerator {
    suspend fun generate(
        ingredients: List<String>,
        styles: List<String> = emptyList()
    ): GeneratedRecipe
}

class GroqInventoryRecipeGenerator(
    private val config: AppConfig,
    private val client: HttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    },
                )
            }
            install(Logging) {
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            println("🤖 GroqRecipeGenerator: $message")
                        }
                    }
                level = LogLevel.INFO
            }
        },
) : InventoryRecipeGenerator {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    override suspend fun generate(
        ingredients: List<String>,
        styles: List<String>
    ): GeneratedRecipe {
        val normalizedIngredients =
            ingredients
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .take(MAX_INGREDIENTS)

        require(normalizedIngredients.isNotEmpty()) { "At least one ingredient is required" }

        val styleText = if (styles.isNotEmpty()) "，請盡量符合以下料理風格：${styles.joinToString(separator = "、")}" else ""

        val apiKey = config.groqApiKey ?: throw IllegalStateException("GROQ_API_KEY is not configured")
        val response =
            try {
                client
                    .post("${config.groqBaseUrl}/chat/completions") {
                        contentType(ContentType.Application.Json)
                        header("Authorization", "Bearer $apiKey")
                        setBody(
                            GroqChatCompletionRequest(
                                model = config.groqModel,
                                messages =
                                    listOf(
                                        GroqMessage(
                                            role = "system",
                                            content =
                                                """
                                                你是一位專業料理助手。請只輸出單一 JSON 物件，不要輸出 markdown、程式碼區塊或額外說明。
                                                JSON 結構必須完全符合：
                                                {
                                                  "title": string,
                                                  "description": string,
                                                  "ingredients": string[],
                                                  "instructions": string[],
                                                  "cookingTime": string,
                                                  "difficulty": string,
                                                  "servings": number
                                                }
                                                規則：
                                                - 使用繁體中文（台灣）
                                                - 優先使用提供的庫存食材
                                                - 可假設基本調味料如鹽、油、胡椒、水可使用，但不要把未提供的主食材當成必要材料
                                                - description 請簡短說明料理特色
                                                - instructions 至少 3 步
                                                - 若食材不足以完成複雜料理，也要提供最合理、可實作的簡單料理
                                                """.trimIndent(),
                                        ),
                                        GroqMessage(
                                            role = "user",
                                            content = "目前庫存食材：${normalizedIngredients.joinToString(
                                                separator = "、"
                                            )}$styleText。請生成一道適合這些食材的食譜。",
                                        ),
                                    ),
                                temperature = 0.7,
                                responseFormat = GroqResponseFormat(type = "json_object"),
                            ),
                        )
                    }.body<GroqChatCompletionResponse>()
            } catch (e: ResponseException) {
                val status = e.response.status.value
                val safe = "Groq request failed (HTTP $status). Check GROQ_MODEL/GROQ_API_KEY configuration."
                throw UpstreamServiceException(safe)
            }

        val content = response.choices.firstOrNull()?.message?.content?.trim().orEmpty()
        if (content.isBlank()) {
            throw IllegalStateException("Groq returned an empty recipe response")
        }

        return try {
            json.decodeFromString<GeneratedRecipe>(content).copy(recipeId = UUID.randomUUID().toString())
        } catch (_: SerializationException) {
            throw UpstreamServiceException("Groq returned invalid recipe format")
        }
    }

    private companion object {
        private const val MAX_INGREDIENTS = 12
    }
}

@Serializable
private data class GroqChatCompletionRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double,
    @kotlinx.serialization.SerialName("response_format")
    val responseFormat: GroqResponseFormat,
)

@Serializable
private data class GroqMessage(
    val role: String,
    val content: String,
)

@Serializable
private data class GroqResponseFormat(
    val type: String,
)

@Serializable
private data class GroqChatCompletionResponse(
    val choices: List<GroqChoice> = emptyList(),
)

@Serializable
private data class GroqChoice(
    val message: GroqResponseMessage,
)

@Serializable
private data class GroqResponseMessage(
    val content: String? = null,
)
