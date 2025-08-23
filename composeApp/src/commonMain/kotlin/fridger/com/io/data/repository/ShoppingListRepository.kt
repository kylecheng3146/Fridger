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
    suspend fun getShoppingList(): List<ShoppingListItem>
    suspend fun addItem(name: String, quantity: String?)
    suspend fun updateItem(id: Long, isChecked: Boolean)
    suspend fun deleteItem(id: Long)
    suspend fun clearPurchasedItems()
}

class ShoppingListRepositoryImpl(
    private val db: FridgerDatabase = DatabaseProvider.database
) : ShoppingListRepository {

    override suspend fun getShoppingList(): List<ShoppingListItem> = withContext(Dispatchers.Default) {
        val rows = db.fridgerDatabaseQueries.selectAllShoppingItems().executeAsList()
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

    override suspend fun addItem(name: String, quantity: String?) = withContext(Dispatchers.Default) {
        db.fridgerDatabaseQueries.insertShoppingItem(
            name = name,
            quantity = quantity,
            isChecked = 0,
            category = null
        )
    }

    override suspend fun updateItem(id: Long, isChecked: Boolean) = withContext(Dispatchers.Default) {
        db.fridgerDatabaseQueries.updateShoppingItemChecked(
            isChecked = if (isChecked) 1 else 0,
            id = id
        )
    }

    override suspend fun deleteItem(id: Long) = withContext(Dispatchers.Default) {
        db.fridgerDatabaseQueries.deleteShoppingItemById(id)
    }

    override suspend fun clearPurchasedItems() = withContext(Dispatchers.Default) {
        db.fridgerDatabaseQueries.clearPurchasedItems()
    }
}
