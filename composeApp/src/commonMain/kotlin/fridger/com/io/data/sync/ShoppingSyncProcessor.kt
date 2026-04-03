package fridger.com.io.data.sync

data class SyncOutcome(
    val failedActionIds: Set<String> = emptySet()
)

interface ShoppingSyncProcessor {
    suspend fun process(pending: List<PendingSyncAction>): Result<SyncOutcome>
}

class NoopShoppingSyncProcessor : ShoppingSyncProcessor {
    override suspend fun process(pending: List<PendingSyncAction>): Result<SyncOutcome> =
        Result.success(SyncOutcome())
}

class ApiShoppingSyncProcessor(
    private val listIdProvider: () -> String,
    private val tokenProvider: () -> String,
    private val api: fridger.com.io.data.remote.ShoppingSyncApiService
) : ShoppingSyncProcessor {
    override suspend fun process(pending: List<PendingSyncAction>): Result<SyncOutcome> {
        if (pending.isEmpty()) return Result.success(SyncOutcome())
        val listId = listIdProvider()
        if (listId.isBlank()) return Result.failure(IllegalArgumentException("Missing listId"))
        return runCatching {
            val token = tokenProvider()
            if (token.isBlank()) throw IllegalArgumentException("Missing access token")
            val response = api.sync(listId, pending, token)
            if (!response.success) {
                throw IllegalStateException(response.error ?: "Sync failed")
            }
            val failedIds =
                response.data
                    ?.results
                    ?.filterNot { it.success }
                    ?.map { it.actionId }
                    ?.toSet()
                    ?: emptySet()
            SyncOutcome(failedActionIds = failedIds)
        }
    }
}
