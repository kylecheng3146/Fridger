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

data class ShoppingListUiState(
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
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val items = repository.getShoppingList()
                _uiState.value = ShoppingListUiState(items = items)
            } catch (e: Exception) {
                _uiState.value = ShoppingListUiState(items = emptyList(), error = e.message)
            }
        }
    }

    fun addItem(name: String, quantity: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                repository.addItem(name, quantity)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleChecked(id: Long, checked: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateItem(id, checked)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteItem(id)
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearPurchased() {
        viewModelScope.launch {
            try {
                repository.clearPurchasedItems()
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
