package fridger.com.io.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fridger.com.io.data.model.Freshness
import fridger.com.io.data.model.Ingredient
import fridger.com.io.data.repository.IngredientRepository
import fridger.com.io.presentation.settings.SettingsManager
import fridger.com.io.utils.todayPlusDaysDisplay
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import fridger.com.io.data.QuickAddCatalog

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
            repository
                .getIngredientsStream()
                .onStart {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }.catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }.collect { ingredients ->
                    val today =
                        Clock.System
                            .now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date

                    val refrigerated = HomeDataMapper.mapToRefrigeratedItems(ingredients, today)
                    originalRefrigeratedItems = refrigerated

                    // Hide any pending-deletion item in visible lists
                    val pendingId =
                        _uiState.value.pendingDeletion
                            ?.item
                            ?.id
                    val visibleBase = if (pendingId != null) refrigerated.filterNot { it.id == pendingId } else refrigerated

                    val (todayExp, weekExp, expiredExp) = HomeDataMapper.groupExpiringItems(visibleBase)
                    val (sorted, grouped) =
                        applySortAndGroup(
                            visibleBase,
                            _uiState.value.sortOption,
                            _uiState.value.groupOption
                        )

                    _uiState.update { currentState ->
                        currentState.copy(
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
    }

    // Dialog visibility
    fun onShowAddItemDialog() {
        _uiState.update { it.copy(showAddNewItemDialog = true) }
    }

    fun onDismissDialog() {
        _uiState.update { it.copy(showAddNewItemDialog = false) }
    }

    fun onQuickAddSearchTextChange(text: String) {
        val suggestions = if (text.isBlank()) emptyList() else QuickAddCatalog.allNames.filter { it.contains(text, ignoreCase = true) }
        _uiState.update { it.copy(quickAddSearchText = text, quickAddSuggestions = suggestions) }
    }

    suspend fun toggleQuickFavorite(name: String) {
        SettingsManager.toggleQuickFavorite(name)
    }

    // Batch add from dialog
    fun onItemsAdded(items: List<NewItem>) {
        viewModelScope.launch {
            try {
                val defaultExpiry = todayPlusDaysDisplay(IngredientConstants.DEFAULT_QUICK_ADD_EXPIRY_DAYS)
                for (item in items) {
                    val date = item.expiryDateDisplay ?: defaultExpiry
                    repository.add(name = item.name, expirationDateDisplay = date)
                }
                _uiState.update { it.copy(showAddNewItemDialog = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, showAddNewItemDialog = false) }
            }
        }
    }

    // Single quick add support (used by current dialog integration)
    fun onQuickAddItem(
        name: String,
        quantity: String?
    ) {
        viewModelScope.launch {
            try {
                val defaultExpiry = todayPlusDaysDisplay(IngredientConstants.DEFAULT_QUICK_ADD_EXPIRY_DAYS)
                repository.add(name = name, expirationDateDisplay = defaultExpiry)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
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
        _uiState.value.pendingDeletion
            ?.job
            ?.cancel()
        confirmRemoveItem(_uiState.value.pendingDeletion?.item)

        val itemToRemove = originalRefrigeratedItems.find { it.id == itemId } ?: return

        // Schedule actual deletion after delay
        val deletionJob =
            viewModelScope.launch {
                delay(4000L)
                confirmRemoveItem(itemToRemove)
                _uiState.update { it.copy(pendingDeletion = null) }
            }

        // Optimistically update UI to hide the item and set pending state
        _uiState.update { current ->
            val newItems = current.refrigeratedItems.filterNot { it.id == itemId }
            val newGrouped =
                if (current.groupedRefrigeratedItems.isEmpty()) {
                    emptyMap()
                } else {
                    current.groupedRefrigeratedItems.mapValues { (_, list) -> list.filterNot { it.id == itemId } }
                }
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

    fun updateSortingAndGrouping(
        sort: SortOption? = null,
        group: GroupOption? = null
    ) {
        val newSort = sort ?: _uiState.value.sortOption
        val newGroup = group ?: _uiState.value.groupOption
        val (sortedBase, groupedBase) = applySortAndGroup(originalRefrigeratedItems, newSort, newGroup)
        val pendingId =
            _uiState.value.pendingDeletion
                ?.item
                ?.id
        val sorted = if (pendingId != null) sortedBase.filterNot { it.id == pendingId } else sortedBase
        val grouped =
            if (pendingId != null && groupedBase.isNotEmpty()) {
                groupedBase.mapValues { (_, list) -> list.filterNot { it.id == pendingId } }
            } else {
                groupedBase
            }
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
        val sorted =
            when (sort) {
                SortOption.EXPIRY -> items.sortedBy { it.daysUntilExpiry }
                SortOption.NAME -> items.sortedBy { it.name }
                SortOption.ADDED_DATE -> items.sortedBy { it.ageDays }
            }
        val grouped =
            if (group == GroupOption.FRESHNESS) sorted.groupBy { it.freshness } else emptyMap()
        return sorted to grouped
    }
}
