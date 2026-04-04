package fridger.backend.repositories

import fridger.backend.db.ShoppingListItemsTable
import fridger.backend.db.ShoppingListsTable
import fridger.backend.db.dbQuery
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

data class ShoppingListItemRecord(
    val id: String,
    val listId: String,
    val userId: UUID,
    val name: String,
    val quantity: String?,
    val isChecked: Boolean,
    val createdAt: Instant
)

data class ShoppingListMetaRecord(
    val id: String,
    val name: String,
    val date: String?
)

class ShoppingListRepository {
    suspend fun upsertList(
        listId: String,
        userId: UUID,
        name: String,
        date: String?
    ) = dbQuery {
        val updated =
            ShoppingListsTable.update({ (ShoppingListsTable.id eq listId) and (ShoppingListsTable.userId eq userId) }) {
                it[ShoppingListsTable.name] = name
                it[ShoppingListsTable.listDate] = date
            }
        if (updated == 0) {
            ShoppingListsTable.insert {
                it[id] = listId
                it[ShoppingListsTable.userId] = userId
                it[ShoppingListsTable.name] = name
                it[listDate] = date
                it[createdAt] = Instant.now()
            }
        }
    }

    suspend fun insertItem(
        id: String,
        listId: String,
        userId: UUID,
        name: String,
        quantity: String?,
        isChecked: Boolean
    ) = dbQuery {
        ShoppingListItemsTable.insert {
            it[ShoppingListItemsTable.id] = id
            it[ShoppingListItemsTable.listId] = listId
            it[ShoppingListItemsTable.userId] = userId
            it[ShoppingListItemsTable.name] = name
            it[ShoppingListItemsTable.quantity] = quantity
            it[ShoppingListItemsTable.isChecked] = isChecked
            it[ShoppingListItemsTable.createdAt] = Instant.now()
        }
    }

    suspend fun updateItem(
        id: String,
        userId: UUID,
        name: String?,
        quantity: String?,
        isChecked: Boolean?
    ) = dbQuery {
        ShoppingListItemsTable.update(
            { (ShoppingListItemsTable.id eq id) and (ShoppingListItemsTable.userId eq userId) }
        ) {
            name?.let { n -> it[ShoppingListItemsTable.name] = n }
            quantity?.let { q -> it[ShoppingListItemsTable.quantity] = q }
            isChecked?.let { c -> it[ShoppingListItemsTable.isChecked] = c }
        }
    }

    suspend fun deleteItem(
        id: String,
        userId: UUID
    ) = dbQuery {
        ShoppingListItemsTable.deleteWhere { (ShoppingListItemsTable.id eq id) and (ShoppingListItemsTable.userId eq userId) }
    }

    suspend fun clearChecked(
        listId: String,
        userId: UUID
    ) = dbQuery {
        ShoppingListItemsTable.deleteWhere {
            (ShoppingListItemsTable.listId eq listId) and (ShoppingListItemsTable.userId eq userId) and (ShoppingListItemsTable.isChecked eq true)
        }
    }

    suspend fun fetchLists(userId: UUID): List<ShoppingListMetaRecord> =
        dbQuery {
            ShoppingListsTable.selectAll().where { ShoppingListsTable.userId eq userId }.map { row ->
                ShoppingListMetaRecord(
                    id = row[ShoppingListsTable.id],
                    name = row[ShoppingListsTable.name],
                    date = row[ShoppingListsTable.listDate]
                )
            }
        }

    suspend fun fetchItems(
        listId: String,
        userId: UUID
    ): List<ShoppingListItemRecord> =
        dbQuery {
            ShoppingListItemsTable
                .selectAll().where { (ShoppingListItemsTable.listId eq listId) and (ShoppingListItemsTable.userId eq userId) }
                .map { row ->
                    ShoppingListItemRecord(
                        id = row[ShoppingListItemsTable.id],
                        listId = row[ShoppingListItemsTable.listId],
                        userId = row[ShoppingListItemsTable.userId],
                        name = row[ShoppingListItemsTable.name],
                        quantity = row[ShoppingListItemsTable.quantity],
                        isChecked = row[ShoppingListItemsTable.isChecked],
                        createdAt = row[ShoppingListItemsTable.createdAt]
                    )
                }
        }

    suspend fun deleteList(
        listId: String,
        userId: UUID
    ) = dbQuery {
        ShoppingListsTable.deleteWhere { (ShoppingListsTable.id eq listId) and (ShoppingListsTable.userId eq userId) }
    }
}
