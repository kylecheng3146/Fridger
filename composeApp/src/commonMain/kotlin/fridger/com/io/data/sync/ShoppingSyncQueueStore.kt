package fridger.com.io.data.sync

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import fridger.com.io.data.settings.SharedDataStoreProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class ShoppingSyncQueueStore(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val KEY_PENDING_SYNC = stringPreferencesKey("shopping_sync_pending")
        val ITEM_DELIMITER = "|"
        val FIELD_DELIMITER = ":"
    }

    val pending: Flow<List<PendingSyncAction>> =
        dataStore.data
            .catch { exception ->
                if (exception is Exception) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val raw = preferences[KEY_PENDING_SYNC] ?: return@map emptyList()
                if (raw.isBlank()) return@map emptyList()
                raw.split(ITEM_DELIMITER)
                    .mapNotNull { item ->
                        val parts = item.split(FIELD_DELIMITER)
                        if (parts.size < 7) return@mapNotNull null
                        val type = runCatching { ShoppingSyncActionType.valueOf(parts[1]) }.getOrNull() ?: return@mapNotNull null
                        PendingSyncAction(
                            id = parts[0],
                            type = type,
                            itemId = parts[2].ifBlank { null },
                            itemName = parts[3].ifBlank { null },
                            quantity = parts[4].ifBlank { null },
                            isChecked = parts[5].toBooleanStrictOrNull(),
                            createdAtEpochMillis = parts[6].toLongOrNull() ?: 0L
                        )
                    }
            }

    suspend fun setPending(list: List<PendingSyncAction>) {
        val raw =
            list.joinToString(ITEM_DELIMITER) { action ->
                listOf(
                    action.id,
                    action.type.name,
                    action.itemId.orEmpty(),
                    action.itemName.orEmpty(),
                    action.quantity.orEmpty(),
                    action.isChecked?.toString().orEmpty(),
                    action.createdAtEpochMillis.toString()
                ).joinToString(FIELD_DELIMITER)
            }
        dataStore.edit { prefs ->
            prefs[KEY_PENDING_SYNC] = raw
        }
    }
}

object ShoppingSyncQueueStoreProvider {
    val store: ShoppingSyncQueueStore by lazy {
        ShoppingSyncQueueStore(SharedDataStoreProvider.instance)
    }
}
