package fridger.backend.config

import fridger.backend.config.ErrorMessages.MISSING_JWT_SECRET
import io.ktor.server.application.Application
import io.ktor.util.AttributeKey

/**
 * Central application configuration loaded from environment variables (or their defaults) once at startup.
 * Enforces type-safety versus ad‑hoc System.getenv lookups scattered across code.
 */
data class AppConfig(
    val port: Int,
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String,
    val jwtSecret: String,
    val jwtIssuer: String,
    val googleClientIds: Set<String>,
    val jwksUrl: String,
    val accessTokenMinutes: Int,
    val refreshTokenDays: Int,
    val refreshTokenBytes: Int,
    val jwksRefreshIntervalHours: Int
)

object EnvKeys {
    const val PORT = "PORT"
    const val DB_URL = "DB_URL"
    const val DB_USER = "DB_USER"
    const val DB_PASSWORD = "DB_PASSWORD"
    const val JWT_SECRET = "JWT_SECRET"
    const val JWT_ISSUER = "JWT_ISSUER"
    const val GOOGLE_CLIENT_ID = "GOOGLE_CLIENT_ID" // comma separated list
    const val ACCESS_TOKEN_MINUTES = "ACCESS_TOKEN_MINUTES"
    const val REFRESH_TOKEN_DAYS = "REFRESH_TOKEN_DAYS"
    const val REFRESH_TOKEN_BYTES = "REFRESH_TOKEN_BYTES"
    const val JWKS_REFRESH_INTERVAL_HOURS = "JWKS_REFRESH_INTERVAL_HOURS"
    const val JWKS_URL = "JWKS_URL"
}

object Defaults {
    const val PORT = 8080
    const val DB_URL = "jdbc:postgresql://localhost:5432/fridger"
    const val DB_USER = "postgres"
    const val DB_PASSWORD = "postgres"
    const val JWT_ISSUER = "fridger-backend"
    const val ACCESS_TOKEN_MINUTES = 15
    const val REFRESH_TOKEN_DAYS = 30
    const val REFRESH_TOKEN_BYTES = 64
    const val JWKS_REFRESH_INTERVAL_HOURS = 6
    const val JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs"
}

/** Loader */
fun loadAppConfig(env: Map<String, String> = System.getenv()): AppConfig {
    fun Map<String, String>.int(
        key: String,
        default: Int
    ) = this[key]?.toIntOrNull() ?: default
    val jwtSecret =
        env[EnvKeys.JWT_SECRET]?.takeIf { it.isNotBlank() }
            ?: error(MISSING_JWT_SECRET)

    val googleClientIds =
        env[EnvKeys.GOOGLE_CLIENT_ID]
            ?.split(',')
            ?.mapNotNull { it.trim().ifBlank { null } }
            ?.toSet()
            ?: emptySet()

    return AppConfig(
        port = env.int(EnvKeys.PORT, Defaults.PORT),
        dbUrl = env[EnvKeys.DB_URL] ?: Defaults.DB_URL,
        dbUser = env[EnvKeys.DB_USER] ?: Defaults.DB_USER,
        dbPassword = env[EnvKeys.DB_PASSWORD] ?: Defaults.DB_PASSWORD,
        jwtSecret = jwtSecret,
        jwtIssuer = env[EnvKeys.JWT_ISSUER] ?: Defaults.JWT_ISSUER,
        googleClientIds = googleClientIds,
        jwksUrl = env[EnvKeys.JWKS_URL] ?: Defaults.JWKS_URL,
        accessTokenMinutes = env.int(EnvKeys.ACCESS_TOKEN_MINUTES, Defaults.ACCESS_TOKEN_MINUTES),
        refreshTokenDays = env.int(EnvKeys.REFRESH_TOKEN_DAYS, Defaults.REFRESH_TOKEN_DAYS),
        refreshTokenBytes = env.int(EnvKeys.REFRESH_TOKEN_BYTES, Defaults.REFRESH_TOKEN_BYTES),
        jwksRefreshIntervalHours = env.int(EnvKeys.JWKS_REFRESH_INTERVAL_HOURS, Defaults.JWKS_REFRESH_INTERVAL_HOURS)
    )
}

val AppConfigAttribute = AttributeKey<AppConfig>("AppConfig")

fun Application.loadAndStoreConfig(): AppConfig {
    val cfg = loadAppConfig()
    attributes.put(AppConfigAttribute, cfg)
    return cfg
}

fun Application.appConfig(): AppConfig = attributes[AppConfigAttribute]
