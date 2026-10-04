package fridger.com.io.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import fridger.com.io.data.model.IngredientCategory
import fridger.com.io.data.remote.FridgeInventoryApiService
import fridger.com.io.data.remote.FridgeItemDto
import fridger.com.io.data.settings.StoredAuthSession
import fridger.com.io.database.FridgerDatabase
import fridger.shared.health.InventoryCategory
import fridger.shared.models.ApiResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IngredientRepositorySyncTest {
    @Test
    fun anonymousMergeOfflineRetryAndAccountIsolation() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        FridgerDatabase.Schema.create(driver)
        val db = FridgerDatabase(driver)
        val sessions = MutableStateFlow(StoredAuthSession("", "", ""))
        val remote = mutableMapOf<String, MutableMap<String, FridgeItemDto>>()
        var online = false
        val api = object : FridgeInventoryApiService() {
            override suspend fun upsert(item: FridgeItemDto, accessToken: String): ApiResponse<Unit> {
                check(online) { "offline" }
                remote.getOrPut(accessToken) { mutableMapOf() }[item.id] = item
                return ApiResponse.ok(Unit)
            }
            override suspend fun fetchItems(accessToken: String, timeZoneId: String): ApiResponse<List<FridgeItemDto>> =
                ApiResponse.ok(remote[accessToken]?.values?.toList().orEmpty())
            override suspend fun delete(id: String, accessToken: String): ApiResponse<Unit> {
                check(online)
                remote[accessToken]?.remove(id)
                return ApiResponse.ok(Unit)
            }
        }
        val repository = IngredientRepositoryImpl(db, api, sessions)
        try {
            repository.add("Apple", "20/10/2026")
            sessions.value = StoredAuthSession("alice", "", "alice")
            assertFailsWith<IllegalStateException> { repository.sync() }
            assertEquals(1, db.fridgerDatabaseQueries.selectPendingInventoryItems("alice").executeAsList().size)
            online = true
            repository.sync()
            val apple = db.fridgerDatabaseQueries.selectIngredientsByOwner("alice").executeAsList().single()
            assertEquals(LocalDate(2026, 10, 20), remote["alice"]!!.values.single().expirationDate)
            repository.updateCategory(apple.id, IngredientCategory.FRUITS)
            repository.sync()
            assertEquals(InventoryCategory.FRUITS, remote["alice"]!!.values.single().category)
            sessions.value = StoredAuthSession("bob", "", "bob")
            repository.sync()
            assertEquals(0, db.fridgerDatabaseQueries.selectIngredientsByOwner("bob").executeAsList().size)
            assertEquals(1, db.fridgerDatabaseQueries.selectIngredientsByOwner("alice").executeAsList().size)
            sessions.value = StoredAuthSession("alice", "", "alice")
            repository.delete(apple.id)
            online = false
            assertFailsWith<IllegalStateException> { repository.sync() }
            assertEquals(0, db.fridgerDatabaseQueries.selectIngredientsByOwner("alice").executeAsList().size)
            online = true
            repository.sync()
            assertEquals(0, remote["alice"]!!.size)
        } finally {
            driver.close()
        }
    }

    @Test
    fun editOrDeleteDuringUploadRemainsPendingAndCannotBeResurrected() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        FridgerDatabase.Schema.create(driver)
        val db = FridgerDatabase(driver)
        val sessions = MutableStateFlow(StoredAuthSession("alice", "", "alice"))
        var uploaded: FridgeItemDto? = null
        var duringUpload: suspend () -> Unit = {}
        val api = object : FridgeInventoryApiService() {
            override suspend fun upsert(item: FridgeItemDto, accessToken: String): ApiResponse<Unit> {
                uploaded = item
                duringUpload()
                return ApiResponse.ok(Unit)
            }
            override suspend fun fetchItems(accessToken: String, timeZoneId: String): ApiResponse<List<FridgeItemDto>> =
                ApiResponse.ok(listOfNotNull(uploaded))
        }
        val repository = IngredientRepositoryImpl(db, api, sessions)
        try {
            repository.add("Mystery", "20/10/2026")
            val id = db.fridgerDatabaseQueries.selectIngredientsByOwner("alice").executeAsList().single().id
            duringUpload = { repository.updateCategory(id, IngredientCategory.VEGETABLES) }
            repository.sync()
            val edited = db.fridgerDatabaseQueries.selectPendingInventoryItems("alice").executeAsList().single()
            assertEquals("VEGETABLES", edited.categoryOverride)
            duringUpload = { repository.delete(id) }
            repository.sync()
            assertEquals(1L, db.fridgerDatabaseQueries.selectPendingInventoryItems("alice").executeAsList().single().isDeleted)
            assertEquals(0, db.fridgerDatabaseQueries.selectIngredientsByOwner("alice").executeAsList().size)
        } finally {
            driver.close()
        }
    }
    @Test
    fun versionOneMigrationPreservesExistingInventoryAsPendingAnonymousItems() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            driver.execute(null, "CREATE TABLE Ingredient (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, addDate TEXT NOT NULL, expirationDate TEXT NOT NULL)", 0)
            driver.execute(null, "INSERT INTO Ingredient(name, addDate, expirationDate) VALUES ('Apple', '2026-10-04', '2026-10-20')", 0)
            FridgerDatabase(driver).transaction {
                FridgerDatabase.Schema.migrate(driver, 1, FridgerDatabase.Schema.version)
            }
            val row = FridgerDatabase(driver).fridgerDatabaseQueries.selectIngredientsByOwner(null).executeAsList().single()
            assertEquals("Apple", row.name)
            assertEquals("2026-10-20", row.expirationDate)
            assertEquals("PENDING", row.syncState)
        } finally {
            driver.close()
        }
    }

}
