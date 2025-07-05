package fridger.com.io.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val todayExpiringItems: List<ExpiringItem> = emptyList(),
    val weekExpiringItems: List<ExpiringItem> = emptyList(),
    val fridgeCapacityPercentage: Float = 0f,
    val refrigeratedItems: List<RefrigeratedItem> = emptyList(),
    val showAddNewItemDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ExpiringItem(
    val id: String,
    val name: String,
    val icon: String,
    val count: Int,
    val expiryDate: String
)

data class RefrigeratedItem(
    val id: String,
    val name: String,
    val icon: String,
    val quantity: String,
    val daysUntilExpiry: Int,
    val hasWarning: Boolean = false
)

class HomeViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadHomeData()
    }
    
    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // TODO: Replace with actual repository calls
                // For now, using mock data
                val mockState = HomeUiState(
                    todayExpiringItems = listOf(
                        ExpiringItem(
                            id = "1",
                            name = "番茄",
                            icon = "🍅",
                            count = 2,
                            expiryDate = "今天"
                        )
                    ),
                    weekExpiringItems = listOf(
                        ExpiringItem(
                            id = "2",
                            name = "肉類",
                            icon = "🥩",
                            count = 5,
                            expiryDate = "本週內"
                        )
                    ),
                    fridgeCapacityPercentage = 0.6f,
                    refrigeratedItems = listOf(
                        RefrigeratedItem(
                            id = "3",
                            name = "雞蛋",
                            icon = "🐔",
                            quantity = "x10",
                            daysUntilExpiry = 7
                        ),
                        RefrigeratedItem(
                            id = "4",
                            name = "青江菜",
                            icon = "🥬",
                            quantity = "x1 束",
                            daysUntilExpiry = 8
                        ),
                        RefrigeratedItem(
                            id = "5",
                            name = "鮮奶",
                            icon = "🥛",
                            quantity = "x1 瓶",
                            daysUntilExpiry = 6,
                            hasWarning = true
                        )
                    ),
                    isLoading = false
                )
                
                _uiState.value = mockState
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun onAddNewItemClick() {
        _uiState.value = _uiState.value.copy(showAddNewItemDialog = true)
    }

    fun onDismissDialog() {
        _uiState.value = _uiState.value.copy(showAddNewItemDialog = false)
    }

    fun onAddNewItemConfirm(name: String, quantity: String, expiryDate: String) {
        // TODO: Add item to repository
        _uiState.value = _uiState.value.copy(showAddNewItemDialog = false)
    }
    
    fun onItemClick(itemId: String) {
        // TODO: Navigate to item detail screen
    }
    
    fun refresh() {
        loadHomeData()
    }
}
