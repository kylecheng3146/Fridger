package fridger.com.io.data.repository

import fridger.com.io.data.remote.HealthDashboardApiService
import fridger.shared.health.DiversityRating
import fridger.shared.health.DiversityScore
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.NutritionCategory
import fridger.shared.models.ApiResponse
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HealthDashboardRepositoryTest {
    private class FakeApiService(
        private val response: ApiResponse<HealthDashboardMetrics>,
        private val onFetch: (Boolean, Int?) -> Unit = { _, _ -> },
    ) : HealthDashboardApiService() {
        override suspend fun fetchDashboard(
            userId: String,
            includeTrends: Boolean,
            rangeDays: Int?,
        ): ApiResponse<HealthDashboardMetrics> {
            onFetch(includeTrends, rangeDays)
            return response
        }
    }

    private val sampleMetrics =
        HealthDashboardMetrics(
            nutritionDistribution = mapOf(NutritionCategory.PRODUCE to 60.0),
            diversityScore = DiversityScore(70, DiversityRating.BALANCED),
            expiryAlerts = emptyList(),
            recommendations = emptyList(),
        )

    @Test
    fun successResponseReturnsMetrics() =
        runTest {
            val repository = HealthDashboardRepositoryImpl(FakeApiService(ApiResponse(success = true, data = sampleMetrics)))

            val result = repository.getDashboardMetrics("user")

            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull() === sampleMetrics)
        }

    @Test
    fun failureResponsePropagatesError() =
        runTest {
            val repository =
                HealthDashboardRepositoryImpl(
                    FakeApiService(
                        ApiResponse(success = false, data = null, error = "boom"),
                    ),
                )

            val result = repository.getDashboardMetrics("user")

            assertTrue(result.isFailure)
            assertFalse(result.exceptionOrNull()?.message.isNullOrBlank())
        }

    @Test
    fun fetchesDashboardFromApi() =
        runTest {
            var invoked = false
            var capturedInclude: Boolean? = null
            var capturedRange: Int? = null
            val repository =
                HealthDashboardRepositoryImpl(
                    FakeApiService(
                        ApiResponse(success = true, data = sampleMetrics),
                        onFetch = { include, range ->
                            invoked = true
                            capturedInclude = include
                            capturedRange = range
                        },
                    ),
                )

            repository.getDashboardMetrics("user", includeTrends = true, rangeDays = 30)

            assertTrue(invoked)
            assertEquals(true, capturedInclude)
            assertEquals(30, capturedRange)
        }
}
