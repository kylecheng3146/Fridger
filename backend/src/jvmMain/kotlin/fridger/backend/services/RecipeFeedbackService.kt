package fridger.backend.services

import fridger.backend.repositories.RecipeFeedbackRepository
import fridger.shared.recipe.SubmitRecipeFeedbackRequest
import java.util.UUID

interface RecipeFeedbackService {
    suspend fun submitFeedback(
        userId: UUID,
        request: SubmitRecipeFeedbackRequest
    )
}

class DefaultRecipeFeedbackService(
    private val repository: RecipeFeedbackRepository,
) : RecipeFeedbackService {
    override suspend fun submitFeedback(
        userId: UUID,
        request: SubmitRecipeFeedbackRequest
    ) {
        repository.submitFeedback(
            userId = userId,
            recipeId = request.recipeId,
            feedbackType = request.feedbackType,
        )
    }
}
