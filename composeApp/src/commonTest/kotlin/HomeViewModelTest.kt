package fridger.com.io.presentation.home

import fridger.com.io.data.model.Freshness
import fridger.com.io.data.model.Ingredient
import fridger.com.io.data.repository.IngredientRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        // Default stream can be overridden per test
        every { repository.getIngredientsStream() } returns flowOf(emptyList())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun smoke() = runTest(testDispatcher) {
        assertTrue(true)
    }

    @Test
    fun `given repository returns ingredients, when loads, then uiState is updated correctly`() = runTest(testDispatcher) {
        // Arrange
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val ingredients = listOf(
            // Today (0 days) -> todayExpiring
            Ingredient(
                id = 1,
                name = "Milk",
                addDate = today.plus(DatePeriod(days = -5)),
                expirationDate = today,
                freshness = Freshness.NearingExpiration
            ),
            // Tomorrow (1 day) -> todayExpiring
            Ingredient(
                id = 2,
                name = "Yogurt",
                addDate = today.plus(DatePeriod(days = -3)),
                expirationDate = today.plus(DatePeriod(days = 1)),
                freshness = Freshness.NearingExpiration
            ),
            // In 3 days -> weekExpiring (2..7)
            Ingredient(
                id = 3,
                name = "Lettuce",
                addDate = today.plus(DatePeriod(days = -1)),
                expirationDate = today.plus(DatePeriod(days = 3)),
                freshness = Freshness.NearingExpiration
            ),
            // Expired 2 days ago -> expired
            Ingredient(
                id = 4,
                name = "Bread",
                addDate = today.plus(DatePeriod(days = -7)),
                expirationDate = today.plus(DatePeriod(days = -2)),
                freshness = Freshness.Expired
            )
        )
        every { repository.getIngredientsStream() } returns flowOf(ingredients)

        // Act (init triggers observe)
        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(null, state.error)
        assertEquals(2, state.todayExpiringItems.size)
        assertEquals(1, state.weekExpiringItems.size)
        assertEquals(1, state.expiredItems.size)
        assertEquals(4, state.refrigeratedItems.size)
    }

    @Test
    fun `given repository is empty, when loads, then uiState shows empty`() = runTest(testDispatcher) {
        // Arrange
        every { repository.getIngredientsStream() } returns flowOf(emptyList())

        // Act
        viewModel = HomeViewModel(repository)
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
    }

    @Test
    fun `given items loaded, when sort by name, then refrigeratedItems are sorted by name`() = runTest(testDispatcher) {
        // Arrange
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val ingredients = listOf(
            Ingredient(
                id = 10,
                name = "Carrot",
                addDate = today.plus(DatePeriod(days = -1)),
                expirationDate = today.plus(DatePeriod(days = 5)),
                freshness = Freshness.Fresh
            ),
            Ingredient(
                id = 11,
                name = "apple",
                addDate = today.plus(DatePeriod(days = -2)),
                expirationDate = today.plus(DatePeriod(days = 2)),
                freshness = Freshness.NearingExpiration
            ),
            Ingredient(
                id = 12,
                name = "Banana",
                addDate = today.plus(DatePeriod(days = -3)),
                expirationDate = today.plus(DatePeriod(days = 3)),
                freshness = Freshness.Fresh
            )
        )
        every { repository.getIngredientsStream() } returns flowOf(ingredients)

        // Act
        viewModel = HomeViewModel(repository)
        advanceUntilIdle() // ensure initial collect complete
        viewModel.updateSortingAndGrouping(sort = SortOption.NAME)

        // Assert
        val names = viewModel.uiState.value.refrigeratedItems.map { it.name }
        assertEquals(listOf("apple", "Banana", "Carrot"), names)
    }

    @Test
    fun `given repository throws exception, when loads, then uiState contains error`() = runTest(testDispatcher) {
        // Arrange
        every { repository.getIngredientsStream() } returns flow { throw Exception("Test Exception") }

        // Act
        viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("Test Exception", state.error)
        assertTrue(state.todayExpiringItems.isEmpty())
        assertTrue(state.weekExpiringItems.isEmpty())
        assertTrue(state.expiredItems.isEmpty())
        assertTrue(state.refrigeratedItems.isEmpty())
        assertTrue(state.groupedRefrigeratedItems.isEmpty())
    }
}
