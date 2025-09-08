package fridger.backend.repositories

import fridger.backend.db.RefreshTokenRecord
import fridger.backend.db.RefreshTokensTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

class RefreshTokenRepository {
    suspend fun store(
        userId: UUID,
        tokenHash: String,
        expiresAt: Instant
    ): RefreshTokenRecord =
        withContext(Dispatchers.IO) {
            val id = UUID.randomUUID()
            val createdAt = Instant.now()
            transaction {
                RefreshTokensTable.insert { r ->
                    r[RefreshTokensTable.id] = id
                    r[RefreshTokensTable.userId] = userId
                    r[RefreshTokensTable.tokenHash] = tokenHash
                    r[RefreshTokensTable.expiresAt] = expiresAt
                    r[RefreshTokensTable.createdAt] = createdAt
                }
            }
            RefreshTokenRecord(id, userId, tokenHash, expiresAt, createdAt)
        }

    suspend fun findValid(
        tokenHash: String,
        now: Instant = Instant.now()
    ): RefreshTokenRecord? =
        withContext(Dispatchers.IO) {
            transaction {
                RefreshTokensTable.select { RefreshTokensTable.tokenHash eq tokenHash }.limit(1).firstOrNull()?.let {
                        row ->
                    val rec = row.toRecord()
                    if (rec.expiresAt.isAfter(now)) rec else null
                }
            }
        }

    /**
     * Atomically rotate a refresh token (single‑use). Deletes the old token row if valid and inserts the new one.
     * Returns the newly created record or null if the old token was not found / invalid / expired.
     */
    suspend fun rotate(
        oldTokenHash: String,
        newTokenHash: String,
        newExpiresAt: Instant,
        now: Instant = Instant.now()
    ): RefreshTokenRecord? =
        withContext(Dispatchers.IO) {
            transaction {
                val row =
                    RefreshTokensTable.select { RefreshTokensTable.tokenHash eq oldTokenHash }.limit(
                        1
                    ).firstOrNull()
                if (row == null) return@transaction null
                val existing = row.toRecord()
                if (!existing.expiresAt.isAfter(now)) return@transaction null
                // delete old; ensure exactly one row deleted to prevent race creating multiple new tokens
                val deleted = RefreshTokensTable.deleteWhere { RefreshTokensTable.tokenHash eq oldTokenHash }
                if (deleted != 1) return@transaction null
                val id = UUID.randomUUID()
                val createdAt = Instant.now()
                RefreshTokensTable.insert { r ->
                    r[RefreshTokensTable.id] = id
                    r[RefreshTokensTable.userId] = existing.userId
                    r[RefreshTokensTable.tokenHash] = newTokenHash
                    r[RefreshTokensTable.expiresAt] = newExpiresAt
                    r[RefreshTokensTable.createdAt] = createdAt
                }
                RefreshTokenRecord(id, existing.userId, newTokenHash, newExpiresAt, createdAt)
            }
        }

    /** Revoke (delete) a refresh token by its hash. Returns true if a row was deleted. */
    suspend fun revokeByHash(tokenHash: String): Boolean =
        withContext(Dispatchers.IO) {
            transaction {
                RefreshTokensTable.deleteWhere { RefreshTokensTable.tokenHash eq tokenHash } > 0
            }
        }

    private fun ResultRow.toRecord() =
        RefreshTokenRecord(
            id = this[RefreshTokensTable.id],
            userId = this[RefreshTokensTable.userId],
            tokenHash = this[RefreshTokensTable.tokenHash],
            expiresAt = this[RefreshTokensTable.expiresAt],
            createdAt = this[RefreshTokensTable.createdAt]
        )
}
