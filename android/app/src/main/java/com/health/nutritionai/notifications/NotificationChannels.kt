package com.health.nutritionai.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDERS_CHANNEL_ID = "meal_reminders"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            REMINDERS_CHANNEL_ID,
            "Recordatorios de comidas",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Recordatorios diarios y rachas de registro de comidas"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
