package fridger.backend.plugins

import fridger.backend.config.ApiPaths
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.util.AttributeKey
import org.slf4j.MDC
import org.slf4j.event.Level
import java.util.UUID

private object MDCKeys {
    const val REQUEST_ID = "requestId"
    const val USER_ID = "userId"
}

private object HeaderNames {
    const val REQUEST_ID = "X-Request-Id"
    const val USER_AGENT = "User-Agent"
}

private const val MAX_HEADER_LEN = 200

fun Application.configureLogging() {
    // Populate MDC (Mapped Diagnostic Context) for each call
    intercept(ApplicationCallPipeline.Setup) {
        val requestId = call.request.header(HeaderNames.REQUEST_ID) ?: UUID.randomUUID().toString()
        MDC.put(MDCKeys.REQUEST_ID, requestId)
        // Placeholder userId extraction (once auth is implemented, extract from principal and set attribute)
        MDC.put(MDCKeys.USER_ID, call.attributes.getOrNull(AttributeKey<String>(MDCKeys.USER_ID)) ?: "anonymous")
        // Echo back request id for tracing on client side
        call.response.headers.append(HeaderNames.REQUEST_ID, requestId, safeOnly = false)
    }

    install(CallLogging) {
        level = Level.INFO
        // Attach MDC values into every log line pattern handled by logback (%X{requestId}, %X{userId})
        mdc(MDCKeys.REQUEST_ID) { MDC.get(MDCKeys.REQUEST_ID) }
        mdc(MDCKeys.USER_ID) { MDC.get(MDCKeys.USER_ID) }
        // Skip very frequent or noisy endpoints if needed later
        filter { call -> call.request.path() != ApiPaths.HEALTH }
        format { call ->
            val method = call.request.httpMethod.value
            val uri = call.request.uri
            val status = call.response.status()?.value?.toString() ?: "-"
            val userAgent = sanitizeHeader(call.request.headers[HeaderNames.USER_AGENT]) ?: "-"
            // Never log raw Authorization header
            "${'$'}method ${'$'}uri -> ${'$'}status ua=${'$'}userAgent"
        }
    }

    // After each call completes, clear MDC to avoid leakage across requests
    intercept(ApplicationCallPipeline.Fallback) {
        try {
            proceed()
        } finally {
            MDC.remove(MDCKeys.REQUEST_ID)
            MDC.remove(MDCKeys.USER_ID)
        }
    }
}

private fun sanitizeHeader(value: String?): String? = value?.take(MAX_HEADER_LEN)

// Helper to redact any sensitive token if ever needed in manual logging
@Suppress("unused")
private fun redact(text: String?): String? =
    text?.let { if (it.startsWith("Bearer ", ignoreCase = true)) "<redacted>" else it }
