package fridger.com.io.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import fridger.com.io.database.FridgerDatabase

internal actual object DriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbFile = java.io.File("fridger.db")
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        val storedVersion = driver.executeQuery(
            null,
            "PRAGMA user_version",
            { cursor: SqlCursor -> QueryResult.Value(if (cursor.next().value) cursor.getLong(0) else null) },
            0,
            null,
        ).value ?: 0L
        check(storedVersion <= FridgerDatabase.Schema.version) {
            "Database schema $storedVersion is newer than this app's ${FridgerDatabase.Schema.version}"
        }
        val hasIngredientTable = driver.executeQuery(
            null,
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'Ingredient'",
            { cursor: SqlCursor -> QueryResult.Value(cursor.next().value) },
            0,
            null,
        ).value
        FridgerDatabase(driver).transaction {
            if (storedVersion == 0L && !hasIngredientTable) {
                FridgerDatabase.Schema.create(driver)
            } else {
                // Previous desktop databases predate user_version but have the version 1 schema.
                val migrationStart = if (storedVersion == 0L) 1L else storedVersion
                if (migrationStart < FridgerDatabase.Schema.version) {
                    FridgerDatabase.Schema.migrate(driver, migrationStart, FridgerDatabase.Schema.version)
                }
            }
            driver.execute(null, "PRAGMA user_version = ${FridgerDatabase.Schema.version}", 0, null)
        }

        return driver
    }
}
