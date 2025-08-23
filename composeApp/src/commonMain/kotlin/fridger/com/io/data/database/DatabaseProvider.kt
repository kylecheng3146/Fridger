package fridger.com.io.data.database

import fridger.com.io.database.FridgerDatabase

object DatabaseProvider {
    // Lazy singleton database instance
    val database: FridgerDatabase by lazy {
        val driver = DriverFactory.createDriver()
        FridgerDatabase(driver)
    }
}
