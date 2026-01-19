package com.health.nutritionai.data.model

import org.junit.Assert.*
import org.junit.Test

class UserTest {

    @Test
    fun `UserProfile creation with all fields`() {
        val goals = NutritionGoals(
            calories = 2000,
            protein = 100.0,
            carbs = 250.0,
            fat = 70.0
        )

        val user = UserProfile(
            userId = "user-123",
            email = "user@example.com",
            name = "Juan García",
            goals = goals
        )

        assertEquals("user-123", user.userId)
        assertEquals("user@example.com", user.email)
        assertEquals("Juan García", user.name)
        assertNotNull(user.goals)
        assertEquals(2000, user.goals?.calories)
    }

    @Test
    fun `UserProfile creation with null goals`() {
        val user = UserProfile(
            userId = "user-456",
            email = "nuevo@example.com",
            name = "María López"
        )

        assertNull(user.goals)
    }

    @Test
    fun `AuthResponse with full data`() {
        val user = UserProfile(
            userId = "user-789",
            email = "auth@example.com",
            name = "Pedro Martínez",
            goals = NutritionGoals(1800, 90.0, 200.0, 60.0)
        )

        val authResponse = AuthResponse(
            token = "jwt-token-abc123",
            user = user,
            userId = "user-789"
        )

        assertEquals("jwt-token-abc123", authResponse.token)
        assertNotNull(authResponse.user)
        assertEquals("user-789", authResponse.userId)
        assertEquals("auth@example.com", authResponse.user?.email)
    }

    @Test
    fun `AuthResponse with null user and userId`() {
        val authResponse = AuthResponse(
            token = "jwt-token-xyz789"
        )

        assertEquals("jwt-token-xyz789", authResponse.token)
        assertNull(authResponse.user)
        assertNull(authResponse.userId)
    }

    @Test
    fun `UserProfile equality`() {
        val goals = NutritionGoals(2000, 100.0, 250.0, 70.0)

        val user1 = UserProfile("user-1", "test@example.com", "Test User", goals)
        val user2 = UserProfile("user-1", "test@example.com", "Test User", goals)

        assertEquals(user1, user2)
    }

    @Test
    fun `UserProfile copy with modifications`() {
        val user = UserProfile(
            userId = "user-100",
            email = "original@example.com",
            name = "Nombre Original"
        )

        val modifiedUser = user.copy(
            name = "Nombre Modificado",
            goals = NutritionGoals(2200, 110.0, 275.0, 75.0)
        )

        assertEquals("Nombre Modificado", modifiedUser.name)
        assertNotNull(modifiedUser.goals)
        assertEquals(2200, modifiedUser.goals?.calories)
        assertEquals("original@example.com", modifiedUser.email)
    }

    @Test
    fun `AuthResponse with token only`() {
        val authResponse = AuthResponse(token = "simple-token")

        assertEquals("simple-token", authResponse.token)
        assertNull(authResponse.user)
        assertNull(authResponse.userId)
    }

    @Test
    fun `NutritionGoals for different user profiles`() {
        val athleteGoals = NutritionGoals(
            calories = 3000,
            protein = 180.0,
            carbs = 350.0,
            fat = 100.0
        )

        val weightLossGoals = NutritionGoals(
            calories = 1500,
            protein = 100.0,
            carbs = 150.0,
            fat = 50.0
        )

        val maintenanceGoals = NutritionGoals(
            calories = 2000,
            protein = 100.0,
            carbs = 250.0,
            fat = 65.0
        )

        assertTrue(athleteGoals.calories > maintenanceGoals.calories)
        assertTrue(weightLossGoals.calories < maintenanceGoals.calories)
        assertTrue(athleteGoals.protein > maintenanceGoals.protein)
    }
}

