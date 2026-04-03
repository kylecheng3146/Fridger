package fridger.com.io.data.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

enum class ShoppingSyncActionType {
    ADD,
    UPDATE,
    DELETE,
    CLEAR
}

data class PendingSyncAction(
    val id: String,
    val type: ShoppingSyncActionType,
    val itemId: String? = null,
    val itemName: String? = null,
    val quantity: String? = null,
    val isChecked: Boolean? = null,
    val createdAtEpochMillis: Long
)

object ShoppingSyncQueue {
    private val _pending = MutableStateFlow<List<PendingSyncAction>>(emptyList())
    val pending: StateFlow<List<PendingSyncAction>> = _pending.asStateFlow()
    private val store = ShoppingSyncQueueStoreProvider.store

    suspend fun loadFromStore() {
        val stored = store.pending.first()
        if (stored.isNotEmpty()) {
            _pending.value = stored
        }
    }

    fun enqueue(
        type: ShoppingSyncActionType,
        itemId: String? = null,
        itemName: String? = null,
        quantity: String? = null,
        isChecked: Boolean? = null
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        val action = PendingSyncAction(
            id = "${now}-${_pending.value.size}",
            type = type,
            itemId = itemId,
            itemName = itemName,
            quantity = quantity,
            isChecked = isChecked,
            createdAtEpochMillis = now
        )
        _pending.value = _pending.value + action
    }

    fun clear() {
        _pending.value = emptyList()
    }

    fun setPending(list: List<PendingSyncAction>) {
        _pending.value = list
    }

    suspend fun persist() {
        store.setPending(_pending.value)
    }
}
