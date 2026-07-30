package com.health.nutritionai.notifications

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.health.nutritionai.data.repository.UserRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

/**
 * Receives FCM pushes for meal-logging reminders and streak nudges (sent by the
 * backend's daily reminder job) and registers this device's token so the server
 * knows where to send them.
 */
class NutritionFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val userRepository: UserRepository by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token generated")
        userRepository.registerDeviceTokenAsync(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""

        val notification = NotificationCompat.Builder(this, NotificationChannels.REMINDERS_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(applicationInfo.icon)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(Random.nextInt(), notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted on Android 13+; drop silently.
            Log.w(TAG, "Notification permission not granted, dropping push", e)
        }
    }

    companion object {
        private const val TAG = "NutritionFcmService"
    }
}
