package fridger.com.io.presentation.shoppinglist

import fridger.shared.health.toNutritionCategory

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
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.shopping_list_add_item
import fridger.composeapp.generated.resources.shopping_list_clear_done
import fridger.composeapp.generated.resources.shopping_list_create
import fridger.composeapp.generated.resources.shopping_list_create_confirm
import fridger.composeapp.generated.resources.shopping_list_create_dialog_title
import fridger.composeapp.generated.resources.shopping_list_empty_body
import fridger.composeapp.generated.resources.shopping_list_empty_title
import fridger.composeapp.generated.resources.shopping_list_error_body
import fridger.composeapp.generated.resources.shopping_list_error_title
import fridger.composeapp.generated.resources.shopping_list_offline_banner
import fridger.composeapp.generated.resources.shopping_list_sync_queue_action
import fridger.composeapp.generated.resources.shopping_list_sync_queue_body
import fridger.composeapp.generated.resources.shopping_list_sync_queue_empty
import fridger.composeapp.generated.resources.shopping_list_sync_queue_title
import fridger.composeapp.generated.resources.shopping_list_sync_pending
import fridger.composeapp.generated.resources.shopping_list_sync_queue
import fridger.composeapp.generated.resources.shopping_list_sync_error
import fridger.composeapp.generated.resources.shopping_list_sync_retry
import fridger.composeapp.generated.resources.shopping_list_sync_action_add
import fridger.composeapp.generated.resources.shopping_list_sync_action_update
import fridger.composeapp.generated.resources.shopping_list_sync_action_delete
import fridger.composeapp.generated.resources.shopping_list_sync_action_clear
import fridger.composeapp.generated.resources.shopping_list_sync_action_time
import fridger.composeapp.generated.resources.shopping_list_title
import org.jetbrains.compose.resources.stringResource
import fridger.com.io.data.sync.ShoppingSyncActionType
import fridger.com.io.utils.epochMillisToDateString
import kotlinx.datetime.toLocalDateTime

// Import the new reusable ShoppingQuickAddTopDialog from components
import fridger.com.io.presentation.components.ShoppingQuickAddTopDialog

