package fridger.com.io.data.user

import fridger.com.io.data.settings.AuthTokenStoreProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object UserSessionManager {
    private val store = AuthTokenStoreProvider.store
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Direct DataStore flows — always reflect persisted state (for Compose observation)
    val accessToken: Flow<String> = store.accessToken
    val refreshToken: Flow<String> = store.refreshToken
    val userId: Flow<String> = store.userId

    // In-memory cache for synchronous reads (e.g. ViewModel HTTP calls)
    private val _cachedAccessToken = MutableStateFlow("")
    private val _cachedUserId = MutableStateFlow("")

    val cachedAccessToken: String get() = _cachedAccessToken.value
    val cachedUserId: String get() = _cachedUserId.value

    init {
        scope.launch { store.accessToken.collect { _cachedAccessToken.value = it } }
        scope.launch { store.userId.collect { _cachedUserId.value = it } }
    }

    suspend fun setTokens(accessToken: String, refreshToken: String, userId: String) {
        store.setTokens(accessToken, refreshToken, userId)
    }

    suspend fun clear() {
        store.clear()
    }
}
