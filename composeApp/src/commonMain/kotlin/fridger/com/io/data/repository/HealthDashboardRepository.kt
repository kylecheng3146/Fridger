package fridger.com.io.data.repository

import fridger.com.io.data.remote.HealthDashboardApiService
import fridger.shared.health.HealthDashboardMetrics

interface HealthDashboardRepository {
    suspend fun getDashboardMetrics(userId: String): Result<HealthDashboardMetrics>
}

class HealthDashboardRepositoryImpl(
    private val apiService: HealthDashboardApiService,
) : HealthDashboardRepository {
    override suspend fun getDashboardMetrics(userId: String): Result<HealthDashboardMetrics> =
        try {
            val response = apiService.fetchDashboard(userId)
            when {
                response.success && response.data != null -> {
                    val metrics: HealthDashboardMetrics = requireNotNull(response.data)
                    Result.success(metrics)
                }
                else -> Result.failure(IllegalStateException(response.error ?: "Unknown dashboard response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}
