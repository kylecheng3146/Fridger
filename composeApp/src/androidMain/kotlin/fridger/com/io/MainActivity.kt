package fridger.com.io

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import fridger.com.io.data.auth.GoogleSignInHandler
import fridger.com.io.data.auth.GoogleSignInLauncher
import fridger.com.io.presentation.settings.components.LocalGoogleSignInLauncher
import fridger.com.io.presentation.settings.components.LocalGoogleSignInStatus
import fridger.com.io.presentation.settings.components.GoogleSignInStatus
import fridger.com.io.presentation.settings.components.GoogleSignInStatusStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var signInLauncher: GoogleSignInLauncher? = null
    private var signInStatus by mutableStateOf(GoogleSignInStatus())

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        signInLauncher =
            GoogleSignInLauncher(
                activity = this,
                onToken = { idToken ->
                    CoroutineScope(Dispatchers.Main).launch {
                        val result = GoogleSignInHandler().handleIdToken(idToken)
                        if (result.isSuccess) {
                            GoogleSignInStatusStore.setSuccess("登入成功")
                        } else {
                            GoogleSignInStatusStore.setError(result.exceptionOrNull()?.message)
                        }
                        signInStatus = GoogleSignInStatusStore.status.value
                    }
                },
                onError = { ex ->
                    GoogleSignInStatusStore.setError(ex.message)
                    signInStatus = GoogleSignInStatusStore.status.value
                }
            )

        setContent {
            CompositionLocalProvider(
                LocalGoogleSignInLauncher provides { launchGoogleSignIn() },
                LocalGoogleSignInStatus provides signInStatus
            ) {
                App()
            }
        }
    }

    fun launchGoogleSignIn() {
        GoogleSignInStatusStore.setSigningIn()
        signInStatus = GoogleSignInStatusStore.status.value
        signInLauncher?.launch()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
