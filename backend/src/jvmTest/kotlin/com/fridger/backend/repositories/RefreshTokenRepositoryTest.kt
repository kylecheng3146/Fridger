package com.fridger.backend.repositories

import com.fridger.backend.DatabaseTestHelper
import fridger.backend.db.UsersTable
import fridger.backend.repositories.RefreshTokenRepository
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.test.*

class RefreshTokenRepositoryTest {
    private val refreshTokenRepository = RefreshTokenRepository()
    private lateinit var userId: UUID

    @BeforeTest
    fun setup() {
        DatabaseTestHelper.setup()
        // Every test needs a user to associate tokens with
        userId =
            transaction {
                val newId = java.util.UUID.randomUUID()
                UsersTable.insert {
                    it[UsersTable.id] = newId
                    it[name] = "Test User"
                    it[email] = "tokenuser@test.com"
                    it[createdAt] = Instant.now()
                }
                newId
            }
    }

    @AfterTest
    fun teardown() {
        DatabaseTestHelper.teardown()
    }

    @Test
    fun `store should insert a new token record`() =
        runTest {
            val expiresAt = Instant.now().plus(Duration.ofDays(30))
            val record = refreshTokenRepository.store(userId, "hash1", expiresAt)

            assertEquals(userId, record.userId)
            assertEquals("hash1", record.tokenHash)
            assertNotNull(refreshTokenRepository.findValid("hash1"))
        }

    @Test
    fun `findValid should return null for non-existent token`() =
        runTest {
            assertNull(refreshTokenRepository.findValid("non-existent-hash"))
        }

    @Test
    fun `findValid should return null for expired token`() =
        runTest {
            val expiredTime = Instant.now().minus(Duration.ofDays(1))
            refreshTokenRepository.store(userId, "hash-expired", expiredTime)

            assertNull(refreshTokenRepository.findValid("hash-expired"))
        }

    @Test
    fun `rotate should fail if old token does not exist`() =
        runTest {
            val newExpiresAt = Instant.now().plus(Duration.ofDays(30))
            val result = refreshTokenRepository.rotate("old-non-existent", "new-hash", newExpiresAt)
            assertNull(result)
        }

    @Test
    fun `rotate should create new token and delete old one`() =
        runTest {
            val oldExpiresAt = Instant.now().plus(Duration.ofDays(30))
            refreshTokenRepository.store(userId, "old-hash", oldExpiresAt)

            val newExpiresAt = Instant.now().plus(Duration.ofDays(30))
            val rotatedRecord = refreshTokenRepository.rotate("old-hash", "new-hash", newExpiresAt)

            assertNotNull(rotatedRecord)
            assertEquals("new-hash", rotatedRecord.tokenHash)
            assertEquals(userId, rotatedRecord.userId)

            // Verify old token is gone and new one is present
            assertNull(refreshTokenRepository.findValid("old-hash"))
            assertNotNull(refreshTokenRepository.findValid("new-hash"))
        }

    @Test
    fun `revokeByHash should delete the token`() =
        runTest {
            val expiresAt = Instant.now().plus(Duration.ofDays(30))
            refreshTokenRepository.store(userId, "hash-to-revoke", expiresAt)

            val revoked = refreshTokenRepository.revokeByHash("hash-to-revoke")
            assertTrue(revoked)

            assertNull(refreshTokenRepository.findValid("hash-to-revoke"))
        }

    @Test
    fun `revokeByHash should return false for non-existent token`() =
        runTest {
            val revoked = refreshTokenRepository.revokeByHash("non-existent-hash")
            assertFalse(revoked)
        }
}
