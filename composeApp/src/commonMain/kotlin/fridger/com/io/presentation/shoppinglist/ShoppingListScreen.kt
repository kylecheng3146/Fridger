package fridger.com.io.presentation.shoppinglist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fridger.com.io.data.repository.ShoppingListItem

@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val vm: ShoppingListViewModel = viewModel()
    val state by vm.uiState.collectAsState()

    // Add row replaced by quick-add dialog trigger
    var showAddDialog by remember { mutableStateOf(false) }

    // 使用 Box 作為最外層容器，讓 dialog 可以覆蓋整個螢幕
    Box(modifier = modifier.fillMaxSize()) {
        // 主要內容
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "採買清單",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                onBack?.let { back ->
                    TextButton(onClick = back) { Text("返回") }
                }
            }
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("新增品項")
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
                Text("清除已購項目")
            }

            Spacer(Modifier.height(12.dp))

            // List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
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

    var query by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") } // optional quantity

    val filtered = remember(query) {
        if (query.isBlank()) allSuggestions else allSuggestions.filter {
            it.name.contains(
                query,
                ignoreCase = true
            )
        }
    }

    // Internal visibility to animate enter from top and exit to top
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true // 立即開始動畫
    }

    // 完全覆蓋整個螢幕的 dialog
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight }, // 從螢幕頂部滑入
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 300,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        ),
        exit = androidx.compose.animation.slideOutVertically(
            targetOffsetY = { fullHeight -> -fullHeight }, // 滑出到螢幕頂部
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 250,
                easing = androidx.compose.animation.core.FastOutLinearInEasing
            )
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        // 半透明背景遮罩
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = { visible = false }), // 點擊背景關閉
            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
        ) {
            // Dialog 內容區域 - 只占 60% 左右的大小
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f) // 改為占螢幕高度的 80%
                        .clickable { /* 防止點擊穿透 */ },
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
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
                                "新增品項",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { visible = false }) {
                                Icon(Icons.Default.Close, contentDescription = "關閉")
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Search field
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("搜尋或直接輸入品項") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        // Optional quantity quick input
                        OutlinedTextField(
                            value = qty,
                            onValueChange = { qty = it },
                            label = { Text("數量(選填)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(20.dp))

                        // Action to add custom query
                        if (query.isNotBlank()) {
                            Button(
                                onClick = { onAdd(query, qty.ifBlank { null }); visible = false },
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("新增 \"$query\"")
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        // Grid title
                        Text(
                            "快速選擇",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))

                        // Grid of suggestions - 使用剩餘空間
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f), // 使用剩餘的所有空間
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                                12.dp
                            ),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                                12.dp
                            ),
                            contentPadding = PaddingValues(bottom = 20.dp) // 底部留白
                        ) {
                            items(filtered.size) { index ->
                                val item = filtered[index]
                                QuickPickCell(name = item.name, icon = item.icon) {
                                    onAdd(item.name, qty.ifBlank { null }); visible = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // When visibility turns false, wait for exit animation then propagate onDismiss
    androidx.compose.runtime.LaunchedEffect(visible) {
        if (!visible) {
            kotlinx.coroutines.delay(250) // 匹配退出動畫時間
            onDismiss()
        }
    }
}

@Composable
private fun QuickPickCell(name: String, icon: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp)
                .heightIn(min = 76.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 26.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
