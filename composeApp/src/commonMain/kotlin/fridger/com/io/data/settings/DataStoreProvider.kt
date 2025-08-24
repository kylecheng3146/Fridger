package fridger.com.io.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Provides a single shared DataStore instance per process/app across all callers.
 * This prevents the Android crash: "There are multiple DataStores active for the same file".
 */
object SharedDataStoreProvider {
    // Lazily initialize exactly once per process
    val instance: DataStore<Preferences> by lazy { createDataStore() }
}
