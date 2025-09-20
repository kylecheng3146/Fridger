package fridger.com.io.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fridger.com.io.data.QuickAddCatalog
import fridger.com.io.data.model.Freshness
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

    fun onItemClick(itemId: String) {
        // TODO: Navigate to item detail screen
    }

    fun onRemoveItemInitiated(itemId: String) {
        _uiState.value.pendingDeletion
            ?.job
            ?.cancel()
        confirmRemoveItem(_uiState.value.pendingDeletion?.item)

        val itemToRemove = originalRefrigeratedItems.find { it.id == itemId } ?: return

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

    // Selection mode functions
    fun onToggleItemSelection(itemId: String) {
        _uiState.update { current ->
            val newSelectedIds = if (current.selectedItemIds.contains(itemId)) {
                current.selectedItemIds - itemId
            } else {
                current.selectedItemIds + itemId
            }
            current.copy(selectedItemIds = newSelectedIds)
        }
    }

    // Recipe generation functions
    fun onGenerateRecipeClick() {
        if (_uiState.value.selectedItemIds.size < 2) return

        viewModelScope.launch {
            try {
                // Show loading state and bottom sheet
                _uiState.update { current ->
                    current.copy(
                        isGeneratingRecipe = true,
                        isRecipeSheetVisible = true
                    )
                }

                // Simulate API call delay (replace with actual recipe generation)
                delay(2000)

                // Mock recipe generation based on selected items
                val selectedItems = originalRefrigeratedItems.filter { it.id in _uiState.value.selectedItemIds }
                val mockRecipe = generateMockRecipe(selectedItems)

                _uiState.update { current ->
                    current.copy(
                        isGeneratingRecipe = false,
                        generatedRecipe = mockRecipe
                    )
                }
            } catch (e: Exception) {
                _uiState.update { current ->
                    current.copy(
                        isGeneratingRecipe = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun onRecipeSheetDismiss() {
        _uiState.update { current ->
            current.copy(
                isRecipeSheetVisible = false,
                isGeneratingRecipe = false,
                generatedRecipe = null
            )
        }
    }

    fun onTryAgainRecipe() {
        _uiState.update { current ->
            current.copy(
                isGeneratingRecipe = false,
                generatedRecipe = null
            )
        }
        onGenerateRecipeClick()
    }

    private fun generateMockRecipe(selectedItems: List<RefrigeratedItem>): RecipeSuggestion {
        val itemNames = selectedItems.map { it.name }
        val title = when {
            itemNames.any { it.contains("肉") || it.contains("雞") } -> "清炒時蔬配肉"
            itemNames.any { it.contains("魚") } -> "清蒸魚配蔬菜"
            itemNames.any { it.contains("奶") } -> "蔬菜沙拉配優格醬"
            else -> "時蔬沙拉"
        }

        return RecipeSuggestion(
            title = title,
            description = "根據您選擇的食材 ${itemNames.joinToString("、")} 推薦的簡單食譜",
            ingredients = itemNames.map { "新鮮的 $it" },
            instructions = listOf(
                "將所有食材洗淨備用",
                "熱鍋加入適量油",
                "放入主要食材翻炒",
                "加入調味料調味",
                "最後加入蔬菜一起翻炒至熟",
                "裝盤享用！"
            ),
            cookingTime = "15 分鐘",
            difficulty = "簡單",
            servings = 2
        )
    }
}
