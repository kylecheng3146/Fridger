package fridger.backend.repositories

import fridger.backend.BaseDbTest
import fridger.backend.db.FridgeItemsTable
import fridger.backend.db.UsersTable
import fridger.shared.health.NutritionCategory
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FridgeItemRepositoryTest : BaseDbTest() {
    private val repository = FridgeItemRepository()

    @Test
    fun fetchItemsForUser_returnsMappedRecords() {
        val userId = UUID.randomUUID()
        val anotherUser = UUID.randomUUID()
        transaction {
            UsersTable.insert {
                it[id] = userId
                it[name] = "Alice"
                it[email] = "alice@example.com"
                it[googleId] = null
                it[pictureUrl] = null
                it[createdAt] = Instant.now()
            }
            UsersTable.insert {
                it[id] = anotherUser
                it[name] = "Bob"
                it[email] = "bob@example.com"
                it[googleId] = null
                it[pictureUrl] = null
                it[createdAt] = Instant.now()
            }
            FridgeItemsTable.insert {
                it[id] = UUID.randomUUID()
                it[userId] = userId
                it[name] = "Kale"
                it[category] = NutritionCategory.PRODUCE.name
                it[quantity] = 4.0
                it[caloriesPerPortion] = 30
                it[expiryDate] = LocalDate.of(2024, 1, 15)
                it[createdAt] = Instant.now()
            }
            FridgeItemsTable.insert {
                it[id] = UUID.randomUUID()
                it[userId] = userId
                it[name] = "Chicken Breast"
                it[category] = NutritionCategory.PROTEIN.name
                it[quantity] = 2.0
                it[caloriesPerPortion] = 220
                it[expiryDate] = LocalDate.of(2024, 1, 12)
                it[createdAt] = Instant.now()
            }
            FridgeItemsTable.insert {
                it[id] = UUID.randomUUID()
                it[userId] = anotherUser
                it[name] = "Yogurt"
                it[category] = NutritionCategory.OTHER.name
                it[quantity] = 1.0
                it[caloriesPerPortion] = 100
                it[expiryDate] = LocalDate.of(2024, 1, 18)
                it[createdAt] = Instant.now()
            }
        }

        val records = repository.fetchItemsForUser(userId)

        assertEquals(2, records.size)
        val names = records.map { it.name }.toSet()
        assertTrue(names.containsAll(listOf("Kale", "Chicken Breast")))
        assertEquals(setOf(NutritionCategory.PRODUCE, NutritionCategory.PROTEIN), records.map { it.category }.toSet())
    }
}
