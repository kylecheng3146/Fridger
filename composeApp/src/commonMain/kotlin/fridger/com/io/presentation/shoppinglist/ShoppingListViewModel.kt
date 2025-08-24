package fridger.com.io.presentation.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fridger.com.io.data.repository.ShoppingListItem
import fridger.com.io.data.repository.ShoppingListRepository
import fridger.com.io.data.repository.ShoppingListRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import fridger.com.io.data.settings.ShoppingListMeta
import fridger.com.io.data.settings.ShoppingListsManager
import kotlinx.coroutines.flow.collect

data class ShoppingListUiState(
    val lists: List<ShoppingListMeta> = emptyList(),
    val currentList: ShoppingListMeta? = null,
    val items: List<ShoppingListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ShoppingListViewModel(
    private val repository: ShoppingListRepository = ShoppingListRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        observeLists()
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

    fun openList(meta: ShoppingListMeta) {
        _uiState.value = _uiState.value.copy(currentList = meta)
        loadItems(meta.id)
    }

    fun backToOverview() {
        _uiState.value = _uiState.value.copy(currentList = null, items = emptyList(), error = null)
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

    fun addItem(name: String, quantity: String?) {
        val list = _uiState.value.currentList ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                repository.addItem(name, quantity, list.id)
                loadItems(list.id)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleChecked(id: Long, checked: Boolean) {
        val list = _uiState.value.currentList ?: return
        viewModelScope.launch {
            try {
                repository.updateItem(id, checked)
                loadItems(list.id)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteItem(id: Long) {
        val list = _uiState.value.currentList ?: return
        viewModelScope.launch {
            try {
                repository.deleteItem(id)
                loadItems(list.id)
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
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun createNewList(name: String, date: String) {
        viewModelScope.launch {
            val id = generateId()
            ShoppingListsManager.addList(ShoppingListMeta(id, name, date))
            // Immediately open the newly created list
            openList(ShoppingListMeta(id, name, date))
        }
    }

    private fun generateId(): String = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString()

    fun deleteList(listId: String) {
        viewModelScope.launch {
            try {
                // Delete all items belonging to this list from DB
                repository.deleteItemsByList(listId)
                // Remove the list entry from DataStore
                ShoppingListsManager.removeList(listId)
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
}
