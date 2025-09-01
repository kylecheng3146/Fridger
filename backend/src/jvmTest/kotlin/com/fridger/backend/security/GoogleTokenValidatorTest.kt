package com.fridger.backend.security

import fridger.backend.security.GoogleTokenValidator
import io.mockk.mockk
import org.slf4j.Logger
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.*
import kotlin.test.*
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import java.util.Date
import org.junit.Before

class GoogleTokenValidatorTest {

    private lateinit var validator: GoogleTokenValidator
    private lateinit var keyPair: java.security.KeyPair
    private lateinit var rsaAlgorithm: Algorithm

    private val keyId = "test-key-id"
    private val clientId = "test-client-id"

    @Before
    fun setup() {
        val logger = mockk<Logger>(relaxed = true)
        // We test validation logic, so network-related params can be dummy values
        validator = GoogleTokenValidator(logger, setOf(clientId), "http://dummy.url", 6)

        // Generate a real RSA key pair for signing and verification
        keyPair = KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }.genKeyPair()

        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey
        rsaAlgorithm = Algorithm.RSA256(publicKey, privateKey)

        // Use reflection to inject our public key into the validator's private key cache
        val keysField = GoogleTokenValidator::class.java.getDeclaredField("keys")
        keysField.isAccessible = true
        keysField.set(validator, mapOf(keyId to publicKey))
    }

    private fun createTestToken(
        issuer: String = "https://accounts.google.com",
        audience: String = clientId,
        expiresInSeconds: Long = 3600,
        subject: String = "1234567890"
    ): String {
        val now = Instant.now()
        return JWT.create()
            .withKeyId(keyId)
            .withIssuer(issuer)
            .withSubject(subject)
            .withAudience(audience)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(expiresInSeconds)))
            .withClaim("email", "test@example.com")
            .withClaim("name", "Test User")
            .sign(rsaAlgorithm)
    }

    @Test
    fun `validate should succeed for a valid, self-signed token`() {
        val token = createTestToken()
        val profile = validator.validate(token)

        assertNotNull(profile)
        assertEquals("1234567890", profile.googleId)
        assertEquals("test@example.com", profile.email)
    }

    @Test
    fun `validate should fail for an expired token`() {
        val token = createTestToken(expiresInSeconds = -10) // Expired 10 seconds ago
        val exception = assertFailsWith<IllegalArgumentException> {
            validator.validate(token)
        }
        assertTrue(exception.message?.contains("Token expired") ?: false)
    }

    @Test
    fun `validate should fail for a wrong audience`() {
        val token = createTestToken(audience = "wrong-client-id")
        val exception = assertFailsWith<IllegalArgumentException> {
            validator.validate(token)
        }
        assertTrue(exception.message?.contains("aud mismatch") ?: false)
    }

    @Test
    fun `validate should fail for a token with wrong signature`() {
        val anotherKeyPair = KeyPairGenerator.getInstance("RSA").genKeyPair()
        val anotherPrivateKey = anotherKeyPair.private as RSAPrivateKey
        val wrongAlgorithm = Algorithm.RSA256(null, anotherPrivateKey)

        val token = JWT.create().withKeyId(keyId).withIssuer("https://accounts.google.com").sign(wrongAlgorithm)

        assertFailsWith<IllegalArgumentException> {
            validator.validate(token)
        }
    }
}
