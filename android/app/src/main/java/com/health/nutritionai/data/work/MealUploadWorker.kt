package com.health.nutritionai.data.work

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.health.nutritionai.data.local.dao.PendingMealDao
import com.health.nutritionai.data.repository.MealRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Retries queued meal-photo uploads captured while offline (see
 * [MealRepository.analyzeMeal]'s IOException branch). Runs only when a network
 * connection is available; leftover failures are retried by WorkManager's own backoff.
 */
class MealUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val pendingMealDao: PendingMealDao by inject()
    private val mealRepository: MealRepository by inject()

    override suspend fun doWork(): Result {
        val pending = pendingMealDao.getAll()
        if (pending.isEmpty()) return Result.success()

        var anyFailed = false
        for (item in pending) {
            val file = File(item.imagePath)
            if (!file.exists()) {
                pendingMealDao.deleteById(item.id)
                continue
            }

            val uploaded = mealRepository.retryPendingUpload(file, item.mealType)
            if (uploaded) {
                pendingMealDao.deleteById(item.id)
                file.delete()
            } else {
                anyFailed = true
            }
        }

        return if (anyFailed) Result.retry() else Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "meal_upload_queue"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<MealUploadWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            try {
                WorkManager.getInstance(context).enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    request
                )
            } catch (e: Exception) {
                Log.e("MealUploadWorker", "Failed to enqueue upload work", e)
            }
        }
    }
}
