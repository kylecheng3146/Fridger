package fridger.com.io.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fridger.com.io.data.IngredientCategoryClassifier
import fridger.com.io.data.database.DatabaseProvider
import fridger.com.io.data.model.Freshness
import fridger.com.io.data.model.Ingredient
import fridger.com.io.data.model.IngredientCategory
import fridger.com.io.data.remote.FridgeInventoryApiService
import fridger.com.io.data.remote.FridgeItemDto
import fridger.com.io.data.user.UserSessionManager
import fridger.com.io.data.settings.StoredAuthSession
import fridger.com.io.database.FridgerDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface IngredientRepository {
    fun getIngredientsStream(): Flow<List<Ingredient>>

    suspend fun add(
        name: String,
        expirationDateDisplay: String,
    )

    suspend fun delete(id: Long)

    suspend fun updateCategory(id: Long, category: IngredientCategory?) {}

    suspend fun sync() {}
}

class IngredientRepositoryImpl(
    private val db: FridgerDatabase = DatabaseProvider.database,
    private val api: FridgeInventoryApiService = FridgeInventoryApiService(),
    private val sessions: Flow<StoredAuthSession> = UserSessionManager.session,
) : IngredientRepository {
    private val syncMutex = Mutex()
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getIngredientsStream(): Flow<List<Ingredient>> =
        sessions
            .distinctUntilChanged()
            .flatMapLatest { session ->
                val ownerId = session.userId.takeIf { it.isNotBlank() }
                flow {
                    ensureSyncIds()
                    if (ownerId != null && sessions.first() == session) {
                        if (session.accessToken.isNotBlank()) {
                            backgroundScope.launch { runCatching { syncUser(ownerId, session.accessToken) } }
                        }
                    }
                    emitAll(
                        db.fridgerDatabaseQueries
                            .selectIngredientsByOwner(ownerId)
                            .asFlow()
                            .mapToList(Dispatchers.Default)
                            .map { rows -> rows.map { it.toIngredient() } },
                    )
                }
            }

    override suspend fun add(
        name: String,
        expirationDateDisplay: String,
    ) = withContext(Dispatchers.Default) {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val ownerId = sessions.first().userId.takeIf { it.isNotBlank() }
        val expiration = parseDisplayDate(expirationDateDisplay)
        db.fridgerDatabaseQueries.insertIngredient(
            name = name,
            addDate = today.toString(),
            expirationDate = expiration.toString(),
            ownerId = ownerId,
            syncId = newSyncId(),
        )
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.Default) {
        val ownerId = sessions.first().userId.takeIf { it.isNotBlank() }
        if (ownerId == null) {
            db.fridgerDatabaseQueries.deleteIngredientById(id, null)
        } else {
            db.fridgerDatabaseQueries.markIngredientDeleted(id, ownerId)
        }
    }

    override suspend fun updateCategory(id: Long, category: IngredientCategory?) = withContext(Dispatchers.Default) {
        val ownerId = sessions.first().userId.takeIf { it.isNotBlank() }
        db.fridgerDatabaseQueries.updateIngredientCategory(category?.name, id, ownerId)
    }

    override suspend fun sync() {
        val session = sessions.first()
        val ownerId = session.userId.takeIf { it.isNotBlank() } ?: return
        if (session.accessToken.isNotBlank()) syncUser(ownerId, session.accessToken)
    }

    private suspend fun syncUser(ownerId: String, accessToken: String) = syncMutex.withLock {
        ensureSyncIds()
        if (sessions.first().let { it.userId != ownerId || it.accessToken != accessToken }) return@withLock
        db.fridgerDatabaseQueries.claimAnonymousIngredients(ownerId)

        val queries = db.fridgerDatabaseQueries
        queries.selectPendingInventoryItems(ownerId).executeAsList().forEach { row ->
            if (row.isDeleted == 1L) {
                val response = api.delete(row.syncId, accessToken)
                check(response.success) { response.error ?: "Inventory delete sync failed" }
                queries.deleteIngredientBySyncId(row.syncId, ownerId)
            } else {
                val category = row.categoryOverride?.let(::parseCategory) ?: IngredientCategoryClassifier.classify(row.name)
                val response =
                    api.upsert(
                        FridgeItemDto(
                            id = row.syncId,
                            name = row.name,
                            addDate = LocalDate.parse(row.addDate),
                            expirationDate = LocalDate.parse(row.expirationDate),
                            category = category,
                            timeZoneId = TimeZone.currentSystemDefault().id,
                        ),
                        accessToken,
                    )
                check(response.success) { response.error ?: "Inventory sync failed" }
                queries.markIngredientSynced(row.syncId, ownerId, row.name, row.addDate, row.expirationDate, row.categoryOverride)
            }
        }

        val response = api.fetchItems(accessToken, TimeZone.currentSystemDefault().id)
        check(response.success) { response.error ?: "Inventory refresh failed" }
        val remoteItems = requireNotNull(response.data) { "Inventory response was empty" }
        db.transaction {
            val remoteIds = remoteItems.mapTo(mutableSetOf()) { it.id }
            queries.selectSyncedInventoryItems(ownerId).executeAsList()
                .filterNot { it.syncId in remoteIds }
                .forEach { queries.deleteIngredientBySyncId(it.syncId, ownerId) }
            remoteItems.forEach { item ->
                val expirationDate = item.expirationDate ?: return@forEach
                val localItem = queries.selectIngredientBySyncId(item.id, ownerId).executeAsOneOrNull()
                if (localItem == null) {
                    queries.insertRemoteIngredient(
                        name = item.name,
                        addDate = item.addDate.toString(),
                        expirationDate = expirationDate.toString(),
                        syncId = item.id,
                        ownerId = ownerId,
                        categoryOverride = item.category.name,
                    )
                } else {
                    queries.updateRemoteIngredient(
                        name = item.name,
                        addDate = item.addDate.toString(),
                        expirationDate = expirationDate.toString(),
                        categoryOverride = item.category.name,
                        syncId = item.id,
                        ownerId = ownerId,
                    )
                }
            }
        }
    }

    private suspend fun ensureSyncIds() = withContext(Dispatchers.Default) {
        db.fridgerDatabaseQueries.selectIngredientsWithoutSyncId().executeAsList().forEach { id ->
            db.fridgerDatabaseQueries.updateIngredientSyncId(newSyncId(), id)
        }
    }

    private fun parseCategory(raw: String): IngredientCategory? =
        runCatching { IngredientCategory.valueOf(raw) }.getOrNull()

    private fun fridger.com.io.database.Ingredient.toIngredient(): Ingredient {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val expirationDate = LocalDate.parse(expirationDate)
        val categoryOverride = categoryOverride?.let(::parseCategory)
        return Ingredient(
            id = id,
            syncId = syncId,
            ownerId = ownerId,
            name = name,
            addDate = LocalDate.parse(addDate),
            expirationDate = expirationDate,
            category = categoryOverride ?: IngredientCategoryClassifier.classify(name),
            categoryOverride = categoryOverride,
            freshness = computeFreshness(today, expirationDate),
        )
    }

    private fun computeFreshness(today: LocalDate, expiration: LocalDate): Freshness {
        val daysUntil = (expiration.toEpochDay() - today.toEpochDay()).toInt()
        return when {
            daysUntil < 0 -> Freshness.Expired
            daysUntil <= 3 -> Freshness.NearingExpiration
            else -> Freshness.Fresh
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun newSyncId(): String = Uuid.random().toString()
}

private fun parseDisplayDate(display: String): LocalDate {
    val parts = display.split("/")
    require(parts.size == 3) { "Invalid date format: $display" }
    return LocalDate(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
}

private fun LocalDate.toEpochDay(): Long = toEpochDays().toLong()
