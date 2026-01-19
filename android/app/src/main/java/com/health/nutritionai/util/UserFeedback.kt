package com.health.nutritionai.util

/**
 * Representa un mensaje de feedback para el usuario
 */
sealed class UserFeedback {
    data class Success(val message: String) : UserFeedback()
    data class Error(val message: String) : UserFeedback()
    data class Info(val message: String) : UserFeedback()
    data object None : UserFeedback()
}

/**
 * Estado de una operación con feedback incluido
 */
sealed class OperationState<out T> {
    data object Idle : OperationState<Nothing>()
    data object Loading : OperationState<Nothing>()
    data class Success<T>(val data: T, val feedback: String? = null) : OperationState<T>()
    data class Error(val message: String) : OperationState<Nothing>()
}

