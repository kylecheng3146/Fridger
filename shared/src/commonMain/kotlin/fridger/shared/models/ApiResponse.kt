package fridger.shared.models

import kotlinx.serialization.Serializable

/**
 * Standard envelope for all API responses.
 * success: whether the request succeeded
 * data: payload when successful
 * error: human-readable error message when failed
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> = ApiResponse(success = true, data = data, error = null)
        fun <T> fail(message: String): ApiResponse<T> = ApiResponse(success = false, data = null, error = message)
    }
}

