package fridger.com.io.data.model

import kotlinx.datetime.LocalDate

// Represents freshness status of an ingredient
sealed class Freshness {
    data object Fresh : Freshness()
    data object NearingExpiration : Freshness()
    data object Expired : Freshness()
}

// Domain model for Ingredient with lifecycle dates
// Note: freshness is computed in the Repository layer based on today's date and expirationDate
// and is not persisted directly in the database.
data class Ingredient(
    val id: Long,
    val name: String,
    val addDate: LocalDate,
    val expirationDate: LocalDate,
    val freshness: Freshness
)
