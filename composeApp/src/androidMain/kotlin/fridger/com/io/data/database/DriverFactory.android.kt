package fridger.com.io.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import fridger.com.io.applicationContext
import fridger.com.io.database.FridgerDatabase

internal actual object DriverFactory {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(FridgerDatabase.Schema, applicationContext, "fridger.db")
}
