package fridger.backend.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.*

object UsersTable : Table("users") {
    val id = uuid("id")
    val name = text("name")
    val email = text("email").uniqueIndex()
    val googleId = text("google_id").nullable().uniqueIndex()
    val pictureUrl = text("picture_url").nullable()
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object RefreshTokensTable : Table("refresh_tokens") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val tokenHash = text("token_hash").uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

data class User(
    val id: UUID,
    val name: String,
    val email: String,
    val googleId: String?,
    val pictureUrl: String?,
    val createdAt: Instant
)

data class RefreshTokenRecord(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
    val createdAt: Instant
)
