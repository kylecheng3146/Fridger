@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package fridger.com.io.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import fridger.com.data.model.remote.MealDto
import fridger.composeapp.generated.resources.*
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.home_refrigerated
import fridger.composeapp.generated.resources.home_title
import fridger.com.io.data.model.Freshness
import fridger.com.io.data.model.IngredientCategory
import fridger.com.io.presentation.ViewModelFactoryProvider
import fridger.com.io.presentation.components.RichEmptyState
import fridger.com.io.presentation.components.ShoppingQuickAddTopDialog
import fridger.com.io.presentation.home.components.BottomActionBar
import fridger.com.io.presentation.home.components.IngredientCompactCard
import fridger.com.io.presentation.home.components.RecipeResultSheet
import fridger.com.io.presentation.home.dashboard.HealthDashboardDetailSheet
import fridger.com.io.presentation.home.dashboard.HealthDashboardBentoGrid
import fridger.com.io.presentation.settings.SettingsScreen
import fridger.com.io.presentation.util.animateItemPlacementCompat
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import fridger.com.io.ui.theme.sizing
import fridger.com.io.ui.theme.spacing
import fridger.com.io.utils.stringResourceFormat

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel(factory = ViewModelFactoryProvider.factory)
    val uiState by viewModel.uiState.collectAsState()
    val recipeState by viewModel.recipeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDashboardDetails by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showAiRecipeSheet by remember { mutableStateOf(false) }
    val dashboardState = uiState.healthDashboard

    fun openAiRecipeSheet() {
        showAiRecipeSheet = true
    }

    fun closeAiRecipeSheet() {
        showAiRecipeSheet = false
    }

    // Random recipe bottom sheet state
    val randomRecipeSheetState = rememberModalBottomSheetState()
    val isRandomRecipeSheetVisible = recipeState !is RecipeUiState.Idle

    // Handle random recipe sheet visibility changes
    LaunchedEffect(isRandomRecipeSheetVisible) {
        if (isRandomRecipeSheetVisible) {
            randomRecipeSheetState.show()
        } else {
            randomRecipeSheetState.hide()
        }
    }

    // Handle random recipe sheet dismissal
    LaunchedEffect(randomRecipeSheetState.isVisible) {
        if (!randomRecipeSheetState.isVisible && isRandomRecipeSheetVisible) {
            // Reset to Idle state when dismissed
            viewModel.resetRecipeState()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            snackbarHost = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 800.dp), // Responsive Max Width
                    contentPadding = PaddingValues(
                        bottom = MaterialTheme.sizing.contentPaddingVertical + 80.dp, // contentPadding + BottomBar height
                        top = MaterialTheme.sizing.contentPaddingVertical
                    ),
                ) {
                    item {
                        HomeHeader(
                            onSettingsClick = { showSettings = true },
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = viewModel::onShowAddItemDialog,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(Res.string.home_add_ingredient))
                            }

                            OutlinedButton(
                                onClick = ::openAiRecipeSheet,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f).pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Icon(
                                    Icons.Default.Dashboard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(Res.string.home_random_recipe),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
                        ) {
                            HealthDashboardBentoGrid(
                                state = dashboardState,
                                onRefresh = { viewModel.refreshHealthDashboard() },
                                onViewDetails = { showDashboardDetails = true }
                            )
                        }
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))
                    }

                expirySection(
                    todayItems = uiState.todayExpiringItems,
                    weekItems = uiState.weekExpiringItems,
                    expiredItems = uiState.expiredItems,
                )

                item {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraHuge))
                }

                if (uiState.refrigeratedItems.isEmpty()) {
                    item {
                        RichEmptyState(
                            message = stringResource(Res.string.home_empty_title),
                            subMessage = stringResource(Res.string.home_empty_subtitle),
                            actionLabel = stringResource(Res.string.home_add_ingredient),
                            onActionClick = viewModel::onShowAddItemDialog
                        )
                    }
                }

                refrigeratedSection(
                    refrigeratedItems = uiState.refrigeratedItems,
                    groupedRefrigeratedItems = uiState.groupedRefrigeratedItems,
                    categorySummaries = uiState.categorySummaries,
                    activeCategoryFilter = uiState.activeCategoryFilter,
                    sortOption = uiState.sortOption,
                    groupOption = uiState.groupOption,
                    viewMode = uiState.viewMode,
                    onSortChange = { sort -> viewModel.updateSortingAndGrouping(sort = sort) },
                    onGroupChange = { group -> viewModel.updateSortingAndGrouping(group = group) },
                    onCategoryFilterChange = viewModel::onCategoryFilterChange,
                    onViewModeChange = viewModel::onViewModeChange,
                    onRemoveItem = { id -> viewModel.onRemoveItemInitiated(id) },
                    selectedItemIds = uiState.selectedItemIds,
                    onToggleItemSelection = { id -> viewModel.onToggleItemSelection(id) }
                )
            }
        }
    }

        LaunchedEffect(uiState.pendingDeletion) {
            val pendingItem = uiState.pendingDeletion?.item
            if (pendingItem != null) {
                val result =
                    snackbarHostState.showSnackbar(
                        message = "${pendingItem.name} 已被移除",
                        actionLabel = "復原",
                        duration = SnackbarDuration.Short
                    )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoRemoveItem()
                }
            }
        }

        if (uiState.showAddNewItemDialog) {
            ShoppingQuickAddTopDialog(
                onDismiss = viewModel::onDismissDialog,
                onItemsAdded = viewModel::onItemsAdded,
                searchText = uiState.quickAddSearchText,
                onSearchTextChange = viewModel::onQuickAddSearchTextChange,
                suggestions = uiState.quickAddSuggestions
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = uiState.selectedItemIds.isNotEmpty(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomActionBar(
                selectedCount = uiState.selectedItemIds.size,
                onCancelClick = {
                    // Clear all selections by toggling each selected item
                    uiState.selectedItemIds.forEach { itemId ->
                        viewModel.onToggleItemSelection(itemId)
                    }
                },
                onGenerateRecipeClick = ::openAiRecipeSheet
            )
        }

        if (showAiRecipeSheet) {
            AiRecipeGeneratorSheet(
                visible = showAiRecipeSheet,
                onDismiss = ::closeAiRecipeSheet,
            )
        }

        // Modal Bottom Sheet for recipe results
        if (isRandomRecipeSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.resetRecipeState() },
                sheetState = randomRecipeSheetState
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with close button
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "食譜建議",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { viewModel.resetRecipeState() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "關閉",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    when (val state = recipeState) {
                        is RecipeUiState.Loading -> {
                            RecipeResultSheet(
                                isGenerating = true,
                                recipe = null,
                                onTryAgain = viewModel::retryLastRecipeRequest,
                                onFeedbackClick = viewModel::submitRecipeFeedback,
                            )
                        }
                        is RecipeUiState.Success -> {
                            RecipeResultSheet(
                                isGenerating = false,
                                recipe = state.recipe,
                                onTryAgain = viewModel::retryLastRecipeRequest,
                                onFeedbackClick = viewModel::submitRecipeFeedback,
                            )
                        }
                        is RecipeUiState.Error -> {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "載入失敗: ${state.message}",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Button(onClick = { viewModel.retryLastRecipeRequest() }) {
                                    Text("重試")
                                }
                            }
                        }
                        is RecipeUiState.Idle -> {
                            // This shouldn't happen when sheet is visible, but just in case
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp))
                        }
                    }
                }
            }
        }

        if (showDashboardDetails) {
            HealthDashboardDetailSheet(
                state = dashboardState,
                onDismiss = { showDashboardDetails = false }
            )
        }
        if (showSettings) {
            Dialog(
                onDismissRequest = { showSettings = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SettingsScreen(
                        onBackClick = { showSettings = false },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

private fun LazyListScope.refrigeratedSection(
    refrigeratedItems: List<RefrigeratedItem>,
    groupedRefrigeratedItems: Map<Freshness, List<RefrigeratedItem>>,
    categorySummaries: List<CategorySummary>,
    activeCategoryFilter: IngredientCategory?,
    sortOption: SortOption,
    groupOption: GroupOption,
    viewMode: InventoryViewMode,
    onSortChange: (SortOption) -> Unit,
    onGroupChange: (GroupOption) -> Unit,
    onCategoryFilterChange: (IngredientCategory?) -> Unit,
    onViewModeChange: (InventoryViewMode) -> Unit,
    onRemoveItem: (String) -> Unit,
    selectedItemIds: Set<String>,
    onToggleItemSelection: (String) -> Unit
) {
    if (refrigeratedItems.isNotEmpty()) {
        item {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                        .padding(bottom = MaterialTheme.spacing.small),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(title = stringResource(Res.string.home_refrigerated))
                    InventoryViewModeToggle(
                        viewMode = viewMode,
                        onViewModeChange = onViewModeChange
                    )
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                if (categorySummaries.isNotEmpty()) {
                    CategorySummaryRow(
                        summaries = categorySummaries,
                        activeCategory = activeCategoryFilter,
                        onCategorySelected = onCategoryFilterChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                }
                SortAndGroupRow(
                    sortOption = sortOption,
                    groupOption = groupOption,
                    onSortChange = onSortChange,
                    onGroupChange = onGroupChange
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            }
        }
    }

    if (groupOption == GroupOption.NONE) {
        if (viewMode == InventoryViewMode.LIST) {
            items(
                items = refrigeratedItems,
                key = { it.id }
            ) { item ->
                fridger.com.io.presentation.home.components.IngredientItem(
                    item = item,
                    isSelected = selectedItemIds.contains(item.id),
                    onClick = { onToggleItemSelection(item.id) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .animateItemPlacementCompat()
                            .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
                    onRemove = { onRemoveItem(item.id) }
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            }
        } else {
            val rows = refrigeratedItems.chunked(2)
            items(
                items = rows,
                key = { row -> row.joinToString("_") { it.id } }
            ) { rowItems ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                            .padding(bottom = MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) {
                    rowItems.forEach { item ->
                        IngredientCompactCard(
                            item = item,
                            isSelected = selectedItemIds.contains(item.id),
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .animateItemPlacementCompat(),
                            onClick = { onToggleItemSelection(item.id) },
                            onRemove = { onRemoveItem(item.id) }
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    } else {
        val groupsOrder = listOf(Freshness.Expired, Freshness.NearingExpiration, Freshness.Fresh)
        val titles =
            mapOf(
                Freshness.Expired to Res.string.home_group_section_expired,
                Freshness.NearingExpiration to Res.string.home_group_section_nearing,
                Freshness.Fresh to Res.string.home_group_section_fresh
            )
        groupsOrder.forEach { freshness ->
            val itemsInGroup = groupedRefrigeratedItems[freshness].orEmpty()
            if (itemsInGroup.isNotEmpty()) {
                item {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                                .padding(
                                    top = MaterialTheme.spacing.medium,
                                    bottom = MaterialTheme.spacing.small
                                )
                    ) {
                        SectionTitle(title = stringResource(titles[freshness]!!))
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    }
                }
                items(
                    items = itemsInGroup,
                    key = { it.id + "_" + freshness::class.simpleName }
                ) { item ->
                    fridger.com.io.presentation.home.components.IngredientItem(
                        item = item,
                        isSelected = selectedItemIds.contains(item.id),
                        onClick = { onToggleItemSelection(item.id) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .animateItemPlacementCompat()
                                .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
                        onRemove = { onRemoveItem(item.id) }
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                }
            }
        }
    }
}

@Composable
private fun InventoryViewModeToggle(
    viewMode: InventoryViewMode,
    onViewModeChange: (InventoryViewMode) -> Unit,
) {
    val options = listOf(InventoryViewMode.LIST, InventoryViewMode.GRID)
    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == viewMode,
                onClick = { onViewModeChange(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                label = {
                    val label =
                        when (option) {
                            InventoryViewMode.LIST -> stringResource(Res.string.home_view_mode_list)
                            InventoryViewMode.GRID -> stringResource(Res.string.home_view_mode_grid)
                        }
                    Text(text = label, fontSize = 12.sp)
                },
                icon = {
                    val icon =
                        when (option) {
                            InventoryViewMode.LIST -> Icons.AutoMirrored.Filled.ViewList
                            InventoryViewMode.GRID -> Icons.Default.Dashboard
                        }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun SortAndGroupRow(
    sortOption: SortOption,
    groupOption: GroupOption,
    onSortChange: (SortOption) -> Unit,
    onGroupChange: (GroupOption) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SortChip(
            label = stringResource(Res.string.home_sort_expiry),
            selected = sortOption == SortOption.EXPIRY,
            onClick = { onSortChange(SortOption.EXPIRY) }
        )
        SortChip(
            label = stringResource(Res.string.home_sort_name),
            selected = sortOption == SortOption.NAME,
            onClick = { onSortChange(SortOption.NAME) }
        )
        SortChip(
            label = stringResource(Res.string.home_sort_added_date),
            selected = sortOption == SortOption.ADDED_DATE,
            onClick = { onSortChange(SortOption.ADDED_DATE) }
        )

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.large))

        Text(
            text = stringResource(Res.string.home_group_label),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp
        )
        val isGrouped = groupOption == GroupOption.FRESHNESS
        SortChip(
            label = stringResource(Res.string.home_group_by_freshness),
            selected = isGrouped,
            onClick = { onGroupChange(if (isGrouped) GroupOption.NONE else GroupOption.FRESHNESS) }
        )
    }
}

@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        label = "sort_chip_bg"
    )
    val fg by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        label = "sort_chip_fg"
    )

    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(bg)
                .clickable(onClick = onClick)
                .pointerHoverIcon(PointerIcon.Hand)
                .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, color = fg, fontSize = 12.sp)
    }
}

@Composable
private fun CategorySummaryRow(
    summaries: List<CategorySummary>,
    activeCategory: IngredientCategory?,
    onCategorySelected: (IngredientCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalExpiring = summaries.sumOf { it.expiringSoonCount }
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        item(key = "all") {
            CategorySummaryCard(
                title = stringResource(Res.string.home_category_all),
                icon = "✨",
                expiringSoon = totalExpiring,
                isSelected = activeCategory == null,
                onClick = { onCategorySelected(null) }
            )
        }
        items(
            items = summaries,
            key = { it.category.name }
        ) { summary ->
            CategorySummaryCard(
                title = categoryLabel(summary.category),
                icon = categoryIcon(summary.category),
                expiringSoon = summary.expiringSoonCount,
                isSelected = activeCategory == summary.category,
                onClick = { onCategorySelected(summary.category) }
            )
        }
    }
}

@Composable
private fun CategorySummaryCard(
    title: String,
    icon: String,
    expiringSoon: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    secondaryLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.outlineVariant,
        label = "category_card_border"
    )

    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        label = "category_card_container"
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "category_card_content"
    )

    val secondaryContentColor by animateColorAsState(
        targetValue =
            if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f)
            else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "category_card_secondary"
    )

    Surface(
        modifier =
            modifier
                .widthIn(min = 120.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .pointerHoverIcon(PointerIcon.Hand),
        tonalElevation = 0.dp,
        color = containerColor,
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, borderColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = icon, fontSize = 18.sp)
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            }

            if (expiringSoon > 0) {
                Text(
                    text = stringResource(Res.string.home_category_expiring_badge, expiringSoon),
                    fontSize = 12.sp,
                    color = if (isSelected) secondaryContentColor else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }

            secondaryLabel?.let {
                Text(
                    text = it,
                    fontSize = 11.sp,
                    color = secondaryContentColor
                )
            }
        }
    }
}

@Composable
private fun categoryLabel(category: IngredientCategory): String {
    val resId =
        when (category) {
            IngredientCategory.VEGETABLES -> Res.string.home_category_vegetables
            IngredientCategory.FRUITS -> Res.string.home_category_fruits
            IngredientCategory.MEAT -> Res.string.home_category_meat
            IngredientCategory.DAIRY -> Res.string.home_category_dairy
            IngredientCategory.SEAFOOD -> Res.string.home_category_seafood
            IngredientCategory.GRAINS -> Res.string.home_category_grains
            IngredientCategory.OTHERS -> Res.string.home_category_others
        }
    return stringResource(resId)
}

private fun categoryIcon(category: IngredientCategory): String =
    when (category) {
        IngredientCategory.VEGETABLES -> "🥬"
        IngredientCategory.FRUITS -> "🍎"
        IngredientCategory.MEAT -> "🥩"
        IngredientCategory.DAIRY -> "🥛"
        IngredientCategory.SEAFOOD -> "🐟"
        IngredientCategory.GRAINS -> "🌾"
        IngredientCategory.OTHERS -> "🧂"
    }

@Composable
private fun HomeHeader(onSettingsClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = MaterialTheme.spacing.large,
                    bottom = MaterialTheme.spacing.extraSmall,
                    start = MaterialTheme.sizing.contentPaddingHorizontal,
                    end = MaterialTheme.sizing.contentPaddingHorizontal
                ),
    ) {
        Column {
            Text(
                text = stringResource(Res.string.home_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(Res.string.settings),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

private fun LazyListScope.expirySection(
    todayItems: List<ExpiringItem>,
    weekItems: List<ExpiringItem>,
    expiredItems: List<ExpiringItem>,
) {
    item {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
        ) {
            SectionTitle(title = stringResource(Res.string.home_section_soon_title))
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }
    }

    if (todayItems.isEmpty()) {
        item {
            ExpiryCard(
                icon = "⚠️",
                label = stringResource(Res.string.home_today_none),
                isEmpty = true,
                modifier = Modifier.padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
            )
        }
    } else {
        items(todayItems, key = { "today_${it.id}" }) { item ->
            ExpiringListItemCard(
                item = item,
                accentColor = MaterialTheme.colorScheme.error,
                modifier =
                    Modifier
                        .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                        .padding(bottom = MaterialTheme.spacing.medium)
            )
        }
    }

    item {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))
            SectionTitle(title = stringResource(Res.string.home_section_week_title))
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }
    }

    if (weekItems.isEmpty()) {
        item {
            ExpiryCard(
                icon = "⚠️",
                label = stringResource(Res.string.home_week_none),
                isEmpty = true,
                modifier = Modifier.padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
            )
        }
    } else {
        items(weekItems, key = { "week_${it.id}" }) { item ->
            ExpiringListItemCard(
                item = item,
                accentColor = MaterialTheme.colorScheme.secondary,
                modifier =
                    Modifier
                        .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                        .padding(bottom = MaterialTheme.spacing.medium)
            )
        }
    }

    item {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))
            SectionTitle(title = stringResource(Res.string.home_section_expired_title))
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }
    }

    if (expiredItems.isEmpty()) {
        item {
            ExpiryCard(
                icon = "❄️",
                label = stringResource(Res.string.home_expired_none),
                isEmpty = true,
                modifier = Modifier.padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
            )
        }
    } else {
        items(expiredItems, key = { "expired_${it.id}" }) { item ->
            ExpiringListItemCard(
                item = item,
                accentColor = MaterialTheme.colorScheme.error,
                modifier =
                    Modifier
                        .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                        .padding(bottom = MaterialTheme.spacing.medium)
            )
        }
    }
}

@Composable
private fun ExpiryCard(
    icon: String,
    label: String,
    modifier: Modifier = Modifier,
    isEmpty: Boolean = false,
) {
    Card(
        modifier = modifier.height(MaterialTheme.sizing.cardHeightMedium),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp,
            ),
    ) {
        if (!isEmpty) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(MaterialTheme.spacing.large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                Text(
                    text = icon,
                    fontSize = MaterialTheme.sizing.iconExtraLarge.value.sp,
                )

                Column {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(MaterialTheme.spacing.large),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ExpiringListItemCard(
    item: ExpiringItem,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(MaterialTheme.sizing.iconHuge)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(item.icon),
                    contentDescription = item.name,
                    modifier = Modifier.size(MaterialTheme.sizing.iconLarge),
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.large))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text =
                        stringResourceFormat(
                            Res.string.home_item_quantity,
                            item.count.toString()
                        ),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val bg = accentColor.copy(alpha = 0.22f)
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(bg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                val daysText =
                    when (val disp = item.expiryDisplay) {
                        is ExpiryDisplay.Overdue ->
                            stringResourceFormat(
                                Res.string.home_days_overdue,
                                disp.days
                            )

                        ExpiryDisplay.DueToday -> stringResource(Res.string.home_days_due_today)
                        is ExpiryDisplay.Until ->
                            stringResourceFormat(
                                Res.string.home_days_until,
                                disp.days
                            )
                    }
                Text(
                    text = daysText,
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun RecipeDetails(meal: MealDto) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 食譜圖片
        if (!meal.strMealThumb.isNullOrBlank()) {
            println("🖼️ UI: Loading recipe image: ${meal.strMealThumb}")

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = meal.strMealThumb,
                    contentDescription = meal.strMeal ?: "Recipe image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = {
                        println("🖼️ COIL: Loading image ${meal.strMealThumb}")
                    },
                    onSuccess = {
                        println("✅ COIL: Successfully loaded image ${meal.strMealThumb}")
                    },
                    onError = { error ->
                        println("❌ COIL: Failed to load image ${meal.strMealThumb} - ${error.result.throwable.message}")
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
        }

        // 食譜標題
        Text(
            text = meal.strMeal ?: "無標題",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        // 分類和地區資訊
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            meal.strCategory?.let { category ->
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "🏷️ $category",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            meal.strArea?.let { area ->
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        ),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "🌍 $area",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // 作法說明
        if (!meal.strInstructions.isNullOrBlank()) {
            Text(
                text = "作法說明：",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = meal.strInstructions,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        } else {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "點擊查看詳細作法...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // YouTube 連結（如果有的話）
        if (!meal.strYoutube.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📺",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "YouTube 影片教學可用",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
