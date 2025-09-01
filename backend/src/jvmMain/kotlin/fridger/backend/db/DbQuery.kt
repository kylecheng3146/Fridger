package fridger.backend.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Helper to run Exposed queries inside a coroutine-friendly transaction on Dispatchers.IO.
 * Always use this from repositories/services to ensure consistent transaction management.
 */
suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
