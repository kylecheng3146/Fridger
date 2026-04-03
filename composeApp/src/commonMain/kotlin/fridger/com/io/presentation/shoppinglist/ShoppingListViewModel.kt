package fridger.com.io.presentation.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fridger.com.io.data.repository.ShoppingListItem
import fridger.com.io.data.remote.ShoppingListApiService
import fridger.com.io.data.repository.ShoppingListRepository
import fridger.com.io.data.repository.ShoppingListRepositoryImpl
import fridger.com.io.data.settings.ShoppingListMeta
import fridger.com.io.data.settings.ShoppingListsManager
import fridger.com.io.data.user.UserSessionProvider
import fridger.com.io.data.user.AppUserSessionProvider
import fridger.com.io.data.connectivity.ConnectivityMonitorProvider
import fridger.com.io.data.sync.PendingSyncAction
import fridger.com.io.data.sync.ShoppingSyncActionType
import fridger.com.io.data.sync.ShoppingSyncProcessor
import fridger.com.io.data.sync.NoopShoppingSyncProcessor
import fridger.com.io.data.sync.ShoppingSyncQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class ShoppingListUiState(
    val lists: List<ShoppingListMeta> = emptyList(),
    val currentList: ShoppingListMeta? = null,
    val items: List<ShoppingListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false,
    val pendingSyncCount: Int = 0,
    val pendingSyncActions: List<PendingSyncAction> = emptyList(),
    val syncRetryAttempt: Int = 0
)

