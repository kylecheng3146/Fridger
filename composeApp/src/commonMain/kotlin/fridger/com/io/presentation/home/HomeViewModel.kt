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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    private val repository: IngredientRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var originalRefrigeratedItems: List<RefrigeratedItem> = emptyList()

    init {
        observeIngredients()
    }

    private fun observeIngredients() {
        viewModelScope.launch {
            repository.getIngredientsStream()
                .onStart {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
                .collect { ingredients ->
                    val today = Clock.System
                        .now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date

                    val refrigerated = mapToRefrigeratedItems(ingredients, today)
                    originalRefrigeratedItems = refrigerated

                    // Hide any pending-deletion item in visible lists
                    val pendingId = _uiState.value.pendingDeletion?.item?.id
                    val visibleBase = if (pendingId != null) refrigerated.filterNot { it.id == pendingId } else refrigerated

                    val (todayExp, weekExp, expiredExp) = groupExpiringItems(visibleBase)
                    val (sorted, grouped) = applySortAndGroup(
                        visibleBase,
                        _uiState.value.sortOption,
                        _uiState.value.groupOption
                    )

                    _uiState.value = _uiState.value.copy(
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
        today: LocalDate
    ): List<RefrigeratedItem> {
        return ingredients.map { ing ->
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
    }

    private fun groupExpiringItems(
        items: List<RefrigeratedItem>
    ): Triple<List<ExpiringItem>, List<ExpiringItem>, List<ExpiringItem>> {
        val todayExp = items
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

        val weekExp = items
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

        val expiredExp = items
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
        // Delegate to undoable flow
        onRemoveItemInitiated(itemId)
    }

    // Start undoable deletion countdown, immediately hide item in UI
    fun onRemoveItemInitiated(itemId: String) {
        // Finalize any previous pending deletion
        _uiState.value.pendingDeletion?.job?.cancel()
        confirmRemoveItem(_uiState.value.pendingDeletion?.item)

        val itemToRemove = originalRefrigeratedItems.find { it.id == itemId } ?: return

        // Schedule actual deletion after delay
        val deletionJob = viewModelScope.launch {
            delay(4000L)
            confirmRemoveItem(itemToRemove)
            _uiState.update { it.copy(pendingDeletion = null) }
        }

        // Optimistically update UI to hide the item and set pending state
        _uiState.update { current ->
            val newItems = current.refrigeratedItems.filterNot { it.id == itemId }
            val newGrouped = if (current.groupedRefrigeratedItems.isEmpty()) emptyMap() else
                current.groupedRefrigeratedItems.mapValues { (_, list) -> list.filterNot { it.id == itemId } }
            current.copy(
                refrigeratedItems = newItems,
                groupedRefrigeratedItems = newGrouped,
                pendingDeletion = PendingDeletion(itemToRemove, deletionJob)
            )
        }
    }

    private fun confirmRemoveItem(item: RefrigeratedItem?) {
        item ?: return
        viewModelScope.launch {
            try {
                repository.delete(item.id.toLong())
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun undoRemoveItem() {
        val pending = _uiState.value.pendingDeletion ?: return
        pending.job.cancel()
        _uiState.update { current ->
            val (sorted, grouped) = applySortAndGroup(originalRefrigeratedItems, current.sortOption, current.groupOption)
            current.copy(
                refrigeratedItems = sorted,
                groupedRefrigeratedItems = grouped,
                pendingDeletion = null
            )
        }
    }

    fun updateSortingAndGrouping(sort: SortOption? = null, group: GroupOption? = null) {
        val newSort = sort ?: _uiState.value.sortOption
        val newGroup = group ?: _uiState.value.groupOption
        val (sortedBase, groupedBase) = applySortAndGroup(originalRefrigeratedItems, newSort, newGroup)
        val pendingId = _uiState.value.pendingDeletion?.item?.id
        val sorted = if (pendingId != null) sortedBase.filterNot { it.id == pendingId } else sortedBase
        val grouped = if (pendingId != null && groupedBase.isNotEmpty())
            groupedBase.mapValues { (_, list) -> list.filterNot { it.id == pendingId } }
        else groupedBase
        _uiState.value = _uiState.value.copy(
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
        val sorted = when (sort) {
            SortOption.EXPIRY -> items.sortedWith(
                compareBy(
                    { it.daysUntilExpiry },
                    { it.name.lowercase() }
                )
            )

            SortOption.NAME -> items.sortedBy { it.name.lowercase() }
            SortOption.ADDED_DATE -> items.sortedByDescending { it.ageDays } // oldest first
        }
        // Group
        val grouped = if (group == GroupOption.FRESHNESS) {
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
