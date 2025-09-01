package fridger.backend.exceptions

/** Simple auth-related exceptions to be thrown by future auth middleware/services */
sealed class AuthException(message: String? = null) : RuntimeException(message)

class UnauthorizedException(message: String? = null) : AuthException(message)

class ForbiddenException(message: String? = null) : AuthException(message)
