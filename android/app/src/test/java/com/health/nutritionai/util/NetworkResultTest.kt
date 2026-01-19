package com.health.nutritionai.util

import org.junit.Assert.*
import org.junit.Test

class NetworkResultTest {

    @Test
    fun `Success contains data`() {
        val data = "Test data"
        val result = NetworkResult.Success(data)

        assertEquals(data, result.data)
        assertNull(result.message)
    }

    @Test
    fun `Success with complex object`() {
        data class TestObject(val id: Int, val name: String)
        val testObj = TestObject(1, "Test")

        val result = NetworkResult.Success(testObj)

        assertEquals(testObj, result.data)
        assertEquals(1, result.data?.id)
        assertEquals("Test", result.data?.name)
    }

    @Test
    fun `Success with list data`() {
        val list = listOf(1, 2, 3, 4, 5)
        val result = NetworkResult.Success(list)

        assertEquals(list, result.data)
        assertEquals(5, result.data?.size)
    }

    @Test
    fun `Error contains message and null data by default`() {
        val errorMessage = "Error occurred"
        val result = NetworkResult.Error<String>(errorMessage)

        assertEquals(errorMessage, result.message)
        assertNull(result.data)
    }

    @Test
    fun `Error can contain both message and data`() {
        val errorMessage = "Partial error"
        val partialData = "Cached data"
        val result = NetworkResult.Error(errorMessage, partialData)

        assertEquals(errorMessage, result.message)
        assertEquals(partialData, result.data)
    }

    @Test
    fun `Loading has null data and message`() {
        val result = NetworkResult.Loading<String>()

        assertNull(result.data)
        assertNull(result.message)
    }

    @Test
    fun `NetworkResult can be used in when expression`() {
        fun handleResult(result: NetworkResult<String>): String {
            return when (result) {
                is NetworkResult.Success -> "Success: ${result.data}"
                is NetworkResult.Error -> "Error: ${result.message}"
                is NetworkResult.Loading -> "Loading..."
            }
        }

        assertEquals("Success: data", handleResult(NetworkResult.Success("data")))
        assertEquals("Error: error", handleResult(NetworkResult.Error("error")))
        assertEquals("Loading...", handleResult(NetworkResult.Loading()))
    }

    @Test
    fun `Success with nullable type`() {
        val result: NetworkResult<String?> = NetworkResult.Success(null)
        assertNull(result.data)
    }

    @Test
    fun `Error with empty message`() {
        val result = NetworkResult.Error<String>("")

        assertEquals("", result.message)
    }
}

