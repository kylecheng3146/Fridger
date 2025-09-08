package fridger.backend.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fridger.backend.config.AppConfig
import fridger.backend.config.ErrorMessages
import fridger.backend.config.JwtClaims
import fridger.backend.config.TokenTypes
import fridger.backend.repositories.GoogleProfile
import fridger.backend.repositories.RefreshTokenRepository
import fridger.backend.repositories.UserRepository
import fridger.backend.security.GoogleTokenValidator
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

class AuthService(
    private val config: AppConfig,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val googleValidator: GoogleTokenValidator
) {
    private val algorithm = Algorithm.HMAC256(config.jwtSecret)
    private val accessTtl = config.accessTokenMinutes.minutes
    private val refreshTtl = config.refreshTokenDays.days
    private val refreshTokenBytes = config.refreshTokenBytes

    suspend fun signInWithGoogle(idToken: String): AuthTokens {
        val profile: GoogleProfile = googleValidator.validate(idToken)
        val user = userRepository.findOrCreateUserByGoogle(profile)
        return issueTokens(user.id)
    }

    /**
     * Single-use refresh token rotation: validates & consumes old refresh token, issues new pair.
     */
    suspend fun refreshAccessToken(refreshTokenRaw: String): AuthTokens {
        val oldHash = hashRefresh(refreshTokenRaw)
        val newRefreshRaw = generateRefreshToken()
        val newHash = hashRefresh(newRefreshRaw)
        val newExpiresAt = Instant.now().plusMillis(refreshTtl.inWholeMilliseconds)
        val rotated =
            refreshTokenRepository.rotate(oldHash, newHash, newExpiresAt)
                ?: throw IllegalArgumentException(ErrorMessages.INVALID_REFRESH_TOKEN)
        val newAccess = createAccessToken(rotated.userId)
        return AuthTokens(accessToken = newAccess, refreshToken = newRefreshRaw)
    }

    suspend fun logout(refreshTokenRaw: String) {
        val hash = hashRefresh(refreshTokenRaw)
        refreshTokenRepository.revokeByHash(hash)
    }

    private suspend fun issueTokens(userId: UUID): AuthTokens {
        val access = createAccessToken(userId)
        val refresh = generateRefreshToken()
        val hash = hashRefresh(refresh)
        val expiresAt = Instant.now().plusMillis(refreshTtl.inWholeMilliseconds)
        refreshTokenRepository.store(userId, hash, expiresAt)
        return AuthTokens(accessToken = access, refreshToken = refresh)
    }

    private fun createAccessToken(userId: UUID): String {
        val now = Instant.now()
        val exp = now.plusMillis(accessTtl.inWholeMilliseconds)
        val jti = UUID.randomUUID().toString()
        return JWT.create()
            .withIssuer(config.jwtIssuer)
            .withSubject(userId.toString())
            .withJWTId(jti)
            .withClaim(JwtClaims.JTI, jti)
            .withClaim(JwtClaims.USER_ID, userId.toString())
            .withClaim(JwtClaims.TYPE, TokenTypes.ACCESS)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(exp))
            .sign(algorithm)
    }

    private fun generateRefreshToken(): String {
        val rnd = SecureRandom()
        val bytes = ByteArray(refreshTokenBytes)
        rnd.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hashRefresh(token: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(token.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