class ShoppingListViewModel(
    private val repository: ShoppingListRepository = ShoppingListRepositoryImpl(),
    private val syncProcessor: ShoppingSyncProcessor = NoopShoppingSyncProcessor(),
    private val listApi: ShoppingListApiService = ShoppingListApiService(),
    private val sessionProvider: UserSessionProvider = AppUserSessionProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()
    private var syncRetryJob: kotlinx.coroutines.Job? = null

    init {
        observeLists()
        observeConnectivity()
        observeSyncQueue()
        loadSyncQueue()
    }

    private fun observeLists() {
        viewModelScope.launch {
            ShoppingListsManager.lists.collect { lists ->
                _uiState.value = _uiState.value.copy(lists = lists)
                // If a list is selected, refresh its items; otherwise keep overview
                _uiState.value.currentList?.let { loadItems(it.id) }
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            ConnectivityMonitorProvider.monitor.isOnline.collect { online ->
                _uiState.value = _uiState.value.copy(isOffline = !online)
                if (online) {
                    triggerBackgroundSync()
                }
            }
        }
    }

    private fun observeSyncQueue() {
        viewModelScope.launch {
            ShoppingSyncQueue.pending.collect { pending ->
                _uiState.value =
                    _uiState.value.copy(
                        pendingSyncCount = pending.size,
                        pendingSyncActions = pending
                    )
                triggerBackgroundSync()
            }
        }
    }

    private fun loadSyncQueue() {
        viewModelScope.launch {
            ShoppingSyncQueue.loadFromStore()
        }
    }

    fun openList(meta: ShoppingListMeta) {
        _uiState.value = _uiState.value.copy(currentList = meta)
        ShoppingListsManager.currentList = meta
        loadItems(meta.id)
    }

    fun backToOverview() {
        _uiState.value = _uiState.value.copy(currentList = null, items = emptyList(), error = null)
        ShoppingListsManager.currentList = null
    }

    private fun loadItems(listId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val items = repository.getShoppingList(listId)
                _uiState.value = _uiState.value.copy(items = items, isLoading = false, error = null)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(items = emptyList(), isLoading = false, error = e.message)
            }
        }
    }

    fun addItem(
        name: String,
        quantity: String?
    ) {
        val list = _uiState.value.currentList ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                repository.addItem(name, quantity, list.id)
                val newItemId = repository.getLastItemId(list.id)?.toString()
                loadItems(list.id)
                if (_uiState.value.isOffline) {
                    ShoppingSyncQueue.enqueue(
                        ShoppingSyncActionType.ADD,
                        itemId = newItemId,
                        itemName = name,
                        quantity = quantity,
                        isChecked = false
                    )
                    ShoppingSyncQueue.persist()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleChecked(
        item: ShoppingListItem,
        checked: Boolean
    ) {
        val list = _uiState.value.currentList ?: return
        viewModelScope.launch {
            try {
                repository.updateItem(item.id, checked)
                loadItems(list.id)
                if (_uiState.value.isOffline) {
                    ShoppingSyncQueue.enqueue(
                        ShoppingSyncActionType.UPDATE,
                        itemId = item.id.toString(),
                        itemName = item.name,
                        quantity = item.quantity,
                        isChecked = checked
                    )
                    ShoppingSyncQueue.persist()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteItem(item: ShoppingListItem) {
        val list = _uiState.value.currentList ?: return
        viewModelScope.launch {
            try {
                repository.deleteItem(item.id)
                loadItems(list.id)
                if (_uiState.value.isOffline) {
                    ShoppingSyncQueue.enqueue(
                        ShoppingSyncActionType.DELETE,
                        itemId = item.id.toString(),
                        itemName = item.name
                    )
                    ShoppingSyncQueue.persist()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearPurchased() {
        val list = _uiState.value.currentList ?: return
        viewModelScope.launch {
            try {
                repository.clearPurchasedItems(list.id)
                loadItems(list.id)
                if (_uiState.value.isOffline) {
                    ShoppingSyncQueue.enqueue(ShoppingSyncActionType.CLEAR)
                    ShoppingSyncQueue.persist()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearSyncQueue() {
        viewModelScope.launch {
            ShoppingSyncQueue.clear()
            ShoppingSyncQueue.persist()
        }
    }

    fun retrySyncQueue() {
        viewModelScope.launch {
            if (_uiState.value.isOffline) return@launch
            val pending = _uiState.value.pendingSyncActions
            if (pending.isEmpty()) return@launch

            val result = syncProcessor.process(pending)
            result.fold(
                onSuccess = { outcome ->
                    if (outcome.failedActionIds.isEmpty()) {
                        ShoppingSyncQueue.clear()
                        ShoppingSyncQueue.persist()
                        _uiState.value = _uiState.value.copy(syncRetryAttempt = 0)
                    } else {
                        val failed = pending.filter { outcome.failedActionIds.contains(it.id) }
                        ShoppingSyncQueue.setPending(failed)
                        ShoppingSyncQueue.persist()
                        _uiState.value = _uiState.value.copy(syncRetryAttempt = 0)
                    }
                },
                onFailure = { ex ->
                    _uiState.value = _uiState.value.copy(
                        error = ex.message,
                        syncRetryAttempt = _uiState.value.syncRetryAttempt + 1
                    )
                    scheduleRetry()
                }
            )
        }
    }

    private fun scheduleRetry() {
        syncRetryJob?.cancel()
        val attempt = _uiState.value.syncRetryAttempt.coerceAtMost(5)
        val delayMs = (1000L * (1 shl attempt)).coerceAtMost(30_000L)
        syncRetryJob = viewModelScope.launch {
            kotlinx.coroutines.delay(delayMs)
            retrySyncQueue()
        }
    }

    private fun triggerBackgroundSync() {
        viewModelScope.launch {
            if (!_uiState.value.isOffline && _uiState.value.pendingSyncActions.isNotEmpty()) {
                retrySyncQueue()
            }
        }
    }

    fun createNewList(
        name: String,
        date: String
    ) {
        viewModelScope.launch {
            val id = generateId()
            val meta = ShoppingListMeta(id, name, date)
            ShoppingListsManager.addList(meta)
            // Immediately open the newly created list
            openList(meta)
            syncListMeta(meta)
        }
    }

    private suspend fun syncListMeta(meta: ShoppingListMeta) {
        if (_uiState.value.isOffline) return
        val token = sessionProvider.accessToken()
        if (token.isBlank()) return
        runCatching {
            listApi.createList(meta.id, meta.name, meta.date, token)
        }.onFailure { ex ->
            _uiState.value = _uiState.value.copy(error = ex.message)
        }
    }

    private fun generateId(): String =
        Clock.System
            .now()
            .toEpochMilliseconds()
            .toString()

    fun deleteList(listId: String) {
        viewModelScope.launch {
            try {
                // Delete all items belonging to this list from DB
                repository.deleteItemsByList(listId)
                // Remove the list entry from DataStore
                ShoppingListsManager.removeList(listId)
                syncDeleteList(listId)
                // If we were viewing this list, go back to overview
                val current = _uiState.value.currentList
                if (current?.id == listId) {
                    backToOverview()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private suspend fun syncDeleteList(listId: String) {
        if (_uiState.value.isOffline) return
        val token = sessionProvider.accessToken()
        if (token.isBlank()) return
        runCatching {
            listApi.deleteList(listId, token)
        }.onFailure { ex ->
            _uiState.value = _uiState.value.copy(error = ex.message)
        }
    }
}
