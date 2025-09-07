package fridger.com.io.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import fridger.com.io.database.FridgerDatabase

internal actual object DriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbFile = java.io.File("fridger.db")
        val needsSchemaCreation = !dbFile.exists()

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        if (needsSchemaCreation) {
            FridgerDatabase.Schema.create(driver)
        }

        return driver
    }
}
