package fridger.backend.config

/** Centralized error message constants to avoid hardcoded strings. */
object ErrorMessages {
    const val INVALID_REFRESH_TOKEN = "Invalid refresh token"
    const val INVALID_JWT_FORMAT = "Invalid JWT format"
    const val MISSING_KID = "Missing kid"
    const val UNSUPPORTED_ALG_PREFIX = "Unsupported alg "
    const val PUBLIC_KEY_NOT_CACHED = "Public key not cached yet"
    const val BAD_SIGNATURE = "Bad signature"
    const val EXP_MISSING = "exp missing"
    const val TOKEN_EXPIRED = "Token expired"
    const val AUD_MISSING = "aud missing"
    const val AUD_MISMATCH = "aud mismatch"
    const val SUB_MISSING = "sub missing"
    const val EMAIL_MISSING = "email missing"
    const val MISSING_JWT_SECRET = "JWT_SECRET not set"
}
