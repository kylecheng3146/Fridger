package fridger.com.io.presentation.home

import AddNewItemDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import fridger.com.io.presentation.ViewModelFactoryProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fridger.com.io.data.model.Freshness
import fridger.com.io.ui.theme.AppColors
import fridger.com.io.ui.theme.spacing
import fridger.com.io.ui.theme.sizing
import fridger.com.io.utils.stringResourceFormat
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.home_days_until_expiry
import fridger.composeapp.generated.resources.home_frozen
import fridger.composeapp.generated.resources.home_refrigerated
import fridger.composeapp.generated.resources.home_title
import fridger.composeapp.generated.resources.empty_fridge
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {}
) {
    val viewModel: HomeViewModel = viewModel(factory = ViewModelFactoryProvider.factory)
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showAddNewItemDialog) {
        AddNewItemDialog(
            onDismiss = viewModel::onDismissDialog,
            onConfirm = viewModel::onAddNewItemConfirm
        )
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = MaterialTheme.sizing.contentPaddingVertical),
        ) {
            // Header
            item {
                HomeHeader(onSettingsClick = onSettingsClick)
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))
            }

            // Expiry Section
            item {
                ExpirySection(
                    todayItems = uiState.todayExpiringItems,
                    weekItems = uiState.weekExpiringItems,
                    expiredItems = uiState.expiredItems,
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraHuge))
            }

            // Empty State when no items
            if (uiState.refrigeratedItems.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                            .padding(vertical = MaterialTheme.spacing.large),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = fridger.com.io.presentation.home.resources.emptyFridgePainter(),
                            contentDescription = null,
                            modifier = Modifier.size(130.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "您的冰箱空空如也～",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "點擊下方按鈕新增第一樣食材吧！",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(MaterialTheme.spacing.extraHuge))
                    }
                }
            }

            // Refrigerated Items
            item {
                if (uiState.refrigeratedItems.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                            .padding(bottom = MaterialTheme.spacing.small),
                    ) {
                        SectionTitle(title = stringResource(Res.string.home_refrigerated))
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                        // Sorting & Grouping controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sort options
                            @Composable
                            fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
                                val bg =
                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                val fg =
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(bg)
                                        .clickable(onClick = onClick)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) { Text(label, color = fg, fontSize = 12.sp) }
                            }
                            SortChip(
                                "過期日",
                                uiState.sortOption==SortOption.EXPIRY
                            ) { viewModel.updateSortingAndGrouping(sort = SortOption.EXPIRY) }
                            SortChip(
                                "名稱",
                                uiState.sortOption==SortOption.NAME
                            ) { viewModel.updateSortingAndGrouping(sort = SortOption.NAME) }
                            SortChip(
                                "新增日期",
                                uiState.sortOption==SortOption.ADDED_DATE
                            ) { viewModel.updateSortingAndGrouping(sort = SortOption.ADDED_DATE) }

                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.large))

                            // Grouping toggle
                            Text(
                                "分組:",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp
                            )
                            val isGrouped = uiState.groupOption==GroupOption.FRESHNESS
                            SortChip("依新鮮度", isGrouped) {
                                viewModel.updateSortingAndGrouping(group = if (isGrouped) GroupOption.NONE else GroupOption.FRESHNESS)
                            }
                        }
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                    }
                }
            }

            if (uiState.groupOption==GroupOption.NONE) {
                items(
                    items = uiState.refrigeratedItems.chunked(3),
                    key = { group -> group.joinToString("_") { it.id } }
                ) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                RefrigeratedItemCard(
                                    item = item,
                                    onClick = { viewModel.onItemClick(item.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    compact = true,
                                    onRemove = { viewModel.removeItem(item.id) }
                                )
                            }
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                }
            } else {
                // Grouped by freshness sections
                val groupsOrder =
                    listOf(Freshness.Expired, Freshness.NearingExpiration, Freshness.Fresh)
                val titles = mapOf(
                    Freshness.Expired to "已過期",
                    Freshness.NearingExpiration to "即將過期",
                    Freshness.Fresh to "新鮮"
                )
                groupsOrder.forEach { freshness ->
                    val itemsInGroup = uiState.groupedRefrigeratedItems[freshness].orEmpty()
                    if (itemsInGroup.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                                    .padding(
                                        top = MaterialTheme.spacing.medium,
                                        bottom = MaterialTheme.spacing.small
                                    )
                            ) {
                                SectionTitle(title = titles[freshness] ?: "")
                                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                            }
                        }
                        items(
                            items = itemsInGroup.chunked(3),
                            key = { group -> group.joinToString("_") { it.id } + "_" + freshness::class.simpleName }
                        ) { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                            ) {
                                rowItems.forEach { item ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        RefrigeratedItemCard(
                                            item = item,
                                            onClick = { viewModel.onItemClick(item.id) },
                                            modifier = Modifier.fillMaxWidth(),
                                            compact = true,
                                            onRemove = { viewModel.removeItem(item.id) }
                                        )
                                    }
                                }
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                        }
                    }
                }
            }
        }
        // Floating action button: + 新增食材
        ExtendedFloatingActionButton(
            onClick = viewModel::onAddNewItemClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = MaterialTheme.sizing.contentPaddingVertical
                ),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("新增食材") }
        )
    }
}

