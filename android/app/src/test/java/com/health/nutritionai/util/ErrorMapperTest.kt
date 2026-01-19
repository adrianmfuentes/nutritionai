package com.health.nutritionai.util

import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMapperTest {

    // ============ HttpException Tests ============

    @Test
    fun `mapErrorToMessage returns correct message for 400 AUTH_LOGIN`() {
        val exception = createHttpException(400)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.AUTH_LOGIN)

        assertEquals("Usuario o contraseña incorrectos.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 400 AUTH_REGISTER`() {
        val exception = createHttpException(400)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.AUTH_REGISTER)

        assertEquals("Los datos proporcionados no son válidos. Por favor, verifica e intenta nuevamente.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 400 NUTRITION_GOALS`() {
        val exception = createHttpException(400)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.NUTRITION_GOALS)

        assertEquals("Los valores de los objetivos nutricionales no son válidos.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 400 PASSWORD_CHANGE`() {
        val exception = createHttpException(400)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.PASSWORD_CHANGE)

        assertEquals("La contraseña actual es incorrecta o la nueva contraseña no cumple los requisitos.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 400 GENERAL`() {
        val exception = createHttpException(400)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("Hay un problema con los datos enviados. Por favor, verifica e intenta nuevamente.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 401`() {
        val exception = createHttpException(401)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("Tu sesión ha expirado. Por favor, inicia sesión nuevamente.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 403`() {
        val exception = createHttpException(403)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("No tienes permiso para realizar esta acción.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 404 MEAL`() {
        val exception = createHttpException(404)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.MEAL)

        assertEquals("No se encontró la comida solicitada.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 404 USER_PROFILE`() {
        val exception = createHttpException(404)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.USER_PROFILE)

        assertEquals("No se encontró tu perfil de usuario.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 404 GENERAL`() {
        val exception = createHttpException(404)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("No se encontró el recurso solicitado.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 409 AUTH_REGISTER`() {
        val exception = createHttpException(409)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.AUTH_REGISTER)

        assertEquals("Ya existe una cuenta con este correo electrónico.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 409 MEAL`() {
        val exception = createHttpException(409)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.MEAL)

        assertEquals("Esta comida ya ha sido registrada.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 422 MEAL_ANALYSIS`() {
        val exception = createHttpException(422)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.MEAL_ANALYSIS)

        assertEquals("No se pudo procesar la imagen. Por favor, intenta con otra foto.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 429`() {
        val exception = createHttpException(429)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("Has realizado demasiadas solicitudes. Por favor, espera un momento e intenta nuevamente.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 500`() {
        val exception = createHttpException(500)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("Estamos experimentando problemas técnicos. Por favor, intenta más tarde.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 502`() {
        val exception = createHttpException(502)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("Estamos experimentando problemas técnicos. Por favor, intenta más tarde.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 503`() {
        val exception = createHttpException(503)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("Estamos experimentando problemas técnicos. Por favor, intenta más tarde.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 504`() {
        val exception = createHttpException(504)

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("Estamos experimentando problemas técnicos. Por favor, intenta más tarde.", message)
    }

    // ============ Network Exception Tests ============

    @Test
    fun `mapErrorToMessage returns correct message for SocketTimeoutException`() {
        val exception = SocketTimeoutException()

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("La operación tardó demasiado tiempo. Por favor, verifica tu conexión e intenta nuevamente.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for UnknownHostException`() {
        val exception = UnknownHostException()

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("No se pudo conectar al servidor. Por favor, verifica tu conexión a internet.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for IOException`() {
        val exception = IOException()

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("Error de conexión. Por favor, verifica tu conexión a internet e intenta nuevamente.", message)
    }

    @Test
    fun `mapErrorToMessage returns default message for unknown exception`() {
        val exception = RuntimeException("Unknown error")

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.GENERAL)

        assertEquals("Ocurrió un error inesperado. Por favor, intenta nuevamente.", message)
    }

    // ============ Default Error Message Tests ============

    @Test
    fun `mapErrorToMessage returns correct default message for AUTH_LOGIN`() {
        val exception = RuntimeException()

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.AUTH_LOGIN)

        assertEquals("No se pudo iniciar sesión. Por favor, intenta nuevamente.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct default message for AUTH_REGISTER`() {
        val exception = RuntimeException()

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.AUTH_REGISTER)

        assertEquals("No se pudo completar el registro. Por favor, intenta nuevamente.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct default message for MEAL_ANALYSIS`() {
        val exception = RuntimeException()

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.MEAL_ANALYSIS)

        assertEquals("No se pudo analizar la comida. Por favor, intenta con otra foto.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct default message for MEAL_DELETE`() {
        val exception = RuntimeException()

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.MEAL_DELETE)

        assertEquals("No se pudo eliminar la comida. Por favor, intenta nuevamente.", message)
    }

    @Test
    fun `mapErrorToMessage returns correct default message for MEAL_UPDATE`() {
        val exception = RuntimeException()

        val message = ErrorMapper.mapErrorToMessage(exception, ErrorContext.MEAL_UPDATE)

        assertEquals("No se pudo actualizar la comida. Por favor, intenta nuevamente.", message)
    }

    // ============ Success Message Tests ============

    @Test
    fun `getSuccessMessage returns correct message for LOGIN`() {
        val message = ErrorMapper.getSuccessMessage(SuccessAction.LOGIN)

        assertEquals("¡Bienvenido de nuevo!", message)
    }

    @Test
    fun `getSuccessMessage returns correct message for REGISTER`() {
        val message = ErrorMapper.getSuccessMessage(SuccessAction.REGISTER)

        assertEquals("¡Cuenta creada exitosamente!", message)
    }

    @Test
    fun `getSuccessMessage returns correct message for MEAL_ANALYZED`() {
        val message = ErrorMapper.getSuccessMessage(SuccessAction.MEAL_ANALYZED)

        assertEquals("¡Comida analizada con éxito!", message)
    }

    @Test
    fun `getSuccessMessage returns correct message for MEAL_DELETED`() {
        val message = ErrorMapper.getSuccessMessage(SuccessAction.MEAL_DELETED)

        assertEquals("Comida eliminada correctamente", message)
    }

    @Test
    fun `getSuccessMessage returns correct message for GOALS_UPDATED`() {
        val message = ErrorMapper.getSuccessMessage(SuccessAction.GOALS_UPDATED)

        assertEquals("Objetivos nutricionales actualizados", message)
    }

    @Test
    fun `getSuccessMessage returns correct message for PASSWORD_CHANGED`() {
        val message = ErrorMapper.getSuccessMessage(SuccessAction.PASSWORD_CHANGED)

        assertEquals("Contraseña cambiada exitosamente", message)
    }

    @Test
    fun `getSuccessMessage returns correct message for PROFILE_UPDATED`() {
        val message = ErrorMapper.getSuccessMessage(SuccessAction.PROFILE_UPDATED)

        assertEquals("Perfil actualizado correctamente", message)
    }

    @Test
    fun `getSuccessMessage returns correct message for LOGOUT`() {
        val message = ErrorMapper.getSuccessMessage(SuccessAction.LOGOUT)

        assertEquals("Sesión cerrada", message)
    }

    // ============ Helper Methods ============

    private fun createHttpException(code: Int): HttpException {
        val response = Response.error<Any>(
            code,
            "Error".toResponseBody(null)
        )
        return HttpException(response)
    }
}

