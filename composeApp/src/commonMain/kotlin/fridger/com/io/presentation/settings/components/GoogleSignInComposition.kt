package fridger.com.io.presentation.settings.components

import androidx.compose.runtime.staticCompositionLocalOf

data class GoogleSignInStatus(
    val isSigningIn: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

val LocalGoogleSignInLauncher = staticCompositionLocalOf<(() -> Unit)?> { null }
val LocalGoogleSignInStatus = staticCompositionLocalOf<GoogleSignInStatus?> { null }