@Composable
private fun HomeHeader(onSettingsClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = MaterialTheme.sizing.contentPaddingTop,
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

        // Settings button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ExpirySection(
    todayItems: List<ExpiringItem>,
    weekItems: List<ExpiringItem>,
    expiredItems: List<ExpiringItem>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal),
    ) {
        // Title: 快到期了！
        SectionTitle(title = "快到期了！")
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
            todayItems.forEach { item ->
                ExpiringListItemCard(item = item, accentColor = MaterialTheme.colorScheme.error)
            }
            if (todayItems.isEmpty()) {
                // Show an empty placeholder card
                ExpiryCard(
                    icon = "⚠️",
                    count = null,
                    label = "今天/明天沒有到期食材",
                    isEmpty = true
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))

        // Title: 這週需要注意
        SectionTitle(title = "需要留意嘍！")
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
            weekItems.forEach { item ->
                ExpiringListItemCard(item = item, accentColor = AppColors.Warning)
            }

            if (weekItems.isEmpty()) {
                // Show an empty placeholder card
                ExpiryCard(
                    icon = "⚠️",
                    count = null,
                    label = "本週沒有快到期食材",
                    isEmpty = true
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))

        // Title: 已過期
        SectionTitle(title = "已過期")
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
            expiredItems.forEach { item ->
                ExpiringListItemCard(item = item, accentColor = MaterialTheme.colorScheme.error)
            }

            if (expiredItems.isEmpty()) {
                ExpiryCard(
                    icon = "❄️",
                    count = null,
                    label = "沒有已過期食材",
                    isEmpty = true
                )
            }
        }
    }
}

@Composable
private fun ExpiryCard(
    icon: String,
    count: Int?,
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
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(MaterialTheme.sizing.cornerRadiusLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(MaterialTheme.sizing.iconHuge)
                    .clip(CircleShape)
                    .background(AppColors.IconBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon, fontSize = MaterialTheme.sizing.iconLarge.value.sp)
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.large))

            // Texts
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(text = "x${item.count}", fontSize = 14.sp, color = AppColors.TextSecondary)
            }

            // Days left badge
            val bg = accentColor.copy(alpha = 0.22f)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                val daysText = when {
                    item.daysUntil < 0 -> "已過期 ${kotlin.math.abs(item.daysUntil)} 天"
                    item.daysUntil == 0 -> "今天到期了"
                    else -> "還剩 ${item.daysUntil} 天到期"
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
) {
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
        onClick = onClick,
    ) {
        if (compact) {
            // Compact tile for grid view - mimic QuickPickCell style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp)
                    .padding(6.dp)
            ) {
                // Quick remove button at top-right for compact tiles
                if (onRemove != null) {
                    androidx.compose.material3.IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "移除食材",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon
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

                // Name and Quantity
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
                    // Age info
                    Text(
                        text = "已放入 ${item.ageDays} 天",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary,
                    )
                }

                // Expiry info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                ) {
                    val (tintColor, textDisplay) = when (item.freshness) {
                        Freshness.Expired -> MaterialTheme.colorScheme.error to "已過期 ${
                            kotlin.math.abs(
                                item.daysUntilExpiry
                            )
                        } 天"

                        Freshness.NearingExpiration -> AppColors.Warning to stringResourceFormat(
                            Res.string.home_days_until_expiry,
                            item.daysUntilExpiry
                        )

                        Freshness.Fresh -> AppColors.TextSecondary to stringResourceFormat(
                            Res.string.home_days_until_expiry,
                            item.daysUntilExpiry
                        )
                    }
                    if (item.freshness!=Freshness.Fresh) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(MaterialTheme.sizing.iconSmall),
                            tint = tintColor,
                        )
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

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}
