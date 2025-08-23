package fridger.com.io.data.database

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import fridger.com.io.applicationContext
import fridger.com.io.database.FridgerDatabase

internal actual object DriverFactory {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(FridgerDatabase.Schema, applicationContext, "fridger.db")
    }
}
