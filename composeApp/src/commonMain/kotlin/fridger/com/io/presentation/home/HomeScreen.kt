@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package fridger.com.io.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import fridger.com.io.data.model.Freshness
import fridger.com.io.presentation.ViewModelFactoryProvider
import fridger.com.io.presentation.components.ShoppingQuickAddTopDialog
import fridger.com.io.presentation.home.components.BottomActionBar
import fridger.com.io.presentation.home.components.RecipeResultSheet
import fridger.com.data.model.remote.MealDto
import fridger.com.io.presentation.util.animateItemPlacementCompat
import fridger.com.io.ui.theme.AppColors
import fridger.com.io.ui.theme.sizing
import fridger.com.io.ui.theme.spacing
import fridger.com.io.utils.stringResourceFormat
import fridger.composeapp.generated.resources.*
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.home_refrigerated
import fridger.composeapp.generated.resources.home_title
import org.jetbrains.compose.resources.stringResource
import coil3.compose.AsyncImage
import androidx.compose.foundation.layout.Box

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel(factory = ViewModelFactoryProvider.factory)
    val uiState by viewModel.uiState.collectAsState()
    val recipeState by viewModel.recipeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Random recipe bottom sheet state
    val randomRecipeSheetState = rememberModalBottomSheetState()
    val isRandomRecipeSheetVisible by remember(recipeState) {
        androidx.compose.runtime.mutableStateOf(recipeState !is RecipeUiState.Idle)
    }

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
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                contentPadding = PaddingValues(bottom = MaterialTheme.sizing.contentPaddingVertical),
            ) {
                item {
                    HomeHeader(onSettingsClick = onSettingsClick)
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                    ) {
                        Button(
                            onClick = viewModel::onShowAddItemDialog,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(Res.string.home_add_ingredient))
                        }
                    }
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                    ) {
                        Button(onClick = { viewModel.fetchRandomRecipe() }) {
                            Text("給我一個隨機食譜")
                        }
                    }

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
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                                    .padding(vertical = MaterialTheme.spacing.large),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter =
                                    fridger.com.io.presentation.home.resources
                                        .emptyFridgePainter(),
                                contentDescription = null,
                                modifier = Modifier.size(130.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = stringResource(Res.string.home_empty_title),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(Res.string.home_empty_subtitle),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraHuge))
                        }
                    }
                }

                refrigeratedSection(
                    refrigeratedItems = uiState.refrigeratedItems,
                    groupedRefrigeratedItems = uiState.groupedRefrigeratedItems,
                    sortOption = uiState.sortOption,
                    groupOption = uiState.groupOption,
                    onSortChange = { sort -> viewModel.updateSortingAndGrouping(sort = sort) },
                    onGroupChange = { group -> viewModel.updateSortingAndGrouping(group = group) },
                    onItemClick = { id -> viewModel.onItemClick(id) },
                    onRemoveItem = { id -> viewModel.onRemoveItemInitiated(id) },
                    selectedItemIds = uiState.selectedItemIds,
                    onToggleItemSelection = { id -> viewModel.onToggleItemSelection(id) }
                )
            }
        }

        LaunchedEffect(uiState.pendingDeletion) {
            val pendingItem = uiState.pendingDeletion?.item
            if (pendingItem!=null) {
                val result =
                    snackbarHostState.showSnackbar(
                        message = "${pendingItem.name} 已被移除",
                        actionLabel = "復原",
                        duration = SnackbarDuration.Short
                    )
                if (result==SnackbarResult.ActionPerformed) {
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
                onGenerateRecipeClick = viewModel::onGenerateRecipeClick
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
                        modifier = Modifier
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
                            println("🎨 UI: Displaying loading state")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("正在搜尋食譜...")
                            }
                        }
                        is RecipeUiState.Success -> {
                            println("🎨 UI: Displaying success state with ${state.meals.size} meals")
                            LazyColumn {
                                if (state.meals.isNotEmpty()) {
                                    items(state.meals) { meal ->
                                        println("🎨 UI: Rendering meal: ${meal.strMeal}")
                                        RecipeDetails(meal = meal)
                                        if (state.meals.size > 1) {
                                            Spacer(Modifier.height(24.dp))
                                        }
                                    }
                                } else {
                                    item {
                                        println("🎨 UI: Displaying no recipes found message")
                                        Text(
                                            "沒有找到相關食譜",
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        is RecipeUiState.Error -> {
                            println("🎨 UI: Displaying error state: ${state.message}")
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "載入失敗: ${state.message}",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Button(onClick = { viewModel.fetchRandomRecipe() }) {
                                    Text("重試")
                                }
                            }
                        }
                        is RecipeUiState.Idle -> {
                            println("🎨 UI: Displaying idle state")
                            // This shouldn't happen when sheet is visible, but just in case
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.refrigeratedSection(
    refrigeratedItems: List<RefrigeratedItem>,
    groupedRefrigeratedItems: Map<Freshness, List<RefrigeratedItem>>,
    sortOption: SortOption,
    groupOption: GroupOption,
    onSortChange: (SortOption) -> Unit,
    onGroupChange: (GroupOption) -> Unit,
    onItemClick: (String) -> Unit,
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
                SectionTitle(title = stringResource(Res.string.home_refrigerated))
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    @Composable
                    fun SortChip(
                        label: String,
                        selected: Boolean,
                        onClick: () -> Unit
                    ) {
                        val bg by animateColorAsState(
                            targetValue = if (selected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surface,
                            label = "sort_chip_bg"
                        )
                        val fg by animateColorAsState(
                            targetValue = if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface,
                            label = "sort_chip_fg"
                        )

                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(bg)
                                    .clickable(onClick = onClick)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text(label, color = fg, fontSize = 12.sp) }
                    }
                    SortChip(
                        stringResource(Res.string.home_sort_expiry),
                        sortOption==SortOption.EXPIRY
                    ) { onSortChange(SortOption.EXPIRY) }
                    SortChip(
                        stringResource(Res.string.home_sort_name),
                        sortOption==SortOption.NAME
                    ) { onSortChange(SortOption.NAME) }
                    SortChip(
                        stringResource(Res.string.home_sort_added_date),
                        sortOption==SortOption.ADDED_DATE
                    ) { onSortChange(SortOption.ADDED_DATE) }

                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.large))

                    Text(
                        stringResource(Res.string.home_group_label),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp
                    )
                    val isGrouped = groupOption==GroupOption.FRESHNESS
                    SortChip(stringResource(Res.string.home_group_by_freshness), isGrouped) {
                        onGroupChange(if (isGrouped) GroupOption.NONE else GroupOption.FRESHNESS)
                    }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            }
        }
    }

    if (groupOption==GroupOption.NONE) {
        items(
            items = refrigeratedItems,
            key = { it.id }
        ) { item ->
            RefrigeratedItemCard(
                item = item,
                onClick = { onToggleItemSelection(item.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacementCompat()
                    .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
                compact = false,
                onRemove = { onRemoveItem(item.id) },
                selectedItemIds = selectedItemIds,
                onItemClick = onItemClick
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
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
                    RefrigeratedItemCard(
                        item = item,
                        onClick = { onToggleItemSelection(item.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacementCompat()
                            .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
                        compact = false,
                        onRemove = { onRemoveItem(item.id) },
                        selectedItemIds = selectedItemIds,
                        onItemClick = onItemClick
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                }
            }
        }
    }
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
        Text(
            text = stringResource(Res.string.home_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.CenterEnd)
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
                modifier = Modifier
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
                accentColor = AppColors.Warning,
                modifier = Modifier
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
                modifier = Modifier
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
        shape = RoundedCornerShape(MaterialTheme.sizing.cornerRadiusExtraLarge),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
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
                    color = AppColors.TextSecondary,
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
        shape = RoundedCornerShape(MaterialTheme.sizing.cornerRadiusLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        .background(AppColors.IconBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon, fontSize = MaterialTheme.sizing.iconLarge.value.sp)
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
                    text = stringResourceFormat(
                        Res.string.home_item_quantity,
                        item.count.toString()
                    ),
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
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
                        is ExpiryDisplay.Overdue -> stringResourceFormat(
                            Res.string.home_days_overdue,
                            disp.days
                        )

                        ExpiryDisplay.DueToday -> stringResource(Res.string.home_days_due_today)
                        is ExpiryDisplay.Until -> stringResourceFormat(
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
private fun RefrigeratedItemCard(
    item: RefrigeratedItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onRemove: (() -> Unit)? = null,
    selectedItemIds: Set<String> = emptySet(),
    onItemClick: ((String) -> Unit)? = null,
) {
    val borderWidth by animateDpAsState(
        targetValue = if (selectedItemIds.contains(item.id)) 2.dp else 0.dp,
        label = "borderWidthAnimation"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selectedItemIds.contains(item.id))
            MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "borderColorAnimation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MaterialTheme.sizing.cornerRadiusLarge),
        colors =
            CardDefaults.cardColors(
                containerColor = if (compact) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (compact) 2.dp else 1.dp,
            ),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            if (compact) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 110.dp)
                            .padding(6.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = onRemove != null && selectedItemIds.isEmpty(),
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        IconButton(
                            onClick = onRemove ?: {},
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.home_remove_ingredient),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }


                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.icon,
                            fontSize = 44.sp,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = item.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.large),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(MaterialTheme.sizing.iconHuge)
                                .clip(CircleShape)
                                .background(AppColors.IconBackground),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item.icon,
                            fontSize = MaterialTheme.sizing.iconLarge.value.sp,
                        )
                    }

                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.large))

                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = item.quantity,
                            fontSize = 14.sp,
                            color = AppColors.TextSecondary,
                        )
                        Text(
                            text = stringResourceFormat(Res.string.home_age_days, item.ageDays),
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary,
                        )
                    }

                    AnimatedVisibility(
                        visible = !selectedItemIds.contains(item.id),
                        enter = slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut(),
                    ) {
                        IconButton(
                            onClick = { onItemClick?.invoke(item.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                        }
                    }

                    AnimatedVisibility(
                        visible = selectedItemIds.isEmpty(),
                        enter = slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                        ) {
                            val (tintColor, textDisplay) =
                                when (item.freshness) {
                                    Freshness.Expired ->
                                        MaterialTheme.colorScheme.error to
                                                when (val d = item.expiryDisplay) {
                                                    is ExpiryDisplay.Overdue -> stringResourceFormat(
                                                        Res.string.home_days_overdue,
                                                        d.days
                                                    )

                                                    ExpiryDisplay.DueToday -> stringResource(Res.string.home_days_due_today)
                                                    is ExpiryDisplay.Until -> stringResourceFormat(
                                                        Res.string.home_days_until,
                                                        d.days
                                                    )
                                                }

                                    Freshness.NearingExpiration ->
                                        AppColors.Warning to
                                                when (val d = item.expiryDisplay) {
                                                    is ExpiryDisplay.Overdue -> stringResourceFormat(
                                                        Res.string.home_days_overdue,
                                                        d.days
                                                    )

                                                    ExpiryDisplay.DueToday -> stringResource(Res.string.home_days_due_today)
                                                    is ExpiryDisplay.Until -> stringResourceFormat(
                                                        Res.string.home_days_until,
                                                        d.days
                                                    )
                                                }

                                    Freshness.Fresh ->
                                        AppColors.TextSecondary to
                                                when (val d = item.expiryDisplay) {
                                                    is ExpiryDisplay.Overdue -> stringResourceFormat(
                                                        Res.string.home_days_overdue,
                                                        d.days
                                                    )

                                                    ExpiryDisplay.DueToday -> stringResource(Res.string.home_days_due_today)
                                                    is ExpiryDisplay.Until -> stringResourceFormat(
                                                        Res.string.home_days_until,
                                                        d.days
                                                    )
                                                }
                                }
                            Text(
                                text = textDisplay,
                                fontSize = 14.sp,
                                color = tintColor,
                            )
                        }
                    }
                }
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
                modifier = Modifier
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
                    colors = CardDefaults.cardColors(
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
                    colors = CardDefaults.cardColors(
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
                colors = CardDefaults.cardColors(
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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
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
