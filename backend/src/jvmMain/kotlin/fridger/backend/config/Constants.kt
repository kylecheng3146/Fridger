package fridger.backend.config

/** Centralized constants to avoid hardcoded literals dispersed across codebase. */
object ApiPaths {
    const val API_V1 = "/api/v1"
    const val AUTH = "$API_V1/auth"
    const val AUTH_GOOGLE = "/google"
    const val AUTH_REFRESH = "/refresh"
    const val AUTH_LOGOUT = "/logout"
    const val HEALTH = "/health"
    const val ROOT = "/"
}

object JwtClaims {
    const val USER_ID = "userId"
    const val TYPE = "type"
    const val JTI = "jti" // optional explicit claim reference (library also sets standard jti header via withJWTId)
}

object TokenTypes {
    const val ACCESS = "access"
}
