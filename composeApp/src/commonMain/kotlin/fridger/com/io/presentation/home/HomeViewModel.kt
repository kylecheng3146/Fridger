package fridger.com.io.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fridger.com.domain.translator.Translator
import fridger.com.io.data.QuickAddCatalog
import fridger.com.io.data.connectivity.ConnectivityMonitorProvider
import fridger.com.io.data.analytics.DashboardSectionAction
import fridger.com.io.data.analytics.DashboardStateSyncSource
import fridger.com.io.data.analytics.HealthDashboardAnalytics
import fridger.com.io.data.model.Ingredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Job
import fridger.com.io.data.model.Freshness
import fridger.com.io.data.model.IngredientCategory
import fridger.com.io.data.repository.HealthDashboardRepository
import fridger.com.io.data.repository.IngredientRepository
import fridger.com.io.data.repository.RecipeRepository
import fridger.com.io.data.settings.HealthDashboardPreferences
import fridger.com.io.data.user.UserSessionProvider
import fridger.com.io.presentation.home.dashboard.DashboardSection
import fridger.com.io.presentation.home.dashboard.DashboardSectionDefaults
import fridger.com.io.utils.todayPlusDaysDisplay
import fridger.shared.health.HealthDashboardCalculator
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.InventoryItem
import fridger.shared.health.NutritionCategory
import fridger.shared.health.toNutritionCategory
import fridger.shared.recipe.RecipeFeedbackType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    private val repository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val translator: Translator,
    private val healthDashboardRepository: HealthDashboardRepository,
    private val userSessionProvider: UserSessionProvider,
    private val dashboardPreferences: HealthDashboardPreferences,
    private val healthDashboardAnalytics: HealthDashboardAnalytics,
    private val userChanges: Flow<String> = flowOf(userSessionProvider.userId()),
) : ViewModel() {
    // Recipe translation-driven state
    private val _recipeState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val recipeState = _recipeState.asStateFlow()
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var inventoryIngredients: List<Ingredient> = emptyList()
    private var dashboardRequest = 0
    private var currentUserId = userSessionProvider.userId()
    private var dashboardJob: Job? = null
    private var originalRefrigeratedItems: List<RefrigeratedItem> = emptyList()
    private var lastRecipeRequestIngredients: List<String> = emptyList()

    private val dashboardCalculator = HealthDashboardCalculator()
    private val defaultSectionStates = DashboardSectionDefaults.defaultStates()

    init {
        viewModelScope.launch {
            userChanges.distinctUntilChanged().collect { userId ->
                if (userId != currentUserId) {
                    currentUserId = userId
                    dashboardRequest++
                    dashboardJob?.cancel()
                    _uiState.value.pendingDeletion?.job?.cancel()
                    inventoryIngredients = emptyList()
                    originalRefrigeratedItems = emptyList()
                    lastRecipeRequestIngredients = emptyList()
                    _recipeState.value = RecipeUiState.Idle
                    _uiState.update { HomeUiState(healthDashboard = HealthDashboardUiState(sectionStates = it.healthDashboard.sectionStates)) }
                    refreshHealthDashboard()
                }
            }
        }
        observeIngredients()
        observeInventoryConnectivity()
        viewModelScope.launch { runCatching { repository.sync() } }
        refreshHealthDashboard()
        observeDashboardSectionState()
    }

    private fun observeInventoryConnectivity() {
        viewModelScope.launch {
            ConnectivityMonitorProvider.monitor.isOnline.collect { online ->
                if (online) runCatching { repository.sync() }
            }
        }
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

                    inventoryIngredients = ingredients
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
                            _uiState.value.groupOption,
                            _uiState.value.activeCategoryFilter
                        )
                    val categorySummaries = buildCategorySummaries(visibleBase)

                    _uiState.update { currentState ->
                        currentState.copy(
                            todayExpiringItems = todayExp,
                            weekExpiringItems = weekExp,
                            expiredItems = expiredExp,
                            fridgeCapacityPercentage = 0.6f,
                            refrigeratedItems = sorted,
                            groupedRefrigeratedItems = grouped,
                            categorySummaries = categorySummaries,
                            isLoading = false,
                            error = null
                        )
                    }
                    updateDashboardWithLocalSnapshot()
                }
        }
    }

    private fun observeDashboardSectionState() {
        viewModelScope.launch {
            dashboardPreferences.sectionStates.collect { stored ->
                val merged = defaultSectionStates.toMutableMap().apply { putAll(stored) }
                _uiState.update { state ->
                    state.copy(
                        healthDashboard =
                            state.healthDashboard.copy(
                                sectionStates = merged,
                            ),
                    )
                }
            }
        }
    }

    fun refreshHealthDashboard(
        includeTrends: Boolean = false,
        trendRangeDays: Int? = null,
    ) {
        val requestId = ++dashboardRequest
        val requestUserId = currentUserId
        dashboardJob?.cancel()
        dashboardJob = viewModelScope.launch {
            runCatching { repository.sync() }
            _uiState.update { state ->
                state.copy(
                    healthDashboard =
                        state.healthDashboard.copy(
                            isLoading = true,
                            error = null,
                        ),
                )
            }
            val userId = requestUserId
            val trendRange = if (includeTrends) trendRangeDays ?: DEFAULT_TREND_RANGE_DAYS else null
            val result = healthDashboardRepository.getDashboardMetrics(userId, includeTrends, trendRange)
            if (requestId != dashboardRequest || requestUserId != currentUserId) return@launch
            val localMetrics = computeLocalDashboardMetrics()
            _uiState.update { state ->
                val remoteMetrics = result.getOrNull()
                val resolvedMetrics =
                    when {
                        remoteMetrics == null -> localMetrics
                        else -> localMetrics.copy(
                            trendMetadata = remoteMetrics.trendMetadata,
                            trendSnapshots = remoteMetrics.trendSnapshots,
                            diversityHistory = remoteMetrics.diversityHistory,
                            expiryHeatmap = remoteMetrics.expiryHeatmap,
                        )
                    }
                val errorMessage = result.exceptionOrNull()?.message
                val shouldSurfaceError =
                    includeTrends && result.isFailure && remoteMetrics == null
                state.copy(
                    healthDashboard =
                        state.healthDashboard.copy(
                            isLoading = false,
                            metrics = resolvedMetrics,
                            error = if (shouldSurfaceError) errorMessage else null,
                            lastUpdatedEpochMillis =
                                if (result.isSuccess) Clock.System.now().toEpochMilliseconds() else state.healthDashboard.lastUpdatedEpochMillis,
                        ),
                )
            }
        }
    }

    fun onDashboardSectionToggle(
        section: DashboardSection,
        isExpanded: Boolean,
    ) {
        val currentStates = _uiState.value.healthDashboard.sectionStates
        val previous = currentStates[section] ?: section.defaultExpanded
        if (previous == isExpanded) return
        val updated = currentStates.toMutableMap().apply { this[section] = isExpanded }
        _uiState.update { state ->
            state.copy(
                healthDashboard =
                    state.healthDashboard.copy(
                        sectionStates = updated,
                    ),
            )
        }
        val action = if (isExpanded) DashboardSectionAction.EXPANDED else DashboardSectionAction.COLLAPSED
        healthDashboardAnalytics.trackSectionToggle(section, action, previous)
        viewModelScope.launch {
            dashboardPreferences.setSectionStates(updated)
            healthDashboardAnalytics.trackStateSync(updated, DashboardStateSyncSource.APP)
        }
    }

    fun onDashboardSectionCollapsedImpression(
        section: DashboardSection,
        isDefault: Boolean,
        durationMillis: Long,
    ) {
        healthDashboardAnalytics.trackCollapsedImpression(section, durationMillis, isDefault)
    }

    fun onDashboardViewed() {
        healthDashboardAnalytics.trackDashboardView()
    }

    fun onRecommendationAction(recommendation: fridger.shared.health.HealthRecommendation) {
        val action = if (recommendation.reason == fridger.shared.health.RecommendationReason.EXPIRY_RISK) "find_recipe" else "open_shopping_list"
        healthDashboardAnalytics.trackRecommendationAction(recommendation.reason.name.lowercase(), action)
    }

    private fun computeLocalDashboardMetrics(): HealthDashboardMetrics {
        val today =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        val items =
            inventoryIngredients.map { item ->
                val category = item.category.toNutritionCategory()
                InventoryItem(
                    id = item.syncId,
                    name = item.name,
                    category = category ?: NutritionCategory.OTHER,
                    expiryDate = item.expirationDate,
                    ownerId = item.ownerId,
                    isClassified = category != null,
                )
            }
        return dashboardCalculator.compute(items)
    }

    private fun updateDashboardWithLocalSnapshot() {
        val localMetrics = computeLocalDashboardMetrics()
        _uiState.update { state ->
                state.copy(
                    healthDashboard =
                        state.healthDashboard.copy(
                            isLoading = false,
                            // Keep any existing backend error surfaced; this function's job is just to keep metrics in sync.
                            metrics = localMetrics.copy(
                                trendMetadata = state.healthDashboard.metrics?.trendMetadata,
                                trendSnapshots = state.healthDashboard.metrics?.trendSnapshots.orEmpty(),
                                diversityHistory = state.healthDashboard.metrics?.diversityHistory.orEmpty(),
                                expiryHeatmap = state.healthDashboard.metrics?.expiryHeatmap.orEmpty(),
                            ),
                        ),
            )
        }
    }

    private fun HealthDashboardMetrics.hasMeaningfulData(): Boolean =
        nutritionDistribution.values.any { it > 0.0 } ||
            expiryAlerts.isNotEmpty() ||
            recommendations.isNotEmpty()

    companion object {
        private const val DEFAULT_TREND_RANGE_DAYS = 30
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
                runCatching { repository.sync() }
                _uiState.update { it.copy(showAddNewItemDialog = false) }
                refreshHealthDashboard()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, showAddNewItemDialog = false) }
            }
        }
    }

    fun onItemClick(itemId: String) {
        // TODO: Navigate to item detail screen
    }

    fun updateIngredientCategory(itemId: String, category: IngredientCategory?) {
        viewModelScope.launch {
            try {
                repository.updateCategory(itemId.toLong(), category)
                runCatching { repository.sync() }
            } catch (error: Exception) {
                _uiState.update { it.copy(error = error.message) }
            }
        }
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
                runCatching { repository.sync() }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun undoRemoveItem() {
        val pending = _uiState.value.pendingDeletion ?: return
        pending.job.cancel()
        _uiState.update { current ->
            val (sorted, grouped) =
                applySortAndGroup(
                    originalRefrigeratedItems,
                    current.sortOption,
                    current.groupOption,
                    current.activeCategoryFilter
                )
            current.copy(
                refrigeratedItems = sorted,
                groupedRefrigeratedItems = grouped,
                pendingDeletion = null,
                categorySummaries = buildCategorySummaries(originalRefrigeratedItems)
            )
        }
    }

    fun updateSortingAndGrouping(
        sort: SortOption? = null,
        group: GroupOption? = null
    ) {
        val newSort = sort ?: _uiState.value.sortOption
        val newGroup = group ?: _uiState.value.groupOption
        val categoryFilter = _uiState.value.activeCategoryFilter
        val (sortedBase, groupedBase) = applySortAndGroup(originalRefrigeratedItems, newSort, newGroup, categoryFilter)
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
        group: GroupOption,
        categoryFilter: IngredientCategory?
    ): Pair<List<RefrigeratedItem>, Map<Freshness, List<RefrigeratedItem>>> {
        val filtered =
            categoryFilter
                ?.let { filter -> items.filter { it.category == filter } }
                ?: items
        val sorted =
            when (sort) {
                SortOption.EXPIRY -> filtered.sortedBy { it.daysUntilExpiry }
                SortOption.NAME -> filtered.sortedBy { it.name.lowercase() }
                SortOption.ADDED_DATE -> filtered.sortedBy { it.ageDays }
            }
        val grouped =
            if (group == GroupOption.FRESHNESS) sorted.groupBy { it.freshness } else emptyMap()
        return sorted to grouped
    }

    fun onCategoryFilterChange(targetCategory: IngredientCategory?) {
        val current = _uiState.value
        if (current.activeCategoryFilter == targetCategory) {
            // Tapping the same chip again clears the filter
            applyFilterChange(null)
        } else {
            applyFilterChange(targetCategory)
        }
    }

    private fun applyFilterChange(newFilter: IngredientCategory?) {
        val current = _uiState.value
        val (sorted, grouped) =
            applySortAndGroup(
                originalRefrigeratedItems,
                current.sortOption,
                current.groupOption,
                newFilter
            )
        _uiState.update {
            it.copy(
                activeCategoryFilter = newFilter,
                refrigeratedItems = sorted,
                groupedRefrigeratedItems = grouped
            )
        }
    }

    fun onViewModeChange(mode: InventoryViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    private fun buildCategorySummaries(items: List<RefrigeratedItem>): List<CategorySummary> {
        val soonThresholdDays = 3
        return items
            .groupBy { it.category }
            .map { (category, categoryItems) ->
                CategorySummary(
                    category = category,
                    totalCount = categoryItems.size,
                    expiringSoonCount = categoryItems.count { it.daysUntilExpiry in 0..soonThresholdDays }
                )
            }.sortedBy { it.category.name }
    }

    // Selection mode functions
    fun onToggleItemSelection(itemId: String) {
        _uiState.update { current ->
            val newSelectedIds =
                if (current.selectedItemIds.contains(itemId)) {
                    current.selectedItemIds - itemId
                } else {
                    current.selectedItemIds + itemId
                }
            current.copy(selectedItemIds = newSelectedIds)
        }
    }

    // Recipe generation functions
    fun onGenerateRecipeClick() {
        val selectedNames =
            originalRefrigeratedItems
                .filter { it.id in _uiState.value.selectedItemIds }
                .map { it.name }
                .distinct()

        if (selectedNames.isEmpty()) {
            return
        }

        generateRecipeFromInventory(selectedNames)
    }

    private fun generateRecipeFromInventory(ingredientNames: List<String>) {
        viewModelScope.launch {
            _recipeState.value = RecipeUiState.Loading
            lastRecipeRequestIngredients = ingredientNames

            try {
                recipeRepository
                    .generateRecipeFromInventory(ingredientNames)
                    .onSuccess { recipe ->
                        _recipeState.value = RecipeUiState.Success(recipe)
                    }.onFailure { e ->
                        _recipeState.value = RecipeUiState.Error(e.message ?: "Unknown error")
                    }
            } catch (e: Exception) {
                _recipeState.value = RecipeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetRecipeState() {
        _recipeState.value = RecipeUiState.Idle
    }

    fun fetchRandomRecipe() {
        val inventoryNames =
            originalRefrigeratedItems
                .sortedBy { it.daysUntilExpiry }
                .map { it.name }
                .distinct()
                .take(12)

        if (inventoryNames.isEmpty()) {
            _recipeState.value = RecipeUiState.Error("目前沒有庫存食材可生成食譜")
            return
        }

        generateRecipeFromInventory(inventoryNames)
    }

    fun retryLastRecipeRequest() {
        if (lastRecipeRequestIngredients.isNotEmpty()) {
            generateRecipeFromInventory(lastRecipeRequestIngredients)
        }
    }

    fun submitRecipeFeedback(feedbackType: RecipeFeedbackType) {
        val current = _recipeState.value as? RecipeUiState.Success ?: return
        val accessToken = userSessionProvider.accessToken()
        if (accessToken.isBlank()) return

        viewModelScope.launch {
            recipeRepository
                .submitRecipeFeedback(
                    recipeId = current.recipe.recipeId,
                    feedbackType = feedbackType,
                    accessToken = accessToken,
                ).onSuccess {
                    _recipeState.value = current.copy(recipe = current.recipe.copy(userFeedback = feedbackType))
                }
        }
    }
}

sealed interface RecipeUiState {
    data object Idle : RecipeUiState

    data object Loading : RecipeUiState

    data class Success(
        val recipe: RecipeSuggestion
    ) : RecipeUiState

    data class Error(
        val message: String
    ) : RecipeUiState
}
