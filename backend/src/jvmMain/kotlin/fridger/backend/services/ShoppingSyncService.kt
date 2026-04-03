package fridger.backend.services

import fridger.backend.models.ShoppingSyncActionDto
import fridger.backend.models.ShoppingSyncActionType
import fridger.backend.models.ShoppingSyncResultDto
import fridger.backend.repositories.ShoppingListRepository
import java.util.UUID

class ShoppingSyncService(
    private val repository: ShoppingListRepository
) {
    suspend fun sync(
        userId: UUID,
        listId: String,
        actions: List<ShoppingSyncActionDto>
    ): List<ShoppingSyncResultDto> {
        if (listId.isBlank()) {
            return actions.map { action ->
                ShoppingSyncResultDto(
                    actionId = action.actionId,
                    success = false,
                    error = "Missing listId"
                )
            }
        }
        return actions.map { action ->
            runCatching {
                when (action.type) {
                    ShoppingSyncActionType.ADD -> {
                        val itemId = action.itemId ?: throw IllegalArgumentException("Missing itemId")
                        val name = action.itemName ?: throw IllegalArgumentException("Missing itemName")
                        repository.insertItem(
                            id = itemId,
                            listId = listId,
                            userId = userId,
                            name = name,
                            quantity = action.quantity,
                            isChecked = action.isChecked ?: false
                        )
                    }
                    ShoppingSyncActionType.UPDATE -> {
                        val itemId = action.itemId ?: throw IllegalArgumentException("Missing itemId")
                        repository.updateItem(
                            id = itemId,
                            userId = userId,
                            name = action.itemName,
                            quantity = action.quantity,
                            isChecked = action.isChecked
                        )
                    }
                    ShoppingSyncActionType.DELETE -> {
                        val itemId = action.itemId ?: throw IllegalArgumentException("Missing itemId")
                        repository.deleteItem(itemId, userId)
                    }
                    ShoppingSyncActionType.CLEAR -> {
                        repository.clearChecked(listId, userId)
                    }
                }
            }.fold(
                onSuccess = {
                    ShoppingSyncResultDto(actionId = action.actionId, success = true, error = null)
                },
                onFailure = { ex ->
                    ShoppingSyncResultDto(actionId = action.actionId, success = false, error = ex.message)
                }
            )
        }
    }
}
