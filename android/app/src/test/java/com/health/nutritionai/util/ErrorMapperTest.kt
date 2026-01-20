package com.health.nutritionai.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.health.nutritionai.R

class ErrorMapperTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    // ============ HttpException Tests ============

    @Test
    fun `mapErrorToMessage returns correct message for 400 AUTH_LOGIN`() {
        val exception = createHttpException(400)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.AUTH_LOGIN)

        assertEquals(context.getString(R.string.error_invalid_credentials), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 400 AUTH_REGISTER`() {
        val exception = createHttpException(400)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.AUTH_REGISTER)

        assertEquals(context.getString(R.string.error_invalid_data), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 400 NUTRITION_GOALS`() {
        val exception = createHttpException(400)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.NUTRITION_GOALS)

        assertEquals(context.getString(R.string.error_invalid_goals), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 400 PASSWORD_CHANGE`() {
        val exception = createHttpException(400)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.PASSWORD_CHANGE)

        assertEquals(context.getString(R.string.error_invalid_password_change), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 400 GENERAL`() {
        val exception = createHttpException(400)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_bad_request), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 401`() {
        val exception = createHttpException(401)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_session_expired), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 403`() {
        val exception = createHttpException(403)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_forbidden), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 404 MEAL`() {
        val exception = createHttpException(404)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.MEAL)

        assertEquals(context.getString(R.string.error_meal_not_found), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 404 USER_PROFILE`() {
        val exception = createHttpException(404)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.USER_PROFILE)

        assertEquals(context.getString(R.string.error_profile_not_found), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 404 GENERAL`() {
        val exception = createHttpException(404)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_resource_not_found), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 409 AUTH_REGISTER`() {
        val exception = createHttpException(409)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.AUTH_REGISTER)

        assertEquals(context.getString(R.string.error_email_exists), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 409 MEAL`() {
        val exception = createHttpException(409)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.MEAL)

        assertEquals(context.getString(R.string.error_meal_exists), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 422 MEAL_ANALYSIS`() {
        val exception = createHttpException(422)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.MEAL_ANALYSIS)

        assertEquals(context.getString(R.string.error_invalid_image), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 429`() {
        val exception = createHttpException(429)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_rate_limit), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 500`() {
        val exception = createHttpException(500)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_server_error), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 502`() {
        val exception = createHttpException(502)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_server_error), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 503`() {
        val exception = createHttpException(503)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_server_error), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for 504`() {
        val exception = createHttpException(504)

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_server_error), message)
    }

    // ============ Network Exception Tests ============

    @Test
    fun `mapErrorToMessage returns correct message for SocketTimeoutException`() {
        val exception = SocketTimeoutException()

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_timeout), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for UnknownHostException`() {
        val exception = UnknownHostException()

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_no_connection), message)
    }

    @Test
    fun `mapErrorToMessage returns correct message for IOException`() {
        val exception = IOException()

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_connection), message)
    }

    @Test
    fun `mapErrorToMessage returns default message for unknown exception`() {
        val exception = RuntimeException("Unknown error")

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.GENERAL)

        assertEquals(context.getString(R.string.error_unexpected), message)
    }

    // ============ Default Error Message Tests ============

    @Test
    fun `mapErrorToMessage returns correct default message for AUTH_LOGIN`() {
        val exception = RuntimeException()

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.AUTH_LOGIN)

        assertEquals(context.getString(R.string.error_login_failed), message)
    }

    @Test
    fun `mapErrorToMessage returns correct default message for AUTH_REGISTER`() {
        val exception = RuntimeException()

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.AUTH_REGISTER)

        assertEquals(context.getString(R.string.error_register_failed), message)
    }

    @Test
    fun `mapErrorToMessage returns correct default message for MEAL_ANALYSIS`() {
        val exception = RuntimeException()

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.MEAL_ANALYSIS)

        assertEquals(context.getString(R.string.error_analysis_failed), message)
    }

    @Test
    fun `mapErrorToMessage returns correct default message for MEAL_DELETE`() {
        val exception = RuntimeException()

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.MEAL_DELETE)

        assertEquals(context.getString(R.string.error_meal_delete_failed), message)
    }

    @Test
    fun `mapErrorToMessage returns correct default message for MEAL_UPDATE`() {
        val exception = RuntimeException()

        val message = ErrorMapper.mapErrorToMessage(context, exception, ErrorContext.MEAL_UPDATE)

        assertEquals(context.getString(R.string.error_meal_update_failed), message)
    }

    // ============ Success Message Tests ============

    @Test
    fun `getSuccessMessage returns correct message for LOGIN`() {
        val message = ErrorMapper.getSuccessMessage(context, SuccessAction.LOGIN)

        assertEquals(context.getString(R.string.success_login), message)
    }

    @Test
    fun `getSuccessMessage returns correct message for REGISTER`() {
        val message = ErrorMapper.getSuccessMessage(context, SuccessAction.REGISTER)

        assertEquals(context.getString(R.string.success_register), message)
    }

    @Test
    fun `getSuccessMessage returns correct message for MEAL_ANALYZED`() {
        val message = ErrorMapper.getSuccessMessage(context, SuccessAction.MEAL_ANALYZED)

        assertEquals(context.getString(R.string.success_meal_analyzed), message)
    }

    @Test
    fun `getSuccessMessage returns correct message for MEAL_DELETED`() {
        val message = ErrorMapper.getSuccessMessage(context, SuccessAction.MEAL_DELETED)

        assertEquals(context.getString(R.string.success_meal_deleted), message)
    }

    @Test
    fun `getSuccessMessage returns correct message for GOALS_UPDATED`() {
        val message = ErrorMapper.getSuccessMessage(context, SuccessAction.GOALS_UPDATED)

        assertEquals(context.getString(R.string.success_goals_updated), message)
    }

    @Test
    fun `getSuccessMessage returns correct message for PASSWORD_CHANGED`() {
        val message = ErrorMapper.getSuccessMessage(context, SuccessAction.PASSWORD_CHANGED)

        assertEquals(context.getString(R.string.success_password_changed), message)
    }

    @Test
    fun `getSuccessMessage returns correct message for PROFILE_UPDATED`() {
        val message = ErrorMapper.getSuccessMessage(context, SuccessAction.PROFILE_UPDATED)

        assertEquals(context.getString(R.string.success_profile_updated), message)
    }

    @Test
    fun `getSuccessMessage returns correct message for LOGOUT`() {
        val message = ErrorMapper.getSuccessMessage(context, SuccessAction.LOGOUT)

        assertEquals(context.getString(R.string.success_logout), message)
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
