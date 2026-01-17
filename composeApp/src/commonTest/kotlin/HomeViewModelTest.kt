package fridger.com.io.presentation.home

import fridger.com.data.model.remote.MealDto
import fridger.com.domain.translator.Translator
import fridger.com.io.data.model.Freshness
import fridger.com.io.data.model.Ingredient
import fridger.com.io.data.repository.HealthDashboardRepository
import fridger.com.io.data.repository.IngredientRepository
import fridger.com.io.data.repository.RecipeRepository
import fridger.com.io.data.user.UserSessionProvider
import fridger.shared.health.CalorieBucket
import fridger.shared.health.DiversityRating
import fridger.shared.health.DiversityScore
import fridger.shared.health.ExpiryAlert
import fridger.shared.health.HealthDashboardMetrics
import fridger.shared.health.HealthRecommendation
import fridger.shared.health.NutritionCategory
import fridger.shared.health.RecommendationReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: HomeViewModel
    private lateinit var repository: IngredientRepository
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var translator: Translator
    private lateinit var dashboardRepository: HealthDashboardRepository
    private val userSessionProvider = UserSessionProvider { "test-user" }
    private val dashboardMetrics =
        HealthDashboardMetrics(
            nutritionDistribution =
                mapOf(
                    NutritionCategory.PRODUCE to 50.0,
                    NutritionCategory.PROTEIN to 30.0,
                ),
            diversityScore = DiversityScore(60, DiversityRating.BALANCED),
            expiryAlerts =
                listOf(
                    ExpiryAlert(
                        itemName = "Berry",
                        category = NutritionCategory.PRODUCE,
                        daysUntilExpiry = 2,
                        calorieBucket = CalorieBucket.LOW,
                    ),
                ),
            recommendations =
                listOf(
                    HealthRecommendation(
                        category = NutritionCategory.PRODUCE,
                        reason = RecommendationReason.LOW_STOCK,
                        message = "多補充蔬果",
                    ),
                ),
        )

    // Simple fake to return a provided flow
    private class FakeIngredientRepository(
        private val stream: Flow<List<Ingredient>>
    ) : IngredientRepository {
        override fun getIngredientsStream(): Flow<List<Ingredient>> = stream

        override suspend fun add(
            name: String,
            expirationDateDisplay: String
        ) { /* no-op for tests */ }

        override suspend fun delete(id: Long) { /* no-op for tests */ }
    }

    // Fake RecipeRepository for tests
    private class FakeRecipeRepository : RecipeRepository {
        override suspend fun getRemoteRandomRecipe(): Result<MealDto> =
            Result.success(
                MealDto(
                    idMeal = "1",
                    strMeal = "Test Meal",
                    strCategory = "Test Category",
                    strArea = "Test Area",
                    strInstructions = "Test instructions",
                    strMealThumb = null,
                    strTags = null,
                    strYoutube = null,
                    strIngredient1 = "Ingredient 1",
                    strIngredient2 = null,
                    strMeasure1 = "Measure 1",
                    strMeasure2 = null
                )
            )

        override suspend fun getRecipesByIngredient(ingredient: String): Result<List<MealDto>> =
            Result.success(emptyList())

        override suspend fun getRecipeById(id: String): Result<MealDto> =
            Result.failure(UnsupportedOperationException("Not needed in this test"))

        override suspend fun getRecipeCategories(): Result<List<fridger.com.data.model.remote.RecipeCategoryDto>> =
            Result.success(emptyList())

        override suspend fun getRecipesByCategory(category: String): Result<List<MealDto>> =
            Result.success(emptyList())

        override suspend fun searchRecipesByName(query: String): Result<List<MealDto>> =
            Result.success(emptyList())
    }

    // Fake Translator for tests
    private class FakeTranslator : Translator {
        override suspend fun translate(
            text: String,
            sourceLang: String,
            targetLang: String
        ): String = "$text [translated]"
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeIngredientRepository(flowOf(emptyList()))
        recipeRepository = FakeRecipeRepository()
        translator = FakeTranslator()
        dashboardRepository =
            object : HealthDashboardRepository {
                override suspend fun getDashboardMetrics(userId: String): Result<HealthDashboardMetrics> =
                    Result.success(dashboardMetrics)
            }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun smoke() =
        runTest(testDispatcher) {
            assertTrue(true)
        }

    @Test
    fun `given repository returns ingredients when loads then uiState is updated correctly`() =
        runTest(testDispatcher) {
            // Arrange
            val today =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
            val ingredients =
                listOf(
                    // Today (0 days) -> todayExpiring
                    Ingredient(
                        id = 1,
                        name = "Milk",
                        addDate = today.plus(DatePeriod(days = -5)),
                        expirationDate = today,
                        category = fridger.com.io.data.model.IngredientCategory.OTHERS,
                        freshness = Freshness.NearingExpiration
                    ),
                    // Tomorrow (1 day) -> todayExpiring
                    Ingredient(
                        id = 2,
                        name = "Yogurt",
                        addDate = today.plus(DatePeriod(days = -3)),
                        expirationDate = today.plus(DatePeriod(days = 1)),
                        category = fridger.com.io.data.model.IngredientCategory.OTHERS,
                        freshness = Freshness.NearingExpiration
                    ),
                    // In 3 days -> weekExpiring (2..7)
                    Ingredient(
                        id = 3,
                        name = "Lettuce",
                        addDate = today.plus(DatePeriod(days = -1)),
                        expirationDate = today.plus(DatePeriod(days = 3)),
                        category = fridger.com.io.data.model.IngredientCategory.OTHERS,
                        freshness = Freshness.NearingExpiration
                    ),
                    // Expired 2 days ago -> expired
                    Ingredient(
                        id = 4,
                        name = "Bread",
                        addDate = today.plus(DatePeriod(days = -7)),
                        expirationDate = today.plus(DatePeriod(days = -2)),
                        category = fridger.com.io.data.model.IngredientCategory.OTHERS,
                        freshness = Freshness.Expired
                    )
                )
            repository = FakeIngredientRepository(flowOf(ingredients))

            // Act (init triggers observe)
            viewModel = HomeViewModel(repository, recipeRepository, translator, dashboardRepository, userSessionProvider)
            advanceUntilIdle()

            // Assert
            val state = viewModel.uiState.value
            assertEquals(false, state.isLoading)
            assertEquals(null, state.error)
            assertEquals(2, state.todayExpiringItems.size)
            assertEquals(1, state.weekExpiringItems.size)
            assertEquals(1, state.expiredItems.size)
            assertEquals(4, state.refrigeratedItems.size)
            assertEquals(dashboardMetrics, state.healthDashboard.metrics)
        }

    @Test
    fun `given repository is empty when loads then uiState shows empty`() =
        runTest(testDispatcher) {
            // Arrange
            repository = FakeIngredientRepository(flowOf(emptyList()))

            // Act
            viewModel = HomeViewModel(repository, recipeRepository, translator, dashboardRepository, userSessionProvider)
            advanceUntilIdle()

            // Assert
            val state = viewModel.uiState.value
            assertEquals(false, state.isLoading)
            assertEquals(null, state.error)
            assertTrue(state.todayExpiringItems.isEmpty())
            assertTrue(state.weekExpiringItems.isEmpty())
            assertTrue(state.expiredItems.isEmpty())
            assertTrue(state.refrigeratedItems.isEmpty())
            assertTrue(state.groupedRefrigeratedItems.isEmpty())
            assertEquals(dashboardMetrics, state.healthDashboard.metrics)
        }

    @Test
    fun `given items loaded when sort by name then refrigeratedItems are sorted by name`() =
        runTest(testDispatcher) {
            // Arrange
            val today =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
            val ingredients =
                listOf(
                    Ingredient(
                        id = 10,
                        name = "Carrot",
                        addDate = today.plus(DatePeriod(days = -1)),
                        expirationDate = today.plus(DatePeriod(days = 5)),
                        category = fridger.com.io.data.model.IngredientCategory.OTHERS,
                        freshness = Freshness.Fresh
                    ),
                    Ingredient(
                        id = 11,
                        name = "apple",
                        addDate = today.plus(DatePeriod(days = -2)),
                        expirationDate = today.plus(DatePeriod(days = 2)),
                        category = fridger.com.io.data.model.IngredientCategory.OTHERS,
                        freshness = Freshness.NearingExpiration
                    ),
                    Ingredient(
                        id = 12,
                        name = "Banana",
                        addDate = today.plus(DatePeriod(days = -3)),
                        expirationDate = today.plus(DatePeriod(days = 3)),
                        category = fridger.com.io.data.model.IngredientCategory.OTHERS,
                        freshness = Freshness.Fresh
                    )
                )
            repository = FakeIngredientRepository(flowOf(ingredients))

            // Act
            viewModel = HomeViewModel(repository, recipeRepository, translator)
            advanceUntilIdle() // ensure initial collect complete
            viewModel.updateSortingAndGrouping(sort = SortOption.NAME)

            // Assert
            val names =
                viewModel.uiState.value.refrigeratedItems
                    .map { it.name }
            assertEquals(listOf("apple", "Banana", "Carrot"), names)
        }

    @Test
    fun `given ingredients with categories when loaded then refrigeratedItems have correct categories`() =
        runTest(testDispatcher) {
            // Arrange
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val ingredients = listOf(
                Ingredient(
                    id = 1,
                    name = "Apple",
                    addDate = today,
                    expirationDate = today.plus(DatePeriod(days = 7)),
                    category = fridger.com.io.data.model.IngredientCategory.FRUITS,
                    freshness = Freshness.Fresh
                ),
                Ingredient(
                    id = 2,
                    name = "Milk",
                    addDate = today,
                    expirationDate = today.plus(DatePeriod(days = 3)),
                    category = fridger.com.io.data.model.IngredientCategory.DAIRY,
                    freshness = Freshness.Fresh
                )
            )
            repository = FakeIngredientRepository(flowOf(ingredients))

            // Act
            viewModel = HomeViewModel(repository, recipeRepository, translator)
            advanceUntilIdle()

            // Assert
            val items = viewModel.uiState.value.refrigeratedItems
            assertEquals(fridger.com.io.data.model.IngredientCategory.FRUITS, items.find { it.name == "Apple" }?.category)
            assertEquals(fridger.com.io.data.model.IngredientCategory.DAIRY, items.find { it.name == "Milk" }?.category)
        }
}
