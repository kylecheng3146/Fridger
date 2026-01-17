package fridger.backend.repositories

import fridger.backend.db.FridgeItemsTable
import fridger.shared.health.NutritionCategory
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class FridgeItemRecord(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val category: NutritionCategory,
    val quantity: Double,
    val caloriesPerPortion: Int,
    val expiryDate: LocalDate?,
    val createdAt: Instant,
)

interface FridgeItemDataSource {
    fun fetchItemsForUser(userId: UUID): List<FridgeItemRecord>
}

class FridgeItemRepository : FridgeItemDataSource {
    override fun fetchItemsForUser(userId: UUID): List<FridgeItemRecord> {
        return transaction {
            FridgeItemsTable
                .select { FridgeItemsTable.userId eq userId }
                .map { it.toRecord() }
        }
    }

    private fun ResultRow.toRecord(): FridgeItemRecord {
        val rawCategory = this[FridgeItemsTable.category]
        val category = runCatching { NutritionCategory.valueOf(rawCategory) }.getOrElse { NutritionCategory.OTHER }
        return FridgeItemRecord(
            id = this[FridgeItemsTable.id],
            userId = this[FridgeItemsTable.userId],
            name = this[FridgeItemsTable.name],
            category = category,
            quantity = this[FridgeItemsTable.quantity],
            caloriesPerPortion = this[FridgeItemsTable.caloriesPerPortion],
            expiryDate = this[FridgeItemsTable.expiryDate],
            createdAt = this[FridgeItemsTable.createdAt],
        )
    }
}
