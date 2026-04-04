package com.fridger.backend.repositories

import com.fridger.backend.DatabaseTestHelper
import fridger.backend.db.UsersTable
import fridger.backend.repositories.RecipeFeedbackRecord
import fridger.backend.repositories.RecipeFeedbackRepository
import fridger.shared.recipe.RecipeFeedbackType
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RecipeFeedbackRepositoryTest {
    private val repository = RecipeFeedbackRepository()
    private lateinit var userId: UUID

    @BeforeTest
    fun setup() {
        DatabaseTestHelper.setup()
        userId =
            transaction {
                val newId = UUID.randomUUID()
                UsersTable.insert {
                    it[UsersTable.id] = newId
                    it[name] = "Feedback User"
                    it[email] = "feedback@test.com"
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
    fun `submitFeedback inserts new record`() =
        runTest {
            val saved = repository.submitFeedback(userId, "recipe-1", RecipeFeedbackType.LIKE)

            assertEquals(userId, saved.userId)
            assertEquals("recipe-1", saved.recipeId)
            assertEquals(RecipeFeedbackType.LIKE, saved.feedbackType)
        }

    @Test
    fun `submitFeedback updates existing feedback for same user and recipe`() =
        runTest {
            repository.submitFeedback(userId, "recipe-1", RecipeFeedbackType.LIKE)

            val updated = repository.submitFeedback(userId, "recipe-1", RecipeFeedbackType.DISLIKE)
            val saved = repository.findByUserAndRecipe(userId, "recipe-1")

            assertEquals(RecipeFeedbackType.DISLIKE, updated.feedbackType)
            assertNotNull(saved)
            assertEquals(RecipeFeedbackType.DISLIKE, saved.feedbackType)
        }
}
