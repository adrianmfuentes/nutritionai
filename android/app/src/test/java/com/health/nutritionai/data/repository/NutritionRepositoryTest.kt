package com.health.nutritionai.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.health.nutritionai.data.model.*
import com.health.nutritionai.data.remote.api.NutritionApiService
import com.health.nutritionai.data.remote.dto.DailyNutritionDto
import com.health.nutritionai.data.remote.dto.DailyNutritionResponse
import com.health.nutritionai.data.remote.dto.MealSummaryDto
import com.health.nutritionai.data.remote.dto.NutritionDto
import com.health.nutritionai.data.remote.dto.NutritionGoalsDto
import com.health.nutritionai.data.remote.dto.NutritionProgressDto
import com.health.nutritionai.data.remote.dto.WeeklyNutritionResponse
import com.health.nutritionai.util.NetworkResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class NutritionRepositoryTest {

    private lateinit var repository: NutritionRepository
    private lateinit var apiService: NutritionApiService
    private lateinit var context: Context

    private val mockNutritionDto = NutritionDto(
        calories = 1500,
        protein = 75.0,
        carbs = 180.0,
        fat = 50.0,
        fiber = 20.0
    )

    private val mockGoalsDto = NutritionGoalsDto(
        calories = 2000,
        protein = 100.0,
        carbs = 250.0,
        fat = 70.0
    )

    private val mockProgressDto = NutritionProgressDto(
        caloriesPercent = 75.0,
        proteinPercent = 75.0,
        carbsPercent = 72.0,
        fatPercent = 71.4
    )

    private val mockMealSummaryDto = MealSummaryDto(
        mealId = "meal-1",
        mealType = "breakfast",
        imageUrl = "https://example.com/meal.jpg",
        totalCalories = 400,
        timestamp = "2024-01-15T08:00:00"
    )

    private val mockDailyNutritionResponse = DailyNutritionResponse(
        date = "2024-01-15",
        totals = mockNutritionDto,
        goals = mockGoalsDto,
        progress = mockProgressDto,
        meals = listOf(mockMealSummaryDto)
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        apiService = mockk()
        repository = NutritionRepository(context, apiService)
    }

    // ============ getDailyNutrition Tests ============

    @Test
    fun `getDailyNutrition success returns nutrition summary`() = runTest {
        coEvery { apiService.getDailyNutrition("2024-01-15") } returns mockDailyNutritionResponse

        val result = repository.getDailyNutrition("2024-01-15")

        assertTrue(result is NetworkResult.Success)
        val summary = (result as NetworkResult.Success).data!!
        assertEquals("2024-01-15", summary.date)
        assertEquals(1500, summary.totals.calories)
        assertEquals(75.0, summary.totals.protein, 0.001)
        assertEquals(2000, summary.goals.calories)
        assertEquals(75.0, summary.progress.caloriesPercent, 0.001)
    }

    @Test
    fun `getDailyNutrition maps nutrition totals correctly`() = runTest {
        coEvery { apiService.getDailyNutrition(any()) } returns mockDailyNutritionResponse

        val result = repository.getDailyNutrition("2024-01-15")

        assertTrue(result is NetworkResult.Success)
        val totals = (result as NetworkResult.Success).data!!.totals
        assertEquals(1500, totals.calories)
        assertEquals(75.0, totals.protein, 0.001)
        assertEquals(180.0, totals.carbs, 0.001)
        assertEquals(50.0, totals.fat, 0.001)
        assertEquals(20.0, totals.fiber)
    }

    @Test
    fun `getDailyNutrition maps goals correctly`() = runTest {
        coEvery { apiService.getDailyNutrition(any()) } returns mockDailyNutritionResponse

        val result = repository.getDailyNutrition("2024-01-15")

        assertTrue(result is NetworkResult.Success)
        val goals = (result as NetworkResult.Success).data!!.goals
        assertEquals(2000, goals.calories)
        assertEquals(100.0, goals.protein, 0.001)
        assertEquals(250.0, goals.carbs, 0.001)
        assertEquals(70.0, goals.fat, 0.001)
    }

    @Test
    fun `getDailyNutrition maps progress correctly`() = runTest {
        coEvery { apiService.getDailyNutrition(any()) } returns mockDailyNutritionResponse

        val result = repository.getDailyNutrition("2024-01-15")

        assertTrue(result is NetworkResult.Success)
        val progress = (result as NetworkResult.Success).data!!.progress
        assertEquals(75.0, progress.caloriesPercent, 0.001)
        assertEquals(75.0, progress.proteinPercent, 0.001)
        assertEquals(72.0, progress.carbsPercent, 0.001)
        assertEquals(71.4, progress.fatPercent, 0.001)
    }

    @Test
    fun `getDailyNutrition maps meals correctly`() = runTest {
        coEvery { apiService.getDailyNutrition(any()) } returns mockDailyNutritionResponse

        val result = repository.getDailyNutrition("2024-01-15")

        assertTrue(result is NetworkResult.Success)
        val meals = (result as NetworkResult.Success).data!!.meals
        assertEquals(1, meals.size)
        assertEquals("meal-1", meals[0].mealId)
        assertEquals("breakfast", meals[0].mealType)
        assertEquals(400, meals[0].totalCalories)
    }

    @Test
    fun `getDailyNutrition filters meals with null fields`() = runTest {
        val responseWithNullMeals = DailyNutritionResponse(
            date = "2024-01-15",
            totals = mockNutritionDto,
            goals = mockGoalsDto,
            progress = mockProgressDto,
            meals = listOf(
                mockMealSummaryDto,
                MealSummaryDto(
                    mealId = null,
                    mealType = "lunch",
                    imageUrl = "url",
                    totalCalories = 500,
                    timestamp = "2024-01-15T12:00:00"
                ),
                MealSummaryDto(
                    mealId = "meal-3",
                    mealType = null,
                    imageUrl = "url",
                    totalCalories = 600,
                    timestamp = "2024-01-15T19:00:00"
                )
            )
        )
        coEvery { apiService.getDailyNutrition(any()) } returns responseWithNullMeals

        val result = repository.getDailyNutrition("2024-01-15")

        assertTrue(result is NetworkResult.Success)
        val meals = (result as NetworkResult.Success).data!!.meals
        assertEquals(1, meals.size) // Only the valid meal should be included
    }

    @Test
    fun `getDailyNutrition error returns error result`() = runTest {
        coEvery { apiService.getDailyNutrition(any()) } throws IOException("Network error")

        val result = repository.getDailyNutrition("2024-01-15")

        assertTrue(result is NetworkResult.Error)
        // IOException with message returns that message
        assertEquals("Network error", (result as NetworkResult.Error).message)
    }

    @Test
    fun `getDailyNutrition error without message returns default error`() = runTest {
        coEvery { apiService.getDailyNutrition(any()) } throws Exception()

        val result = repository.getDailyNutrition("2024-01-15")

        assertTrue(result is NetworkResult.Error)
        assertEquals("Error al obtener la nutrición diaria", (result as NetworkResult.Error).message)
    }

    @Test
    fun `getDailyNutrition with exception message returns that message`() = runTest {
        coEvery { apiService.getDailyNutrition(any()) } throws Exception("Custom error message")

        val result = repository.getDailyNutrition("2024-01-15")

        assertTrue(result is NetworkResult.Error)
        assertEquals("Custom error message", (result as NetworkResult.Error).message)
    }

    // ============ getWeeklyNutrition Tests ============

    @Test
    fun `getWeeklyNutrition success returns weekly nutrition`() = runTest {
        val weeklyResponse = WeeklyNutritionResponse(
            days = listOf(
                DailyNutritionDto("2024-01-08", mockNutritionDto, 3),
                DailyNutritionDto("2024-01-09", mockNutritionDto, 4),
                DailyNutritionDto("2024-01-10", mockNutritionDto, 3)
            ),
            averages = mockNutritionDto,
            trend = "stable"
        )
        coEvery { apiService.getWeeklyNutrition("2024-01-08") } returns weeklyResponse

        val result = repository.getWeeklyNutrition("2024-01-08")

        assertTrue(result is NetworkResult.Success)
        val weekly = (result as NetworkResult.Success).data!!
        assertEquals(3, weekly.days.size)
        assertEquals("stable", weekly.trend)
    }

    @Test
    fun `getWeeklyNutrition maps daily nutrition correctly`() = runTest {
        val weeklyResponse = WeeklyNutritionResponse(
            days = listOf(
                DailyNutritionDto("2024-01-08", mockNutritionDto, 3)
            ),
            averages = mockNutritionDto,
            trend = "up"
        )
        coEvery { apiService.getWeeklyNutrition(any()) } returns weeklyResponse

        val result = repository.getWeeklyNutrition("2024-01-08")

        assertTrue(result is NetworkResult.Success)
        val day = (result as NetworkResult.Success).data!!.days[0]
        assertEquals("2024-01-08", day.date)
        assertEquals(1500, day.totals.calories)
        assertEquals(3, day.mealCount)
    }

    @Test
    fun `getWeeklyNutrition maps averages correctly`() = runTest {
        val weeklyResponse = WeeklyNutritionResponse(
            days = emptyList(),
            averages = mockNutritionDto,
            trend = "down"
        )
        coEvery { apiService.getWeeklyNutrition(any()) } returns weeklyResponse

        val result = repository.getWeeklyNutrition("2024-01-08")

        assertTrue(result is NetworkResult.Success)
        val averages = (result as NetworkResult.Success).data!!.averages
        assertEquals(1500, averages.calories)
        assertEquals(75.0, averages.protein, 0.001)
    }

    @Test
    fun `getWeeklyNutrition error returns user friendly message`() = runTest {
        coEvery { apiService.getWeeklyNutrition(any()) } throws IOException("Network error")

        val result = repository.getWeeklyNutrition("2024-01-08")

        assertTrue(result is NetworkResult.Error)
        // Uses ErrorMapper which returns a user-friendly message
        assertNotNull((result as NetworkResult.Error).message)
    }

    @Test
    fun `getWeeklyNutrition with empty days list`() = runTest {
        val weeklyResponse = WeeklyNutritionResponse(
            days = emptyList(),
            averages = mockNutritionDto,
            trend = "stable"
        )
        coEvery { apiService.getWeeklyNutrition(any()) } returns weeklyResponse

        val result = repository.getWeeklyNutrition("2024-01-08")

        assertTrue(result is NetworkResult.Success)
        assertTrue((result as NetworkResult.Success).data!!.days.isEmpty())
    }

    // ============ Edge Cases ============

    @Test
    fun `getDailyNutrition with empty meals list`() = runTest {
        val responseWithNoMeals = mockDailyNutritionResponse.copy(meals = emptyList())
        coEvery { apiService.getDailyNutrition(any()) } returns responseWithNoMeals

        val result = repository.getDailyNutrition("2024-01-15")

        assertTrue(result is NetworkResult.Success)
        assertTrue((result as NetworkResult.Success).data!!.meals.isEmpty())
    }

    @Test
    fun `getDailyNutrition with null fiber in nutrition`() = runTest {
        val nutritionWithNullFiber = NutritionDto(
            calories = 1500,
            protein = 75.0,
            carbs = 180.0,
            fat = 50.0,
            fiber = null
        )
        val response = mockDailyNutritionResponse.copy(totals = nutritionWithNullFiber)
        coEvery { apiService.getDailyNutrition(any()) } returns response

        val result = repository.getDailyNutrition("2024-01-15")

        assertTrue(result is NetworkResult.Success)
        assertNull((result as NetworkResult.Success).data!!.totals.fiber)
    }
}
