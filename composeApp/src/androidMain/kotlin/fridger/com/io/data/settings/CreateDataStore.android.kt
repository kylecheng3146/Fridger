package fridger.com.io.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import fridger.com.io.applicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private const val SETTINGS_PREFERENCES = "fridger_settings.preferences_pb"

/**
 * Creates a DataStore instance.
 *
 * @return A DataStore instance.
 */
actual fun createDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
        corruptionHandler = null,
        migrations = emptyList(),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { applicationContext.filesDir.resolve(SETTINGS_PREFERENCES) }
    )
