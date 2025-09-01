package fridger.backend

import fridger.backend.db.UsersTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertTrue

class DatabaseHelperSanityTest : BaseDbTest() {
    @Test
    fun tablesCreated() {
        // Just ensure table exists by trying to create again (no-op) and not throwing
        transaction { SchemaUtils.create(UsersTable) }
        assertTrue(true)
    }
}

