package fridger.com.io.data.user

fun interface UserSessionProvider {
    fun userId(): String
    fun accessToken(): String = ""
}

object DemoUserSessionProvider : UserSessionProvider {
    private const val DEMO_USER_ID = "00000000-0000-0000-0000-000000000000"

    override fun userId(): String = DEMO_USER_ID
    override fun accessToken(): String = ""
}

object AppUserSessionProvider : UserSessionProvider {
    override fun userId(): String = UserSessionManager.cachedUserId.ifBlank { DemoUserSessionProvider.userId() }
    override fun accessToken(): String = UserSessionManager.cachedAccessToken
}
