package fridger.backend.repositories

import fridger.backend.db.FridgeItemsTable
import fridger.backend.db.UsersTable
import fridger.shared.health.InventoryCategory
import fridger.shared.health.NutritionCategory
import fridger.shared.health.toNutritionCategory
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
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
    val addedDate: LocalDate? = null,
    val isClassified: Boolean = true,
    val inventoryCategory: InventoryCategory = when (category) {
        NutritionCategory.PRODUCE -> InventoryCategory.VEGETABLES
        NutritionCategory.PROTEIN -> InventoryCategory.MEAT
        NutritionCategory.REFINED_GRAIN -> InventoryCategory.GRAINS
        NutritionCategory.OTHER -> InventoryCategory.OTHERS
    },
)

interface FridgeItemDataSource {
    fun fetchItemsForUser(userId: UUID): List<FridgeItemRecord>

    fun upsertItem(
        userId: UUID,
        id: UUID,
        name: String,
        category: InventoryCategory,
        addDate: LocalDate,
        expiryDate: LocalDate,
        timeZoneId: String,
    ): Boolean = false

    fun deleteItem(userId: UUID, id: UUID): Boolean = false

    fun fetchTimeZoneForUser(userId: UUID): String = "UTC"

    fun updateTimeZoneForUser(userId: UUID, timeZoneId: String) {}
}

class FridgeItemRepository : FridgeItemDataSource {
    override fun fetchItemsForUser(userId: UUID): List<FridgeItemRecord> {
        return transaction {
            FridgeItemsTable
                .select { FridgeItemsTable.userId eq userId }
                .map { it.toRecord() }
        }
    }

    override fun upsertItem(
        userId: UUID,
        id: UUID,
        name: String,
        category: InventoryCategory,
        addDate: LocalDate,
        expiryDate: LocalDate,
        timeZoneId: String,
    ): Boolean = transaction {
        val predicate = (FridgeItemsTable.id eq id) and (FridgeItemsTable.userId eq userId)
        val existing = FridgeItemsTable.select { FridgeItemsTable.id eq id }.singleOrNull()
        if (existing != null && existing[FridgeItemsTable.userId] != userId) return@transaction false
        if (existing == null) {
            FridgeItemsTable.insert {
                it[FridgeItemsTable.id] = id
                it[FridgeItemsTable.userId] = userId
                it[FridgeItemsTable.name] = name
                it[FridgeItemsTable.category] = category.name
                it[FridgeItemsTable.quantity] = 0.0
                it[FridgeItemsTable.caloriesPerPortion] = 0
                it[FridgeItemsTable.expiryDate] = expiryDate
                it[FridgeItemsTable.addedDate] = addDate
                it[FridgeItemsTable.createdAt] = addDate.atStartOfDay(java.time.ZoneId.of(timeZoneId)).toInstant()
            }
        } else {
            FridgeItemsTable.update({ predicate }) {
                it[FridgeItemsTable.name] = name
                it[FridgeItemsTable.category] = category.name
                it[FridgeItemsTable.expiryDate] = expiryDate
                it[FridgeItemsTable.addedDate] = addDate
                it[FridgeItemsTable.createdAt] = addDate.atStartOfDay(java.time.ZoneId.of(timeZoneId)).toInstant()
            }
        }
        true
    }

    override fun deleteItem(userId: UUID, id: UUID): Boolean = transaction {
        FridgeItemsTable.deleteWhere { (FridgeItemsTable.id eq id) and (FridgeItemsTable.userId eq userId) } > 0
    }

    override fun fetchTimeZoneForUser(userId: UUID): String = transaction {
        UsersTable.select { UsersTable.id eq userId }.singleOrNull()?.get(UsersTable.timeZoneId) ?: "UTC"
    }

    override fun updateTimeZoneForUser(userId: UUID, timeZoneId: String) {
        transaction { UsersTable.update({ UsersTable.id eq userId }) { it[UsersTable.timeZoneId] = timeZoneId } }
    }

    private fun ResultRow.toRecord(): FridgeItemRecord {
        val rawCategory = this[FridgeItemsTable.category]
        val inventoryCategory = runCatching { InventoryCategory.valueOf(rawCategory) }.getOrNull()
        val legacyCategory = runCatching { NutritionCategory.valueOf(rawCategory) }.getOrNull()
        val nutritionCategory =
            inventoryCategory?.let { it.toNutritionCategory() }
                ?: legacyCategory
                ?: NutritionCategory.OTHER
        val isClassified = inventoryCategory != InventoryCategory.UNCATEGORIZED && (inventoryCategory != null || legacyCategory != null)
        return FridgeItemRecord(
            id = this[FridgeItemsTable.id],
            userId = this[FridgeItemsTable.userId],
            name = this[FridgeItemsTable.name],
            category = nutritionCategory,
            quantity = this[FridgeItemsTable.quantity],
            caloriesPerPortion = this[FridgeItemsTable.caloriesPerPortion],
            expiryDate = this[FridgeItemsTable.expiryDate],
            createdAt = this[FridgeItemsTable.createdAt],
            addedDate = this[FridgeItemsTable.addedDate],
            isClassified = isClassified,
            inventoryCategory = inventoryCategory ?: when (legacyCategory) {
                NutritionCategory.PRODUCE -> InventoryCategory.VEGETABLES
                NutritionCategory.PROTEIN -> InventoryCategory.MEAT
                NutritionCategory.REFINED_GRAIN -> InventoryCategory.GRAINS
                NutritionCategory.OTHER -> InventoryCategory.OTHERS
                null -> InventoryCategory.UNCATEGORIZED
            },
        )
    }
}
