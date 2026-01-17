package fridger.com.io.data.remote

/**
 * Centralizes the base URL resolution for the backend API so each platform
 * can override the host value (e.g. Android emulator vs. Desktop).
 */
object BackendConfig {
    val baseUrl: String = platformBackendBaseUrl()
}

expect fun platformBackendBaseUrl(): String
