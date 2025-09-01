package fridger.backend.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.util.AttributeKey
import org.slf4j.MDC
import org.slf4j.event.Level
import java.util.UUID

fun Application.configureLogging() {
    // Populate MDC (Mapped Diagnostic Context) for each call
    intercept(ApplicationCallPipeline.Setup) {
        val requestId = call.request.header("X-Request-Id") ?: UUID.randomUUID().toString()
        MDC.put("requestId", requestId)
        // Placeholder userId extraction (once auth is implemented, extract from principal and set attribute)
        MDC.put("userId", call.attributes.getOrNull(AttributeKey<String>("userId")) ?: "anonymous")
        // Echo back request id for tracing on client side
        call.response.headers.append("X-Request-Id", requestId, safeOnly = false)
    }

    install(CallLogging) {
        level = Level.INFO
        // Attach MDC values into every log line pattern handled by logback (%X{requestId}, %X{userId})
        mdc("requestId") { MDC.get("requestId") }
        mdc("userId") { MDC.get("userId") }
        // Skip very frequent or noisy endpoints if needed later
        filter { call ->
            val path = call.request.path()
            path != "/health"
        }
        format { call ->
            val method = call.request.httpMethod.value
            val uri = call.request.uri
            val status = call.response.status()?.value?.toString() ?: "-"
            val userAgent = sanitizeHeader(call.request.headers["User-Agent"]) ?: "-"
            // Never log raw Authorization header
            "${'$'}method ${'$'}uri -> ${'$'}status ua=${'$'}userAgent"
        }
    }

    // After each call completes, clear MDC to avoid leakage across requests
    intercept(ApplicationCallPipeline.Fallback) {
        try {
            proceed()
        } finally {
            MDC.remove("requestId")
            MDC.remove("userId")
        }
    }
}

private fun sanitizeHeader(value: String?): String? {
    if (value == null) return null
    // Truncate very long headers to avoid log bloat
    return value.take(200)
}

// Helper to redact any sensitive token if ever needed in manual logging
@Suppress("unused")
private fun redact(text: String?): String? = text?.let { if (it.startsWith("Bearer ", ignoreCase = true)) "<redacted>" else it }
