package fridger.com.io.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.io.File

actual fun createDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            val userHome = System.getProperty("user.home")
            val appDataDir = File(userHome, ".fridger")
            if (!appDataDir.exists()) {
                appDataDir.mkdirs()
            }
            File(appDataDir, "settings.preferences_pb").absolutePath.toPath()
        }
    )
}
