package fridger.com.io.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Simple metadata for a shopping list
data class ShoppingListMeta(
    val id: String,
    val name: String,
    val date: String // display date, e.g., yyyy-MM-dd or localized
)

/**
 * Minimal registry of shopping lists backed by Preferences DataStore.
 * Stored as a single string where each record is encoded as id|name|date and separated by \n.
 */
class ShoppingListsStore(
    private val dataStore: DataStore<Preferences>
) {
    private val KEY = stringPreferencesKey("shopping_lists_registry")

    val lists: Flow<List<ShoppingListMeta>> =
        dataStore.data.map { prefs ->
            prefs[KEY]
                ?.lines()
                ?.filter { it.isNotBlank() }
                ?.mapNotNull { line ->
                    val parts = line.split("|")
                    if (parts.size >= 3) ShoppingListMeta(parts[0], parts[1], parts[2]) else null
                }
                ?: emptyList()
        }

    suspend fun addList(meta: ShoppingListMeta) {
        dataStore.edit { prefs ->
            val current = prefs[KEY]?.lines()?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
            // avoid duplicate ids
            current.removeAll { it.startsWith(meta.id + "|") }
            current.add(listToLine(meta))
            prefs[KEY] = current.joinToString("\n")
        }
    }

    suspend fun removeList(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY]?.lines()?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
            current.removeAll { it.startsWith(id + "|") }
            prefs[KEY] = current.joinToString("\n")
        }
    }

    private fun listToLine(meta: ShoppingListMeta): String = listOf(meta.id, meta.name, meta.date).joinToString("|")
}

object ShoppingListsManager {
    private val store by lazy { ShoppingListsStore(SharedDataStoreProvider.instance) }
    val lists: Flow<List<ShoppingListMeta>> = store.lists

    suspend fun addList(meta: ShoppingListMeta) = store.addList(meta)

    suspend fun removeList(id: String) = store.removeList(id)
}
