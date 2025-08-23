package fridger.com.io.data.database

import app.cash.sqldelight.db.SqlDriver

internal expect object DriverFactory {
    fun createDriver(): SqlDriver
}
