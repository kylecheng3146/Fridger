package fridger.com.io.presentation.shoppinglist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fridger.com.io.data.repository.ShoppingListItem
import fridger.com.io.presentation.ViewModelFactoryProvider
import fridger.com.io.ui.theme.sizing
import fridger.com.io.ui.theme.spacing
import kotlinx.datetime.toLocalDateTime
import fridger.com.io.utils.epochMillisToDateString

// Import the new reusable ShoppingQuickAddTopDialog from components
import fridger.com.io.presentation.components.ShoppingQuickAddTopDialog

@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
) {
    val vm: ShoppingListViewModel = viewModel(factory = ViewModelFactoryProvider.factory)
    val state by vm.uiState.collectAsState()

    // Dialog flags
    var showAddDialog by rememberSaveable { mutableStateOf(false) } // add item dialog (detail view)
    var showCreateListDialog by rememberSaveable { mutableStateOf(false) } // create list dialog (overview)

    // Pending delete list confirmation
    var pendingDeleteList by remember { mutableStateOf<fridger.com.io.data.settings.ShoppingListMeta?>(null) }

    // 使用 Box 作為最外層容器，讓 dialog 可以覆蓋整個螢幕
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header - align with Home header style
            ShoppingHeader()
            Spacer(Modifier.height(MaterialTheme.spacing.small))

            // Content padding aligned to Home's horizontal padding
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                        .weight(1f)
            ) {
                if (state.currentList == null) {
                    // Overview: show list of shopping lists
                    Button(
                        onClick = { showCreateListDialog = true },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("新增購物清單")
                    }

                    Spacer(Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.lists.size) { index ->
                            val list = state.lists[index]
                            ShoppingListCard(
                                name = list.name,
                                date = list.date,
                                onClick = { vm.openList(list) },
                                onDelete = { pendingDeleteList = list }
                            )
                        }
                    }
                } else {
                    // Detail: items of selected list
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { vm.backToOverview() }) { Text("← 返回清單") }
                        Spacer(Modifier.width(12.dp))
                        state.currentList?.let { cur ->
                            Text(
                                text = "${cur.name}  ·  ${cur.date}",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Actions
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { showAddDialog = true },
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("新增食材")
                        }

                        OutlinedButton(
                            onClick = { vm.clearPurchased() },
                            enabled = state.items.any { it.isChecked },
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                        ) { Text("清除清單") }
                    }

                    Spacer(Modifier.height(12.dp))

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
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
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
            }
        }

        // Dialogs overlay
        if (showAddDialog) {
            // Use the imported ShoppingQuickAddTopDialog
            ShoppingQuickAddTopDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, qty ->
                    vm.addItem(name.trim(), qty?.trim().takeUnless { it.isNullOrBlank() })
                    showAddDialog = false
                }
            )
        }

        if (showCreateListDialog) {
            CreateShoppingListDialog(
                onDismiss = { showCreateListDialog = false },
                onConfirm = { name, date ->
                    vm.createNewList(name, date)
                    showCreateListDialog = false
                }
            )
        }

        // Confirm delete shopping list
        pendingDeleteList?.let { list ->
            AlertDialog(
                onDismissRequest = { pendingDeleteList = null },
                title = { Text("刪除清單") },
                text = { Text("確定要刪除 \"${list.name}\"？此操作將移除清單內所有項目。") },
                confirmButton = {
                    TextButton(onClick = {
                        vm.deleteList(list.id)
                        pendingDeleteList = null
                    }) { Text("刪除", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteList = null }) { Text("取消") }
                }
            )
        }
    }
}

@Composable
private fun ShoppingListCard(
    name: String,
    date: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(onClick = onClick, shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simple leading icon
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(text = date, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "刪除清單", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateShoppingListDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, date: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(todayDisplay()) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds()
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增購物清單") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("清單名稱") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Box {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { /* readonly */ },
                        readOnly = true,
                        label = { Text("日期") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier.matchParentSize().clickable { showDatePicker = true }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name.ifBlank { "未命名清單" }, date.ifBlank { todayDisplay() })
                },
                enabled = name.isNotBlank() || date.isNotBlank()
            ) { Text("建立") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = epochMillisToDateString(it)
                    }
                    showDatePicker = false
                }) { Text("確認") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) { DatePicker(state = datePickerState) }
    }
}

private fun todayDisplay(): String {
    val now =
        kotlinx.datetime.Clock.System
            .now()
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            .date
    val y = now.year
    val m = now.monthNumber.toString().padStart(2, '0')
    val d = now.dayOfMonth.toString().padStart(2, '0')
    return "$d/$m/$y"
}

@Composable
private fun ShoppingHeader() {
    Box(
        modifier =
            Modifier
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange,
                colors =
                    CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text =
                        buildString {
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
