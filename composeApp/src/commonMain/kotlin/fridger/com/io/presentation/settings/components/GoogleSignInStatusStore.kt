package fridger.com.io.presentation.settings.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object GoogleSignInStatusStore {
    private val _status = MutableStateFlow(GoogleSignInStatus())
    val status: StateFlow<GoogleSignInStatus> = _status

    fun setSigningIn() {
        _status.value = GoogleSignInStatus(isSigningIn = true)
    }

    fun setError(message: String?) {
        _status.value = GoogleSignInStatus(errorMessage = message ?: "登入失敗")
    }

    fun setSuccess(message: String? = null) {
        _status.value = GoogleSignInStatus(successMessage = message ?: "登入成功")
    }

    fun clear() {
        _status.update { GoogleSignInStatus() }
    }
}
