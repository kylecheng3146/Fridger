package fridger.com.io.data.database

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import fridger.com.io.database.FridgerDatabase

internal actual object DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(FridgerDatabase.Schema, "fridger.db")
    }
}
