package fridger.backend.repositories

import fridger.backend.db.HealthDashboardExpirySnapshotsTable
import fridger.backend.db.HealthDashboardEventsTable
import fridger.backend.db.HealthDashboardSnapshotsTable
import fridger.backend.db.UsersTable
import fridger.shared.health.DiversityRating
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.InventoryItem
import fridger.shared.health.NutritionCategory
import fridger.shared.health.ExpirySeverity
import fridger.shared.health.ExpiryHeatmapCell
import fridger.shared.health.DiversityHistoryEntry
import fridger.shared.health.TrendSnapshot
import kotlin.math.roundToInt
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.LocalDate as KotlinLocalDate

data class HealthDashboardSnapshot(
    val date: KotlinLocalDate,
    val distribution: Map<NutritionCategory, Double>,
    val totalItems: Int,
    val unclassifiedPercent: Double,
    val diversityScore: Int,
    val diversityRating: DiversityRating,
)

data class HealthDashboardSnapshotRange(
    val snapshots: List<TrendSnapshot>,
    val diversityHistory: List<DiversityHistoryEntry>,
    val expiryHeatmap: List<ExpiryHeatmapCell>,
    val snapshotDates: Set<KotlinLocalDate>,
)

interface HealthDashboardSnapshotStore {
    fun users(): List<Pair<UUID, String>>

    fun save(
        userId: UUID,
        date: LocalDate,
        timeZoneId: String,
        metrics: HealthDashboardMetrics,
        items: List<InventoryItem>,
    )

    fun fetch(userId: UUID, from: LocalDate, through: LocalDate): HealthDashboardSnapshotRange
}

class HealthDashboardSnapshotRepository : HealthDashboardSnapshotStore {
    override fun users(): List<Pair<UUID, String>> = transaction {
        UsersTable.selectAll().map { it[UsersTable.id] to it[UsersTable.timeZoneId] }
    }

    override fun save(
        userId: UUID,
        date: LocalDate,
        timeZoneId: String,
        metrics: HealthDashboardMetrics,
        items: List<InventoryItem>,
    ) = transaction {
        UsersTable.select { UsersTable.id eq userId }.forUpdate().single()
        val classified = items.filter { it.isClassified }
        val counts = NutritionCategory.entries.associateWith { category -> classified.count { it.category == category } }
        val existing = HealthDashboardSnapshotsTable.select {
            (HealthDashboardSnapshotsTable.userId eq userId) and
                (HealthDashboardSnapshotsTable.snapshotDate eq date)
        }.singleOrNull()
        if (existing == null) {
            HealthDashboardSnapshotsTable.insert {
                it[HealthDashboardSnapshotsTable.userId] = userId
                it[HealthDashboardSnapshotsTable.snapshotDate] = date
                it[HealthDashboardSnapshotsTable.timeZoneId] = timeZoneId
                it[HealthDashboardSnapshotsTable.produceCount] = counts[NutritionCategory.PRODUCE] ?: 0
                it[HealthDashboardSnapshotsTable.proteinCount] = counts[NutritionCategory.PROTEIN] ?: 0
                it[HealthDashboardSnapshotsTable.grainCount] = counts[NutritionCategory.REFINED_GRAIN] ?: 0
                it[HealthDashboardSnapshotsTable.otherCount] = counts[NutritionCategory.OTHER] ?: 0
                it[HealthDashboardSnapshotsTable.unclassifiedCount] = items.size - classified.size
                it[HealthDashboardSnapshotsTable.totalTrackedItems] = items.size
                it[HealthDashboardSnapshotsTable.diversityScore] = metrics.diversityScore.value
                it[HealthDashboardSnapshotsTable.diversityRating] = metrics.diversityScore.rating.name
                it[HealthDashboardSnapshotsTable.createdAt] = Instant.now()
            }
        } else {
            HealthDashboardSnapshotsTable.update({
                (HealthDashboardSnapshotsTable.userId eq userId) and
                    (HealthDashboardSnapshotsTable.snapshotDate eq date)
            }) {
                it[HealthDashboardSnapshotsTable.timeZoneId] = timeZoneId
                it[HealthDashboardSnapshotsTable.produceCount] = counts[NutritionCategory.PRODUCE] ?: 0
                it[HealthDashboardSnapshotsTable.proteinCount] = counts[NutritionCategory.PROTEIN] ?: 0
                it[HealthDashboardSnapshotsTable.grainCount] = counts[NutritionCategory.REFINED_GRAIN] ?: 0
                it[HealthDashboardSnapshotsTable.otherCount] = counts[NutritionCategory.OTHER] ?: 0
                it[HealthDashboardSnapshotsTable.unclassifiedCount] = items.size - classified.size
                it[HealthDashboardSnapshotsTable.totalTrackedItems] = items.size
                it[HealthDashboardSnapshotsTable.diversityScore] = metrics.diversityScore.value
                it[HealthDashboardSnapshotsTable.diversityRating] = metrics.diversityScore.rating.name
            }
        }

        HealthDashboardExpirySnapshotsTable.deleteWhere {
            (HealthDashboardExpirySnapshotsTable.userId eq userId) and
                (HealthDashboardExpirySnapshotsTable.snapshotDate eq date)
        }
        val maxExpiry = date.plusDays(30)
        items.mapNotNull { item ->
            val expiry = item.expiryDate?.let { LocalDate.parse(it.toString()) } ?: return@mapNotNull null
            if (expiry.isBefore(date) || expiry.isAfter(maxExpiry)) return@mapNotNull null
            Triple(UUID.fromString(item.id), item.name, expiry to item.category)
        }.forEach { (itemId, name, expiryAndCategory) ->
            HealthDashboardExpirySnapshotsTable.insert {
                it[HealthDashboardExpirySnapshotsTable.userId] = userId
                it[HealthDashboardExpirySnapshotsTable.snapshotDate] = date
                it[HealthDashboardExpirySnapshotsTable.itemId] = itemId
                it[HealthDashboardExpirySnapshotsTable.itemName] = name
                it[HealthDashboardExpirySnapshotsTable.category] = expiryAndCategory.second.name
                it[HealthDashboardExpirySnapshotsTable.expiryDate] = expiryAndCategory.first
            }
        }
    }

