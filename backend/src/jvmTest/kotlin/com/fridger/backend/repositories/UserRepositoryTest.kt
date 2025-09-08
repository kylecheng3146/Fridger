package com.fridger.backend.repositories

import com.fridger.backend.DatabaseTestHelper
import fridger.backend.db.UsersTable
import fridger.backend.repositories.GoogleProfile
import fridger.backend.repositories.UserRepository
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class UserRepositoryTest {
    private val userRepository = UserRepository()

    @BeforeTest
    fun setup() {
        DatabaseTestHelper.setup()
    }

    @AfterTest
    fun teardown() {
        DatabaseTestHelper.teardown()
    }

    @Test
    fun `findOrCreateUserByGoogle should create new user if not exists`() =
        runTest {
            val profile = GoogleProfile("google123", "test@example.com", "Test User", null)

            val user = userRepository.findOrCreateUserByGoogle(profile)

            assertEquals("Test User", user.name)
            assertEquals("test@example.com", user.email)
            assertEquals("google123", user.googleId)

            val dbUser =
                transaction {
                    UsersTable.select { UsersTable.googleId eq "google123" }.singleOrNull()
                }
            assertNotNull(dbUser)
            assertEquals("Test User", dbUser[UsersTable.name])
        }

    @Test
    fun `findOrCreateUserByGoogle should find existing user by googleId`() =
        runTest {
            // First, create a user
            val profile = GoogleProfile("google123", "test@example.com", "Test User", null)
            val firstUser = userRepository.findOrCreateUserByGoogle(profile)

            // Now, try to find the same user again, with updated info
            val sameProfile = GoogleProfile("google123", "test@example.com", "Test User Updated", "new_pic.jpg")
            val secondUser = userRepository.findOrCreateUserByGoogle(sameProfile)

            assertEquals(firstUser.id, secondUser.id)
            assertEquals("Test User Updated", secondUser.name) // Name should be updated
            assertEquals("new_pic.jpg", secondUser.pictureUrl) // Picture should be updated

            val count = transaction { UsersTable.select { UsersTable.id eq firstUser.id }.count() }
            assertEquals(1, count)
        }

    @Test
    fun `findOrCreateUserByGoogle should find existing user by email and update googleId`() =
        runTest {
            // Create a user with only email (imagine they signed up via another method)
            val id =
                transaction {
                    val newId = java.util.UUID.randomUUID()
                    UsersTable.insert { row ->
                        row[UsersTable.id] = newId
                        row[name] = "Email User"
                        row[email] = "test@example.com"
                        row[createdAt] = java.time.Instant.now()
                    }
                    newId
                }

            // Now, they sign in with Google
            val profile = GoogleProfile("google456", "test@example.com", "Google Name", "pic.jpg")
            val user = userRepository.findOrCreateUserByGoogle(profile)

            assertEquals(id, user.id) // Should be the same user
            assertEquals("google456", user.googleId) // Google ID should be updated
            assertEquals("Google Name", user.name) // Name should be updated

            val dbUser = transaction { UsersTable.select { UsersTable.id eq id }.single() }
            assertEquals("google456", dbUser[UsersTable.googleId])
        }
}
