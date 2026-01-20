package fridger.com.io.presentation.home.util

import fridger.com.io.data.model.Freshness
import fridger.com.io.data.model.Ingredient
import fridger.com.io.data.model.IngredientCategory
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object HomeMockData {
    private val today: LocalDate =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

    val mockIngredients =
        listOf(
            Ingredient(
                id = 1,
                name = "蘋果",
                addDate = today,
                expirationDate = LocalDate(today.year, today.monthNumber, today.dayOfMonth + 7),
                category = IngredientCategory.FRUITS,
                freshness = Freshness.Fresh
            ),
            Ingredient(
                id = 2,
                name = "牛奶",
                addDate = today,
                expirationDate = LocalDate(today.year, today.monthNumber, today.dayOfMonth + 1),
                category = IngredientCategory.DAIRY,
                freshness = Freshness.NearingExpiration
            ),
            Ingredient(
                id = 3,
                name = "雞肉",
                addDate = LocalDate(today.year, today.monthNumber, today.dayOfMonth - 5),
                expirationDate = LocalDate(today.year, today.monthNumber, today.dayOfMonth - 1),
                category = IngredientCategory.MEAT,
                freshness = Freshness.Expired
            )
        )
}