@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
) {
    val vm: ShoppingListViewModel = viewModel(factory = ViewModelFactoryProvider.factory)
    val state by vm.uiState.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(false) } // add item dialog (detail view)
    var showCreateListDialog by rememberSaveable { mutableStateOf(false) } // create list dialog (overview)
    var defaultListName by rememberSaveable { mutableStateOf("") }
    var pendingDeleteList by remember { mutableStateOf<fridger.com.io.data.settings.ShoppingListMeta?>(null) }
    var showSyncQueueDialog by rememberSaveable { mutableStateOf(false) }
    val suggestedCategory by DashboardShoppingIntent.category.collectAsState()
    var addCategory by remember { mutableStateOf<fridger.shared.health.NutritionCategory?>(null) }
    androidx.compose.runtime.LaunchedEffect(suggestedCategory, state.currentList) {
        if (suggestedCategory != null && state.currentList != null) {
            addCategory = suggestedCategory
            showAddDialog = true
            DashboardShoppingIntent.category.value = null
        }
    }


    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ShoppingHeader()
            Spacer(Modifier.height(MaterialTheme.spacing.small))
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.sizing.contentPaddingHorizontal)
                        .weight(1f)
            ) {
                if (suggestedCategory != null && state.currentList == null) {
                    Text("選擇或建立購物清單，再挑選要補充的${suggestedCategory!!.displayName}食材。")
                    TextButton(onClick = { DashboardShoppingIntent.category.value = null }) { Text("取消補貨") }
                }
                if (state.isOffline) {
                    OfflineSyncBanner(
                        pendingCount = state.pendingSyncCount,
                        onViewQueue = { showSyncQueueDialog = true }
                    )
                    Spacer(Modifier.height(10.dp))
                }
                if (state.currentList == null) {
                    if (state.lists.isEmpty()) {
                        ShoppingListEmptyOverview(onCreateClick = { name -> 
                            defaultListName = name
                            showCreateListDialog = true 
                        })
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "所有清單",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Button(
                                onClick = { 
                                    defaultListName = ""
                                    showCreateListDialog = true 
                                },
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(Res.string.shopping_list_create))
                            }
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
                            onClick = { addCategory = null; showAddDialog = true },
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(Res.string.shopping_list_add_item))
                        }

                        OutlinedButton(
                            onClick = { vm.clearPurchased() },
                            enabled = state.items.any { it.isChecked },
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                        ) { Text(stringResource(Res.string.shopping_list_clear_done)) }
                    }

                    Spacer(Modifier.height(12.dp))

                    when {
                        state.isLoading -> {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        state.error != null -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(Res.string.shopping_list_error_title),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = stringResource(Res.string.shopping_list_error_body),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = stringResource(Res.string.shopping_list_sync_error),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
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
                                    text = stringResource(Res.string.shopping_list_empty_title),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = stringResource(Res.string.shopping_list_empty_body),
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
                                onCheckedChange = { vm.toggleChecked(item, it) },
                                onDelete = { vm.deleteItem(item) }
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            ShoppingQuickAddTopDialog(
                suggestions = addCategory?.let { category ->
                    fridger.com.io.data.QuickAddCatalog.allNames.filter {
                        fridger.com.io.data.IngredientCategoryClassifier.classify(it).toNutritionCategory() == category
                    }
                },
                onDismiss = { showAddDialog = false },
                onAdd = { name, qty ->
                    vm.addItem(name.trim(), qty?.trim().takeUnless { it.isNullOrBlank() })
                    showAddDialog = false
                }
            )
        }

        if (showCreateListDialog) {
            CreateShoppingListDialog(
                defaultName = defaultListName,
                onDismiss = { showCreateListDialog = false },
                onConfirm = { name, date ->
                    vm.createNewList(name, date)
                    showCreateListDialog = false
                }
            )
        }

        if (showSyncQueueDialog) {
            AlertDialog(
                onDismissRequest = { showSyncQueueDialog = false },
                title = { Text(stringResource(Res.string.shopping_list_sync_queue_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(Res.string.shopping_list_sync_queue_body, state.pendingSyncCount))
                        if (state.pendingSyncActions.isEmpty()) {
                            Text(stringResource(Res.string.shopping_list_sync_queue_empty))
                        } else {
                            state.pendingSyncActions.take(6).forEach { action ->
                                val label =
                                    when (action.type) {
                                        ShoppingSyncActionType.ADD -> stringResource(Res.string.shopping_list_sync_action_add, action.itemName ?: "")
                                        ShoppingSyncActionType.UPDATE -> stringResource(Res.string.shopping_list_sync_action_update, action.itemName ?: "")
                                        ShoppingSyncActionType.DELETE -> stringResource(Res.string.shopping_list_sync_action_delete, action.itemName ?: "")
                                        ShoppingSyncActionType.CLEAR -> stringResource(Res.string.shopping_list_sync_action_clear)
                                    }
                                Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val timeLabel = epochMillisToDateString(action.createdAtEpochMillis)
                                Text(
                                    text = stringResource(Res.string.shopping_list_sync_action_time, timeLabel),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSyncQueueDialog = false }) {
                        Text(stringResource(Res.string.shopping_list_sync_queue_action))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        vm.retrySyncQueue()
                        showSyncQueueDialog = false
                    }) {
                        Text(stringResource(Res.string.shopping_list_sync_retry))
                    }
                }
            )
        }

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
private fun ShoppingListEmptyOverview(onCreateClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = "準備好要去採買了嗎？",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            text = "建立您的第一份購物清單，輕鬆記錄需要補貨的食材與日常用品，讓採買更有效率！",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(Modifier.height(40.dp))
        
        Button(
            onClick = { onCreateClick("") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.shopping_list_create), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(Modifier.height(48.dp))
        
        Text(
            text = "💡 常用清單靈感",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionChip(
                onClick = { onCreateClick("週末大採購") },
                label = { Text("週末大採購") }
            )
            SuggestionChip(
                onClick = { onCreateClick("全聯補貨") },
                label = { Text("全聯補貨") }
            )
            SuggestionChip(
                onClick = { onCreateClick("好市多必買") },
                label = { Text("好市多必買") }
            )
            SuggestionChip(
                onClick = { onCreateClick("今晚的晚餐") },
                label = { Text("今晚的晚餐") }
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
    defaultName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, date: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(defaultName) }
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
        title = { Text(stringResource(Res.string.shopping_list_create_dialog_title)) },
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
            ) { Text(stringResource(Res.string.shopping_list_create_confirm)) }
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
    fridger.com.io.presentation.components
        .AppTopTitle(title = stringResource(Res.string.shopping_list_title))
}

@Composable
private fun OfflineSyncBanner(
    pendingCount: Int,
    onViewQueue: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                text = stringResource(Res.string.shopping_list_offline_banner),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            if (pendingCount > 0) {
                Text(
                    text = stringResource(Res.string.shopping_list_sync_pending, pendingCount),
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onViewQueue) {
                Text(stringResource(Res.string.shopping_list_sync_queue))
            }
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
