package com.health.nutritionai.data.model

import org.junit.Assert.*
import org.junit.Test

class FoodTest {

    @Test
    fun `Food creation with all fields`() {
        val nutrition = Nutrition(
            calories = 250,
            protein = 15.0,
            carbs = 30.0,
            fat = 8.0,
            fiber = 3.0
        )
        val portion = Portion(amount = 100.0, unit = "g")

        val food = Food(
            name = "Pollo a la plancha",
            confidence = 0.95,
            portion = portion,
            nutrition = nutrition,
            category = "Proteínas",
            imageUrl = "https://example.com/chicken.jpg"
        )

        assertEquals("Pollo a la plancha", food.name)
        assertEquals(0.95, food.confidence, 0.001)
        assertEquals(100.0, food.portion.amount, 0.001)
        assertEquals("g", food.portion.unit)
        assertEquals(250, food.nutrition.calories)
        assertEquals(15.0, food.nutrition.protein, 0.001)
        assertEquals(30.0, food.nutrition.carbs, 0.001)
        assertEquals(8.0, food.nutrition.fat, 0.001)
        assertEquals(3.0, food.nutrition.fiber)
        assertEquals("Proteínas", food.category)
        assertEquals("https://example.com/chicken.jpg", food.imageUrl)
    }

    @Test
    fun `Food creation with null imageUrl`() {
        val nutrition = Nutrition(
            calories = 100,
            protein = 5.0,
            carbs = 20.0,
            fat = 2.0,
            fiber = null
        )
        val portion = Portion(amount = 50.0, unit = "g")

        val food = Food(
            name = "Arroz",
            confidence = 0.85,
            portion = portion,
            nutrition = nutrition,
            category = "Carbohidratos"
        )

        assertNull(food.imageUrl)
        assertNull(food.nutrition.fiber)
    }

    @Test
    fun `Nutrition with zero values`() {
        val nutrition = Nutrition(
            calories = 0,
            protein = 0.0,
            carbs = 0.0,
            fat = 0.0,
            fiber = 0.0
        )

        assertEquals(0, nutrition.calories)
        assertEquals(0.0, nutrition.protein, 0.001)
        assertEquals(0.0, nutrition.carbs, 0.001)
        assertEquals(0.0, nutrition.fat, 0.001)
        assertEquals(0.0, nutrition.fiber)
    }

    @Test
    fun `Portion with different units`() {
        val portionGrams = Portion(amount = 100.0, unit = "g")
        val portionMl = Portion(amount = 250.0, unit = "ml")
        val portionUnits = Portion(amount = 2.0, unit = "unidades")

        assertEquals(100.0, portionGrams.amount, 0.001)
        assertEquals("g", portionGrams.unit)
        assertEquals(250.0, portionMl.amount, 0.001)
        assertEquals("ml", portionMl.unit)
        assertEquals(2.0, portionUnits.amount, 0.001)
        assertEquals("unidades", portionUnits.unit)
    }

    @Test
    fun `Food equality`() {
        val nutrition = Nutrition(100, 10.0, 20.0, 5.0, 2.0)
        val portion = Portion(100.0, "g")

        val food1 = Food("Manzana", 0.9, portion, nutrition, "Frutas")
        val food2 = Food("Manzana", 0.9, portion, nutrition, "Frutas")

        assertEquals(food1, food2)
    }

    @Test
    fun `Food copy with modification`() {
        val nutrition = Nutrition(100, 10.0, 20.0, 5.0, 2.0)
        val portion = Portion(100.0, "g")
        val food = Food("Manzana", 0.9, portion, nutrition, "Frutas")

        val modifiedFood = food.copy(confidence = 0.95)

        assertEquals(0.95, modifiedFood.confidence, 0.001)
        assertEquals("Manzana", modifiedFood.name)
    }
}

