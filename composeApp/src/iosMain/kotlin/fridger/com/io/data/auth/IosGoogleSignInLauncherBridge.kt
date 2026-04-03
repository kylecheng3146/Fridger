package fridger.com.io.data.auth

object IosGoogleSignInLauncherBridge {
    private var launcher: (() -> Unit)? = null

    fun setLauncher(block: () -> Unit) {
        launcher = block
    }

    fun launch() {
        launcher?.invoke()
    }
}
