package fridger.backend.security

import fridger.backend.config.AppConfig
import fridger.backend.config.ErrorMessages
import fridger.backend.config.appConfig
import fridger.backend.repositories.GoogleProfile
import io.ktor.server.application.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.Logger
import java.math.BigInteger
import java.net.URL
import java.security.KeyFactory
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.time.Instant
import java.util.*

/**
 * 下載並快取 Google JWKS；提供本地驗證 Google ID Token 的功能。
 */
class GoogleTokenValidator private constructor(
    private val logger: Logger,
    private val clientIds: Set<String>,
    private val jwksUrl: String,
    private val refreshIntervalHours: Int
) {
    @Volatile
    private var keys: Map<String, PublicKey> = emptyMap()
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        // 啟動時預取
        scope.launch { refreshKeys() }
        // 週期刷新 (可配置)
        scope.launch {
            while (true) {
                delay(refreshIntervalHours * 60L * 60L * 1000L)
                refreshKeys()
            }
        }
    }

    private suspend fun refreshKeys() {
        runCatching {
            val jsonText = URL(jwksUrl).readText()
            val json = Json.parseToJsonElement(jsonText).jsonObject
            val keysArray = json["keys"]?.jsonArray ?: return
            val map = mutableMapOf<String, PublicKey>()
            for (k in keysArray) {
                val obj = k.jsonObject
                val kid = obj["kid"]?.jsonPrimitive?.content ?: continue
                val n = obj["n"]?.jsonPrimitive?.content ?: continue
                val e = obj["e"]?.jsonPrimitive?.content ?: continue
                map[kid] = buildRsaPublicKey(n, e)
            }
            if (map.isNotEmpty()) {
                keys = map
                logger.info("GoogleTokenValidator: JWKS refreshed. keyCount=${map.size}")
            }
        }.onFailure { ex ->
            logger.error("Failed refreshing Google JWKS", ex)
        }
    }

    private fun buildRsaPublicKey(
        nB64: String,
        eB64: String
    ): PublicKey {
        val decoder = Base64.getUrlDecoder()
        val nBytes = decoder.decode(nB64)
        val eBytes = decoder.decode(eB64)
        val n = BigInteger(1, nBytes)
        val e = BigInteger(1, eBytes)
        val spec = RSAPublicKeySpec(n, e)
        return KeyFactory.getInstance("RSA").generatePublic(spec) as RSAPublicKey
    }

    fun validate(idToken: String): GoogleProfile {
        // 拆解 JWT (header.payload.signature)
        val parts = idToken.split('.')
        require(parts.size == 3) { ErrorMessages.INVALID_JWT_FORMAT }
        val headerJson = String(Base64.getUrlDecoder().decode(parts[0]))
        val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
        val header = Json.parseToJsonElement(headerJson).jsonObject
        val payload = Json.parseToJsonElement(payloadJson).jsonObject
        val kid = header["kid"]?.jsonPrimitive?.content ?: throw IllegalArgumentException(ErrorMessages.MISSING_KID)
        val alg = header["alg"]?.jsonPrimitive?.content ?: ""
        if (alg != "RS256") throw IllegalArgumentException(ErrorMessages.UNSUPPORTED_ALG_PREFIX + alg)
        val key = keys[kid] ?: throw IllegalStateException(ErrorMessages.PUBLIC_KEY_NOT_CACHED)
        // Verify signature
        val data = parts[0] + "." + parts[1]
        val signature = Base64.getUrlDecoder().decode(parts[2])
        val sigOk =
            java.security.Signature.getInstance("SHA256withRSA").apply {
                initVerify(key)
                update(data.toByteArray())
            }.verify(signature)
        if (!sigOk) throw IllegalArgumentException(ErrorMessages.BAD_SIGNATURE)
        // exp
        val exp = payload["exp"]?.jsonPrimitive?.content?.toLongOrNull() ?: throw IllegalArgumentException(ErrorMessages.EXP_MISSING)
        if (Instant.ofEpochSecond(
                exp
            ).isBefore(Instant.now())
        ) {
            throw IllegalArgumentException(ErrorMessages.TOKEN_EXPIRED)
        }
        // aud 驗證 (允許多個 client id)
        val aud = payload["aud"]?.jsonPrimitive?.content ?: throw IllegalArgumentException(ErrorMessages.AUD_MISSING)
        if (aud !in clientIds) throw IllegalArgumentException(ErrorMessages.AUD_MISMATCH)
        val sub = payload["sub"]?.jsonPrimitive?.content ?: throw IllegalArgumentException(ErrorMessages.SUB_MISSING)
        val email = payload["email"]?.jsonPrimitive?.content ?: throw IllegalArgumentException(ErrorMessages.EMAIL_MISSING)
        val name = payload["name"]?.jsonPrimitive?.content ?: email.substringBefore('@')
        val picture = payload["picture"]?.jsonPrimitive?.content
        return GoogleProfile(
            googleId = sub,
            email = email,
            name = name,
            pictureUrl = picture
        )
    }

    companion object {
        private val KEY = AttributeKey<GoogleTokenValidator>("GoogleTokenValidator")

        fun install(app: Application): GoogleTokenValidator {
            return app.attributes.computeIfAbsent(KEY) {
                val cfg: AppConfig = app.appConfig()
                if (cfg.googleClientIds.isEmpty()) {
                    app.log.warn(
                        "GOOGLE_CLIENT_ID is not set; validation will likely fail"
                    )
                }
                GoogleTokenValidator(app.log, cfg.googleClientIds, cfg.jwksUrl, cfg.jwksRefreshIntervalHours)
            }
        }

        fun instance(app: Application): GoogleTokenValidator = app.attributes[KEY]
    }
}
