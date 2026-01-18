package com.health.nutritionai.ui.dashboard

import app.cash.turbine.test
import com.health.nutritionai.data.model.*
import com.health.nutritionai.data.repository.MealRepository
import com.health.nutritionai.data.repository.NutritionRepository
import com.health.nutritionai.data.repository.UserRepository
import com.health.nutritionai.util.Constants
import com.health.nutritionai.util.NetworkResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var nutritionRepository: NutritionRepository
    private lateinit var mealRepository: MealRepository
    private lateinit var userRepository: UserRepository
    private val testDispatcher = StandardTestDispatcher()

    private val mockNutritionSummary = NutritionSummary(
        date = getCurrentDate(),
        totals = Nutrition(1500, 75.0, 180.0, 50.0, 20.0),
        goals = NutritionGoals(2000, 100.0, 250.0, 70.0),
        progress = NutritionProgress(75.0, 75.0, 72.0, 71.4),
        meals = listOf(
            MealSummary("meal-1", "breakfast", "url1", 400, "2024-01-15T08:00:00"),
            MealSummary("meal-2", "lunch", "url2", 600, "2024-01-15T13:00:00"),
            MealSummary("meal-3", "snack", "url3", 500, "2024-01-15T16:00:00")
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        nutritionRepository = mockk()
        mealRepository = mockk()
        userRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============ Load Daily Nutrition Tests ============

    @Test
    fun `initial state is Loading`() = runTest {
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Loading()

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)

        assertEquals(DashboardUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `loadDailyNutrition success updates state with nutrition summary`() = runTest {
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Success(mockNutritionSummary)

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DashboardUiState.Success)
        assertEquals(mockNutritionSummary, (state as DashboardUiState.Success).nutritionSummary)
    }

    @Test
    fun `loadDailyNutrition error updates state with error message`() = runTest {
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Error("Error de conexión")

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DashboardUiState.Error)
        assertEquals("Error de conexión", (state as DashboardUiState.Error).message)
    }

    @Test
    fun `loadDailyNutrition with null data shows error`() = runTest {
        @Suppress("UNCHECKED_CAST")
        val nullResult = NetworkResult.Success(null) as NetworkResult<NutritionSummary>
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns nullResult

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DashboardUiState.Error)
        assertEquals("No se pudieron cargar los datos", (state as DashboardUiState.Error).message)
    }

    @Test
    fun `loadDailyNutrition updates selected date`() = runTest {
        val testDate = "2024-01-10"
        coEvery { nutritionRepository.getDailyNutrition(testDate) } returns NetworkResult.Success(mockNutritionSummary)
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Success(mockNutritionSummary)

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadDailyNutrition(testDate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(testDate, viewModel.selectedDate.value)
    }

    // ============ Refresh Tests ============

    @Test
    fun `refresh reloads current date data`() = runTest {
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Success(mockNutritionSummary)

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DashboardUiState.Success)
    }

    // ============ Date Navigation Tests ============

    @Test
    fun `selectPreviousDay navigates to previous day`() = runTest {
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Success(mockNutritionSummary)

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val currentDate = viewModel.selectedDate.value
        viewModel.selectPreviousDay()
        testDispatcher.scheduler.advanceUntilIdle()

        val newDate = viewModel.selectedDate.value
        assertTrue(newDate < currentDate)
    }

    @Test
    fun `selectNextDay does not navigate to future date`() = runTest {
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Success(mockNutritionSummary)

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val todayDate = getCurrentDate()
        viewModel.selectedDate.test {
            assertEquals(todayDate, awaitItem())

            viewModel.selectNextDay()
            testDispatcher.scheduler.advanceUntilIdle()

            // Date should not change if it's today
            expectNoEvents()
        }
    }

    @Test
    fun `selectNextDay navigates to next day when not today`() = runTest {
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Success(mockNutritionSummary)

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // First go to a previous day
        viewModel.selectPreviousDay()
        testDispatcher.scheduler.advanceUntilIdle()
        val previousDate = viewModel.selectedDate.value

        // Then try to go forward
        viewModel.selectNextDay()
        testDispatcher.scheduler.advanceUntilIdle()
        val nextDate = viewModel.selectedDate.value

        assertTrue(nextDate > previousDate)
    }

    // ============ State Flow Tests ============

    @Test
    fun `uiState emits loading then success`() = runTest {
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Success(mockNutritionSummary)

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertTrue(successState is DashboardUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectedDate initial value is today`() = runTest {
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Success(mockNutritionSummary)

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)

        assertEquals(getCurrentDate(), viewModel.selectedDate.value)
    }

    // ============ Nutrition Data Tests ============

    @Test
    fun `success state contains correct meal count`() = runTest {
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Success(mockNutritionSummary)

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as DashboardUiState.Success
        assertEquals(3, state.nutritionSummary.meals.size)
    }

    @Test
    fun `success state contains correct nutrition totals`() = runTest {
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Success(mockNutritionSummary)

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as DashboardUiState.Success
        assertEquals(1500, state.nutritionSummary.totals.calories)
        assertEquals(75.0, state.nutritionSummary.totals.protein, 0.001)
    }

    @Test
    fun `success state contains correct progress percentages`() = runTest {
        coEvery { nutritionRepository.getDailyNutrition(any()) } returns NetworkResult.Success(mockNutritionSummary)

        viewModel = DashboardViewModel(nutritionRepository, mealRepository, userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as DashboardUiState.Success
        assertEquals(75.0, state.nutritionSummary.progress.caloriesPercent, 0.001)
    }

    // ============ Helper Methods ============

    private fun getCurrentDate(): String {
        return SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(Date())
    }
}
