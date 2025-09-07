package fridger.com.io.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import fridger.com.io.data.database.DatabaseProvider
import fridger.com.io.data.model.Freshness
import fridger.com.io.data.model.Ingredient
import fridger.com.io.database.FridgerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface IngredientRepository {
    fun getIngredientsStream(): Flow<List<Ingredient>>

    suspend fun add(
        name: String,
        expirationDateDisplay: String
    )

    suspend fun delete(id: Long)
}

class IngredientRepositoryImpl(
    private val db: FridgerDatabase = DatabaseProvider.database
) : IngredientRepository {
    override fun getIngredientsStream(): Flow<List<Ingredient>> =
        db.fridgerDatabaseQueries
            .selectAllIngredients()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows ->
                val today =
                    Clock.System
                        .now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                rows.map { row ->
                    val addDate = LocalDate.parse(row.addDate)
                    val expirationDate = LocalDate.parse(row.expirationDate)
                    Ingredient(
                        id = row.id,
                        name = row.name,
                        addDate = addDate,
                        expirationDate = expirationDate,
                        freshness = computeFreshness(today, expirationDate)
                    )
                }
            }

    override suspend fun add(
        name: String,
        expirationDateDisplay: String
    ) = withContext(Dispatchers.Default) {
        val today =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        val expiration = parseDisplayDate(expirationDateDisplay)
        db.fridgerDatabaseQueries.insertIngredient(
            name = name,
            addDate = today.toString(),
            expirationDate = expiration.toString()
        )
    }

    override suspend fun delete(id: Long) =
        withContext(Dispatchers.Default) {
            db.fridgerDatabaseQueries.deleteIngredientById(id)
        }

    private fun computeFreshness(
        today: LocalDate,
        expiration: LocalDate
    ): Freshness {
        val daysUntil = (expiration.toEpochDay() - today.toEpochDay()).toInt()
        return when {
            daysUntil < 0 -> Freshness.Expired
            daysUntil <= 3 -> Freshness.NearingExpiration
            else -> Freshness.Fresh
        }
    }
}

// Utilities
private fun parseDisplayDate(display: String): LocalDate {
    // Expected format: dd/MM/yyyy
    val parts = display.split("/")
    require(parts.size == 3) { "Invalid date format: $display" }
    val day = parts[0].toInt()
    val month = parts[1].toInt()
    val year = parts[2].toInt()
    return LocalDate(year, month, day)
}

private fun LocalDate.toEpochDay(): Long {
    // Use kotlinx-datetime built-in conversion to epoch days
    return this.toEpochDays().toLong()
}
