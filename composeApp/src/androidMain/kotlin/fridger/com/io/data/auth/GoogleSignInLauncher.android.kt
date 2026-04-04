package fridger.com.io.data.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleSignInLauncher(
    activity: ComponentActivity,
    private val onToken: (String) -> Unit,
    private val onError: (Throwable) -> Unit,
    private val onCancel: () -> Unit = {}
) {
    private val signInClient: GoogleSignInClient
    private val launcher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleResult(result)
        }

    init {
        val options =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(activity.getString(fridger.com.io.R.string.google_web_client_id))
                .build()
        signInClient = GoogleSignIn.getClient(activity, options)
    }

    fun launch() {
        launcher.launch(signInClient.signInIntent)
    }

    private fun handleResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            onCancel()
            return
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                onError(IllegalStateException("Missing idToken"))
                return
            }
            onToken(idToken)
        } catch (ex: Exception) {
            onError(ex)
        }
    }
}
