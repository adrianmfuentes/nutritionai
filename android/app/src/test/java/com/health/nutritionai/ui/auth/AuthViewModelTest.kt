package com.health.nutritionai.ui.auth

import android.app.Application
import app.cash.turbine.test
import com.health.nutritionai.data.model.AuthResponse
import com.health.nutritionai.data.model.UserProfile
import com.health.nutritionai.data.repository.UserRepository
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "es")
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var application: Application
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk()
        application = RuntimeEnvironment.application
        viewModel = AuthViewModel(userRepository, application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============ Login Tests ============

    @Test
    fun `login with empty email shows error`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.login("", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)
            assertEquals("Por favor, completa todos los campos", (errorState as AuthUiState.Error).message)
        }
    }

    @Test
    fun `login with empty password shows error`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.login("test@example.com", "")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)
            assertEquals("Por favor, completa todos los campos", (errorState as AuthUiState.Error).message)
        }
    }

    @Test
    fun `login with invalid email format shows error`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.login("invalid-email", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)
            assertEquals("Correo electrónico inválido", (errorState as AuthUiState.Error).message)
        }
    }

    @Test
    fun `login success returns user id and success message`() = runTest {
        val authResponse = AuthResponse(
            token = "test-token",
            user = UserProfile("user-123", "test@example.com", "Test User"),
            userId = "user-123"
        )
        coEvery { userRepository.login("test@example.com", "password123") } returns NetworkResult.Success(authResponse)

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.login("test@example.com", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val successState = awaitItem()
            assertTrue(successState is AuthUiState.Success)
            assertEquals("user-123", (successState as AuthUiState.Success).userId)
            assertEquals("¡Bienvenido de nuevo!", successState.successMessage)
        }
    }

    @Test
    fun `login error shows error message`() = runTest {
        coEvery { userRepository.login("test@example.com", "wrong-password") } returns NetworkResult.Error("Credenciales incorrectas")

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.login("test@example.com", "wrong-password")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)
            assertEquals("Credenciales incorrectas", (errorState as AuthUiState.Error).message)
        }
    }

    @Test
    fun `login trims whitespace from email and password`() = runTest {
        val authResponse = AuthResponse(
            token = "test-token",
            userId = "user-123"
        )
        coEvery { userRepository.login("test@example.com", "password123") } returns NetworkResult.Success(authResponse)

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.login("  test@example.com  ", "  password123  ")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val successState = awaitItem()
            assertTrue(successState is AuthUiState.Success)
        }
    }

    // ============ Register Tests ============

    @Test
    fun `register with empty name shows error`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.register("", "test@example.com", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)
            assertEquals("Por favor, completa todos los campos", (errorState as AuthUiState.Error).message)
        }
    }

    @Test
    fun `register with empty email shows error`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.register("Test User", "", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)
            assertEquals("Por favor, completa todos los campos", (errorState as AuthUiState.Error).message)
        }
    }

    @Test
    fun `register with empty password shows error`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.register("Test User", "test@example.com", "")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)
            assertEquals("Por favor, completa todos los campos", (errorState as AuthUiState.Error).message)
        }
    }

    @Test
    fun `register with invalid email format shows error`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.register("Test User", "invalid-email", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)
            assertEquals("Correo electrónico inválido", (errorState as AuthUiState.Error).message)
        }
    }

    @Test
    fun `register with short name shows error`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.register("A", "test@example.com", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)
            assertEquals("El nombre debe tener al menos 2 caracteres", (errorState as AuthUiState.Error).message)
        }
    }

    @Test
    fun `register with short password shows error`() = runTest {
        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.register("Test User", "test@example.com", "pass")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)
            assertEquals("La contraseña debe tener al menos 8 caracteres", (errorState as AuthUiState.Error).message)
        }
    }

    @Test
    fun `register success returns user id and success message`() = runTest {
        val authResponse = AuthResponse(
            token = "test-token",
            user = UserProfile("user-456", "new@example.com", "New User"),
            userId = "user-456"
        )
        coEvery { userRepository.register("new@example.com", "password123", "New User") } returns NetworkResult.Success(authResponse)

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.register("New User", "new@example.com", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val successState = awaitItem()
            assertTrue(successState is AuthUiState.Success)
            assertEquals("user-456", (successState as AuthUiState.Success).userId)
            assertEquals("¡Cuenta creada exitosamente!", successState.successMessage)
        }
    }

    @Test
    fun `register error shows error message`() = runTest {
        coEvery { userRepository.register("existing@example.com", "password123", "Existing User") } returns NetworkResult.Error("Ya existe una cuenta con este correo electrónico.")

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.register("Existing User", "existing@example.com", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)
            assertEquals("Ya existe una cuenta con este correo electrónico.", (errorState as AuthUiState.Error).message)
        }
    }

    @Test
    fun `register trims whitespace from inputs`() = runTest {
        val authResponse = AuthResponse(
            token = "test-token",
            userId = "user-789"
        )
        coEvery { userRepository.register("trim@example.com", "password123", "Trimmed User") } returns NetworkResult.Success(authResponse)

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())

            viewModel.register("  Trimmed User  ", "  trim@example.com  ", "  password123  ")
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(AuthUiState.Loading, awaitItem())
            val successState = awaitItem()
            assertTrue(successState is AuthUiState.Success)
        }
    }

    // ============ Initial State Tests ============

    @Test
    fun `initial state is Idle`() {
        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
    }
}
