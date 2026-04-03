package fridger.com.io.data.remote

import fridger.shared.models.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val AUTH_PATH = "/api/v1/auth"

@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class GoogleSignInRequest(val idToken: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

class AuthApiService(
    private val client: HttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }
        },
    private val baseUrl: String = BackendConfig.baseUrl,
) {
    suspend fun signInWithGoogle(idToken: String): ApiResponse<AuthTokens> {
        return client
            .post("$baseUrl$AUTH_PATH/google") {
                contentType(ContentType.Application.Json)
                setBody(GoogleSignInRequest(idToken))
            }.body()
    }

    suspend fun refresh(refreshToken: String): ApiResponse<AuthTokens> {
        return client
            .post("$baseUrl$AUTH_PATH/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken))
            }.body()
    }

    suspend fun logout(refreshToken: String): ApiResponse<Unit> {
        return client
            .post("$baseUrl$AUTH_PATH/logout") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken))
            }.body()
    }
}
