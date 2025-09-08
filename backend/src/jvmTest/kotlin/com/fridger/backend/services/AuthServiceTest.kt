package com.fridger.backend.services

import fridger.backend.config.AppConfig
import fridger.backend.db.RefreshTokenRecord
import fridger.backend.db.User
import fridger.backend.repositories.GoogleProfile
import fridger.backend.repositories.RefreshTokenRepository
import fridger.backend.repositories.UserRepository
import fridger.backend.security.GoogleTokenValidator
import fridger.backend.services.AuthService
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import java.time.Instant
import java.util.UUID
import kotlin.test.*

class AuthServiceTest {
    @RelaxedMockK
    private lateinit var userRepository: UserRepository

    @RelaxedMockK
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @RelaxedMockK
    private lateinit var googleValidator: GoogleTokenValidator

    @RelaxedMockK
    private lateinit var appConfig: AppConfig

    private lateinit var authService: AuthService

    private val testUser = User(UUID.randomUUID(), "Test User", "test@test.com", "google123", null, Instant.now())

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // Default config setup
        every { appConfig.jwtSecret } returns "a-very-long-and-secure-secret-key-for-testing"
        every { appConfig.jwtIssuer } returns "test-issuer"
        every { appConfig.accessTokenMinutes } returns 15
        every { appConfig.refreshTokenDays } returns 30
        every { appConfig.refreshTokenBytes } returns 32
        authService = AuthService(appConfig, userRepository, refreshTokenRepository, googleValidator)
    }

    @Test
    fun `signInWithGoogle should return tokens for valid google token`() =
        runTest {
            val googleProfile = GoogleProfile("google123", "test@test.com", "Test User", null)
            coEvery { googleValidator.validate(any()) } returns googleProfile
            coEvery { userRepository.findOrCreateUserByGoogle(googleProfile) } returns testUser
            coEvery { refreshTokenRepository.store(any(), any(), any()) } returns mockk()

            val tokens = authService.signInWithGoogle("valid-id-token")

            assertNotNull(tokens)
            assertTrue(tokens.accessToken.isNotEmpty())
            assertTrue(tokens.refreshToken.isNotEmpty())
            coVerify(exactly = 1) { refreshTokenRepository.store(eq(testUser.id), any(), any()) }
        }

    @Test
    fun `refreshAccessToken should return new tokens on valid rotation`() =
        runTest {
            val oldRefreshToken = "old-refresh-token"
            val rotatedRecord =
                RefreshTokenRecord(UUID.randomUUID(), testUser.id, "new-hash", Instant.now(), Instant.now())
            coEvery { refreshTokenRepository.rotate(any(), any(), any(), any()) } returns rotatedRecord

            val tokens = authService.refreshAccessToken(oldRefreshToken)

            assertNotNull(tokens)
            assertTrue(tokens.accessToken.isNotEmpty())
            assertTrue(tokens.refreshToken.isNotEmpty())
            assertNotEquals(oldRefreshToken, tokens.refreshToken)
            coVerify(exactly = 1) { refreshTokenRepository.rotate(any(), any(), any(), any()) }
        }

    @Test
    fun `refreshAccessToken should throw exception on invalid rotation`() =
        runTest {
            coEvery { refreshTokenRepository.rotate(any(), any(), any(), any()) } returns null

            assertFailsWith<IllegalArgumentException> {
                authService.refreshAccessToken("invalid-token")
            }
        }

    @Test
    fun `logout should call repository to revoke token`() =
        runTest {
            val refreshToken = "token-to-revoke"
            coEvery { refreshTokenRepository.revokeByHash(any()) } returns true

            authService.logout(refreshToken)

            coVerify(exactly = 1) { refreshTokenRepository.revokeByHash(any()) }
        }
}
