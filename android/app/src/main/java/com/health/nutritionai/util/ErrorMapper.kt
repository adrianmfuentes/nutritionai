package com.health.nutritionai.util

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
    fun mapErrorToMessage(throwable: Throwable, context: ErrorContext = ErrorContext.GENERAL): String {
        return when (throwable) {
            is HttpException -> mapHttpError(throwable, context)
            is SocketTimeoutException -> "La operación tardó demasiado tiempo. Por favor, verifica tu conexión e intenta nuevamente."
            is UnknownHostException -> "No se pudo conectar al servidor. Por favor, verifica tu conexión a internet."
            is IOException -> "Error de conexión. Por favor, verifica tu conexión a internet e intenta nuevamente."
            else -> getDefaultErrorMessage(context)
        }
    }

    /**
     * Mapea errores HTTP específicos a mensajes user-friendly
     */
    private fun mapHttpError(exception: HttpException, context: ErrorContext): String {
        return when (exception.code()) {
            400 -> when (context) {
                ErrorContext.AUTH_LOGIN -> "Usuario o contraseña incorrectos."
                ErrorContext.AUTH_REGISTER -> "Los datos proporcionados no son válidos. Por favor, verifica e intenta nuevamente."
                ErrorContext.NUTRITION_GOALS -> "Los valores de los objetivos nutricionales no son válidos."
                ErrorContext.PASSWORD_CHANGE -> "La contraseña actual es incorrecta o la nueva contraseña no cumple los requisitos."
                else -> "Hay un problema con los datos enviados. Por favor, verifica e intenta nuevamente."
            }

            401 -> "Tu sesión ha expirado. Por favor, inicia sesión nuevamente."

            403 -> "No tienes permiso para realizar esta acción."

            404 -> when (context) {
                ErrorContext.MEAL -> "No se encontró la comida solicitada."
                ErrorContext.USER_PROFILE -> "No se encontró tu perfil de usuario."
                else -> "No se encontró el recurso solicitado."
            }

            409 -> when (context) {
                ErrorContext.AUTH_REGISTER -> "Ya existe una cuenta con este correo electrónico."
                ErrorContext.MEAL -> "Esta comida ya ha sido registrada."
                else -> "Ya existe un registro con estos datos."
            }

            422 -> when (context) {
                ErrorContext.AUTH_REGISTER -> "Los datos de registro no son válidos. Verifica tu correo electrónico y contraseña."
                ErrorContext.NUTRITION_GOALS -> "Los valores de los objetivos no son válidos."
                ErrorContext.MEAL_ANALYSIS -> "No se pudo procesar la imagen. Por favor, intenta con otra foto."
                else -> "Los datos enviados no son válidos. Por favor, verifica e intenta nuevamente."
            }

            429 -> "Has realizado demasiadas solicitudes. Por favor, espera un momento e intenta nuevamente."

            500, 502, 503, 504 -> "Estamos experimentando problemas técnicos. Por favor, intenta más tarde."

            else -> getDefaultErrorMessage(context)
        }
    }

    /**
     * Mensajes de error por defecto según el contexto
     */
    private fun getDefaultErrorMessage(context: ErrorContext): String {
        return when (context) {
            ErrorContext.AUTH_LOGIN -> "No se pudo iniciar sesión. Por favor, intenta nuevamente."
            ErrorContext.AUTH_REGISTER -> "No se pudo completar el registro. Por favor, intenta nuevamente."
            ErrorContext.MEAL_ANALYSIS -> "No se pudo analizar la comida. Por favor, intenta con otra foto."
            ErrorContext.MEAL -> "Error al procesar la comida. Por favor, intenta nuevamente."
            ErrorContext.MEAL_UPDATE -> "No se pudo actualizar la comida. Por favor, intenta nuevamente."
            ErrorContext.NUTRITION_GOALS -> "No se pudieron actualizar los objetivos. Por favor, intenta nuevamente."
            ErrorContext.USER_PROFILE -> "No se pudo cargar tu perfil. Por favor, intenta nuevamente."
            ErrorContext.PASSWORD_CHANGE -> "No se pudo cambiar la contraseña. Por favor, intenta nuevamente."
            ErrorContext.MEAL_DELETE -> "No se pudo eliminar la comida. Por favor, intenta nuevamente."
            ErrorContext.GENERAL -> "Ocurrió un error inesperado. Por favor, intenta nuevamente."
        }
    }

    /**
     * Mensajes de éxito para acciones críticas
     */
    fun getSuccessMessage(action: SuccessAction): String {
        return when (action) {
            SuccessAction.LOGIN -> "¡Bienvenido de nuevo!"
            SuccessAction.REGISTER -> "¡Cuenta creada exitosamente!"
            SuccessAction.MEAL_ANALYZED -> "¡Comida analizada con éxito!"
            SuccessAction.MEAL_DELETED -> "Comida eliminada correctamente"
            SuccessAction.GOALS_UPDATED -> "Objetivos nutricionales actualizados"
            SuccessAction.PASSWORD_CHANGED -> "Contraseña cambiada exitosamente"
            SuccessAction.PROFILE_UPDATED -> "Perfil actualizado correctamente"
            SuccessAction.LOGOUT -> "Sesión cerrada"
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
    PASSWORD_CHANGE
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

