package fridger.backend.db

import fridger.shared.recipe.RecipeFeedbackType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.*

object UsersTable : Table("users") {
    val id = uuid("id")
    val name = text("name")
    val email = text("email").uniqueIndex()
    val googleId = text("google_id").nullable().uniqueIndex()
    val pictureUrl = text("picture_url").nullable()
    val timeZoneId = text("time_zone_id").default("UTC")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object RefreshTokensTable : Table("refresh_tokens") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val tokenHash = text("token_hash").uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object FridgeItemsTable : Table("fridge_items") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val name = text("name")
    val category = text("category")
    val quantity = double("quantity")
    val caloriesPerPortion = integer("calories_per_portion")
    val expiryDate = date("expiry_date").nullable()
    val addedDate = date("added_date").nullable()
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object HealthDashboardSnapshotsTable : Table("health_dashboard_snapshots") {
    val userId = uuid("user_id")
    val snapshotDate = date("snapshot_date")
    val timeZoneId = text("time_zone_id")
    val produceCount = integer("produce_count")
    val proteinCount = integer("protein_count")
    val grainCount = integer("grain_count")
    val otherCount = integer("other_count")
    val unclassifiedCount = integer("unclassified_count")
    val totalTrackedItems = integer("total_tracked_items")
    val diversityScore = integer("diversity_score")
    val diversityRating = text("diversity_rating")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(userId, snapshotDate)
}

object HealthDashboardExpirySnapshotsTable : Table("health_dashboard_expiry_snapshots") {
    val userId = uuid("user_id")
    val snapshotDate = date("snapshot_date")
    val itemId = uuid("item_id")
    val itemName = text("item_name")
    val category = text("category")
    val expiryDate = date("expiry_date")
    override val primaryKey = PrimaryKey(userId, snapshotDate, itemId)
}

object HealthDashboardEventsTable : Table("health_dashboard_events") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val eventName = text("event_name")
    val payload = text("payload")
    val occurredAt = timestamp("occurred_at")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ShoppingListsTable : Table("shopping_lists") {
    val id = text("id")
    val userId = uuid("user_id")
    val name = text("name")
    val listDate = text("list_date").nullable()
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ShoppingListItemsTable : Table("shopping_list_items") {
    val id = text("id")
    val listId = text("list_id")
    val userId = uuid("user_id")
    val name = text("name")
    val quantity = text("quantity").nullable()
    val isChecked = bool("is_checked")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object RecipeFeedbackTable : Table("recipe_feedback") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val recipeId = text("recipe_id")
    val feedbackType = text("feedback_type")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex(userId, recipeId)
    }

    override val primaryKey = PrimaryKey(id)
}

data class User(
    val id: UUID,
    val name: String,
    val email: String,
    val googleId: String?,
    val pictureUrl: String?,
    val createdAt: Instant
)

data class RefreshTokenRecord(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
    val createdAt: Instant
)

data class RecipeFeedbackRow(
    val id: UUID,
    val userId: UUID,
    val recipeId: String,
    val feedbackType: RecipeFeedbackType,
    val createdAt: Instant,
    val updatedAt: Instant,
)
