package fridger.backend.repositories

import fridger.backend.db.RecipeFeedbackRow
import fridger.backend.db.RecipeFeedbackTable
import fridger.backend.db.dbQuery
import fridger.shared.recipe.RecipeFeedbackType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

typealias RecipeFeedbackRecord = RecipeFeedbackRow

class RecipeFeedbackRepository {
    suspend fun submitFeedback(
        userId: UUID,
        recipeId: String,
        feedbackType: RecipeFeedbackType,
    ): RecipeFeedbackRecord =
        dbQuery {
            val existing = findByUserAndRecipeInternal(userId, recipeId)
            val now = Instant.now()

            if (existing == null) {
                RecipeFeedbackTable.insert {
                    it[id] = UUID.randomUUID()
                    it[RecipeFeedbackTable.userId] = userId
                    it[RecipeFeedbackTable.recipeId] = recipeId
                    it[RecipeFeedbackTable.feedbackType] = feedbackType.name
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            } else {
                RecipeFeedbackTable.update(
                    { (RecipeFeedbackTable.userId eq userId) and (RecipeFeedbackTable.recipeId eq recipeId) }
                ) {
                    it[RecipeFeedbackTable.feedbackType] = feedbackType.name
                    it[updatedAt] = now
                }
            }

            requireNotNull(findByUserAndRecipeInternal(userId, recipeId))
        }

    suspend fun findByUserAndRecipe(
        userId: UUID,
        recipeId: String
    ): RecipeFeedbackRecord? = dbQuery { findByUserAndRecipeInternal(userId, recipeId) }

    private fun findByUserAndRecipeInternal(
        userId: UUID,
        recipeId: String
    ): RecipeFeedbackRecord? =
        RecipeFeedbackTable
            .selectAll()
            .where { (RecipeFeedbackTable.userId eq userId) and (RecipeFeedbackTable.recipeId eq recipeId) }
            .singleOrNull()
            ?.toRecord()

    private fun ResultRow.toRecord(): RecipeFeedbackRecord =
        RecipeFeedbackRecord(
            id = this[RecipeFeedbackTable.id],
            userId = this[RecipeFeedbackTable.userId],
            recipeId = this[RecipeFeedbackTable.recipeId],
            feedbackType = RecipeFeedbackType.valueOf(this[RecipeFeedbackTable.feedbackType]),
            createdAt = this[RecipeFeedbackTable.createdAt],
            updatedAt = this[RecipeFeedbackTable.updatedAt],
        )
}
