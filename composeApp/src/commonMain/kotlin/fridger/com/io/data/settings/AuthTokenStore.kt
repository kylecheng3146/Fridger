package fridger.com.io.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthTokenStore(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")
        val KEY_USER_ID = stringPreferencesKey("auth_user_id")
    }

    val accessToken: Flow<String> = dataStore.data.map { prefs -> prefs[KEY_ACCESS_TOKEN].orEmpty() }
    val refreshToken: Flow<String> = dataStore.data.map { prefs -> prefs[KEY_REFRESH_TOKEN].orEmpty() }
    val userId: Flow<String> = dataStore.data.map { prefs -> prefs[KEY_USER_ID].orEmpty() }

    suspend fun setTokens(
        accessToken: String,
        refreshToken: String,
        userId: String
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID] = userId
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_USER_ID)
        }
    }
}

object AuthTokenStoreProvider {
    val store: AuthTokenStore by lazy { AuthTokenStore(SharedDataStoreProvider.instance) }
}
