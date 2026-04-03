package fridger.com.io.data.user

import fridger.com.io.data.settings.AuthTokenStoreProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object UserSessionManager {
    private val store = AuthTokenStoreProvider.store
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _accessToken = MutableStateFlow("")
    val accessToken: StateFlow<String> = _accessToken

    private val _refreshToken = MutableStateFlow("")
    val refreshToken: StateFlow<String> = _refreshToken

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId

    init {
        scope.launch {
            store.accessToken.collect { _accessToken.value = it }
        }
        scope.launch {
            store.refreshToken.collect { _refreshToken.value = it }
        }
        scope.launch {
            store.userId.collect { _userId.value = it }
        }
    }

    suspend fun setTokens(accessToken: String, refreshToken: String, userId: String) {
        store.setTokens(accessToken, refreshToken, userId)
    }

    suspend fun clear() {
        store.clear()
    }
}
