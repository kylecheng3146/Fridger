package fridger.com.io.data.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GoogleSignInBridge {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun handleIdToken(idToken: String, onComplete: (Boolean, String?) -> Unit) {
        scope.launch {
            val result = GoogleSignInHandler().handleIdToken(idToken)
            if (result.isSuccess) {
                onComplete(true, null)
            } else {
                onComplete(false, result.exceptionOrNull()?.message)
            }
        }
    }
}
