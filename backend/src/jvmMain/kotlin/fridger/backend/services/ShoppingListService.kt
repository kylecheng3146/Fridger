package fridger.backend.services

import fridger.backend.models.ShoppingListItemDto
import fridger.backend.models.ShoppingListResponse
import fridger.backend.repositories.ShoppingListRepository
import java.util.UUID

class ShoppingListService(
    private val repository: ShoppingListRepository
) {
    suspend fun upsert(
        userId: UUID,
        id: String,
        name: String,
        date: String?
    ): ShoppingListResponse {
        repository.upsertList(id, userId, name, date)
        return ShoppingListResponse(id = id, name = name, date = date)
    }

    suspend fun fetchLists(userId: UUID): List<ShoppingListResponse> {
        return repository.fetchLists(userId).map { meta ->
            ShoppingListResponse(id = meta.id, name = meta.name, date = meta.date)
        }
    }

    suspend fun fetchItems(
        userId: UUID,
        listId: String
    ): List<ShoppingListItemDto> {
        return repository.fetchItems(listId, userId).map { item ->
            ShoppingListItemDto(
                id = item.id,
                name = item.name,
                quantity = item.quantity,
                isChecked = item.isChecked
            )
        }
    }

    suspend fun deleteList(
        userId: UUID,
        listId: String
    ) {
        repository.deleteList(listId, userId)
    }
}