    override fun fetch(userId: UUID, from: LocalDate, through: LocalDate): HealthDashboardSnapshotRange = transaction {
        val rows = HealthDashboardSnapshotsTable.select {
            (HealthDashboardSnapshotsTable.userId eq userId) and
                (HealthDashboardSnapshotsTable.snapshotDate greaterEq from) and
                (HealthDashboardSnapshotsTable.snapshotDate lessEq through)
        }.orderBy(HealthDashboardSnapshotsTable.snapshotDate to SortOrder.ASC).map { it.toSnapshot() }
        val trendSnapshots = rows.map { snapshot ->
            TrendSnapshot(
                date = snapshot.date,
                distribution = snapshot.distribution,
                deficitCategories = snapshot.distribution.filterValues { it == 0.0 }.keys.toList(),
                totalTrackedItems = snapshot.totalItems,
                unclassifiedPercent = snapshot.unclassifiedPercent,
            )
        }
        val diversityHistory = rows.groupBy { snapshot ->
            val date = LocalDate.parse(snapshot.date.toString())
            KotlinLocalDate.parse(date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).toString())
        }.map { (weekStart, weekly) ->
            weekly.last().let { DiversityHistoryEntry(weekStart, it.diversityScore, it.diversityRating) }
        }
        val latestDate = rows.lastOrNull()?.date?.let { LocalDate.parse(it.toString()) }
        val heatmap = if (latestDate == null) emptyList() else {
            HealthDashboardExpirySnapshotsTable.select {
                (HealthDashboardExpirySnapshotsTable.userId eq userId) and
                    (HealthDashboardExpirySnapshotsTable.snapshotDate eq latestDate)
            }.map { it.toHeatmapItem() }
                .groupBy { it.date to it.category }
                .map { (key, cells) ->
                    val daysAhead = java.time.temporal.ChronoUnit.DAYS.between(latestDate, LocalDate.parse(key.first.toString()))
                    val severity = when {
                        daysAhead <= 2 -> ExpirySeverity.HIGH
                        daysAhead <= 5 -> ExpirySeverity.MEDIUM
                        else -> ExpirySeverity.LOW
                    }
                    ExpiryHeatmapCell(key.first, key.second, cells.size, severity, cells.flatMap { it.items })
                }.sortedBy { it.date }
        }
        HealthDashboardSnapshotRange(trendSnapshots, diversityHistory, heatmap, rows.map { it.date }.toSet())
    }

    private fun ResultRow.toSnapshot(): HealthDashboardSnapshot {
        val total = this[HealthDashboardSnapshotsTable.totalTrackedItems]
        fun percent(count: Int) = if (total == 0) 0.0 else (count.toDouble() / total * 100.0 * 10.0).roundToInt() / 10.0
        return HealthDashboardSnapshot(
            date = KotlinLocalDate.parse(this[HealthDashboardSnapshotsTable.snapshotDate].toString()),
            distribution = mapOf(
                NutritionCategory.PRODUCE to percent(this[HealthDashboardSnapshotsTable.produceCount]),
                NutritionCategory.PROTEIN to percent(this[HealthDashboardSnapshotsTable.proteinCount]),
                NutritionCategory.REFINED_GRAIN to percent(this[HealthDashboardSnapshotsTable.grainCount]),
                NutritionCategory.OTHER to percent(this[HealthDashboardSnapshotsTable.otherCount]),
            ),
            totalItems = total,
            unclassifiedPercent = if (total == 0) 0.0 else (this[HealthDashboardSnapshotsTable.unclassifiedCount].toDouble() / total * 100.0 * 10.0).roundToInt() / 10.0,
            diversityScore = this[HealthDashboardSnapshotsTable.diversityScore],
            diversityRating = runCatching { DiversityRating.valueOf(this[HealthDashboardSnapshotsTable.diversityRating]) }.getOrDefault(DiversityRating.LOW),
        )
    }

    private fun ResultRow.toHeatmapItem(): ExpiryHeatmapCell {
        val category = runCatching { NutritionCategory.valueOf(this[HealthDashboardExpirySnapshotsTable.category]) }.getOrDefault(NutritionCategory.OTHER)
        val date = KotlinLocalDate.parse(this[HealthDashboardExpirySnapshotsTable.expiryDate].toString())
        return ExpiryHeatmapCell(date, category, 1, ExpirySeverity.LOW, listOf(this[HealthDashboardExpirySnapshotsTable.itemName]))
    }
}

class HealthDashboardEventRepository {
    fun record(userId: UUID, eventName: String, payload: Map<String, String>, occurredAt: Instant) = transaction {
        HealthDashboardEventsTable.insert {
            it[HealthDashboardEventsTable.id] = UUID.randomUUID()
            it[HealthDashboardEventsTable.userId] = userId
            it[HealthDashboardEventsTable.eventName] = eventName
            it[HealthDashboardEventsTable.payload] = Json.encodeToString(payload)
            it[HealthDashboardEventsTable.occurredAt] = occurredAt
            it[HealthDashboardEventsTable.createdAt] = Instant.now()
        }
    }
}
