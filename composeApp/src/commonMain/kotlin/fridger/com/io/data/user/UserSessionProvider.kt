package fridger.com.io.data.user

fun interface UserSessionProvider {
    fun userId(): String
}

object DemoUserSessionProvider : UserSessionProvider {
    private const val DEMO_USER_ID = "00000000-0000-0000-0000-000000000000"
    override fun userId(): String = DEMO_USER_ID
}
