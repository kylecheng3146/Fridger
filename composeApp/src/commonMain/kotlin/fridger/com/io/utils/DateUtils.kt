package fridger.com.io.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

fun epochMillisToDateString(epochMillis: Long): String {
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val localDateTime =
        instant
            .toLocalDateTime(TimeZone.currentSystemDefault())

    // Format: dd/MM/yyyy
    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    val month = localDateTime.monthNumber.toString().padStart(2, '0')
    val year = localDateTime.year

    return "$day/$month/$year"
}

// New helpers for display formatting and defaults
fun todayPlusDaysDisplay(days: Int = 7): String {
    val today =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    val target = today.plus(DatePeriod(days = days))
    val day = target.dayOfMonth.toString().padStart(2, '0')
    val month = target.monthNumber.toString().padStart(2, '0')
    val year = target.year
    return "$day/$month/$year"
}
