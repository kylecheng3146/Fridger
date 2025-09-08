package fridger.com.io.presentation.home

import fridger.com.io.data.model.Freshness
import fridger.com.io.data.model.Ingredient
import kotlinx.datetime.LocalDate

object HomeDataMapper {
    fun mapToRefrigeratedItems(
        ingredients: List<Ingredient>,
        today: LocalDate
    ): List<RefrigeratedItem> =
        ingredients.map { ing ->
            val daysUntil = ing.expirationDate.toEpochDays() - today.toEpochDays()
            val age = today.toEpochDays() - ing.addDate.toEpochDays()
            RefrigeratedItem(
                id = ing.id.toString(),
                name = ing.name,
                icon = IngredientIconMapper.getIcon(ing.name),
                quantity = "x1", // Quantity not yet stored; default
                daysUntilExpiry = daysUntil,
                ageDays = age,
                freshness = ing.freshness,
                hasWarning = ing.freshness != Freshness.Fresh
            )
        }

    fun groupExpiringItems(
        items: List<RefrigeratedItem>
    ): Triple<List<ExpiringItem>, List<ExpiringItem>, List<ExpiringItem>> {
        val todayExp =
            items
                .filter { it.daysUntilExpiry in IngredientConstants.TodayOrTomorrowRange }
                .map { item ->
                    ExpiringItem(
                        id = item.id,
                        name = item.name,
                        icon = item.icon,
                        count = 1,
                        daysUntil = item.daysUntilExpiry
                    )
                }

        val weekExp =
            items
                .filter { it.daysUntilExpiry in IngredientConstants.WeekRange }
                .map { item ->
                    ExpiringItem(
                        id = item.id + "_w",
                        name = item.name,
                        icon = item.icon,
                        count = 1,
                        daysUntil = item.daysUntilExpiry
                    )
                }

        val expiredExp =
            items
                .filter { it.daysUntilExpiry < 0 }
                .map { item ->
                    ExpiringItem(
                        id = item.id + "_e",
                        name = item.name,
                        icon = item.icon,
                        count = 1,
                        daysUntil = item.daysUntilExpiry
                    )
                }

        return Triple(todayExp, weekExp, expiredExp)
    }
}
