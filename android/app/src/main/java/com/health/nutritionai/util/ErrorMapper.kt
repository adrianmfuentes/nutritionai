package com.health.nutritionai.util

import android.content.Context
import com.health.nutritionai.R
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Mapea errores técnicos del backend a mensajes amigables para el usuario.
 * Evita exponer información técnica como códigos HTTP, stack traces, etc.
 */
object ErrorMapper {

    /**
     * Convierte una excepción en un mensaje user-friendly
     */
    fun mapErrorToMessage(context: Context, throwable: Exception, errorContext: ErrorContext = ErrorContext.GENERAL): String {
        return when (throwable) {
            is HttpException -> mapHttpError(context, throwable, errorContext)
            is SocketTimeoutException -> context.getString(R.string.error_timeout)
            is UnknownHostException -> context.getString(R.string.error_no_connection)
            is IOException -> context.getString(R.string.error_connection)
            else -> getDefaultErrorMessage(context, errorContext)
        }
    }

    /**
     * Mapea errores HTTP específicos a mensajes user-friendly
     */
    private fun mapHttpError(context: Context, exception: HttpException, errorContext: ErrorContext): String {
        // For auth contexts, try to use backend message first
        if (errorContext == ErrorContext.AUTH_LOGIN || errorContext == ErrorContext.AUTH_REGISTER) {
            val backendMessage = getBackendErrorMessage(exception)
            if (backendMessage != null) {
                return backendMessage
            }
        }

        return when (exception.code()) {
            400 -> when (errorContext) {
                ErrorContext.AUTH_LOGIN -> context.getString(R.string.error_invalid_credentials)
                ErrorContext.AUTH_REGISTER -> context.getString(R.string.error_invalid_data)
                ErrorContext.NUTRITION_GOALS -> context.getString(R.string.error_invalid_goals)
                ErrorContext.PASSWORD_CHANGE -> context.getString(R.string.error_invalid_password_change)
                else -> context.getString(R.string.error_bad_request)
            }

            401 -> context.getString(R.string.error_session_expired)

            403 -> context.getString(R.string.error_forbidden)

            404 -> when (errorContext) {
                ErrorContext.MEAL -> context.getString(R.string.error_meal_not_found)
                ErrorContext.USER_PROFILE -> context.getString(R.string.error_profile_not_found)
                else -> context.getString(R.string.error_resource_not_found)
            }

            409 -> when (errorContext) {
                ErrorContext.AUTH_REGISTER -> context.getString(R.string.error_email_exists)
                ErrorContext.MEAL -> context.getString(R.string.error_meal_exists)
                else -> context.getString(R.string.error_duplicate)
            }

            422 -> when (errorContext) {
                ErrorContext.AUTH_REGISTER -> context.getString(R.string.error_invalid_registration)
                ErrorContext.NUTRITION_GOALS -> context.getString(R.string.error_invalid_goals)
                ErrorContext.MEAL_ANALYSIS -> context.getString(R.string.error_invalid_image)
                else -> context.getString(R.string.error_unprocessable)
            }

            429 -> context.getString(R.string.error_rate_limit)

            500, 502, 503, 504 -> context.getString(R.string.error_server_error)

            else -> getDefaultErrorMessage(context, errorContext)
        }
    }

    /**
     * Intenta extraer el mensaje de error del cuerpo de la respuesta del backend
     */
    private fun getBackendErrorMessage(exception: HttpException): String? {
        return try {
            val errorBody = exception.response()?.errorBody()?.string()
            if (errorBody != null) {
                val jsonObject = JSONObject(errorBody)
                jsonObject.optString("message").takeIf { it.isNotEmpty() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Mensajes de error por defecto según el contexto
     */
    private fun getDefaultErrorMessage(context: Context, errorContext: ErrorContext): String {
        return when (errorContext) {
            ErrorContext.AUTH_LOGIN -> context.getString(R.string.error_login_failed)
            ErrorContext.AUTH_REGISTER -> context.getString(R.string.error_register_failed)
            ErrorContext.MEAL_ANALYSIS -> context.getString(R.string.error_analysis_failed)
            ErrorContext.MEAL -> context.getString(R.string.error_meal_processing)
            ErrorContext.MEAL_UPDATE -> context.getString(R.string.error_meal_update_failed)
            ErrorContext.NUTRITION_GOALS -> context.getString(R.string.error_goals_update_failed)
            ErrorContext.USER_PROFILE -> context.getString(R.string.error_profile_load_failed)
            ErrorContext.PASSWORD_CHANGE -> context.getString(R.string.error_password_change_failed)
            ErrorContext.MEAL_DELETE -> context.getString(R.string.error_meal_delete_failed)
            ErrorContext.EMAIL_VERIFICATION -> context.getString(R.string.error_email_verification_failed)
            ErrorContext.ACCOUNT_DELETION -> context.getString(R.string.error_account_deletion_failed)
            ErrorContext.GENERAL -> context.getString(R.string.error_unexpected)
        }
    }

    /**
     * Mensajes de éxito para acciones críticas
     */
    fun getSuccessMessage(context: Context, action: SuccessAction): String {
        return when (action) {
            SuccessAction.LOGIN -> context.getString(R.string.success_login)
            SuccessAction.REGISTER -> context.getString(R.string.success_register)
            SuccessAction.MEAL_ANALYZED -> context.getString(R.string.success_meal_analyzed)
            SuccessAction.MEAL_DELETED -> context.getString(R.string.success_meal_deleted)
            SuccessAction.GOALS_UPDATED -> context.getString(R.string.success_goals_updated)
            SuccessAction.PASSWORD_CHANGED -> context.getString(R.string.success_password_changed)
            SuccessAction.PROFILE_UPDATED -> context.getString(R.string.success_profile_updated)
            SuccessAction.LOGOUT -> context.getString(R.string.success_logout)
        }
    }
}

/**
 * Contextos de error para proporcionar mensajes más específicos
 */
enum class ErrorContext {
    GENERAL,
    AUTH_LOGIN,
    AUTH_REGISTER,
    MEAL_ANALYSIS,
    MEAL,
    MEAL_DELETE,
    MEAL_UPDATE,
    NUTRITION_GOALS,
    USER_PROFILE,
    PASSWORD_CHANGE,
    EMAIL_VERIFICATION,
    ACCOUNT_DELETION
}

/**
 * Acciones exitosas que requieren feedback al usuario
 */
enum class SuccessAction {
    LOGIN,
    REGISTER,
    MEAL_ANALYZED,
    MEAL_DELETED,
    GOALS_UPDATED,
    PASSWORD_CHANGED,
    PROFILE_UPDATED,
    LOGOUT
}
