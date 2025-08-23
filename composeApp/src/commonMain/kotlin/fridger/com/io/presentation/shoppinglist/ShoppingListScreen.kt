package fridger.com.io.presentation.shoppinglist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import fridger.com.io.data.repository.ShoppingListItem
import fridger.com.io.presentation.ViewModelFactoryProvider
import fridger.com.io.ui.theme.spacing
import fridger.com.io.ui.theme.sizing

@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
) {
    val vm: ShoppingListViewModel = viewModel(factory = ViewModelFactoryProvider.factory)
    val state by vm.uiState.collectAsState()

    // Add row replaced by quick-add dialog trigger
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    // 使用 Box 作為最外層容器，讓 dialog 可以覆蓋整個螢幕
    Box(modifier = modifier.fillMaxSize()) {
        // 主要內容
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header - align with Home header style
            ShoppingHeader()
            Spacer(Modifier.height(MaterialTheme.spacing.small))

            // Content padding aligned to Home's horizontal padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
            ) {
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("新增食材")
                }

                Spacer(Modifier.height(16.dp))

                // Clear purchased
                OutlinedButton(
                    onClick = { vm.clearPurchased() },
                    enabled = state.items.any { it.isChecked },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("清除清單")
                }

                Spacer(Modifier.height(12.dp))

                // Loading / Error / Empty states
                when {
                    state.isLoading -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null -> {
                        Text(
                            text = "載入失敗：${state.error}",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }
                    state.items.isEmpty() -> {
                        // Empty state with centered content and SVG icon
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(16.dp))

                            // Empty cart icon (Material, cross-platform)
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = "購物車為空",
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(12.dp))
                            
                            Text(
                                text = "清單空空如也～",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Text(
                                text = "點擊上方「新增食材」開始採買吧！",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        ShoppingListRow(
                            item = item,
                            onCheckedChange = { vm.toggleChecked(item.id, it) },
                            onDelete = { vm.deleteItem(item.id) }
                        )
                    }
                }
            }
        }

        // Dialog 放在 Box 的最外層，可以覆蓋整個螢幕
        if (showAddDialog) {
            ShoppingQuickAddTopDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, qty ->
                    vm.addItem(name.trim(), qty?.trim().takeUnless { it.isNullOrBlank() })
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun ShoppingHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = MaterialTheme.sizing.contentPaddingTop,
                bottom = MaterialTheme.spacing.extraSmall,
                start = MaterialTheme.sizing.contentPaddingHorizontal,
                end = MaterialTheme.sizing.contentPaddingHorizontal
            )
    ) {
        Text(
            text = "購物清單",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun ShoppingListRow(
    item: ShoppingListItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildString {
                        append(item.name)
                        if (!item.quantity.isNullOrBlank()) append("  ·  ${item.quantity}")
                    },
                    fontSize = 16.sp,
                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}

@Composable
private fun ShoppingQuickAddTopDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: String?) -> Unit
) {
    // Suggestions with emoji icons for quick add
    data class QuickItem(val name: String, val icon: String)

    val allSuggestions = remember {
        listOf(
            QuickItem("牛奶", "🥛"),
            QuickItem("雞蛋", "🥚"),
            QuickItem("麵包", "🍞"),
            QuickItem("蘋果", "🍎"),
            QuickItem("香蕉", "🍌"),
            QuickItem("雞胸肉", "🍗"),
            QuickItem("牛肉", "🥩"),
            QuickItem("豬肉", "🍖"),
            QuickItem("鮭魚", "🐟"),
            QuickItem("蝦子", "🦐"),
            QuickItem("豆腐", "🧈"),
            QuickItem("優格", "🥛"),
            QuickItem("起司", "🧀"),
            QuickItem("馬鈴薯", "🥔"),
            QuickItem("胡蘿蔔", "🥕"),
            QuickItem("番茄", "🍅"),
            QuickItem("洋蔥", "🧅"),
            QuickItem("大蒜", "🧄"),
            QuickItem("生菜", "🥬"),
            QuickItem("黃瓜", "🥒"),
            QuickItem("彩椒", "🫑"),
            QuickItem("米", "🍚"),
            QuickItem("義大利麵", "🍝"),
            QuickItem("麵條", "🍜"),
            QuickItem("醬油", "🧂"),
            QuickItem("鹽", "🧂"),
            QuickItem("糖", "🧂"),
            QuickItem("麵粉", "🌾"),
            QuickItem("食用油", "🫗"),
            QuickItem("水", "💧")
        )
    }

    var query by rememberSaveable { mutableStateOf("") }
    var qty by rememberSaveable { mutableStateOf("") } // optional quantity

    // Multi-select state
    val selected = remember { mutableStateListOf<String>() }

    val filtered = remember(query) {
        if (query.isBlank()) allSuggestions else allSuggestions.filter {
            it.name.contains(
                query,
                ignoreCase = true
            )
        }
    }

    // Favorites for quick selection
    val favorites by fridger.com.io.presentation.settings.SettingsManager.quickFavorites.collectAsState(initial = emptySet())
    val ordered = remember(filtered, favorites) {
        filtered.sortedBy { if (favorites.contains(it.name)) 0 else 1 }
    }

    val scope = rememberCoroutineScope()

    // Internal visibility to animate enter from top and exit to top
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // 保持首次開啟時有進場動畫；返回時若狀態已保存為 true，則不會再次觸發
        visible = true // 立即開始動畫
    }

    // 完全覆蓋整個螢幕的 dialog
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight }, // 從螢幕頂部滑入
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight }, // 滑出到螢幕頂部
            animationSpec = tween(
                durationMillis = 250,
                easing = FastOutLinearInEasing
            )
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        // 透明背景，保留可點擊區域以關閉對話框
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = { visible = false }), // 點擊背景關閉
            color = Color.Transparent
        ) {
            // Dialog 內容區域 - 只占 60% 左右的大小
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f) // 改為占螢幕高度的 80%
                        .clickable { /* 防止點擊穿透 */ },
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                ) {
                    // 整個 Dialog 內容區域 - 不使用 statusBarsPadding，讓顏色延伸到狀態列
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = 50.dp, // 手動增加頂部空間避開狀態列
                                start = 20.dp,
                                end = 20.dp,
                                bottom = 20.dp
                            )
                    ) {
                        // Header row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "新增食材",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { visible = false }) {
                                Icon(Icons.Default.Close, contentDescription = "關閉")
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Search + Quantity in the same row (50% / 50%)
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                label = { Text("搜尋或直接輸入食材") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // Add custom query into selection (do not add immediately)
                        if (query.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    if (!selected.contains(query)) selected.add(query)
                                    // keep dialog open for multi-select
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("選取 \"$query\"")
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        // Partition filtered suggestions into favorites and others
                        val favoriteList = remember(filtered, favorites) { filtered.filter { favorites.contains(it.name) } }
                        val otherList = remember(filtered, favorites) { filtered.filterNot { favorites.contains(it.name) } }

                        // Grid of suggestions - includes headers and sections; uses remaining space
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f), // 使用剩餘的所有空間
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 20.dp) // 底部留白
                        ) {
                            if (favoriteList.isNotEmpty()) {
                                // Favorites header spans all columns
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        "最愛",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                items(favoriteList.size) { index ->
                                    val item = favoriteList[index]
                                    QuickPickCell(
                                        name = item.name,
                                        icon = item.icon,
                                        isFavorite = true,
                                        isSelected = selected.contains(item.name),
                                        onToggleFavorite = {
                                            scope.launch {
                                                fridger.com.io.presentation.settings.SettingsManager.toggleQuickFavorite(item.name)
                                            }
                                        }
                                    ) {
                                        if (selected.contains(item.name)) selected.remove(item.name) else selected.add(item.name)
                                    }
                                }
                                // Divider spacing before quick picks
                                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(8.dp)) }
                                // Quick picks header spans all columns
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        "快速選擇",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                // When there are no favorites to show, keep the old single header
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        "快速選擇",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Render non-favorite items
                            items(otherList.size) { index ->
                                val item = otherList[index]
                                QuickPickCell(
                                    name = item.name,
                                    icon = item.icon,
                                    isFavorite = favorites.contains(item.name),
                                    isSelected = selected.contains(item.name),
                                    onToggleFavorite = {
                                        scope.launch {
                                            fridger.com.io.presentation.settings.SettingsManager.toggleQuickFavorite(item.name)
                                        }
                                    }
                                ) {
                                    if (selected.contains(item.name)) selected.remove(item.name) else selected.add(item.name)
                                }
                            }
                        }

                        // Selected summary and unified Add button
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (selected.isEmpty()) "未選取項目" else "已選取 " + selected.joinToString("、"),
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (selected.isNotEmpty()) {
                                TextButton(onClick = { selected.clear() }) { Text("清除選取") }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val q = qty.ifBlank { null }
                                selected.forEach { name -> onAdd(name, q) }
                                selected.clear()
                                visible = false
                            },
                            enabled = selected.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("加入選取")
                        }
                    }
                }
            }
        }
    }

    // When visibility turns false, wait for exit animation then propagate onDismiss
    LaunchedEffect(visible) {
        if (!visible) {
            kotlinx.coroutines.delay(250) // 匹配退出動畫時間
            onDismiss()
        }
    }
}

@Composable
private fun QuickPickCell(
    name: String,
    icon: String,
    isFavorite: Boolean,
    isSelected: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 110.dp) // 增加高度以容納更大的內容
                .padding(6.dp) // 減少外層padding，為星號留出更多margin空間
        ) {
            // Heart button at top-end with margin - 右上角置頂
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp) // 減少margin，靠近邊框一點
                    .size(28.dp) // 設定固定點擊區域大小
                    .clickable(onClick = onToggleFavorite)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "移除最愛" else "加入最愛",
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(18.dp) // 稍微縮小愛心圖示
                )
            }
            
            // Center content - 往下移並放大
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 16.dp), // 調整padding，增加垂直空間
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 往下偏移並放大emoji
                Text(
                    text = icon, 
                    fontSize = 44.sp, // 從34sp大幅增加到44sp
                    modifier = Modifier.padding(top = 6.dp) // 往下偏移
                )
                Spacer(Modifier.height(12.dp)) // 增加間距
                Text(
                    text = name,
                    fontSize = 15.sp, // 稍微增大文字
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
