package fridger.com.io.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fridger.com.io.data.model.Freshness
import fridger.com.io.data.model.Ingredient
import fridger.com.io.data.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    private val repository: IngredientRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var originalRefrigeratedItems: List<RefrigeratedItem> = emptyList()

    init {
        observeIngredients()
    }

    private fun observeIngredients() {
        viewModelScope.launch {
            repository
                .getIngredientsStream()
                .onStart {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                }.catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }.collect { ingredients ->
                    val today =
                        Clock.System
                            .now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date

                    val refrigerated = mapToRefrigeratedItems(ingredients, today)
                    val (todayExp, weekExp, expiredExp) = groupExpiringItems(refrigerated)

                    originalRefrigeratedItems = refrigerated

                    val (sorted, grouped) =
                        applySortAndGroup(
                            originalRefrigeratedItems,
                            _uiState.value.sortOption,
                            _uiState.value.groupOption
                        )

                    _uiState.value =
                        _uiState.value.copy(
                            todayExpiringItems = todayExp,
                            weekExpiringItems = weekExp,
                            expiredItems = expiredExp,
                            fridgeCapacityPercentage = 0.6f,
                            refrigeratedItems = sorted,
                            groupedRefrigeratedItems = grouped,
                            isLoading = false,
                            error = null
                        )
                }
        }
    }

    private fun mapToRefrigeratedItems(
        ingredients: List<Ingredient>,
        today: kotlinx.datetime.LocalDate
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

    private fun groupExpiringItems(
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

    fun onAddNewItemClick() {
        _uiState.value = _uiState.value.copy(showAddNewItemDialog = true)
    }

    fun onDismissDialog() {
        _uiState.value = _uiState.value.copy(showAddNewItemDialog = false)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onAddNewItemConfirm(
        name: String,
        quantity: String,
        expiryDate: String
    ) {
        viewModelScope.launch {
            try {
                repository.add(name = name, expirationDateDisplay = expiryDate)
                _uiState.value = _uiState.value.copy(showAddNewItemDialog = false)
                // No manual reload; Flow will emit new data
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(showAddNewItemDialog = false, error = e.message)
            }
        }
    }

    fun onItemClick(itemId: String) {
        // TODO: Navigate to item detail screen
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            try {
                repository.delete(itemId.toLong())
                // No manual reload; Flow will emit new data
            } catch (e: Exception) {
                // keep state but record error
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateSortingAndGrouping(
        sort: SortOption? = null,
        group: GroupOption? = null
    ) {
        val newSort = sort ?: _uiState.value.sortOption
        val newGroup = group ?: _uiState.value.groupOption
        val (sorted, grouped) = applySortAndGroup(originalRefrigeratedItems, newSort, newGroup)
        _uiState.value =
            _uiState.value.copy(
                sortOption = newSort,
                groupOption = newGroup,
                refrigeratedItems = sorted,
                groupedRefrigeratedItems = grouped
            )
    }

    private fun applySortAndGroup(
        items: List<RefrigeratedItem>,
        sort: SortOption,
        group: GroupOption
    ): Pair<List<RefrigeratedItem>, Map<Freshness, List<RefrigeratedItem>>> {
        // Sort
        val sorted =
            when (sort) {
                SortOption.EXPIRY ->
                    items.sortedWith(
                        compareBy(
                            { it.daysUntilExpiry },
                            { it.name.lowercase() }
                        )
                    )

                SortOption.NAME -> items.sortedBy { it.name.lowercase() }
                SortOption.ADDED_DATE -> items.sortedByDescending { it.ageDays } // oldest first
            }
        // Group
        val grouped =
            if (group == GroupOption.FRESHNESS) {
                mapOf(
                    Freshness.Expired to sorted.filter { it.freshness is Freshness.Expired },
                    Freshness.NearingExpiration to sorted.filter { it.freshness is Freshness.NearingExpiration },
                    Freshness.Fresh to sorted.filter { it.freshness is Freshness.Fresh }
                )
            } else {
                emptyMap()
            }

        return Pair(sorted, grouped)
    }
}
