package fridger.com.io.data.database

import android.database.sqlite.SQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import fridger.com.io.applicationContext
import fridger.com.io.database.FridgerDatabase

internal actual object DriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbName = "fridger.db"
        try {
            val dbFile = applicationContext.getDatabasePath(dbName)
            if (dbFile.exists()) {
                // Read current user_version to detect potential downgrade.
                val sqlite = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
                val cursor = sqlite.rawQuery("PRAGMA user_version;", null)
                var userVersion = 0
                if (cursor.moveToFirst()) {
                    userVersion = cursor.getInt(0)
                }
                cursor.close()
                sqlite.close()

                // SQLDelight exposes the expected schema version on the generated Schema.
                val expectedVersion = FridgerDatabase.Schema.version.toInt()
                if (userVersion > expectedVersion) {
                    // Detected a downgrade scenario. To avoid "can't downgrade" crash, wipe the old DB.
                    // Data will be recreated on next open according to the current schema.
                    applicationContext.deleteDatabase(dbName)
                }
            }
        } catch (_: Throwable) {
            // If anything goes wrong during pre-check, proceed to open with the normal driver.
        }

        return AndroidSqliteDriver(FridgerDatabase.Schema, applicationContext, dbName)
    }
}
