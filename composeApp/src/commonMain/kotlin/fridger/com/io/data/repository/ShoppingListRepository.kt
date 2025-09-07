package fridger.com.io.data.repository

import fridger.com.io.data.database.DatabaseProvider
import fridger.com.io.database.FridgerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Domain model for shopping list item (simple data holder for UI)
data class ShoppingListItem(
    val id: Long,
    val name: String,
    val quantity: String?,
    val isChecked: Boolean,
    val category: String?
)

interface ShoppingListRepository {
    suspend fun getShoppingList(listId: String? = null): List<ShoppingListItem>

    suspend fun addItem(
        name: String,
        quantity: String?,
        listId: String? = null
    )

    suspend fun updateItem(
        id: Long,
        isChecked: Boolean
    )

    suspend fun deleteItem(id: Long)

    suspend fun clearPurchasedItems(listId: String? = null)

    suspend fun deleteItemsByList(listId: String)
}

class ShoppingListRepositoryImpl(
    private val db: FridgerDatabase = DatabaseProvider.database
) : ShoppingListRepository {
    override suspend fun getShoppingList(listId: String?): List<ShoppingListItem> =
        withContext(Dispatchers.Default) {
            val rows =
                if (listId == null) {
                    db.fridgerDatabaseQueries.selectAllShoppingItems().executeAsList()
                } else {
                    db.fridgerDatabaseQueries.selectShoppingItemsByCategory(listId).executeAsList()
                }
            rows.map { row ->
                ShoppingListItem(
                    id = row.id,
                    name = row.name,
                    quantity = row.quantity,
                    isChecked = row.isChecked == 1L,
                    category = row.category
                )
            }
        }

    override suspend fun addItem(
        name: String,
        quantity: String?,
        listId: String?
    ) = withContext(Dispatchers.Default) {
        db.fridgerDatabaseQueries.insertShoppingItem(
            name = name,
            quantity = quantity,
            isChecked = 0,
            category = listId
        )
    }

    override suspend fun updateItem(
        id: Long,
        isChecked: Boolean
    ) = withContext(Dispatchers.Default) {
        db.fridgerDatabaseQueries.updateShoppingItemChecked(
            isChecked = if (isChecked) 1 else 0,
            id = id
        )
    }

    override suspend fun deleteItem(id: Long) =
        withContext(Dispatchers.Default) {
            db.fridgerDatabaseQueries.deleteShoppingItemById(id)
        }

    override suspend fun clearPurchasedItems(listId: String?) =
        withContext(Dispatchers.Default) {
            if (listId == null) {
                db.fridgerDatabaseQueries.clearPurchasedItems()
            } else {
                db.fridgerDatabaseQueries.clearPurchasedItemsByCategory(listId)
            }
        }

    override suspend fun deleteItemsByList(listId: String) =
        withContext(Dispatchers.Default) {
            db.fridgerDatabaseQueries.deleteShoppingItemsByCategory(listId)
        }
}
