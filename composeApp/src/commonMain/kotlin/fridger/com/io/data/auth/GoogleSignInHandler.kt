package fridger.com.io.data.auth

import fridger.com.io.data.remote.AuthApiService
import fridger.com.io.data.user.UserSessionManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.ByteString.Companion.decodeBase64

class GoogleSignInHandler(
    private val authApi: AuthApiService = AuthApiService()
) {
    suspend fun handleIdToken(idToken: String): Result<Unit> {
        if (idToken.isBlank()) {
            return Result.failure(IllegalArgumentException("Missing idToken"))
        }
        val response = authApi.signInWithGoogle(idToken)
        if (!response.success) {
            return Result.failure(IllegalStateException(response.error ?: "Sign-in failed"))
        }
        val tokens = response.data ?: return Result.failure(IllegalStateException("Missing tokens"))
        val userId = decodeUserId(tokens.accessToken)
        UserSessionManager.setTokens(tokens.accessToken, tokens.refreshToken, userId)
        return Result.success(Unit)
    }

    private fun decodeUserId(accessToken: String): String {
        val parts = accessToken.split('.')
        if (parts.size < 2) return ""
        val payloadBytes = decodeJwtSegment(parts[1])
        val payloadJson = payloadBytes.decodeToString(0, 0 + payloadBytes.size)
        return runCatching {
            val obj = Json.parseToJsonElement(payloadJson).jsonObject
            obj["userId"]?.jsonPrimitive?.content.orEmpty()
        }.getOrDefault("")
    }

    private fun decodeJwtSegment(segment: String): ByteArray {
        val normalized = segment.replace('-', '+').replace('_', '/')
        val padded = normalized.padEnd((normalized.length + 3) / 4 * 4, '=')
        return padded.decodeBase64()?.toByteArray() ?: ByteArray(0)
    }
}
