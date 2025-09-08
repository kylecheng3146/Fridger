package fridger.backend

import fridger.backend.db.RefreshTokensTable
import fridger.backend.db.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.atomic.AtomicBoolean

/** Helper for setting up and tearing down an isolated in-memory database for tests. */
object DatabaseTestHelper {
    private val initialized = AtomicBoolean(false)

    /** Connect (once) to an in-memory H2 database in PostgreSQL compatibility mode. */
    private fun connectIfNeeded() {
        if (initialized.compareAndSet(false, true)) {
            Database.connect(
                url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_UPPER=false",
                driver = "org.h2.Driver"
            )
        }
    }

    /** Create tables fresh before each test. */
    fun setup() {
        connectIfNeeded()
        transaction { SchemaUtils.create(UsersTable, RefreshTokensTable) }
    }

    /** Drop tables after each test to guarantee clean slate. */
    fun teardown() {
        // Safe to attempt drop even if not present
        transaction { SchemaUtils.drop(RefreshTokensTable, UsersTable) }
    }
}

/** Base class that automatically sets up / tears down the database per test. */
open class BaseDbTest {
    @kotlin.test.BeforeTest
    fun before() = DatabaseTestHelper.setup()

    @kotlin.test.AfterTest
    fun after() = DatabaseTestHelper.teardown()
}
