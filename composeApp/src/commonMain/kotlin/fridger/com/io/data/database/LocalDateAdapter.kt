package fridger.com.io.data.database

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalDate

/**
 * SQLDelight ColumnAdapter for mapping between LocalDate and ISO-8601 date string (yyyy-MM-dd).
 */
object LocalDateAdapter : ColumnAdapter<LocalDate, String> {
    override fun decode(databaseValue: String): LocalDate = LocalDate.parse(databaseValue)

    override fun encode(value: LocalDate): String = value.toString() // LocalDate.toString() is ISO yyyy-MM-dd
}
