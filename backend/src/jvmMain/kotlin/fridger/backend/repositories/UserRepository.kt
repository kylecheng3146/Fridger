package fridger.backend.repositories

import fridger.backend.db.User
import fridger.backend.db.UsersTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

data class GoogleProfile(
    val googleId: String,
    val email: String,
    val name: String,
    val pictureUrl: String?
)

class UserRepository {
    suspend fun findOrCreateUserByGoogle(profile: GoogleProfile): User =
        withContext(Dispatchers.IO) {
            transaction {
                // Try find existing by google_id or email (email unique; google might be null first time)
                val existing =
                    UsersTable.select {
                        (UsersTable.googleId eq profile.googleId) or (UsersTable.email eq profile.email)
                    }.limit(1).firstOrNull()
                if (existing != null) {
                    // Optionally update name/picture if changed
                    val needUpdate = existing[UsersTable.name] != profile.name || existing[UsersTable.pictureUrl] != profile.pictureUrl || existing[UsersTable.googleId] == null
                    if (needUpdate) {
                        UsersTable.update({ UsersTable.id eq existing[UsersTable.id] }) {
                            it[name] = profile.name
                            it[pictureUrl] = profile.pictureUrl
                            it[googleId] = profile.googleId
                        }
                    }
                    existing.toUser()
                } else {
                    val id = UUID.randomUUID()
                    UsersTable.insert { row ->
                        row[UsersTable.id] = id
                        row[name] = profile.name
                        row[email] = profile.email
                        row[googleId] = profile.googleId
                        row[pictureUrl] = profile.pictureUrl
                        row[createdAt] = Instant.now()
                    }
                    User(
                        id = id,
                        name = profile.name,
                        email = profile.email,
                        googleId = profile.googleId,
                        pictureUrl = profile.pictureUrl,
                        createdAt = Instant.now()
                    )
                }
            }
        }

    suspend fun getUser(userId: UUID): User? =
        withContext(Dispatchers.IO) {
            transaction {
                UsersTable.select { UsersTable.id eq userId }.limit(1).firstOrNull()?.toUser()
            }
        }

    private fun ResultRow.toUser(): User =
        User(
            id = this[UsersTable.id],
            name = this[UsersTable.name],
            email = this[UsersTable.email],
            googleId = this[UsersTable.googleId],
            pictureUrl = this[UsersTable.pictureUrl],
            createdAt = this[UsersTable.createdAt]
        )
}
